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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade.State;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.MonitorManager.AddPropertiesMessage;
import net.sf.taverna.t2.monitor.MonitorManager.DeregisterNodeMessage;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.monitor.MonitorManager.RegisterNodeMessage;
import net.sf.taverna.t2.workbench.views.monitor.graph.GraphMonitor;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.log4j.Logger;

/**
 * An implementation of the Monitor interface that updates the TreeTable when
 * MonitorableProperties change.
 * 
 */
public class WorkflowRunProgressMonitor implements Observer<MonitorMessage> {

	private static final String STATUS_FINISHED = "Finished";
	private static final String STATUS_CANCELLED = "Cancelled";

	// Workflow run status label - we can only tell of workflow is running
	// or is finished from inside this monitor. If workfow run is stopped or
	// paused - this will be updated form the run-ui.
//	private JLabel workflowRunStatusLabel;
	
	private static Logger logger = Logger.getLogger(WorkflowRunProgressMonitor.class);
	
	private static long deregisterDelay = 10000;

	private static long monitorRate = 1000;

	private final WorkflowRunProgressTreeTable progressTreeTable;

	// Map of owning process ids for processors to their corresponding MonitorNodes
	// (we only create MonitorNodes for processors)
	private Map<String, WorkflowRunProgressMonitorNode> processorMonitorNodes = new HashMap<String, WorkflowRunProgressMonitorNode>();

	// Map of owning process ids to workflow objects (including the processor,
	// dataflow and facade objects)
	private Map<String, Object> workflowObjects = Collections.synchronizedMap(new HashMap<String, Object>());

	// Map from invocation process ID to start time
	private static Map<String, Date> activitityInvocationStartTimes = Collections.synchronizedMap(new HashMap<String, Date>());
	
	private static Map<Processor, List<Long>> processorInvocationTimes = Collections.synchronizedMap(new WeakHashMap<Processor, List<Long>>());
	
	//private Map<String, ResultListener> resultListeners = new HashMap<String, ResultListener>();

	private Timer updateTimer = new Timer("Progress table monitor update timer", true);

	private UpdateTask updateTask;

	// Filter only events for the workflow shown in the progressTreeTable
	private String filter;
	
	private WorkflowInstanceFacade facade;

	public WorkflowRunProgressMonitor(WorkflowRunProgressTreeTable progressTreeTable, WorkflowInstanceFacade facade) {
		this.progressTreeTable = progressTreeTable;
		this.facade = facade;
	}

	public void onDispose() {
	    if (updateTimer != null) {
		updateTimer.cancel();
	    }
	    if (updateTask != null) {
		updateTask.run();
	    }
	}

	@Override
	protected void finalize() throws Throwable {
		onDispose();
	}

	public void notify(Observable<MonitorMessage> sender, MonitorMessage message)
			throws Exception {
		if (message instanceof RegisterNodeMessage) {
			RegisterNodeMessage regMessage = (RegisterNodeMessage) message;
			registerNode(regMessage.getWorkflowObject(), regMessage
					.getOwningProcess(), regMessage.getProperties());
		} else if (message instanceof DeregisterNodeMessage) {
			deregisterNode(message.getOwningProcess());
		} else if (message instanceof AddPropertiesMessage) {
			AddPropertiesMessage addMessage = (AddPropertiesMessage) message;
			addPropertiesToNode(addMessage.getOwningProcess(), addMessage
					.getNewProperties());
		} else {
			logger.warn("Unknown message " + message + " from " + sender);
		}		
	}

