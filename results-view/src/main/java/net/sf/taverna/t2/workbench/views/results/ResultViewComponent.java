/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.views.results;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPIRegistry;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

/**
 * This component contains a tabbed pane, where each tab displays results for one of
 * the output ports of a workflow, and a set of 'save results' buttons that save results 
 * from all ports in a certain format.
 * 
 * @author David Withers
 * @author Alex Nenadic
 *
 */
public class ResultViewComponent extends JPanel implements UIComponentSPI, ResultListener {

	private static final long serialVersionUID = 988812623494396366L;
	
	// Invocation context
	private InvocationContext context = null;
	
	// The map contains a mapping for each port to a T2Reference pointing to the port's result(s)
	private HashMap<String, T2Reference> resultReferencesMap = new HashMap<String, T2Reference>();
	
	// Per-port boolean values indicating if all results have been received per port
	private HashMap<String, Boolean> receivedAllResultsForPort = new HashMap<String, Boolean>();
	
	// Tabbed pane - each tab contains a results tree and a RenderedResultComponent, 
	// which in turn contains the currently selected result node rendered according 
	// to its mime type and a button for saving the selected inividual result
	private JTabbedPane tabbedPane;
	
	// Panel containing the save buttons
	private JPanel saveButtonsPanel;

	private WorkflowInstanceFacade facade;
		
