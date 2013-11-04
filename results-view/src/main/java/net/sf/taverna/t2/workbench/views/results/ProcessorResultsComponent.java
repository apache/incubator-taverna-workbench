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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.results.processor.FilteredIterationTreeModel;
import net.sf.taverna.t2.workbench.views.results.processor.FilteredIterationTreeModel.FilterType;
import net.sf.taverna.t2.workbench.views.results.processor.ProcessorEnactmentsTreeModel;
import net.sf.taverna.t2.workbench.views.results.processor.ProcessorEnactmentsTreeNode;
import net.sf.taverna.t2.workbench.views.results.processor.ProcessorPortResultsViewTab;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.log4j.Logger;

import uk.org.taverna.platform.report.Invocation;
import uk.org.taverna.platform.report.ProcessorReport;
import uk.org.taverna.scufl2.api.core.Processor;

/**
 * A component that contains a tabbed pane for displaying inputs and outputs of a processor (i.e.
 * intermediate results for a workflow run).
 *
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class ProcessorResultsComponent extends JPanel implements Updatable {

	private static Logger logger = Logger.getLogger(ProcessorResultsComponent.class);

	private static SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String HOURS = "h";
	private static final String MINUTES = "m";
	private static final String SECONDS = "s";
	private static final String MILLISECONDS = "ms";

	private CardLayout cardLayout = new CardLayout();

	private Updatable updatableComponent;

	// JSplitPane that contains the invocation list for the processor on the
	// left and a tabbed pane with processors ports on the right.
	private JSplitPane splitPane;

	// Tree containing enactments (invocations) of the processor.
	protected JTree processorEnactmentsTree;

	private JList<Invocation> invocationsList;

	private Map<String, InvocationView> invocationResults = new HashMap<>();

	// Panel containing the title
	private JPanel titlePanel;

	boolean resultsUpdateNeeded = false;

	private ProcessorEnactmentsTreeModel processorEnactmentsTreeModel;

	private FilteredIterationTreeModel filteredTreeModel;

	private JLabel iterationLabel;

	// List of all existing 'save results' actions, each one can save results
	// in a different format
	private final List<SaveAllResultsSPI> saveActions;

	private JButton saveAllButton;

	private String processorId = null;

	private ProcessorEnactmentsTreeNode procEnactmentTreeNode = null;

	private final RendererRegistry rendererRegistry;

	private final List<SaveIndividualResultSPI> saveIndividualActions;

	private final ProcessorReport processorReport;

	private Processor processor;

	public ProcessorResultsComponent(ProcessorReport processorReport,
			RendererRegistry rendererRegistry, List<SaveAllResultsSPI> saveActions,
			List<SaveIndividualResultSPI> saveIndividualActions) {
		super(new BorderLayout());
		this.processorReport = processorReport;
		this.rendererRegistry = rendererRegistry;
		this.saveActions = saveActions;
		this.saveIndividualActions = saveIndividualActions;

		processor = processorReport.getSubject();

		initComponents();
	}

	public void initComponents() {
		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EmptyBorder(5, 0, 5, 0));

		String title = "<html><body>Intermediate values for the service <b>" + processor.getName()
				+ "</b></body></html>";
		JLabel tableLabel = new JLabel(title);
		titlePanel.add(tableLabel, BorderLayout.WEST);
		iterationLabel = new JLabel();
		int spacing = iterationLabel.getFontMetrics(iterationLabel.getFont()).charWidth(' ');
		iterationLabel.setBorder(BorderFactory.createEmptyBorder(0, spacing * 5, 0, 0));
		titlePanel.add(iterationLabel, BorderLayout.CENTER);

		saveAllButton = new JButton(new SaveAllAction("Save iteration values", this));
		saveAllButton.setEnabled(false);

		titlePanel.add(saveAllButton, BorderLayout.EAST);
		add(titlePanel, BorderLayout.NORTH);


		invocationsList = new JList<Invocation>(processorReport.getInvocations().toArray(
				new Invocation[0]));
		invocationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		invocationsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
//					Component selectedComponent = tabbedPane.getSelectedComponent();
//					if (selectedComponent instanceof PortResultsViewTab) {
//						PortResultsViewTab portResultsViewTab = (PortResultsViewTab) selectedComponent;
//						Port port = portResultsViewTab.getPort();
//						Invocation invocation = invocationsList.getSelectedValue();
//						if (port instanceof InputProcessorPort) {
//							portResultsViewTab.setPath(invocation.getInputs().get(port.getName()));
//						} else if (port instanceof OutputProcessorPort) {
//							portResultsViewTab.setPath(invocation.getOutputs().get(port.getName()));
//						}
//					}
				}
			}
		});

		// processorEnactmentsTreeModel = new ProcessorEnactmentsTreeModel(enactmentsGotSoFar,
		// enactmentsWithErrorInputs, enactmentsWithErrorOutputs);
		// filteredTreeModel = new FilteredIterationTreeModel(processorEnactmentsTreeModel);
		// processorEnactmentsTree = new JTree(filteredTreeModel);
		// processorEnactmentsTree.setRootVisible(false);
		// processorEnactmentsTree.setShowsRootHandles(true);
		// processorEnactmentsTree.getSelectionModel().setSelectionMode(
		// TreeSelectionModel.SINGLE_TREE_SELECTION);
		// // Start listening for selections in the enactments tree
		// processorEnactmentsTree.addTreeSelectionListener(new TreeSelectionListener() {
		// public void valueChanged(TreeSelectionEvent e) {
		// // Change the result for the selected enactment in the
		// // current tab
		// setDataTreeForResultTab();
		// }
		// });
		// processorEnactmentsTree.setCellRenderer(new DefaultTreeCellRenderer() {
		// public Component getTreeCellRendererComponent(JTree tree, Object value,
		// boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		// super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
		// hasFocus);
		// if (value instanceof IterationTreeNode) {
		// IterationTreeNode iterationTreeNode = (IterationTreeNode) value;
		// ErrorState errorState = iterationTreeNode.getErrorState();
		// if (errorState.equals(ErrorState.OUTPUT_ERRORS)) {
		// setForeground(Color.RED);
		// } else if (errorState.equals(ErrorState.INPUT_ERRORS)) {
		// setForeground(new Color(0xdd, 0xa7, 0x00));
		// }
		// }
		// return this;
		// }
		// });


		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
//		splitPane.setBottomComponent(tabbedPane);

		final JComboBox filterChoiceBox = new JComboBox(new FilterType[] { FilterType.ALL,
				FilterType.RESULTS, FilterType.ERRORS, FilterType.SKIPPED });
		filterChoiceBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// filteredTreeModel.setFilter((FilterType) filterChoiceBox.getSelectedItem());
				// ProcessorResultsComponent.this.updateTree();
			}
		});

		// filterChoiceBox.setSelectedIndex(0);
		JPanel enactmentsTreePanel = new JPanel(new BorderLayout());
		// JPanel enactmentsComboPanel = new JPanel(new BorderLayout());
		// enactmentsComboPanel.add(filterChoiceBox, BorderLayout.WEST);
		// enactmentsTreePanel.add(enactmentsComboPanel, BorderLayout.NORTH);

		JScrollPane jScrollPane = new JScrollPane(invocationsList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jScrollPane.setMinimumSize(new Dimension(100, 0));
		enactmentsTreePanel.add(jScrollPane, BorderLayout.CENTER);
		splitPane.setTopComponent(enactmentsTreePanel);
		add(splitPane, BorderLayout.CENTER);

		resultsUpdateNeeded = true;
		update();
	}

	public static String formatMilliseconds(long timeInMiliseconds) {
		double timeInSeconds;
		if (timeInMiliseconds < 1000) {
			return timeInMiliseconds + " " + MILLISECONDS;
		}
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(1);
		numberFormat.setMinimumFractionDigits(1);
		timeInSeconds = timeInMiliseconds / 1000.0;
		if (timeInSeconds < 60) {
			return numberFormat.format(timeInSeconds) + " " + SECONDS;
		}
		double timeInMinutes = timeInSeconds / 60.0;
		if (timeInMinutes < 60) {
			return numberFormat.format(timeInMinutes) + " " + MINUTES;
		}
		double timeInHours = timeInMinutes / 60.0;
		return numberFormat.format(timeInHours) + " " + HOURS;

	}

	private void disableResultTabForNode(final ProcessorPortResultsViewTab selectedResultTab,
			DefaultMutableTreeNode lastPathComponent) {
		selectedResultTab.setResultsTree(null);
		String label = labelForNode(lastPathComponent);
		iterationLabel.setText(label);
		saveAllButton.setEnabled(false);
	}

	// private StringBuffer labelForProcEnactment(ProcessorEnactmentsTreeNode procEnactmentTreeNode,
	// ProcessorEnactment processorEnactment) {
	// StringBuffer iterationLabelText = new StringBuffer();
	// // Use <html> so we can match font metrics of titleJLabel
	// iterationLabelText.append("<html><body>");
	// iterationLabelText.append(procEnactmentTreeNode);
	// Timestamp started = processorEnactment.getEnactmentStarted();
	// Timestamp ended = processorEnactment.getEnactmentEnded();
	// if (started != null) {
	// if (procEnactmentTreeNode.getErrorState().equals(ErrorState.INPUT_ERRORS)) {
	// iterationLabelText.append(" <font color='#cc9700'>skipped</font> ");
	// } else {
	// iterationLabelText.append(" started ");
	// }
	// iterationLabelText.append(ISO_8601.format(started));
	// }
	// if (ended != null && !procEnactmentTreeNode.getErrorState().equals(ErrorState.INPUT_ERRORS))
	// {
	// // Don't show End time if there was input errors
	//
	// if (started != null) {
	// iterationLabelText.append(", ");
	// }
	// if (procEnactmentTreeNode.getErrorState().equals(ErrorState.OUTPUT_ERRORS)) {
	// iterationLabelText.append(" <font color='red'>failed</font> ");
	// } else {
	// iterationLabelText.append(" ended ");
	// }
	// iterationLabelText.append(ISO_8601.format(ended));
	// if (started != null) {
	// long duration = ended.getTime() - started.getTime();
	// iterationLabelText.append(" (");
	// iterationLabelText.append(formatMilliseconds(duration));
	// iterationLabelText.append(")");
	// }
	// }
	// iterationLabelText.append("</body></html>");
	// return iterationLabelText;
	// }

	private String labelForNode(DefaultMutableTreeNode node) {
		StringBuffer label = new StringBuffer();
		if (node == null) {
			label.append("No selection");
		} else {
			label.append(node);
			if (node.getUserObject() != null) {
				label.append(" containing ");
				label.append(node.getLeafCount());
				label.append(" iterations");
			}
		}
		return label.toString();
	}

	// public void populateEnactmentsMaps() {
	// synchronized (enactmentsGotSoFar) {
	// // Get processor enactments (invocations) from provenance
	//
	// // Create the array of nested processors' names
	// String[] processorNamesPath = null;
	// if (processorsPath != null) { // should not be null really
	// processorNamesPath = new String[processorsPath.size()];
	// int i = 0;
	// for (Processor proc : processorsPath) {
	// processorNamesPath[i++] = proc.getLocalName();
	// }
	// } else { // This should not really happen!
	// processorNamesPath = new String[1];
	// processorNamesPath[0] = processor.getLocalName();
	// }
	//
	// List<ProcessorEnactment> processorEnactmentsStack = provenanceAccess
	// .getProcessorEnactments(runId, processorNamesPath);
	//
	// if (processorId == null && !processorEnactmentsStack.isEmpty()) {
	// // Extract processor ID from very first processorEnactment
	// processorId = processorEnactmentsStack.get(0).getProcessorId();
	// }
	//
	// while (!processorEnactmentsStack.isEmpty()) {
	// // fetch LAST one first, so we'll get the parent's early
	// ProcessorEnactment processorEnactment = processorEnactmentsStack
	// .remove(processorEnactmentsStack.size() - 1);
	// if (!enactmentsGotSoFar.contains(processorEnactment)) {
	// enactmentsGotSoFar.add(processorEnactment);
	// enactmentIdsGotSoFar.add(processorEnactment.getProcessEnactmentId());
	//
	// String parentId = processorEnactment.getParentProcessorEnactmentId();
	// if (parentId != null && !enactmentIdsGotSoFar.contains(parentId)) {
	// /*
	// * Also add parent (and their parent, etc) - so that we can show the full
	// * iteration treeenactmentIdsGotSoFar
	// */
	//
	// ProcessorEnactment parentEnactment = provenanceAccess
	// .getProcessorEnactment(parentId);
	// if (parentEnactment == null) {
	// logger.error("Could not find parent processor enactment id=" + parentId
	// + ", skipping " + processorEnactment);
	// enactmentsGotSoFar.remove(processorEnactment);
	// enactmentIdsGotSoFar.remove(processorEnactment);
	// continue;
	// }
	// processorEnactmentsStack.add(parentEnactment);
	// }
	// }
	// if (!processorEnactment.getProcessorId().equals(processorId)) {
	// // A parent processors, no need to fetch their data bindings
	// continue;
	// }
	//
	// String initialInputs = processorEnactment.getInitialInputsDataBindingId();
	// String finalOutputs = processorEnactment.getFinalOutputsDataBindingId();
	//
	// boolean fetchingInputs = initialInputs != null
	// && !enactmentsToInputPortData.containsKey(processorEnactment);
	// boolean fetchingOutputs = finalOutputs != null
	// && !enactmentsToOutputPortData.containsKey(processorEnactment);
	//
	// Map<Port, T2Reference> dataBindings = new HashMap<Port, T2Reference>();
	//
	// if (fetchingInputs) {
	// dataBindings = provenanceAccess.getDataBindings(initialInputs);
	// enactmentsToInputPortData
	// .put(processorEnactment, new ArrayList<List<Object>>());
	// }
	// if (fetchingOutputs) {
	// enactmentsToOutputPortData.put(processorEnactment,
	// new ArrayList<List<Object>>());
	// if (!fetchingInputs || !finalOutputs.equals(initialInputs)) {
	// dataBindings.putAll(provenanceAccess.getDataBindings(finalOutputs));
	// }
	// }
	//
	// for (Entry<Port, T2Reference> entry : dataBindings.entrySet()) {
	// /*
	// * Create (port, t2Ref, tree) list for this enactment. Tree is set to null
	// * initially and populated on demand (when user clicks on particular
	// * enactment/iteration node).
	// */
	// List<Object> dataOnPortList = new ArrayList<Object>();
	// Port port = entry.getKey();
	// dataOnPortList.add(port); // port
	// T2Reference t2Reference = entry.getValue();
	// dataOnPortList.add(t2Reference); // t2Ref
	// /*
	// * tree (will be populated when a user clicks on this iteration and this port
	// * tab is selected)
	// */
	// dataOnPortList.add(null);
	//
	// if (port.isInputPort() && fetchingInputs) { // Input port
	// if (t2Reference.containsErrors()) {
	// enactmentsWithErrorInputs.add(processorEnactment);
	// }
	// List<List<Object>> listOfPortDataLists = enactmentsToInputPortData
	// .get(processorEnactment);
	// listOfPortDataLists.add(dataOnPortList);
	// enactmentsToInputPortData.put(processorEnactment, listOfPortDataLists);
	// } else if (!port.isInputPort() && fetchingOutputs) { // output port
	// if (t2Reference.containsErrors()) {
	// enactmentsWithErrorOutputs.add(processorEnactment);
	// }
	// List<List<Object>> listOfPortDataLists = enactmentsToOutputPortData
	// .get(processorEnactment);
	// listOfPortDataLists.add(dataOnPortList);
	// enactmentsToOutputPortData.put(processorEnactment, listOfPortDataLists);
	// }
	// }
	// }
	// }
	// }

	private List<TreePath> expandedPaths = new ArrayList<TreePath>();
	private TreePath selectionPath = null;

	private void rememberPaths() {
		expandedPaths.clear();
		for (Enumeration e = processorEnactmentsTree.getExpandedDescendants(new TreePath(
				filteredTreeModel.getRoot())); (e != null) && e.hasMoreElements();) {
			expandedPaths.add((TreePath) e.nextElement());
		}
		selectionPath = processorEnactmentsTree.getSelectionPath();
	}

	private void reinstatePaths() {
		for (TreePath path : expandedPaths) {
			if (filteredTreeModel.isShown((DefaultMutableTreeNode) path.getLastPathComponent())) {
				processorEnactmentsTree.expandPath(path);
			}
		}
		if (selectionPath != null) {
			if (filteredTreeModel.isShown((DefaultMutableTreeNode) selectionPath
					.getLastPathComponent())) {
				processorEnactmentsTree.setSelectionPath(selectionPath);
			} else {
				processorEnactmentsTree.clearSelection();
			}
		}
	}

	// public void updateTree() {
	// rememberPaths();
	// processorEnactmentsTreeModel.update(enactmentsGotSoFar);
	// filteredTreeModel.reload();
	// reinstatePaths();
	// DefaultMutableTreeNode firstLeaf = ((DefaultMutableTreeNode) filteredTreeModel.getRoot())
	// .getFirstLeaf();
	// if ((firstLeaf != null) && (processorEnactmentsTree.getPathForRow(0) == null)) {
	// processorEnactmentsTree
	// .scrollPathToVisible(new TreePath((Object[]) firstLeaf.getPath()));
	// }
	//
	// setDataTreeForResultTab();
	// }

	// private Runnable updateTreeRunnable = new Runnable() {
	// public void run() {
	// updateTree();
	// }
	// };

	public void update() {
		// if (resultsUpdateNeeded) {
		// IntermediateValuesSwingWorker intermediateValuesSwingWorker = new
		// IntermediateValuesSwingWorker(
		// this);
		// IntermediateValuesInProgressDialog dialog = new IntermediateValuesInProgressDialog();
		// intermediateValuesSwingWorker
		// .addPropertyChangeListener(new SwingWorkerCompletionWaiter(dialog));
		// intermediateValuesSwingWorker.execute();
		//
		// try {
		// Thread.sleep(500);
		// } catch (InterruptedException e) {
		// }
		// if (!intermediateValuesSwingWorker.isDone()) {
		// dialog.setVisible(true);
		// }
		// if (intermediateValuesSwingWorker.getException() != null) {
		// logger.error("Populating enactments failed",
		// intermediateValuesSwingWorker.getException());
		// } else {
		// if (SwingUtilities.isEventDispatchThread()) {
		// updateTreeRunnable.run();
		// } else {
		// SwingUtilities.invokeLater(updateTreeRunnable);
		// }
		// }
		// }
	}

	public void onDispose() {
	}

	@Override
	protected void finalize() throws Throwable {
		onDispose();
	}

	@SuppressWarnings("serial")
	private class SaveAllAction extends AbstractAction {

		// private WorkflowResultsComponent parent;

		public SaveAllAction(String name, ProcessorResultsComponent resultViewComponent) {
			super(name);
			// this.parent = resultViewComponent;
			putValue(SMALL_ICON, WorkbenchIcons.saveAllIcon);
		}

		public void actionPerformed(ActionEvent e) {

			Invocation iteration = invocationsList.getSelectedValue();
			// ProcessorEnactment processorEnactment = (ProcessorEnactment) procEnactmentTreeNode
			// .getUserObject();

			String title = "Service iteration data saver";

			final JDialog dialog = new HelpEnabledDialog(MainWindow.getMainWindow(), title, true);
			dialog.setResizable(false);
			dialog.setLocationRelativeTo(MainWindow.getMainWindow());
			JPanel panel = new JPanel(new BorderLayout());
			DialogTextArea explanation = new DialogTextArea();
			explanation
					.setText("Select the service input and output ports to save the associated data");
			explanation.setColumns(40);
			explanation.setEditable(false);
			explanation.setOpaque(false);
			explanation.setBorder(new EmptyBorder(5, 20, 5, 20));
			explanation.setFocusable(false);
			explanation.setFont(new JLabel().getFont()); // make the font the same as for other
															// components in the dialog
			panel.add(explanation, BorderLayout.NORTH);
			final Map<String, JCheckBox> inputChecks = new HashMap<>();
			final Map<String, JCheckBox> outputChecks = new HashMap<>();
			final Map<JCheckBox, Path> checkReferences = new HashMap<>();
			final Map<String, Path> chosenReferences = new HashMap<>();
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
			Map<String, Path> inputPorts = iteration.getInputs();
			Map<String, Path> outputPorts = iteration.getOutputs();
			JPanel portsPanel = new JPanel();
			portsPanel.setBorder(new CompoundBorder(new EmptyBorder(new Insets(5, 10, 5, 10)),
					new EtchedBorder(EtchedBorder.LOWERED)));
			portsPanel.setLayout(new GridBagLayout());
			if (!inputPorts.isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 0;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel("Iteration inputs:"), gbc);
				// JPanel inputsPanel = new JPanel();
				// WeakHashMap<String, T2Reference> pushedDataMap = null;

				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (Entry<String, Path> inputEntry : inputPorts.entrySet()) {
					String portName = inputEntry.getKey();
					Path value = inputEntry.getValue();
					if (value != null) {
						JCheckBox checkBox = new JCheckBox(portName);
						checkBox.setSelected(!outputPorts.containsKey(portName));
						checkBox.addItemListener(listener);
						inputChecks.put(portName, checkBox);
						sortedBoxes.put(portName, checkBox);
						checkReferences.put(checkBox, value);
					}
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
			if (!outputPorts.isEmpty()) {
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = 1;
				gbc.gridy = 0;
				gbc.anchor = GridBagConstraints.WEST;
				gbc.fill = GridBagConstraints.NONE;
				gbc.weightx = 0.0;
				gbc.weighty = 0.0;
				gbc.insets = new Insets(5, 10, 5, 10);
				portsPanel.add(new JLabel("Iteration outputs:"), gbc);
				TreeMap<String, JCheckBox> sortedBoxes = new TreeMap<String, JCheckBox>();
				for (Entry<String, Path> outputEntry : outputPorts.entrySet()) {
					String portName = outputEntry.getKey();
					Path value = outputEntry.getValue();
					if (value != null) {
						JCheckBox checkBox = new JCheckBox(portName);
						checkBox.setSelected(true);

						checkReferences.put(checkBox, value);
						checkBox.addItemListener(listener);
						outputChecks.put(portName, checkBox);
						sortedBoxes.put(portName, checkBox);
					}
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
					// ((SaveAllResultsSPI) action).setChosenReferences(chosenReferences);
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
