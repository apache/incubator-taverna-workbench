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
package net.sf.taverna.t2.workbench.views.monitor;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.monitor.MonitorableProperty;
import net.sf.taverna.t2.monitor.MonitorManager.AddPropertiesMessage;
import net.sf.taverna.t2.monitor.MonitorManager.DeregisterNodeMessage;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.monitor.MonitorManager.RegisterNodeMessage;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent.Status;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

/**
 * An implementation of the Monitor interface that updates a Graph when
 * MonitorableProperties change.
 * 
 * @author David Withers
 */
public class GraphMonitor implements Observer<MonitorMessage> {

	private static Logger logger = Logger.getLogger(GraphMonitor.class);

	private static long deregisterDelay = 1000;

	private static long monitorRate = 300;

	private GraphController graphController;

	private Map<String, Object> workflowObjects = new HashMap<String, Object>();

	private Set<String> datalinks = Collections.synchronizedSet(new HashSet<String>());

	private Map<String, GraphMonitorNode> processors = new HashMap<String, GraphMonitorNode>();

	private Map<String, ResultListener> resultListeners = new HashMap<String, ResultListener>();

	private Timer updateTimer = new Timer("GraphMonitor update timer", true);

	private UpdateTask updateTask;

	private String filter;

	private final MonitorViewComponent monitorViewComponent;

	public GraphMonitor(GraphController graphController, MonitorViewComponent monitorViewComponent) {
		this.graphController = graphController;
		this.monitorViewComponent = monitorViewComponent;
	}

	public void addPropertiesToNode(String[] owningProcess,
			Set<MonitorableProperty<?>> newProperties) {
		if (owningProcess[0].equals(filter)) {
			GraphMonitorNode monitorNode = processors
					.get(getProcessorId(owningProcess));
			if (monitorNode != null) {
				for (MonitorableProperty<?> property : newProperties) {
					monitorNode.addMonitorableProperty(property);
				}
			}
		}
	}

	public void deregisterNode(String[] owningProcess) {
		if (owningProcess[0].equals(filter)) {
			final String owningProcessId = getOwningProcessId(owningProcess);
			Object workflowObject = workflowObjects.remove(owningProcessId);
			if (workflowObject instanceof Processor) {
				 processors.get(getProcessorId(owningProcess)).update();
			} else if (workflowObject instanceof Dataflow) {
				if (owningProcess.length == 2) {
					// outermost dataflow finished so schedule a task to cancel
					// the update task
					synchronized (this) {
						if (updateTask != null) {
							updateTimer.schedule(new TimerTask() {
								public void run() {
									updateTask.cancel();
									updateTask = null;
								}
							}, deregisterDelay);
						}
					}
				}
			} else if (workflowObject instanceof WorkflowInstanceFacade) {
				final WorkflowInstanceFacade facade = (WorkflowInstanceFacade) workflowObject;
				// Is this the workflow facade for the outer most workflow?
				// (If it is the facade for one of the contained nested workflows then the
				// workflow status should not be set to COMPLETED after the nested one has finished
				// as the main workflow may still be running)
				if (owningProcess.length == 1){
					monitorViewComponent.setStatus(Status.COMPLETE);
					// Stop observing monitor messages as workflow has finished running
					// This observer may have been already removed (in which case the command 
					// will have no effect) but in the case the workflow has no outputs
					// we have to do the removing here.
					MonitorManager.getInstance().removeObserver(
							this);
				}
				updateTimer.schedule(new TimerTask() {
					public void run() {
						facade.removeResultListener(resultListeners
								.remove(owningProcessId));
					}
				}, deregisterDelay);
			}
		}
	}

	public void registerNode(Object workflowObject, String[] owningProcess,
			Set<MonitorableProperty<?>> properties) {
		if (filter == null && owningProcess.length == 1) {
			filter = owningProcess[0];
		}
		if (owningProcess[0].equals(filter)) {
			String owningProcessId = getOwningProcessId(owningProcess);
			workflowObjects.put(owningProcessId, workflowObject);
			if (workflowObject instanceof Processor) {
				Processor processor = (Processor) workflowObject;
				GraphMonitorNode monitorNode = new GraphMonitorNode(
						processor, owningProcess, properties, graphController);
				processors.put(getProcessorId(owningProcess), monitorNode);
			} else if (workflowObject instanceof Dataflow) {
				// outermost dataflow 
				if (owningProcess.length == 2) {
					synchronized (this) {
						if (updateTask != null) {
							// updateTask.cancel();
						}
						updateTask = new UpdateTask();
						updateTimer.schedule(updateTask, monitorRate,
								monitorRate);
					}
				}
			} else if (workflowObject instanceof WorkflowInstanceFacade) {
				WorkflowInstanceFacade facade = (WorkflowInstanceFacade) workflowObject;
				ResultListener resultListener = new MonitorResultListener(
						getProcessorId(owningProcess));
				facade.addResultListener(resultListener);
				resultListeners.put(owningProcessId, resultListener);
				monitorViewComponent.setStatus(Status.RUNNING);
			}
		}
	}

	/**
	 * Calculates the id that will identify the box on the diagram that
	 * represents the processor.
	 * 
	 * @param owningProcess
	 *            the owning process id for a processor
	 * @return the id that will identify the box on the diagram that represents
	 *         the processor
	 */
	public static String getProcessorId(String[] owningProcess) {
		StringBuffer sb = new StringBuffer();
		for (int i = 1, skip = 0; i < owningProcess.length; i++, skip--) {
			if (i <= 2 || skip < 0) {
				sb.append(owningProcess[i]);
				skip = 3;
			}
		}
		return sb.toString();
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
			for (GraphMonitorNode node : processors.values()) {
				node.update();
			}
			synchronized (datalinks) {
				for (String datalink : datalinks) {
					graphController.setEdgeActive(datalink, true);
				}
				datalinks.clear();
			}
		}
	}

	class MonitorResultListener implements ResultListener {

		private String context;

		public MonitorResultListener(String context) {
			if ("".equals(context)) {
				this.context = graphController.getDataflow().getLocalName();
			} else {
				this.context = context;
			}
		}

		public void resultTokenProduced(WorkflowDataToken token, String portName) {
			String id = context + "WORKFLOWINTERNALSINK_" + portName;
//			datalinks.add(id);
			if (token.isFinal()) {
				graphController.setNodeCompleted(id, 1f);
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
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

	public void onDispose() {
		updateTimer.cancel();
	}

	@Override
	protected void finalize() throws Throwable {
		onDispose();
	}
	
}
