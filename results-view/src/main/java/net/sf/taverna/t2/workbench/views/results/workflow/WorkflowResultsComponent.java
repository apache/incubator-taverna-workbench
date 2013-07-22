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
package net.sf.taverna.t2.workbench.views.results.workflow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
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

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.log4j.Logger;

import uk.org.taverna.databundle.DataBundles;
import uk.org.taverna.platform.report.ReportListener;
import uk.org.taverna.platform.report.State;
import uk.org.taverna.platform.report.WorkflowReport;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.port.WorkflowPort;

/**
 * This component contains a tabbed pane, where each tab displays results for one of the output
 * ports of a workflow, and a set of 'save results' buttons that save results from all ports in a
 * certain format.
 *
 * @author David Withers
 * @author Alex Nenadic
 */
public class WorkflowResultsComponent extends JPanel implements UIComponentSPI, ReportListener {

	private static Logger logger = Logger.getLogger(WorkflowResultsComponent.class);

	private static final long serialVersionUID = 1L;

	// The map contains a mapping for each port to a Path pointing to the port's result(s)
	private HashMap<String, Path> resultReferencesMap = new HashMap<>();

	private HashMap<String, Path> inputReferencesMap = new HashMap<>();

	// Per-port boolean values indicating if all results have been received per port
	private HashMap<String, Boolean> receivedAllResultsForPort = new HashMap<String, Boolean>();

	// Tabbed pane - each tab contains a results tree and a RenderedResultComponent,
	// which in turn contains the currently selected result node rendered according
	// to its mime type and a button for saving the selected individual result
	private JTabbedPane tabbedPane;

	// Panel containing the save buttons
	private JPanel saveButtonsPanel;

	private JButton saveButton;

	// List of all existing 'save results' actions, each one can save results
	// in a different format
	private List<SaveAllResultsSPI> saveActions;

	private Map<String, PortResultsViewTab> inputPortTabMap = new HashMap<String, PortResultsViewTab>();
	private Map<String, PortResultsViewTab> outputPortTabMap = new HashMap<String, PortResultsViewTab>();

	private final RendererRegistry rendererRegistry;

	private final List<SaveIndividualResultSPI> saveIndividualActions;

	private final WorkflowReport workflowReport;

