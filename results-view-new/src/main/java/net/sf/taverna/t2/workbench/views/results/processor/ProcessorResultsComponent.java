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
package net.sf.taverna.t2.workbench.views.results.processor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;

import org.apache.log4j.Logger;

/**
 * A component that contains a tabbed pane for displaying inputs and outputs
 * of a processor (i.e. intermediate results for a workflow run).
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ProcessorResultsComponent extends JPanel{

	private static Logger logger = Logger
	.getLogger(ProcessorResultsComponent.class);

	// Invocation context
	private InvocationContext context = null;
	
	// The map contains a mapping for each port to a T2Reference pointing to the port's result(s)
	//private HashMap<String, T2Reference> resultReferencesMap = new HashMap<String, T2Reference>();
	
	// Per-port boolean values indicating if all results have been received per port
	//private HashMap<String, Boolean> receivedAllResultsForPort = new HashMap<String, Boolean>();
	
	// JSplitPane that contains the invocation list for the processor on the left and
	// a tabbed pane with processors ports on the right.
	private JSplitPane splitPane;
	
	// Tree containing enactments (invocations) of the processor.
	JTree processorEnactmentsTree;
	
	// Tabbed pane - each tab contains a processor input/outputs data/results tree and 
	// a RenderedProcessorResultComponent, which in turn contains the currently selected 
	// node rendered according to its mime type.
	private JTabbedPane tabbedPane;
	
	// Panel containing the title
	private JPanel titlePanel;
	
	//private WorkflowInstanceFacade facade;

	private ProvenanceConnector provenanceConnector;
	private Processor processor;
	private Dataflow dataflow;
	private String runId;
	private ReferenceService referenceService;
		
	private HashMap<String, ProcessorPortResultsViewTab> inputPortTabMap = new HashMap<String, ProcessorPortResultsViewTab>();
	private HashMap<String, ProcessorPortResultsViewTab> outputPortTabMap = new HashMap<String, ProcessorPortResultsViewTab>();
	
//	private HashMap<String, Object> inputPortObjectMap = new HashMap<String, Object> ();
//	private HashMap<String, Object> outputPortObjectMap = new HashMap<String, Object> ();

	// All data for intermediate results is pulled from provenance.
	private ProvenanceAccess provenanceAccess;

	// Map: enactment -> (port, t2Ref, tree).
	// Each enactment is mapped to a list of 3-element lists. The 3-element list contains
	// processor input/output port, t2ref to data consumed/produced on that port and tree 
	// view of the data. Tree is only created on demand - i.e. when user selects a particular 
	// enactment and a specific port.
	HashMap<ProcessorEnactment, ArrayList<ArrayList<Object>>> enactmentsToInputPortData;
	HashMap<ProcessorEnactment, ArrayList<ArrayList<Object>>> enactmentsToOutputPortData;

	public ProcessorResultsComponent(Processor processor, Dataflow dataflow, String runId, ProvenanceConnector provenanceConnector, ReferenceService referenceService) {
		super(new BorderLayout());
		this.processor = processor;
		this.dataflow = dataflow;
		this.runId = runId;
		this.provenanceConnector = provenanceConnector;
		this.referenceService = referenceService;

		provenanceAccess = new ProvenanceAccess(DataManagementConfiguration.getInstance().getConnectorType());
		
		initComponents();
	}

	public void initComponents() {

		setBorder(new EtchedBorder());
		
		titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBorder(new EmptyBorder(5,0,5,0));
		titlePanel.add(new JLabel("Intermediate results for service: " + processor.getLocalName()), BorderLayout.WEST);
		add(titlePanel, BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();

		// Create enactment to (port, t2ref, tree) lists maps. Populate them later from provenance.
		enactmentsToInputPortData = new HashMap<ProcessorEnactment, ArrayList<ArrayList<Object>>>();
		enactmentsToOutputPortData = new HashMap<ProcessorEnactment, ArrayList<ArrayList<Object>>>();

		// Get processor enactments (invocations) from provenance
		List<ProcessorEnactment> processorEnactments = provenanceAccess.getProcessorEnactments(runId, processor.getLocalName());
		for (ProcessorEnactment processorEnactment : processorEnactments){
			
			String finalOutputs = processorEnactment.getFinalOutputsDataBindingId();
			String initialInputs = processorEnactment.getInitialInputsDataBindingId();

			if (initialInputs != null) {
				Map<Port, T2Reference> dataBindings = provenanceAccess.getDataBindings(initialInputs);
				for (java.util.Map.Entry<Port, T2Reference> entry : dataBindings
						.entrySet()) {
					
					// Create (port, t2Ref, tree) list for this enactment. Tree is set to null 
					// initially and populated on demand (when user clicks on particular 
					// enactment/iteration node).
					ArrayList<Object> dataOnPortList = new ArrayList<Object>();
					dataOnPortList.add(entry.getKey()); // port
					dataOnPortList.add(entry.getValue()); // t2Ref
					dataOnPortList.add(null);// tree (will be populated when a user clicks on this iteration and this port tab is selected)
					
					// Create map which links enactments to the list of (port, t2Ref, tree) lists;
					// one for each processor input and output port
					ArrayList<ArrayList<Object>> listOfPortDataLists = null;
					
					// Have we already created an entry for this enactment in the map?
					// If not - create one now.
					if (entry.getKey().isInputPort()){
						if (enactmentsToInputPortData.get(entry
								.getKey()) == null) { // input port
							enactmentsToInputPortData.put(processorEnactment, new ArrayList<ArrayList<Object>>());
						}	
						listOfPortDataLists = enactmentsToInputPortData.get(processorEnactment);
						listOfPortDataLists.add(dataOnPortList); // add entry for this port
						enactmentsToInputPortData.put(processorEnactment, listOfPortDataLists);
					}
				}
			}	

			if (finalOutputs != null) {
				Map<Port, T2Reference> dataBindings = provenanceAccess.getDataBindings(finalOutputs);
				for (java.util.Map.Entry<Port, T2Reference> entry : dataBindings
						.entrySet()) {
					
					// Create (port, t2Ref, tree) list for this enactment. Tree is set to null 
					// initially and populated on demand (when user clicks on particular 
					// enactment/iteration node).
					ArrayList<Object> dataOnPortList = new ArrayList<Object>();
					dataOnPortList.add(entry.getKey()); // port
					dataOnPortList.add(entry.getValue()); // t2Ref
					dataOnPortList.add(null);// tree (will be populated when a user clicks on this iteration and this port tab is selected)
					
					// Create map which links enactments to the list of (port, t2Ref, tree) lists;
					// one for each processor input and output port
					ArrayList<ArrayList<Object>> listOfPortDataLists = null;
					
					// Have we already created an entry for this enactment in the map?
					// If not - create one now.
					if (!entry.getKey().isInputPort()){ // output port
						if (enactmentsToOutputPortData.get(entry
								.getKey()) == null){
							enactmentsToOutputPortData.put(processorEnactment, new ArrayList<ArrayList<Object>>());
						}
						listOfPortDataLists = enactmentsToOutputPortData.get(processorEnactment);
						listOfPortDataLists.add(dataOnPortList); // add entry for this port
						enactmentsToOutputPortData.put(processorEnactment, listOfPortDataLists);
					}

				}
			}	
		}

		// Processor output ports
		List<ProcessorOutputPort> processorOutputPorts = new ArrayList<ProcessorOutputPort>(
				processor.getOutputPorts());
		Collections.sort(processorOutputPorts,
				new Comparator<ProcessorOutputPort>() {

					public int compare(ProcessorOutputPort o1,
							ProcessorOutputPort o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
		for (ProcessorOutputPort processorOutputPort : processorOutputPorts) {
			String portName = processorOutputPort.getName();
			ProcessorPortResultsViewTab resultTab = new ProcessorPortResultsViewTab(
					portName);
			resultTab.setIsOutputPortTab(true);
			outputPortTabMap.put(portName, resultTab);
			tabbedPane.addTab(portName, WorkbenchIcons.outputIcon, resultTab);
		}

		// Processor input ports
		List<ProcessorInputPort> processorInputPorts = new ArrayList<ProcessorInputPort>(
				processor.getInputPorts());
		Collections.sort(processorInputPorts,
				new Comparator<ProcessorInputPort>() {

					public int compare(ProcessorInputPort o1,
							ProcessorInputPort o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
		for (ProcessorInputPort processorInputPort : processorInputPorts) {
			String portName = processorInputPort.getName();
			ProcessorPortResultsViewTab resultTab = new ProcessorPortResultsViewTab(portName);
			resultTab.setIsOutputPortTab(false);
			inputPortTabMap.put(portName,resultTab);
			tabbedPane.addTab(portName, WorkbenchIcons.inputIcon, resultTab);
		}
		
		// Create the invocations tree
		processorEnactmentsTree = new JTree(new ProcessorEnactmentsTreeModel(processorEnactments));
		processorEnactmentsTree.setRootVisible(false);
		processorEnactmentsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		// Start listening for selections in the enactments tree
		processorEnactmentsTree.addTreeSelectionListener(new TreeSelectionListener() {		
			public void valueChanged(TreeSelectionEvent e) {
				// Change the result for the selected enactment in the current tab
				setDataTreeForResultTab();
			}
		});
	    
		// Register a tab change listener 
		tabbedPane.addChangeListener(new ChangeListener() {			
			public void stateChanged(ChangeEvent evt) { 
				setDataTreeForResultTab();
			} 
		}); 
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setBottomComponent(tabbedPane);
		
		JPanel enactmentsTreePanel = new JPanel(new BorderLayout());
		JPanel enactmentsLabelPanel = new JPanel(new BorderLayout());
		enactmentsLabelPanel.add(new JLabel("Iteration index:"), BorderLayout.WEST);
		enactmentsTreePanel.add(enactmentsLabelPanel, BorderLayout.NORTH);
		enactmentsTreePanel.add(new JScrollPane(processorEnactmentsTree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
		splitPane.setTopComponent(enactmentsTreePanel);
		add(splitPane, BorderLayout.CENTER);
	}
	
	private void setDataTreeForResultTab(){
		ProcessorPortResultsViewTab selectedResultTab = (ProcessorPortResultsViewTab) tabbedPane
				.getSelectedComponent();
		if (!processorEnactmentsTree.getSelectionModel().isSelectionEmpty()){ // it is empty initially
			TreePath selectedPath = processorEnactmentsTree.getSelectionModel().getSelectionPath();
			ProcessorEnactment processorEnactment = (ProcessorEnactment)((ProcessorEnactmentsTreeNode)selectedPath.getLastPathComponent()).getUserObject();
			HashMap<ProcessorEnactment, ArrayList<ArrayList<Object>>> map = null;
			if (selectedResultTab.getIsOutputPortTab()){ // output port tab
				map  = enactmentsToOutputPortData;
			}
			else{ // input port tab
				map = enactmentsToInputPortData;
			}
			ArrayList<ArrayList<Object>> listOfListsOfPortData = map.get(processorEnactment);

			JTree tree = null;
			int index = -1;
			ArrayList<Object> triple = null;
			// Get the tree for this port and this enactment and show it on results tab
			for ( ArrayList<Object> listOfPortData : listOfListsOfPortData){
				// Find data in the map for this port
				if (selectedResultTab.getPortName().equals(((Port)listOfPortData.get(0)).getPortName())){ 
					// list.get(0) contains the port
					// list.get(1) contains the t2Ref to data
					// list.get(2) contains the tree
					if (listOfPortData.get(2) == null){ // tree has not been created yet
						// Create a tree for this data
						tree = new JTree(new ProcessorResultsTreeModel((T2Reference)listOfPortData.get(1), referenceService));
						// Remember this triple and its index in the big list so we can 
						// update the map for this enactment after we have finished iterating
						index = listOfListsOfPortData.indexOf(listOfPortData);
						triple = listOfPortData;
						tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
						tree.setExpandsSelectedPaths(true);
						tree.setLargeModel(true);
						tree.setRootVisible(false);
						tree.setCellRenderer(new ProcessorResultCellRenderer());
						// Expand the whole tree
						 for (int row = 0; row < tree.getRowCount(); row ++) {
						    tree.expandRow(row);
						 }
						tree.addTreeSelectionListener(new TreeSelectionListener() {
							public void valueChanged(TreeSelectionEvent e) {
								TreePath selectionPath = e.getNewLeadSelectionPath();
								if (selectionPath != null) {
									// Get the selected node
									final Object selectedNode = selectionPath.getLastPathComponent();
									ProcessorPortResultsViewTab selectedResultTab = (ProcessorPortResultsViewTab) tabbedPane
									.getSelectedComponent();
									RenderedProcessorResultComponent renderedResultComponent = selectedResultTab.getRenderedResultComponent();
									renderedResultComponent.setNode((ProcessorResultTreeNode) selectedNode);
								}
							}

						});
						triple.set(2, tree); // set the new tree
					}
					else{
						tree = (JTree)listOfPortData.get(2);
					}
					break;
				}
			}
			if (index != -1){
				// Put the tree in the map and put the modified list back to the map
				listOfListsOfPortData.set(index, triple);
				map.put(processorEnactment, listOfListsOfPortData);
			}
			
			// Show the tree
			selectedResultTab.setResultsTree(tree);
		}
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
		//saveButtonsPanel.removeAll();
		tabbedPane.removeAll();
	}

//	public void resultTokenProduced(WorkflowDataToken token, String portName) {
//		
//		// Set the invocation context the first time you get the chance
//		if (context == null)
//			context = token.getContext();
//		
//		// If we have finished receiving results - token.getIndex().length is 0
//		if (token.getIndex().length == 0){
//			receivedAllResultsForPort.put(portName, new Boolean(Boolean.TRUE));
//			// We know that at this point the token.getData() contains a T2Reference to 
//			// all result(s)
//			T2Reference resultsRef = token.getData();
//			// Put the resultsRef in the resultReferencesMap
//			resultReferencesMap.put(portName, resultsRef);
//		}
//		
//		// If this is the last token for all ports - update the save buttons' state
//		 boolean receivedAll = true;
//		 for (String pName : receivedAllResultsForPort.keySet()){
//		 	if (!receivedAllResultsForPort.get(pName).booleanValue()){
//		 		receivedAll = false;
//		 		break;
//		 	}
//		 }
//		 if (receivedAll){
//			 HashMap<String, T2Reference> inputValuesMap = new HashMap<String, T2Reference> ();
//				for (DataflowInputPort dataflowInputPort : dataflow.getInputPorts()) {
//					String name = dataflowInputPort.getName();
//					inputValuesMap.put(name, facade.getPushedDataMap().get(name));
//				}
//					//saveButton.setEnabled(true);
//					//saveButton.setFocusable(false);
//		 }
//	}
	
//	public void pushInputData(WorkflowDataToken token, String portName) {
//		WorkflowResultTreeModel model = inputPortModelMap.get(portName);
//		if (model != null) {
//			model.resultTokenProduced(token, portName);
//		}
//	}
	
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

