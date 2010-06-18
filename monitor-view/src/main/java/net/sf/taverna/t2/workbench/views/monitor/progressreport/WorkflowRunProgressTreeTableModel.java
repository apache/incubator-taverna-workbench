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

import static net.sf.taverna.t2.workbench.views.results.processor.ProcessorResultsComponent.formatMilliseconds;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import net.sf.taverna.t2.workflowmodel.utils.NamedWorkflowEntityComparator;
import net.sf.taverna.t2.workflowmodel.utils.Tools;


/**
 * A TreeTableModel used to display the progress of a workfow run. 
 * Workflow and its processors (some of which may be nested) 
 * are represented as a tree, where their properties, such 
 * as status, start and finish times, number of iterations, etc. 
 * are represented as table columns.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 */
public class WorkflowRunProgressTreeTableModel extends AbstractTreeTableModel{

	public static final String NAME = "Name";
	public static final String STATUS = "Status";	
	public static final String AVERAGE_ITERATION_TIME = "Average time per iteration";
	public static final String ITERATIONS = "Queued iterations";
	public static final String ITERATIONS_DONE= "Completed iterations";
	public static final String ITERATIONS_FAILED = "Iterations with errors";

	public static final String STATUS_PENDING = "Pending";
	public static final String STATUS_FINISHED = "Finished";
	public static final String STATUS_CANCELLED = "Cancelled";
	public static final String STATUS_PAUSED = "Paused";
	public static final String STATUS_RUNNING = "Running";

	public enum Column { 
		
		NAME("Name", TreeTableModel.class), 
		STATUS("Status"), 
		ITERATIONS_QUEUED("Queued iterations"), 
		ITERATIONS_DONE("Iterations done"), 
		ITERATIONS_FAILED("Iterations w/errors"),
		AVERAGE_ITERATION_TIME("Average time/iteration"), 
		START_TIME("First iteration started", Date.class), 
		FINISH_TIME("Last iteration ended", Date.class);
		
		private final String label;
		private final Class<?> columnClass;

		Column(String label) {
			this(label, String.class);
		}
		
		Column(String label, Class<?> columnClass) {
			this.label = label;
			this.columnClass = columnClass;
		}
		