	private void registerNode(Object workflowObject, String[] owningProcess,
			Set<MonitorableProperty<?>> properties) {
				
		if (filter == null && owningProcess.length == 1) {
			filter = owningProcess[0];
		}
		// Is this event is for the workflow we are monitoring?
		// (exclude events for other workflows)
		if (owningProcess[0].equals(filter)) {
			String owningProcessId = getOwningProcessId(owningProcess);
			workflowObjects.put(owningProcessId, workflowObject);
			if (workflowObject instanceof Processor) {
				Processor processor = (Processor) workflowObject;
				if (! processorInvocationTimes.containsKey(processor)) {
					processorInvocationTimes.put(processor, new ArrayList<Long>());
				}
				WorkflowRunProgressMonitorNode parentMonitorNode = findParentMonitorNode(owningProcess);
				
				WorkflowRunProgressMonitorNode monitorNode = new WorkflowRunProgressMonitorNode(
						processor, owningProcess, properties, progressTreeTable, facade, parentMonitorNode);
				synchronized(processorMonitorNodes) {
					processorMonitorNodes.put(GraphMonitor.getProcessorId(owningProcess), monitorNode);
				}
			} else if (workflowObject instanceof Dataflow) {

				// outermost dataflow 
				if (owningProcess.length == 2) {
					synchronized (this) {
						if (updateTask != null) {
							// updateTask.cancel();
						}
						updateTask = new UpdateTask();
						try{
							updateTimer.schedule(updateTask, monitorRate,
									monitorRate);
						}
						catch(IllegalStateException ex){ // task seems already cancelled
							// Do nothing
						}
					}
				}
			}
			// This is the beginning of the actual workflow run - the facade object is received
			else if (workflowObject instanceof WorkflowInstanceFacade) {

//				WorkflowInstanceFacade facade = (WorkflowInstanceFacade) workflowObject;
//				ResultListener resultListener = new MonitorResultListener(
//						getProcessorId(owningProcess));
//				facade.addResultListener(resultListener);
//				resultListeners.put(owningProcessId, resultListener);
				
//				if (workflowRunStatusLabel != null){
//					workflowRunStatusLabel.setText(STATUS_RUNNING);
//					workflowRunStatusLabel.setIcon(WorkbenchIcons.workingIcon);
//				}
			} else if (workflowObject instanceof Activity<?>) {				
				activitityInvocationStartTimes.put(owningProcessId, new Date());
			}
		}
	}
	
	private WorkflowRunProgressMonitorNode findParentMonitorNode(
			String[] owningProcess) {
		List<String> parentOwningProcess = Arrays.asList(owningProcess);
		while (!parentOwningProcess.isEmpty()) {
			// Remove last element
			parentOwningProcess = parentOwningProcess.subList(0, parentOwningProcess.size()-1);
			String parentId = GraphMonitor.getProcessorId(parentOwningProcess.toArray(new String[parentOwningProcess.size()]));
			synchronized (processorMonitorNodes) {
				WorkflowRunProgressMonitorNode parentNode = processorMonitorNodes
						.get(parentId);
				if (parentNode != null) {
					return parentNode;
				}
			}
		}
		return null;
	}