	// Registry of all existing 'save results' actions, each one can save results
	// in a different format
	private static SaveAllResultsSPIRegistry saveAllResultsRegistry = SaveAllResultsSPIRegistry.getInstance();	
	
	
	public ResultViewComponent() {
		super(new BorderLayout());
		setBorder(new EtchedBorder());
		tabbedPane = new JTabbedPane();
		saveButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		add(saveButtonsPanel, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Results View Component";
	}

	public void onDisplay() {
	}

	public void onDispose() {
	}

	public void register(WorkflowInstanceFacade facade)
			throws EditException {
		
		clear();
		
		this.facade = facade;
		
		saveButtonsPanel.add(new JButton(new SaveAllAction("Save values", this)));

		List<DataflowOutputPort> dataflowOutputPorts = new ArrayList<DataflowOutputPort>(facade
				.getDataflow().getOutputPorts());
		
		Collections.sort(dataflowOutputPorts, new Comparator<DataflowOutputPort>() {

			public int compare(DataflowOutputPort o1, DataflowOutputPort o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		
		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			String portName = dataflowOutputPort.getName();
						
			// Initially we have no results for a port
			receivedAllResultsForPort.put(portName, new Boolean(Boolean.FALSE));
			
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowOutputPort);

			// Per-port tree model listens for results coming out of the data facade
			facade.addResultListener(resultTab.getResultModel());
			// This component also listens to the results coming out in order to know
			// when receiving of results has finished
			facade.addResultListener(this);
			
			tabbedPane.add(portName, resultTab);
		}
		revalidate();
	}

	public void clear() {
		saveButtonsPanel.removeAll();
		tabbedPane.removeAll();
	}

	public void resultTokenProduced(WorkflowDataToken token, String portName) {
		
		// Set the invocation context the first time you get the chance
		if (context == null)
			context = token.getContext();
		
		// If we have finished receiving results - token.getIndex().length is 0
		if (token.getIndex().length == 0){
			receivedAllResultsForPort.put(portName, new Boolean(Boolean.TRUE));
			// We know that at this point the token.getData() contains a T2Reference to 
			// all result(s)
			T2Reference resultsRef = token.getData();
			// Put the resultsRef in the resultReferencesMap
			resultReferencesMap.put(portName, resultsRef);
		}
		
		// If this is the last token for all ports - update the save buttons' state
		 boolean receivedAll = true;
		 for (String pName : receivedAllResultsForPort.keySet()){
		 	if (!receivedAllResultsForPort.get(pName).booleanValue()){
		 		receivedAll = false;
		 		break;
		 	}
		 }
		 if (receivedAll){
			 HashMap<String, T2Reference> inputValuesMap = new HashMap<String, T2Reference> ();
				for (DataflowInputPort dataflowInputPort : facade.getDataflow().getInputPorts()) {
					String name = dataflowInputPort.getName();
					inputValuesMap.put(name, facade.getPushedDataMap().get(name));
				}
			 for (int i=0; i< saveButtonsPanel.getComponents().length; i++){
					JButton saveButton = (JButton)saveButtonsPanel.getComponent(i);
					saveButton.setEnabled(true);
					saveButton.setFocusable(false);
			}
		 }
	}
	
	private class SaveAllAction extends AbstractAction {
		
		private ResultViewComponent parent;

		public SaveAllAction(String name, ResultViewComponent resultViewComponent) {
			super(name);
			this.parent = resultViewComponent;
		}

		public void actionPerformed(ActionEvent e) {
			
			final JDialog dialog = new JDialog((Frame) null, true);
			dialog.setResizable(false);
			dialog.setLocationRelativeTo(null);
			dialog.setTitle("Workflow run data saver");
			JPanel panel = new JPanel(new BorderLayout());
			DialogTextArea explanation = new DialogTextArea();
			explanation.setText("Select the workflow input and output ports to save the associated data");
			explanation.setColumns(40);
			explanation.setEditable(false);
			explanation.setOpaque(false);
			explanation.setBorder(new EmptyBorder(5, 20, 5, 20));
			panel.add(explanation, BorderLayout.NORTH);
			final Map<String, JCheckBox> inputChecks = new HashMap<String, JCheckBox> ();
			final Map<String, JCheckBox> outputChecks = new HashMap<String, JCheckBox> ();
			final Map<JCheckBox, T2Reference> checkReferences =
				new HashMap<JCheckBox, T2Reference>();
			final Map<String, T2Reference> chosenReferences =
				new HashMap<String, T2Reference> ();
			final Set<SaveAllResultsSPI> actionSet = new HashSet<SaveAllResultsSPI>();

			ItemListener listener = new ItemListener() {

				public void itemStateChanged(ItemEvent e) {
					JCheckBox source = (JCheckBox) e.getItemSelectable();
					if (inputChecks.containsValue(source)) {
						if (source.isSelected()) {
							if (outputChecks.containsKey(source.getText())) {
								outputChecks.get(source.getText()).setSelected(false);
							}
						}
					}
					if (outputChecks.containsValue(source)) {
						if (source.isSelected()) {
							if (inputChecks.containsKey(source.getText())) {
								inputChecks.get(source.getText()).setSelected(false);
							}
						}
					}
					chosenReferences.clear();
					for (JCheckBox checkBox : checkReferences.keySet()) {
						if (checkBox.isSelected()) {
							chosenReferences.put(checkBox.getText(),
									checkReferences.get(checkBox));
						}
					}
				}
				
			};
			if (!facade.getDataflow().getInputPorts().isEmpty()) {
				JPanel inputsPanel = new JPanel();
				inputsPanel.setBorder(new EmptyBorder(5, 20, 5, 20));

				inputsPanel.setLayout(new GridLayout(0, 1));
				inputsPanel.add(new JLabel("Workflow inputs:"));
				WeakHashMap<String, T2Reference> pushedDataMap = facade.getPushedDataMap();
				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (DataflowInputPort port : facade.getDataflow().getInputPorts()) {
					String portName = port.getName();
					JCheckBox checkBox = new JCheckBox(portName);
					checkBox
							.setSelected(!resultReferencesMap.containsKey(portName));
					checkReferences.put(checkBox, pushedDataMap.get(portName));
					checkBox.addItemListener(listener);
					inputChecks.put(portName, checkBox);
					sortedBoxes.put(portName, checkBox);
				}
				for (String portName : sortedBoxes.keySet()) {
					inputsPanel.add(sortedBoxes.get(portName));
				}
				panel.add(inputsPanel, BorderLayout.WEST);
			}			
			if (!resultReferencesMap.isEmpty()) {
				JPanel outputsPanel = new JPanel();
				outputsPanel.setBorder(new EmptyBorder(5, 20, 5, 20));
				outputsPanel.setLayout(new GridLayout(0, 1));
				outputsPanel.add(new JLabel("Workflow outputs:"));
				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (String portName : resultReferencesMap.keySet()) {
					JCheckBox checkBox = new JCheckBox(portName);
					checkBox
							.setSelected(true);
					checkReferences.put(checkBox, resultReferencesMap.get(portName));
					checkBox.addItemListener(listener);
					outputChecks.put(portName, checkBox);
					sortedBoxes.put(portName, checkBox);
				}
				for (String portName : sortedBoxes.keySet()) {
					outputsPanel.add(sortedBoxes.get(portName));
				}
				
				panel.add(outputsPanel, BorderLayout.EAST);
			}
			chosenReferences.clear();
			for (JCheckBox checkBox : checkReferences.keySet()) {
				if (checkBox.isSelected()) {
					chosenReferences.put(checkBox.getText(),
							checkReferences.get(checkBox));
				}
			}


			JPanel buttonsBar = new JPanel();
			buttonsBar.setLayout(new FlowLayout());
			// Get all existing 'Save result' actions
			List<SaveAllResultsSPI> saveActions = saveAllResultsRegistry.getSaveResultActions();
			for (SaveAllResultsSPI spi : saveActions){
				SaveAllResultsSPI action = (SaveAllResultsSPI) spi.getAction();
				actionSet.add(action);
				JButton saveButton = new JButton((AbstractAction) action);
				action.setChosenReferences(chosenReferences);
				action.setInvocationContext(context);
				action.setParent(dialog);
				saveButton.setEnabled(true);
				buttonsBar.add(saveButton);
			}
			JButton cancelButton = new JButton("Cancel", WorkbenchIcons.closeIcon);
			cancelButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
				}
				
			});
			buttonsBar.add(cancelButton);
			panel.add(buttonsBar, BorderLayout.SOUTH);
			panel.revalidate();
			dialog.add(panel);
			dialog.pack();
			dialog.setVisible(true);
		}
		
	}

}