		public Class<?> getColumnClass() {
			return columnClass;
		}
		public String getLabel() {
			return label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
    
	// Table data (maps workflow element nodes to column data associated with them)
	private Map<DefaultMutableTreeNode, List<Object>> data = new HashMap<DefaultMutableTreeNode, List<Object>>();;
	private DefaultMutableTreeNode rootNode;
	
	private Dataflow dataflow;
	private ProvenanceConnector provenanceConnector;
	private Object referenceService;
	// For fetching data for past runs from provenance
	private String workflowRunId;
	private ProvenanceAccess provenanceAccess;
	
    private Set<Processor> processors;

 	public WorkflowRunProgressTreeTableModel(Dataflow dataflow) {

		super(new DefaultMutableTreeNode(dataflow));	
		rootNode = (DefaultMutableTreeNode) this.getRoot();
		this.dataflow = dataflow;
		processors = new HashSet<Processor>();
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
	private final NamedWorkflowEntityComparator namedWorkflowEntitiyComparator = new NamedWorkflowEntityComparator();

	private void createTree(Dataflow df, DefaultMutableTreeNode root) {
    	   	
    	// If this is the root of the tree rather than a root of the nested sub-tree
    	if (root.equals(rootNode)){
			List<Object> workflowData = new ArrayList<Object>(Collections.nCopies(Column.values().length, null));

			workflowData.set(Column.NAME.ordinal(), df.getLocalName()); // name

    		// If this is an old run - populate the tree from provenance
    		if (provenanceConnector != null && referenceService != null && provenanceAccess != null){    			
    			workflowData.set(Column.STATUS.ordinal(), STATUS_FINISHED); // status
    			
    			workflowData.set(Column.ITERATIONS_DONE.ordinal(), "-"); 
    			workflowData.set(Column.ITERATIONS_FAILED.ordinal(), "-");
    			workflowData.set(Column.ITERATIONS_QUEUED.ordinal(), "-");
    			
				
    			DataflowInvocation dataflowInvocation = provenanceAccess.getDataflowInvocation(workflowRunId);
    			Timestamp workflowStartTime = null;
    			Timestamp workflowFinishTime = null;
    			if (dataflowInvocation != null) {
					workflowStartTime = dataflowInvocation.getInvocationStarted();
					workflowFinishTime = dataflowInvocation.getInvocationEnded();    			
    			}
    			
    			if (workflowStartTime != null &&  workflowFinishTime != null) {
    				workflowData.set(Column.AVERAGE_ITERATION_TIME.ordinal(), formatMilliseconds(workflowFinishTime.getTime() - workflowStartTime.getTime())); // average running time in ms
    			} else {
    				workflowData.set(Column.AVERAGE_ITERATION_TIME.ordinal(),"-");
    			}    			
    			workflowData.set(Column.START_TIME.ordinal(),workflowStartTime);
    			workflowData.set(Column.FINISH_TIME.ordinal(),workflowFinishTime);

			}
    		else{    			
    			workflowData.set(Column.STATUS.ordinal(), STATUS_PENDING); // status
    			
    			workflowData.set(Column.ITERATIONS_DONE.ordinal(), "-"); 
    			workflowData.set(Column.ITERATIONS_FAILED.ordinal(), "-");
    			workflowData.set(Column.ITERATIONS_QUEUED.ordinal(), "-");
    			

    			workflowData.set(Column.AVERAGE_ITERATION_TIME.ordinal(), null); // average running time
    			workflowData.set(Column.START_TIME.ordinal(), null); // wf start time
    			workflowData.set(Column.FINISH_TIME.ordinal(), null); // wf finish time

    		}
			data.put(root, workflowData);
    	}

    	// One row for each processor
		List<Processor> processorsList = new ArrayList<Processor>(df.getProcessors());
		Collections.sort(processorsList, namedWorkflowEntitiyComparator);
		for (Processor processor : processorsList){
		    processors.add(processor);
			DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(processor);
			List<Object> processorData = new ArrayList<Object>(Collections.nCopies(Column.values().length, null));
			processorData.set(Column.NAME.ordinal(), processor.getLocalName()); // name

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
    			processorData.set(Column.STATUS.ordinal(), STATUS_FINISHED); // status
    			
    			if (processorEnactments.isEmpty()){
    				
    				processorData.set(Column.ITERATIONS_QUEUED.ordinal(), 0); // no. of queued iterations 
    				processorData.set(Column.ITERATIONS_DONE.ordinal(), processorEnactments.size()); // no. of iterations done so far
    				processorData.set(Column.ITERATIONS_FAILED.ordinal(), "Unknown"); // no. of failed iterations
        			
    				processorData.set(Column.START_TIME.ordinal(), null); // start time
    				processorData.set(Column.FINISH_TIME.ordinal(), null); // finish time
    				processorData.set(Column.AVERAGE_ITERATION_TIME.ordinal(), null); // average time per iteration
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


					processorData.set(Column.ITERATIONS_QUEUED.ordinal(), 0); // no. of queued iterations 
					processorData.set(Column.ITERATIONS_DONE.ordinal(), processorEnactments.size()); // no. of iterations done so far
					processorData.set(Column.ITERATIONS_FAILED.ordinal(), errors); // no. of failed iterations
					
					
					processorData.set(Column.START_TIME.ordinal(), earliestStartTime); // start time
					processorData.set(Column.FINISH_TIME.ordinal(), latestFinishTime); // finish time
					if (averageTime > -1) {
						processorData.set(Column.AVERAGE_ITERATION_TIME.ordinal(), formatMilliseconds(averageTime)); // average time per iteration
					} else {
						processorData.set(Column.AVERAGE_ITERATION_TIME.ordinal(), "-");
					}
    			}
    		}
    		else{ 
    			processorData.set(Column.STATUS.ordinal(),STATUS_PENDING); // status
    			processorData.set(Column.ITERATIONS_QUEUED.ordinal(),"0"); // no. of queued iterations
    			processorData.set(Column.ITERATIONS_DONE.ordinal(),"0"); // no. of iterations done so far
    			processorData.set(Column.ITERATIONS_FAILED.ordinal(),"0"); // no. of failed iterations
    			processorData.set(Column.START_TIME.ordinal(),null); // start time
    			processorData.set(Column.FINISH_TIME.ordinal(),null); // finish time
    			processorData.set(Column.AVERAGE_ITERATION_TIME.ordinal(),null); // average time per iteration
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
		return Column.values().length;
	}

	public String getColumnName(int column) {
		return Column.values()[column].getLabel();
	}

	public Class<?> getColumnClass(int column) {
		return Column.values()[column].getColumnClass();
	}

	public Object getValueAt(Object node, Column column) {			
		return getValueAt(node, column.ordinal());
	}
	
	public Object getValueAt(Object node, int column) {			
		if (data!= null && data.get(node) != null){
			return data.get(node).get(column);
		} 
		else {
			return null;
		}
	}
	
	@Override
	public void setValueAt(Object aValue, Object node, int column) {
		data.get(node).set(column, aValue);
		//		this.fireTreeNodesChanged(node, ((DefaultMutableTreeNode)node).getPath(), null, null);
	}

    public void refresh() {
	this.fireTreeNodesChanged(getRoot(), ((DefaultMutableTreeNode)(getRoot())).getPath(), null, null);
    }

	public void setValueAt(Object aValue, Object node, Column column) {
		setValueAt(aValue, node, column.ordinal());
	}

	public void setProcessorStatus(Processor processor, String status) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);
		setValueAt(status, node, Column.STATUS);		
	}
	
	public void setProcessorStartDate(Processor processor, Date date) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);
		setValueAt(date, node, Column.START_TIME);
	}
	
