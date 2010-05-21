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
package net.sf.taverna.t2.workbench.views.monitor.progressreport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.lang.ui.treetable.AbstractTreeTableModel;
import net.sf.taverna.t2.lang.ui.treetable.TreeTableModel;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * A TreeTableModel used to display the progress of a workfow run. 
 * Workflow and its processors (some of which may be nested) 
 * are represented as a tree, where their properties, such 
 * as status, start and finish times, number of iterations, etc. 
 * are represented as table columns.
 * 
 * @author Alex Nenadic
 */
public class WorkflowRunProgressTreeTableModel extends AbstractTreeTableModel{

	private static final String NAME = "Name";
	private static final String STATUS = "Status";	
	private static final String START_TIME = "Start time";
	private static final String FINISH_TIME = "Finish time";
	private static final String ITERATIONS = "Total iterations";
	private static final String ITERATIONS_DONE= "Done iterations";
	private static final String ITERATIONS_FAILED = "Failed iterations";

	private static final String STATUS_PENDING = "Pending";
	private static final String STATUS_FINISHED = "Finished";

	// Column names
	private String[] columnNames = new String[] {
			NAME, // name of the workflow or processor
			STATUS, // status of the workflow or processor
			START_TIME, // start time of workflow or processor
			FINISH_TIME, // finish time of workflow or processor
			ITERATIONS, // total number of iterations
			ITERATIONS_DONE, // number of iteration done so far
			ITERATIONS_FAILED, // number of failed iterations
    };

    // Column types
    @SuppressWarnings("unchecked")
	static protected Class[] columnTypes = { TreeTableModel.class,
			String.class, String.class, String.class, String.class, String.class, String.class };
    
	// Table data (maps workflow element nodes to column data associated with them)
	private Map<DefaultMutableTreeNode, ArrayList<Object>> data = new HashMap<DefaultMutableTreeNode, ArrayList<Object>>();;
		
	private DefaultMutableTreeNode rootNode;
	
	private ProvenanceConnector provenanceConnector;
	private Object referenceService;
	// For fetching data for past runs from provenance
	private String workflowRunId;
	
 	public WorkflowRunProgressTreeTableModel(Dataflow dataflow) {

		super(new DefaultMutableTreeNode(dataflow));	
		rootNode = (DefaultMutableTreeNode) this.getRoot();
		createTree(dataflow, rootNode);	
	}

 	// This constructor is to be used for previous wf run, where tree is
 	// populated form provenance.
    public WorkflowRunProgressTreeTableModel(Dataflow dataflow,
			ProvenanceConnector connector, ReferenceService refService, String workflowRunId) {
		super(new DefaultMutableTreeNode(dataflow));	
		this.provenanceConnector = connector;
		this.referenceService = refService;
		this.workflowRunId = workflowRunId;
		rootNode = (DefaultMutableTreeNode) this.getRoot();
		createTree(dataflow, rootNode);	
    }

