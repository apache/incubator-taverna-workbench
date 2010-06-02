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

import java.sql.Timestamp;
import java.text.ParseException;
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
import net.sf.taverna.t2.provenance.lineageservice.utils.DataflowInvocation;
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
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
	private static final String AVERAGE_ITERATION_TIME = "Average time per iteration (in ms)";
	private static final String ITERATIONS = "Total iterations";
	private static final String ITERATIONS_DONE= "Iterations done";
	private static final String ITERATIONS_FAILED = "Iterations with errors";

	private static final String STATUS_PENDING = "Pending";
	private static final String STATUS_FINISHED = "Finished";

	// Column names
	private String[] columnNames = new String[] {
			NAME, // name of the workflow or processor
			STATUS, // status of the workflow or processor
			START_TIME, // start time of workflow or processor
			FINISH_TIME, // finish time of workflow or processor
			AVERAGE_ITERATION_TIME, // average time per iteration (in ms)
			ITERATIONS, // total number of iterations
			ITERATIONS_DONE, // number of iteration done so far
			ITERATIONS_FAILED, // number of failed iterations
    };

    // Column types
    @SuppressWarnings("unchecked")
	static protected Class[] columnTypes = { TreeTableModel.class,
			String.class, String.class, String.class, String.class, String.class, String.class, String.class };
    
	// Table data (maps workflow element nodes to column data associated with them)
	private Map<DefaultMutableTreeNode, ArrayList<Object>> data = new HashMap<DefaultMutableTreeNode, ArrayList<Object>>();;
		
	private DefaultMutableTreeNode rootNode;
	
	private Dataflow dataflow;
	private ProvenanceConnector provenanceConnector;
	private Object referenceService;
	// For fetching data for past runs from provenance
	private String workflowRunId;
	private ProvenanceAccess provenanceAccess;
	
 	public WorkflowRunProgressTreeTableModel(Dataflow dataflow) {

		super(new DefaultMutableTreeNode(dataflow));	
		rootNode = (DefaultMutableTreeNode) this.getRoot();
		this.dataflow = dataflow;
		createTree(dataflow, rootNode);	
	}

 	// This constructor is to be used for previous wf run, where tree is
 	// populated form provenance.
    public WorkflowRunProgressTreeTableModel(Dataflow dataflow,
			ProvenanceConnector connector, ReferenceService refService, String workflowRunId) {
		super(new DefaultMutableTreeNode(dataflow));	
		this.dataflow = dataflow;
		this.provenanceConnector = connector;
		this.referenceService = refService;
		this.workflowRunId = workflowRunId;
		provenanceAccess = new ProvenanceAccess(DataManagementConfiguration.getInstance().getConnectorType());

		rootNode = (DefaultMutableTreeNode) this.getRoot();
		createTree(dataflow, rootNode);	
    }

	private void createTree(Dataflow df, DefaultMutableTreeNode root) {
    	   	
    	// If this is the root of the tree rather than a root of the nested sub-tree
    	if (root.equals(rootNode)){
			ArrayList<Object> workflowData = new ArrayList<Object>();

			workflowData.add(df.getLocalName()); // name

    		// If this is an old run - populate the tree from provenance
    		if (provenanceConnector != null && referenceService != null && provenanceAccess != null){    			
    			workflowData.add(STATUS_FINISHED); // status
    			
				SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
    			DataflowInvocation dataflowInvocation = provenanceAccess.getDataflowInvocation(workflowRunId);
				Timestamp workflowStartTime = dataflowInvocation.getInvocationStarted();
    			Timestamp workflowFinishTime = dataflowInvocation.getInvocationEnded();
    			
    			if (workflowStartTime != null) {
    				workflowData.add(sdf.format(workflowStartTime));
    			} else {
    				workflowData.add("-");
    			}
    			if (workflowFinishTime != null) {
    				workflowData.add(sdf.format(workflowFinishTime)); 
    			} else {
    				workflowData.add("-");
    			}
    			if (workflowStartTime != null &&  workflowFinishTime != null) {
    				workflowData.add(String.valueOf(workflowFinishTime.getTime() - workflowStartTime.getTime()) + " ms"); // average running time in ms
    			} else {
    				workflowData.add("-");
    			}

    			workflowData.add("-"); // no. of iterations
    			workflowData.add("-"); // no. of iterations done so far
    			workflowData.add("-"); // no. of failed iterations
			}
    		else{    			
				workflowData.add(STATUS_PENDING); // status
    			
				workflowData.add(null); // wf start time
				workflowData.add(null); // wf finish time
				workflowData.add(null); // average running time

    			workflowData.add("-"); // no. of iterations
    			workflowData.add("-"); // no. of iterations done so far
    			workflowData.add("-"); // no. of failed iterations
    		}
			data.put(root, workflowData);
    	}

    	// One row for each processor
		for (Processor processor : df.getProcessors()) {
			DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(processor);
			ArrayList<Object> processorData = new ArrayList<Object>();
			processorData.add(processor.getLocalName()); // name

    		// If this is an old run - populate the tree from provenance
    		if (provenanceConnector != null && referenceService != null && provenanceAccess != null){
    			
    			// Get the processors' path for this processor, including all parent nested processors
    			List<Processor> processorsPath = Tools.getNestedPathForProcessor(processor, dataflow);
    			// Create the array of nested processors' names
    			String[] processorNamesPath = null;
    			if (processorsPath != null){ // should not be null really
    				processorNamesPath = new String[processorsPath.size()];
    				int i = 0;
    				for(Processor proc : processorsPath){
    					processorNamesPath[i++] = proc.getLocalName();
    				}
    			}
    			else{ // This should not really happen!
    				processorNamesPath = new String[1];
    				processorNamesPath[0] = processor.getLocalName();
    			}

    			List<ProcessorEnactment> processorEnactments = provenanceAccess.getProcessorEnactments(workflowRunId, processorNamesPath);
    			processorData.add(STATUS_FINISHED); // status
    			
    			if (processorEnactments.isEmpty()){
    				processorData.add(null); // start time
    				processorData.add(null); // finish time
    				processorData.add(null); // average time per iteration
    				
        			processorData.add(processorEnactments.size()); // no. of iterations 
        			processorData.add(processorEnactments.size()); // no. of iterations done so far
        			processorData.add("Do not know"); // no. of failed iterations
    			}
    			else{
					Timestamp earliestStartTime = processorEnactments.get(0)
							.getEnactmentStarted();
					Timestamp latestFinishTime = processorEnactments.get(0)
							.getEnactmentEnded();
					long averageTime = 0;
					int errors = 0;
					int averageNumberOfProcessors = 0;

					for (ProcessorEnactment processorEnactment : processorEnactments) {
						// Get the earliest start time of all invocations
						Timestamp startTime = processorEnactment
								.getEnactmentStarted();
						if (startTime.before(earliestStartTime)) {
							earliestStartTime = startTime;
						}
						// Get the latest finish time of all invocations
						Timestamp finishTime = processorEnactment
								.getEnactmentEnded();
						if (finishTime != null) {
							if (finishTime.after(latestFinishTime)) {
								latestFinishTime = finishTime;
							}
							averageTime += (finishTime.getTime() - startTime
									.getTime());
							averageNumberOfProcessors++;
						}
						
						// Do any outputs of this iteration contain errors?
						String finalOutputs = processorEnactment.getFinalOutputsDataBindingId();
						if (finalOutputs != null) {
							Map<Port, T2Reference> dataBindings = provenanceAccess.getDataBindings(finalOutputs);
							for (java.util.Map.Entry<Port,T2Reference> entry : dataBindings.entrySet()) {
								if (entry.getKey().isInputPort()) {
									continue;
								}
								T2Reference t2Ref = entry.getValue();
								if (t2Ref.containsErrors()) {
									// only count output errors
									errors++;
									break; // we only care if there is at least one error so break the loop here
								}
							}
						}
					}
					// Get the average time of invocations (in ms)
					if (averageNumberOfProcessors > 0) {
						averageTime = averageTime / averageNumberOfProcessors;
					} else {
						averageTime = -1;
					}

					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					processorData.add(sdf.format(earliestStartTime)); // start time
					processorData.add(sdf.format(latestFinishTime)); // finish time
					if (averageTime > -1) {
						processorData.add(averageTime + " ms"); // average time per iteration
					} else {
						processorData.add("-");
					}
					
	    			processorData.add(processorEnactments.size()); // no. of iterations 
	    			processorData.add(processorEnactments.size()); // no. of iterations done so far
	    			processorData.add(errors); // no. of failed iterations
    			}
    		}
    		else{ 
    			processorData.add(STATUS_PENDING); // status
    			processorData.add(null); // start time
    			processorData.add(null); // finish time
    			processorData.add(null); // average time per iteration
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
	
	public void setAverageInvocationTimeForObject(Object object, long timeInMiliseconds) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		setValueAt(String.valueOf(timeInMiliseconds) + " ms", node, 4);
	}
	
	public void setNumberOfIterationsForObject(Object object, Integer iterations) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		setValueAt(iterations, node, 5);
	}
	
	public void setNumberOfIterationsDoneSoFarForObject(Object object, Integer doneIterations) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		setValueAt(doneIterations, node, 6);
	}
	
	public void setNumberOfFailedIterationsForObject(Object object, Integer failedIterations) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		setValueAt(failedIterations, node, 7);
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

	public Date getStartDateForObject(Object object) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		String dateString = (String)getValueAt(node, 2);
		try {
			Date date = sdf.parse(dateString);
			return date;
		} catch (ParseException e) {
			return null;
		}
	}

	public Integer getIterationsNumberForObject(Object object) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		String iterationsNumber = (String)getValueAt(node, 4);
		try{
			return Integer.valueOf(iterationsNumber);
		}
		catch(Exception ex){
			return null;
		}
	}

}