	public WorkflowResultsComponent(WorkflowReport workflowReport,
			RendererRegistry rendererRegistry, List<SaveAllResultsSPI> saveActions,
			List<SaveIndividualResultSPI> saveIndividualActions) {
		super(new BorderLayout());
		this.workflowReport = workflowReport;
		this.rendererRegistry = rendererRegistry;
		this.saveActions = saveActions;
		this.saveIndividualActions = saveIndividualActions;

		workflowReport.addReportListener(this);

		tabbedPane = new JTabbedPane();
		saveButtonsPanel = new JPanel(new BorderLayout());
		add(saveButtonsPanel, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
		try {
			init();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Results View Component";
	}

	@Override
	public void onDisplay() {
	}

	@Override
	public void onDispose() {
	}

	public void init() throws IOException {
		clear();
		populateSaveButtonsPanel();

		Workflow workflow = workflowReport.getSubject();
		Path inputs = DataBundles.getInputs(workflowReport.getInputs());
		Path outputs = DataBundles.getOutputs(workflowReport.getOutputs());

		// Input ports
		for (InputWorkflowPort dataflowInputPort : workflow.getInputPorts()) {
			String portName = dataflowInputPort.getName();
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(DataBundles.getPort(inputs, portName),
					rendererRegistry, saveIndividualActions);

			inputPortTabMap.put(portName, resultTab);

			tabbedPane.addTab(portName, WorkbenchIcons.inputIcon, resultTab, "Input port "
					+ portName);
		}

		// Output ports
		for (OutputWorkflowPort dataflowOutputPort : workflow.getOutputPorts()) {
			String portName = dataflowOutputPort.getName();

			// Initially we have no results for a port
			State state = workflowReport.getState();
			boolean workflowFinished = state == State.CANCELLED || state == State.FAILED
					|| state == State.COMPLETED;
			receivedAllResultsForPort.put(portName, new Boolean(workflowFinished));

			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(DataBundles.getPort(outputs, portName),
					rendererRegistry, saveIndividualActions);
			outputPortTabMap.put(portName, resultTab);

			tabbedPane.addTab(portName, WorkbenchIcons.outputIcon, resultTab, "Output port "
					+ portName);
		}
		// Select the first output port tab
		if (!workflow.getOutputPorts().isEmpty()) {
			PortResultsViewTab tab = outputPortTabMap.get(workflow.getOutputPorts().first()
					.getName());
			tabbedPane.setSelectedComponent(tab);
		}

		revalidate();

	}

	private void populateSaveButtonsPanel() {
		saveButton = new JButton(new SaveAllAction("Save all values", this));
		saveButtonsPanel.add(saveButton, BorderLayout.EAST);
	}

	public void selectWorkflowPortTab(WorkflowPort port) {
		PortResultsViewTab tab;
		if (port instanceof InputWorkflowPort) {
			tab = inputPortTabMap.get(port.getName());
		} else {
			tab = outputPortTabMap.get(port.getName());
		}
		if (tab != null) {
			tabbedPane.setSelectedComponent(tab);
		}
	}

	public void clear() {
		saveButtonsPanel.removeAll();
		tabbedPane.removeAll();
	}

	public void outputAdded(Path path, String portName, int[] index) {
		// If we have finished receiving results - index.length is 0
		if (index.length == 0) {
			receivedAllResultsForPort.put(portName, new Boolean(Boolean.TRUE));
			// We know that at this point the path contains all result(s)
			// Put the resultsRef in the resultReferencesMap
			resultReferencesMap.put(portName, path);
		}

		// If this is the last token for all ports - update the save buttons' state
		boolean receivedAll = true;
		for (String pName : receivedAllResultsForPort.keySet()) {
			if (!receivedAllResultsForPort.get(pName).booleanValue()) {
				receivedAll = false;
				break;
			}
		}
		if (receivedAll) {
			try {
				Path inputs = DataBundles.getInputs(workflowReport.getInputs());
				for (InputWorkflowPort dataflowInputPort : workflowReport.getSubject()
						.getInputPorts()) {
					String name = dataflowInputPort.getName();
					inputReferencesMap.put(name, DataBundles.getPort(inputs, portName));
				}
				saveButton.setEnabled(true);
				saveButton.setFocusable(false);
			} catch (IOException e) {
				logger.warn("Error retieving workflow inputs", e);
			}
		}
	}

	public void update() {
		for (PortResultsViewTab portResultsViewTab : inputPortTabMap.values()) {
			portResultsViewTab.update();
		}
		for (PortResultsViewTab portResultsViewTab : outputPortTabMap.values()) {
			portResultsViewTab.update();
		}
	}

	@SuppressWarnings("serial")
	private class SaveAllAction extends AbstractAction {

		// private WorkflowResultsComponent parent;

		public SaveAllAction(String name, WorkflowResultsComponent resultViewComponent) {
			super(name);
			// this.parent = resultViewComponent;
			putValue(SMALL_ICON, WorkbenchIcons.saveAllIcon);
		}

		public void actionPerformed(ActionEvent e) {

			String title = "Workflow run data saver";

			final JDialog dialog = new HelpEnabledDialog(MainWindow.getMainWindow(), title, true);
			dialog.setResizable(false);
			dialog.setLocationRelativeTo(MainWindow.getMainWindow());
			JPanel panel = new JPanel(new BorderLayout());
			DialogTextArea explanation = new DialogTextArea();
			explanation
					.setText("Select the workflow input and output ports to save the associated data");
			explanation.setColumns(40);
			explanation.setEditable(false);
			explanation.setOpaque(false);
			explanation.setBorder(new EmptyBorder(5, 20, 5, 20));
			explanation.setFocusable(false);
			explanation.setFont(new JLabel().getFont()); // make the font the same as for other
															// components in the dialog
			panel.add(explanation, BorderLayout.NORTH);
			final Map<String, JCheckBox> inputChecks = new HashMap<String, JCheckBox>();
			final Map<String, JCheckBox> outputChecks = new HashMap<String, JCheckBox>();
			final Map<JCheckBox, Path> checkReferences = new HashMap<JCheckBox, Path>();
			final Map<String, Path> chosenReferences = new HashMap<String, Path>();
			final Set<Action> actionSet = new HashSet<Action>();

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
							chosenReferences.put(checkBox.getText(), checkReferences.get(checkBox));
						}
					}
				}

			};
			JPanel portsPanel = new JPanel();
			portsPanel.setLayout(new GridBagLayout());
			if (!workflowReport.getSubject().getInputPorts().isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel("Workflow inputs:"), gbc);
				// JPanel inputsPanel = new JPanel();
				// WeakHashMap<String, T2Reference> pushedDataMap = null;

				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (InputWorkflowPort port : workflowReport.getSubject().getInputPorts()) {
					String portName = port.getName();
					Path o = inputReferencesMap.get(portName);
					if (o == null) {
						WorkflowResultTreeNode root = (WorkflowResultTreeNode) inputPortTabMap
								.get(portName).getResultModel().getRoot();
						o = root.getReference();
					}
					JCheckBox checkBox = new JCheckBox(portName);
					checkBox.setSelected(!resultReferencesMap.containsKey(portName));
					checkBox.addItemListener(listener);
					inputChecks.put(portName, checkBox);
					sortedBoxes.put(portName, checkBox);
					checkReferences.put(checkBox, o);
				}
				gbc.insets = new Insets(0, 10, 0, 10);
				for (String portName : sortedBoxes.keySet()) {
					gbc.gridy++;
					portsPanel.add(sortedBoxes.get(portName), gbc);
				}
				gbc.gridy++;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel(""), gbc); // empty space
			}
			if (!workflowReport.getSubject().getOutputPorts().isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel("Workflow outputs:"), gbc);
				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (OutputWorkflowPort port : workflowReport.getSubject().getOutputPorts()) {
					String portName = port.getName();
					Path o = resultReferencesMap.get(portName);
					if (o == null) {
						WorkflowResultTreeNode root = (WorkflowResultTreeNode) outputPortTabMap
								.get(portName).getResultModel().getRoot();
						o = root.getReference();
					}
					JCheckBox checkBox = new JCheckBox(portName);
					checkBox.setSelected(true);

					checkReferences.put(checkBox, o);
					checkBox.addItemListener(listener);
					outputChecks.put(portName, checkBox);
					sortedBoxes.put(portName, checkBox);
				}
				gbc.insets = new Insets(0, 10, 0, 10);
				for (String portName : sortedBoxes.keySet()) {
					gbc.gridy++;
					portsPanel.add(sortedBoxes.get(portName), gbc);
				}
				gbc.gridy++;
				gbc.fill = GridBagConstraints.BOTH;
				gbc.weightx = 1.0;
				gbc.weighty = 1.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel(""), gbc); // empty space
			}
			panel.add(portsPanel, BorderLayout.CENTER);
			chosenReferences.clear();
			for (JCheckBox checkBox : checkReferences.keySet()) {
				if (checkBox.isSelected()) {
					chosenReferences.put(checkBox.getText(), checkReferences.get(checkBox));
				}
			}

			JPanel buttonsBar = new JPanel();
			buttonsBar.setLayout(new FlowLayout());
			// Get all existing 'Save result' actions
			for (SaveAllResultsSPI spi : saveActions) {
				AbstractAction action = spi.getAction();
				actionSet.add(action);
				JButton saveButton = new JButton((AbstractAction) action);
				if (action instanceof SaveAllResultsSPI) {
					((SaveAllResultsSPI) action).setChosenReferences(chosenReferences);
					((SaveAllResultsSPI) action).setParent(dialog);
				}
				// saveButton.setEnabled(true);
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