	public void deregisterNode(String[] owningProcess) {
		if (owningProcess[0].equals(filter)) {
			final String owningProcessId = getOwningProcessId(owningProcess);
			Object workflowObject = workflowObjects.remove(owningProcessId);
			if (workflowObject instanceof Processor) {
				WorkflowRunProgressMonitorNode workflowRunProgressMonitorNode;
				synchronized(processorMonitorNodes) {
					workflowRunProgressMonitorNode = processorMonitorNodes.get(GraphMonitor.getProcessorId(owningProcess));
				}
				workflowRunProgressMonitorNode.update();
				Date processorFinishDate = new Date();
				Date processorStartTime = progressTreeTable.getProcessorStartDate(((Processor) workflowObject));
				
				// For some reason total number of iterations is messed up when we update it
				// from inside the node, so the final number is set here.
				// If total number of iterations is 0 that means there was just one invocation.
				int total = workflowRunProgressMonitorNode.getTotalNumberOfIterations();
				total = (total == 0) ? 1 : total;
				progressTreeTable.setProcessorFinishDate(((Processor) workflowObject), processorFinishDate);
				progressTreeTable.setProcessorStatus(((Processor) workflowObject), STATUS_FINISHED);
			} else if (workflowObject instanceof Dataflow) {
				if (owningProcess.length == 2) {
					// outermost dataflow finished so schedule a task to cancel
					// the update task
					synchronized (this) {
						if (updateTask != null) {
							try{
								updateTimer.schedule(new TimerTask() {
									public void run() {
										updateTask.cancel();
										updateTask = null;
									}
								}, deregisterDelay);
							}
							catch(IllegalStateException ex){ // task seems already cancelled
								// Do nothing
							}
						}
					}
				}
			} else if (workflowObject instanceof WorkflowInstanceFacade) {
				//final WorkflowInstanceFacade facade = (WorkflowInstanceFacade) workflowObject;
				
				WorkflowInstanceFacade instanceFacade = (WorkflowInstanceFacade) workflowObject;
				// Is this the workflow facade for the outer most workflow?
				// (If it is the facade for one of the contained nested workflows then the
				// workflow status should not be set to FINISHED after the nested one has finished
				// as the main workflow may still be running)
				if (owningProcess.length == 1){
					
					Date workflowFinishDate = new Date();
					Date workflowStartTime = progressTreeTable.getWorkflowStartDate();
					progressTreeTable.setWorkflowFinishDate(workflowFinishDate);
					boolean isCancelled = instanceFacade.getState().equals(State.cancelled); 						
					progressTreeTable.setWorkflowStatus(isCancelled ? STATUS_CANCELLED : STATUS_FINISHED);
					if (workflowStartTime != null){
						long averageInvocationTime = (workflowFinishDate.getTime() - workflowStartTime.getTime());
						progressTreeTable.setWorkflowInvocationTime(averageInvocationTime);
					}
					
//					if (workflowRunStatusLabel != null){
//						workflowRunStatusLabel.setText(STATUS_FINISHED);
//						workflowRunStatusLabel.setIcon(WorkbenchIcons.tickIcon);
//					}
					
					// Stop observing monitor messages as workflow has finished running
					// This observer may have been already removed (in which case the command 
					// will have no effect) but in the case the workflow has no outputs
					// we have to do the removing here.
					MonitorManager.getInstance().removeObserver(this);
				}				
			} else if (workflowObject instanceof Activity<?>) {
				Date endTime = new Date();
				Date startTime = activitityInvocationStartTimes.remove(owningProcessId);
				ArrayList<String> owningProcessList = new ArrayList<String>(Arrays.asList(owningProcess));
				owningProcessList.remove(owningProcess.length-1);
				String parentProcessId = getOwningProcessId(owningProcessList);										
				Object parentObject = workflowObjects.get(parentProcessId);
				if (parentObject instanceof Processor) {
					try {
						Processor processor = (Processor) parentObject;
						if (startTime != null) {
							long invocationTime = endTime.getTime()
									- startTime.getTime();
							List<Long> invocationTimes = processorInvocationTimes
									.get(processor);
							invocationTimes.add(invocationTime);
							long totalTime = 0;
							for (Long time : invocationTimes) {
								totalTime += time;
							}
							if (!invocationTimes.isEmpty()) {
								long averageInvocationTime = totalTime
										/ invocationTimes.size();
								progressTreeTable
										.setProcessorAverageInvocationTime(
												processor,
												averageInvocationTime);
							}
						}
				} catch (ConcurrentModificationException e) {
						logger.error("Concurrency problem calculating times", e);
					}
				}										
			}
		}
	}

	public void addPropertiesToNode(String[] owningProcess,
			Set<MonitorableProperty<?>> newProperties) {
		if (owningProcess[0].equals(filter)) {
			WorkflowRunProgressMonitorNode monitorNode;
			synchronized(processorMonitorNodes) {
				monitorNode = processorMonitorNodes
						.get(GraphMonitor.getProcessorId(owningProcess));
			}
			new Exception().printStackTrace();
			if (monitorNode != null) {
				for (MonitorableProperty<?> property : newProperties) {
					monitorNode.addMonitorableProperty(property);
				}
			}
		}
	}

	private static String getOwningProcessId(String[] owningProcess) {
		return getOwningProcessId(Arrays.asList(owningProcess));
	}
	/**
	 * Converts the owning process array to a string.
	 * 
	 * @param owningProcess
	 *            the owning process id
	 * @return the owning process as a string
	 */
	private static String getOwningProcessId(List<String> owningProcess) {
		StringBuffer sb = new StringBuffer();
		Iterator<String> iterator = owningProcess.iterator();
		while (iterator.hasNext()) {
			String string = iterator.next();			
			sb.append(string);
			if (iterator.hasNext()) {
				sb.append(":");
			}
		}
		return sb.toString();
	}
	
	public class UpdateTask extends TimerTask {
		public void run() {
			try { 
				List<WorkflowRunProgressMonitorNode> nodes;
				synchronized(processorMonitorNodes) {
					nodes = new ArrayList<WorkflowRunProgressMonitorNode>(processorMonitorNodes.values());
				}
				for (WorkflowRunProgressMonitorNode node : nodes) {
					node.update();
				}
				progressTreeTable.refreshTable();
			} catch (RuntimeException ex) {
				logger.error("UpdateTask update failed", ex);
			}
		}
	}
	
}