	private void createTree(Dataflow df, DefaultMutableTreeNode root) {
    	   	
    	// If this is the root of the tree rather than a root of the nested sub-tree
    	if (root.equals(rootNode)){
			ArrayList<Object> workflowData = new ArrayList<Object>();
			workflowData.add(df.getLocalName()); // name
			workflowData.add(STATUS_FINISHED); // status
			workflowData.add(null); // start time
			workflowData.add(null); // finish time
			workflowData.add("-"); // no. of iterations
			workflowData.add("-"); // no. of iterations done so far
			workflowData.add("-"); // no. of failed iterations
			
    		// If this is an old run - populate the tree from provenance
    		if (provenanceConnector != null && referenceService != null){
    			ProvenanceAccess provenanceAccess = new ProvenanceAccess(DataManagementConfiguration.getInstance().getConnectorType());
				workflowData.add(STATUS_FINISHED); // status
			}
    		else{
				workflowData.add(STATUS_PENDING); // status
    		}
			data.put(root, workflowData);
    	}

    	// One row for each processor
		for (Processor processor : df.getProcessors()) {
			DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(processor);
			ArrayList<Object> processorData = new ArrayList<Object>();
			processorData.add(processor.getLocalName()); // name

    		// If this is an old run - populate the tree from provenance
    		if (provenanceConnector != null && referenceService != null){
    			ProvenanceAccess provenanceAccess = new ProvenanceAccess(DataManagementConfiguration.getInstance().getConnectorType());
    			List<ProcessorEnactment> processorEnactments = provenanceAccess.getProcessorEnactments(workflowRunId, processor.getLocalName());
    			processorData.add(STATUS_FINISHED); // status
    			processorData.add(null); // start time
    			processorData.add(null); // finish time
    			processorData.add(processorEnactments.size() == 0 ? 1 : processorEnactments.size()); // no. of iterations (if size is 0 - that means only 1 invocation)
    			processorData.add(processorEnactments.size() == 0 ? 1 : processorEnactments.size()); // no. of iterations done so far
    			processorData.add("Do not know yet"); // no. of failed iterations
    		}
    		else{ 
    			processorData.add(STATUS_PENDING); // status
    			processorData.add(null); // start time
    			processorData.add(null); // finish time
    			processorData.add("Calculating..."); // no. of iterations
    			processorData.add("0"); // no. of iterations done so far
    			processorData.add("0"); // no. of failed iterations
    		}

   			data.put(processorNode, processorData);
			root.add(processorNode);
			
			if (Tools.containsNestedWorkflow(processor)){ // nested workflow
				Dataflow nestedWorkflow = ((NestedDataflow)processor.getActivityList().get(0)).getNestedDataflow();	
				// create sub-tree
				createTree(nestedWorkflow, processorNode);
			}
		}
	}

    protected Object[] getChildren(Object node) {
    	
    	DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)node;
    	Object[] children = new Object[treeNode.getChildCount()];
		for (int i = 0; i < children.length; i++) {
			children[i] = treeNode.getChildAt(i);
		}
		return children; 
    }

    //
    // The TreeModel interface
    //

	public int getChildCount(Object node) {
		Object[] children = getChildren(node);
		return (children == null) ? 0 : children.length;
	}

	public Object getChild(Object node, int i) {
		return getChildren(node)[i];
	}

    //
    //  The TreeTableNode interface. 
	//

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public Class getColumnClass(int column) {
		return columnTypes[column];
	}

	public Object getValueAt(Object node, int column) {	
		
		if (data!= null && data.get(node) != null){
			return data.get(node).get(column);
		} 
		else {
			return null;
		}
	}

	public void setValueAt(Object aValue, Object node, int column) {
		data.get(node).set(column, aValue);
		this.fireTreeNodesChanged(node, ((DefaultMutableTreeNode)node).getPath(), null, null);
	}

	public void setStatusForObject(Object object, String status) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		setValueAt(status, node, 1);		
	}
	
	public void setStartDateForObject(Object object, Date date) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		setValueAt(sdf.format(date), node, 2);
	}
	
	public void setFinishDateForObject(Object object, Date date) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		SimpleDateFormat sdf = new SimpleDateFormat(
		"yyyy-MM-dd HH:mm:ss");
		setValueAt(sdf.format(date), node, 3);
	}
	
	public void setNumberOfIterationsForObject(Object object, Integer iterations) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		setValueAt(iterations, node, 4);
	}
	
	public void setNumberOfFailedIterationsForObject(Object object, Integer failedIterations) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		setValueAt(failedIterations, node, 6);
	}
	
	public void setNumberOfIterationsDoneSoFarForObject(Object object, Integer doneIterations) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		setValueAt(doneIterations, node, 5);
	}
	
	public DefaultMutableTreeNode getNodeForObject(Object object){
		 Iterator<?> iter = data.keySet().iterator();
		 while (iter.hasNext()) {
			 DefaultMutableTreeNode node = (DefaultMutableTreeNode)(iter.next());
			 if (node.getUserObject().equals(object)){
				 return node;
			 }
		 }
		 return null;
	}

}