	public void setProcessorFinishDate(Processor processor, Date date) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);
		setValueAt(date, node, Column.FINISH_TIME);
	}
	
	public void setProcessorAverageInvocationTime(Object object, long timeInMiliseconds) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(object);
		setValueAt(formatMilliseconds(timeInMiliseconds), node, Column.AVERAGE_ITERATION_TIME);
	}
	
	public void setProcessorNumberOfQueuedIterations(Processor processor, Integer iterations) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);
		setValueAt(iterations.toString(), node, Column.ITERATIONS_QUEUED);
	}
	
	public void setProcessorNumberOfIterationsDoneSoFar(Processor processor, Integer doneIterations) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);
		setValueAt(doneIterations.toString(), node, Column.ITERATIONS_DONE);
	}
	
	public void setProcessorNumberOfFailedIterations(Processor processor, Integer failedIterations) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);
		setValueAt(failedIterations.toString(), node, Column.ITERATIONS_FAILED);
	}
	
	// Get tree node containing the passed object. Root node contains the
	// workflow object and children contain processor nodes.
	public DefaultMutableTreeNode getNodeForObject(Object object){
		
		if (object == dataflow){ // no need to loop over processors
			return (DefaultMutableTreeNode)root;
		}
		
		for (DefaultMutableTreeNode node : data.keySet()) {
			 if (node.getUserObject().equals(object)){
				 return node;
			 }
		 }
		 return null;
	}

    public Set<Processor> getProcessors() {
	return Collections.unmodifiableSet(processors);
    }

	public Date getProcessorStartDate(Processor processor) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);	
		return (Date) getValueAt(node, Column.START_TIME);		
	}

	public String getProcessorStatus(Processor processor) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);
		return (String)getValueAt(node, Column.STATUS);
	}

	public int getProcessorNumberOfIterationsDoneSoFar(Processor processor) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);
		
		String iterationsString = (String)getValueAt(node, Column.ITERATIONS_DONE);
		return Integer.valueOf(iterationsString).intValue();
	}
	
	public int getProcessorNumberOfQueuedIterations(Processor processor) {
		// First get the node for object
		DefaultMutableTreeNode node = getNodeForObject(processor);
		
		String iterationsString = (String)getValueAt(node, Column.ITERATIONS_QUEUED);
		return Integer.valueOf(iterationsString).intValue();
	}
	
	public Dataflow getDataflow(){
		return dataflow;
	}
}