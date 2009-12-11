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
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.lineageservice.Dependencies;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
// import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsAsOPM;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPIRegistry;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;

import org.apache.log4j.Logger;

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

	private static Logger logger = Logger
	.getLogger(ResultViewComponent.class);

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
	private Dataflow dataflow;
	
	private JButton saveButton;

	private String runId;

	private ReferenceService referenceService;

	// This is needed for "Save data as OPM" action so that we know if
	// we shoudl try to geth the OPM graph or not (if provanance was not
	// enabled there is no point in trying to save data as OPM as it will be missing)
	private boolean isProvenanceEnabledForRun;
		
	// Registry of all existing 'save results' actions, each one can save results
	// in a different format
	private static SaveAllResultsSPIRegistry saveAllResultsRegistry = SaveAllResultsSPIRegistry.getInstance();	
	
	private HashMap<String, ResultTreeModel> inputPortModelMap = new HashMap<String, ResultTreeModel>();
	private HashMap<String, ResultTreeModel> outputPortModelMap = new HashMap<String, ResultTreeModel>();
	
	private HashMap<String, Object> inputPortObjectMap = new HashMap<String, Object> ();
	private HashMap<String, Object> outputPortObjectMap = new HashMap<String, Object> ();
	
	
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

	public void register(WorkflowInstanceFacade facade, boolean isProvenanceEnabledForRun)
			throws EditException {
		
		clear();
		
		this.facade = facade;
		this.dataflow = facade.getDataflow();
		this.runId = facade.getWorkflowRunId();
		this.isProvenanceEnabledForRun = isProvenanceEnabledForRun;
		
		saveButton = new JButton(new SaveAllAction("Save values", this));
		saveButtonsPanel.add(saveButton);

		List<DataflowInputPort> dataflowInputPorts = new ArrayList<DataflowInputPort>(facade
				.getDataflow().getInputPorts());
		
		Collections.sort(dataflowInputPorts, new Comparator<DataflowInputPort>() {

			public int compare(DataflowInputPort o1, DataflowInputPort o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		
		List<DataflowOutputPort> dataflowOutputPorts = new ArrayList<DataflowOutputPort>(facade
				.getDataflow().getOutputPorts());
		
		Collections.sort(dataflowOutputPorts, new Comparator<DataflowOutputPort>() {

			public int compare(DataflowOutputPort o1, DataflowOutputPort o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		
		for (DataflowInputPort dataflowInputPort : dataflowInputPorts) {
			String portName = dataflowInputPort.getName();
			
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowInputPort.getName(), dataflowInputPort.getDepth());
			
			inputPortModelMap.put(portName, resultTab.getResultModel());
			
			tabbedPane.addTab(portName, WorkbenchIcons.inputIcon, resultTab);
		}

		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			String portName = dataflowOutputPort.getName();
						
			// Initially we have no results for a port
			receivedAllResultsForPort.put(portName, new Boolean(Boolean.FALSE));
			
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowOutputPort.getName(),
					dataflowOutputPort.getDepth());
			outputPortModelMap.put(portName, resultTab.getResultModel());

			// Per-port tree model listens for results coming out of the data facade
			facade.addResultListener(resultTab.getResultModel());
			// This component also listens to the results coming out in order to know
			// when receiving of results has finished
			facade.addResultListener(this);
			
			tabbedPane.addTab(portName, WorkbenchIcons.outputIcon, resultTab);
		}
		revalidate();
	}
	
	public void repopulate(Dataflow dataflow, String runId, Date date, ReferenceService referenceService, boolean isProvenanceEnabledForRun) {
		this.dataflow = dataflow;
		this.runId = runId;
		this.referenceService = referenceService;
		this.isProvenanceEnabledForRun = isProvenanceEnabledForRun;
		
		this.dataflow.checkValidity();
		
		String connectorType = DataManagementConfiguration.getInstance()
		.getConnectorType();
		ProvenanceAccess provenanceAccess = new ProvenanceAccess(connectorType);
		clear();
		
		InvocationContext dummyContext = new InvocationContextImpl(referenceService, null);
		context = dummyContext;
		saveButton = new JButton(new SaveAllAction("Save values", this));
		JButton reloadWorkflowButton = new JButton(new ReloadWorkflowAction("Reopen workflow", this.dataflow, date));
		saveButtonsPanel.add(saveButton);
		saveButtonsPanel.add(reloadWorkflowButton);

		List<DataflowInputPort> dataflowInputPorts = new ArrayList<DataflowInputPort>(dataflow.getInputPorts());
		
		Collections.sort(dataflowInputPorts, new Comparator<DataflowInputPort>() {

			public int compare(DataflowInputPort o1, DataflowInputPort o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		
		for (DataflowInputPort dataflowInputPort : dataflowInputPorts) {
			String portName = dataflowInputPort.getName();
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowInputPort.getName(), dataflowInputPort.getDepth());
			ResultTreeModel model = resultTab.getResultModel();
			
			Dependencies dependencies = provenanceAccess.fetchPortData(runId, dataflow.getInternalIdentier(), dataflow.getLocalName(), portName, null);
			List<LineageQueryResultRecord> records = dependencies.getRecords();
			for (LineageQueryResultRecord record : records) {

				String value = record.getValue();
				T2Reference referenceValue = referenceService
						.referenceFromString(value);
				String iteration = record.getIteration();
				int[] elementIndex = getElementIndex(iteration);
				WorkflowDataToken token = new WorkflowDataToken("", elementIndex, referenceValue,
						dummyContext);
				model.resultTokenProduced(token, portName);
			}
			inputPortModelMap.put(portName, model);
			tabbedPane.addTab(portName, WorkbenchIcons.inputIcon, resultTab);
		}
		
		List<DataflowOutputPort> dataflowOutputPorts = new ArrayList<DataflowOutputPort>(dataflow.getOutputPorts());
		
		Collections.sort(dataflowOutputPorts, new Comparator<DataflowOutputPort>() {

			public int compare(DataflowOutputPort o1, DataflowOutputPort o2) {
				return o1.getName().compareTo(o2.getName());
			}});
		
		for (DataflowOutputPort dataflowOutputPort : dataflowOutputPorts) {
			String portName = dataflowOutputPort.getName();
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(dataflowOutputPort.getName(), dataflowOutputPort.getDepth());
			ResultTreeModel model = resultTab.getResultModel();
			
			Dependencies dependencies = provenanceAccess.fetchPortData(runId, dataflow.getInternalIdentier(), dataflow.getLocalName(), portName, null);
			List<LineageQueryResultRecord> records = dependencies.getRecords();
			for (LineageQueryResultRecord record : records) {

				String value = record.getValue();
				T2Reference referenceValue = referenceService
						.referenceFromString(value);
				String iteration = record.getIteration();
				int[] elementIndex = getElementIndex(iteration);
				WorkflowDataToken token = new WorkflowDataToken("", elementIndex, referenceValue,
						dummyContext);
				model.resultTokenProduced(token, portName);
			}
			outputPortModelMap.put(portName, model);
			tabbedPane.addTab(portName, WorkbenchIcons.outputIcon, resultTab);
		}
		revalidate();
	}

	private int[] getElementIndex(String iteration) {
		iteration = iteration.replaceAll("\\[\\]", ",");
		iteration = iteration.replaceAll("\\[", "");
		iteration = iteration.replaceAll("\\]", "");
		String[] parts = iteration.split(",");
		int[] elementIndex = new int[parts.length];
		for (int i = 0; i < parts.length; i++) {
			elementIndex[i] = Integer.parseInt(parts[i]);
		}
		return elementIndex;
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
				for (DataflowInputPort dataflowInputPort : dataflow.getInputPorts()) {
					String name = dataflowInputPort.getName();
					inputValuesMap.put(name, facade.getPushedDataMap().get(name));
				}
					saveButton.setEnabled(true);
					saveButton.setFocusable(false);
		 }
	}
	
	@SuppressWarnings("serial")
	private class SaveAllAction extends AbstractAction {
		
		private ResultViewComponent parent;

		public SaveAllAction(String name, ResultViewComponent resultViewComponent) {
			super(name);
			this.parent = resultViewComponent;
		}

		public void actionPerformed(ActionEvent e) {
			
			final JDialog dialog = new JDialog((Frame) null, true);
			dialog.setResizable(false);
			dialog.setLocationRelativeTo(saveButton);
			dialog.setTitle("Workflow run data saver");
			JPanel panel = new JPanel(new BorderLayout());
			DialogTextArea explanation = new DialogTextArea();
			explanation.setText("Select the workflow input and output ports to save the associated data");
			explanation.setColumns(40);
			explanation.setEditable(false);
			explanation.setOpaque(false);
			explanation.setBorder(new EmptyBorder(5, 20, 5, 20));
			explanation.setFocusable(false);
			explanation.setFont(new JLabel().getFont()); // make the font the same as for other components in the dialog
			panel.add(explanation, BorderLayout.NORTH);
			final Map<String, JCheckBox> inputChecks = new HashMap<String, JCheckBox> ();
			final Map<String, JCheckBox> outputChecks = new HashMap<String, JCheckBox> ();
			final Map<JCheckBox, Object> checkReferences =
				new HashMap<JCheckBox, Object>();
			final Map<String, Object> chosenReferences =
				new HashMap<String, Object> ();
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
			JPanel portsPanel = new JPanel();
			portsPanel.setBorder(new CompoundBorder(new EmptyBorder(new Insets(5,10,5,10)), new EtchedBorder(EtchedBorder.LOWERED)));
			portsPanel.setLayout(new GridBagLayout());
			if (!dataflow.getInputPorts().isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5,10,5,10);
				portsPanel.add(new JLabel("Workflow inputs:"), gbc);				JPanel inputsPanel = new JPanel();
				WeakHashMap<String, T2Reference> pushedDataMap =  null;

				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (DataflowInputPort port : dataflow.getInputPorts()) {
					String portName = port.getName();
					Object o = inputPortObjectMap.get(portName);
					if (o == null) {
						ResultTreeNode root = (ResultTreeNode) inputPortModelMap.get(portName).getRoot();
						o = root.getAsObject();
						inputPortObjectMap.put(portName, o);
					}
					JCheckBox checkBox = new JCheckBox(portName);
					checkBox
							.setSelected(!resultReferencesMap.containsKey(portName));
					checkBox.addItemListener(listener);
					inputChecks.put(portName, checkBox);
					sortedBoxes.put(portName, checkBox);
					checkReferences.put(checkBox, o);
				}
				gbc.insets = new Insets(0,10,0,10);
				for (String portName : sortedBoxes.keySet()) {
					gbc.gridy++;
					portsPanel.add(sortedBoxes.get(portName), gbc);
				gbc.gridy++;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new Insets(5,10,5,10);
				portsPanel.add(new JLabel(""), gbc); // empty space
				}
			}
			if (!dataflow.getOutputPorts().isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.FIRST_LINE_START;
				gbc.fill = GridBagConstraints.HORIZONTAL;
				gbc.weightx = 1.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5,10,5,10);
				portsPanel.add(new JLabel("Workflow outputs:"), gbc);
				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (DataflowOutputPort port : dataflow.getOutputPorts()) {
					String portName = port.getName();
					Object o = outputPortObjectMap.get(portName);
					if (o == null) {
						ResultTreeNode root = (ResultTreeNode) outputPortModelMap.get(portName).getRoot();
						o = root.getAsObject();
						outputPortObjectMap.put(portName, o);
					}
					resultReferencesMap.put(portName, null);
					JCheckBox checkBox = new JCheckBox(portName);
					checkBox
								.setSelected(true);
						
					checkReferences.put(checkBox, o);
					checkBox.addItemListener(listener);
					outputChecks.put(portName, checkBox);
					sortedBoxes.put(portName, checkBox);
				}
				gbc.insets = new Insets(0,10,0,10);
				for (String portName : sortedBoxes.keySet()) {
					gbc.gridy++;
					portsPanel.add(sortedBoxes.get(portName), gbc);
				}
				gbc.gridy++;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new Insets(5,10,5,10);
				portsPanel.add(new JLabel(""), gbc); // empty space
			}
			panel.add(portsPanel, BorderLayout.CENTER);
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
				/*if (spi instanceof SaveAllResultsAsOPM){
				((SaveAllResultsAsOPM)spi).setIsProvenanceEnabledForRun(isProvenanceEnabledForRun);
				((SaveAllResultsAsOPM)spi).setRunId(runId);
				((SaveAllResultsAsOPM)spi).setDataflow(dataflow);
				}*/
				SaveAllResultsSPI action = (SaveAllResultsSPI) spi.getAction();
				actionSet.add(action);
				JButton saveButton = new JButton((AbstractAction) action);
				action.setChosenReferences(chosenReferences);
				action.setParent(dialog);
				//saveButton.setEnabled(true);
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

	public void pushInputData(WorkflowDataToken token, String portName) {
		ResultTreeModel model = inputPortModelMap.get(portName);
		if (model != null) {
			model.resultTokenProduced(token, portName);
		}
	}
	
	private class ReloadWorkflowAction extends AbstractAction {
		private Dataflow dataflow;
		private Date date;
		
		PerspectiveSPI designPerspective = null;		

		public ReloadWorkflowAction(String name, Dataflow dataflow, Date date) {
			super(name);
			this.dataflow = dataflow;
			this.date = date;
		}

		public void actionPerformed(ActionEvent e) {
			XMLSerializer serialiser = new XMLSerializerImpl();
			XMLDeserializer deserialiser = new XMLDeserializerImpl();
			try {
				FileManager manager = FileManager.getInstance();
				String newName = dataflow.getLocalName() + "_"
				+ DateFormat.getDateTimeInstance().format(date);
				newName = sanitiseName(newName);
				Dataflow alreadyOpened = null;
				for (Dataflow d : manager.getOpenDataflows()) {
					if (d.getLocalName().equals(newName)) {
						alreadyOpened = d;
						break;
					}
				}
				if (alreadyOpened != null) {
					manager.setCurrentDataflow(alreadyOpened);
					switchToDesignPerspective();
				} else {
					DataflowImpl dataflowCopy = (DataflowImpl) deserialiser.deserializeDataflow(serialiser
							.serializeDataflow(dataflow));
					dataflowCopy.setLocalName(newName);
					manager.openDataflow(dataflowCopy);
				}
			} catch (SerializationException e1) {
				logger.error("Unable to copy workflow", e1);
			} catch (DeserializationException e1) {
				logger.error("Unable to copy workflow", e1);
			} catch (EditException e1) {
				logger.error("Unable to copy workflow", e1);
			}
		}
		
		private void switchToDesignPerspective() {
			if (designPerspective == null) {
				for (PerspectiveSPI perspective : Workbench.getInstance()
						.getPerspectives().getPerspectives()) {
					if (perspective.getText().equalsIgnoreCase("design")) {
						designPerspective = perspective;
						break;
					}
				}
			}
			if (designPerspective != null) {
				ModelMap.getInstance().setModel(
						ModelMapConstants.CURRENT_PERSPECTIVE, designPerspective);
			}
		
		}
	}
	
	/**
	 * Checks that the name does not have any characters that are invalid for a
	 * processor name.
	 * 
	 * The name must contain only the chars[A-Za-z_0-9].
	 * 
	 * @param name
	 *            the original name
	 * @return the sanitised name
	 */
	private static String sanitiseName(String name) {
		String result = name;
		if (Pattern.matches("\\w++", name) == false) {
			result = "";
			for (char c : name.toCharArray()) {
				if (Character.isLetterOrDigit(c) || c == '_') {
					result += c;
				} else {
					result += "_";
				}
			}
		}
		return result;
	}


}
