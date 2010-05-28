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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.MonitorManager.AddPropertiesMessage;
import net.sf.taverna.t2.monitor.MonitorManager.DeregisterNodeMessage;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.monitor.MonitorManager.RegisterNodeMessage;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

/**
 * An implementation of the Monitor interface that updates the TreeTable when
 * MonitorableProperties change.
 * 
 */
public class WorkflowRunProgressMonitor implements Observer<MonitorMessage> {

//	private static final String STATUS_RUNNING = "Running";
	private static final String STATUS_FINISHED = "Finished";

	// Workflow run status label - we can only tell of workflow is running
	// or is finished from inside this monitor. If workfow run is stopped or
	// paused - this will be updated form the run-ui.
//	private JLabel workflowRunStatusLabel;
	
	private static Logger logger = Logger.getLogger(WorkflowRunProgressMonitor.class);
	
	private static long deregisterDelay = 1000;

	private static long monitorRate = 300;

	private final WorkflowRunProgressTreeTable progressTreeTable;

	// Map of owning process ids for processors to their corresponding MonitorNodes
	// (we only create MonitorNodes for processors)
	private Map<String, WorkflowRunProgressMonitorNode> processorMonitorNodes = new HashMap<String, WorkflowRunProgressMonitorNode>();

	// Map of owning process ids to workflow objects (including the processor,
	// dataflow and facade objects)
	private Map<String, Object> workflowObjects = new HashMap<String, Object>();

	//private Map<String, ResultListener> resultListeners = new HashMap<String, ResultListener>();

	private Timer updateTimer = new Timer("Progress table monitor update timer", true);

	private UpdateTask updateTask;

	// Filter only events for the workflow shown in the progressTreeTable
	private String filter;

	public WorkflowRunProgressMonitor(WorkflowRunProgressTreeTable progressTreeTable) {
		this.progressTreeTable = progressTreeTable;
	}

	public void onDispose() {
		try{
			updateTimer.cancel();
		}
		catch(IllegalStateException ex){ // task seems already cancelled
			logger.warn("Cannot cancel task: " + updateTimer.toString() + ".Task already seems cancelled", ex);
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
				WorkflowRunProgressMonitorNode monitorNode = new WorkflowRunProgressMonitorNode(
						processor, owningProcess, properties, progressTreeTable);
				synchronized(processorMonitorNodes) {
					processorMonitorNodes.put(owningProcessId, monitorNode);
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
			}
		}
	}
	
	public void deregisterNode(String[] owningProcess) {
		if (owningProcess[0].equals(filter)) {
			final String owningProcessId = getOwningProcessId(owningProcess);
			Object workflowObject = workflowObjects.remove(owningProcessId);
			if (workflowObject instanceof Processor) {
				WorkflowRunProgressMonitorNode workflowRunProgressMonitorNode;
				synchronized(processorMonitorNodes) {
					workflowRunProgressMonitorNode = processorMonitorNodes.get(owningProcessId);
				}
				workflowRunProgressMonitorNode.update();
				Date processorFinishDate = new Date();
				Date processorStartTime = progressTreeTable.getStartDateForObject(((Processor) workflowObject));
				
				// For some reason total number of iterations is messed up when we update it
				// from inside the node, so the final number is set here.
				// If total number of iterations is 0 that means there was just one invocation.
				int total = workflowRunProgressMonitorNode.getTotalNumberOfIterations();
				total = (total == 0) ? 1 : total;
				progressTreeTable.setNumberOfIterationsForObject(
						((Processor) workflowObject), total);
				
				progressTreeTable.setFinishDateForObject(((Processor) workflowObject), processorFinishDate);
				progressTreeTable.setStatusForObject(((Processor) workflowObject), STATUS_FINISHED);
				if (processorStartTime != null){
					long averageInvocationTime = (processorFinishDate.getTime() - processorStartTime.getTime())/total;
					progressTreeTable.setAverageInvocationTimeForObject(((Processor) workflowObject), averageInvocationTime);
				}
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
				
				// Is this the workflow facade for the outer most workflow?
				// (If it is the facade for one of the contained nested workflows then the
				// workflow status should not be set to FINISHED after the nested one has finished
				// as the main workflow may still be running)
				if (owningProcess.length == 1){
					
					Date workflowFinishDate = new Date();
					Date workflowStartTime = progressTreeTable.getWorkflowStartDate();
					progressTreeTable.setWorkflowFinishDate(workflowFinishDate);
					progressTreeTable.setWorkflowStatus(STATUS_FINISHED);
					if (workflowStartTime != null){
						long averageInvocationTime = (workflowFinishDate.getTime() - workflowStartTime.getTime());
						progressTreeTable.setWorkflowInvocationTime(averageInvocationTime);
					}
					
//					if (workflowRunStatusLabel != null){
//						workflowRunStatusLabel.setText(STATUS_FINISHED);
//						workflowRunStatusLabel.setIcon(WorkbenchIcons.greentickIcon);
//					}
					
					// Stop observing monitor messages as workflow has finished running
					// This observer may have been already removed (in which case the command 
					// will have no effect) but in the case the workflow has no outputs
					// we have to do the removing here.
					MonitorManager.getInstance().removeObserver(this);
				}
//				updateTimer.schedule(new TimerTask() {
//					public void run() {
//						facade.removeResultListener(resultListeners
//								.remove(owningProcessId));
//					}
//				}, deregisterDelay);
			}
		}
	}

	public void addPropertiesToNode(String[] owningProcess,
			Set<MonitorableProperty<?>> newProperties) {
		if (owningProcess[0].equals(filter)) {
			WorkflowRunProgressMonitorNode monitorNode;
			synchronized(processorMonitorNodes) {
				monitorNode = processorMonitorNodes
						.get(getOwningProcessId(owningProcess));
			}
			new Exception().printStackTrace();
			if (monitorNode != null) {
				for (MonitorableProperty<?> property : newProperties) {
					monitorNode.addMonitorableProperty(property);
				}
			}
		}
	}

	/**
	 * Converts the owning process array to a string.
	 * 
	 * @param owningProcess
	 *            the owning process id
	 * @return the owning process as a string
	 */
	private static String getOwningProcessId(String[] owningProcess) {
		StringBuffer sb = new StringBuffer();
		for (String string : owningProcess) {
			sb.append(string);
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
			} catch (RuntimeException ex) {
				logger.error("UpdateTask update failed", ex);
			}
		}
	}
	
}

