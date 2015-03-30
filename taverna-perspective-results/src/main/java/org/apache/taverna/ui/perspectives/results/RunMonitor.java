/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.ui.perspectives.results;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.PerspectiveSelectionEvent;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.selection.events.WorkflowRunSelectionEvent;
import org.apache.taverna.workbench.ui.Updatable;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;

import org.apache.log4j.Logger;

import org.apache.taverna.platform.report.State;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
public class RunMonitor {
	private static final Logger logger = Logger.getLogger(RunMonitor.class);
	private static final long monitorRate = 300;

	private RunService runService;
	private SelectionManager selectionManager;
	private final Updatable updatable;

	private Timer updateTimer = new Timer("RunMonitor update timer", true);
	private UpdateTask updateTask;
	private String workflowRun;
	private Set<String> finishedRuns = new HashSet<>();
	private SelectionManagerObserver selectionManagerObserver = new SelectionManagerObserver();

	public RunMonitor(RunService runService, SelectionManager selectionManager, Updatable updatable) {
		this.runService = runService;
		this.selectionManager = selectionManager;
		this.updatable = updatable;
		selectionManager.addObserver(selectionManagerObserver);
	}

	@Override
	protected void finalize() throws Throwable {
		selectionManager.removeObserver(selectionManagerObserver);
	}

	public void start() {
		synchronized (this) {
			if (updateTask != null)
				updateTask.cancel();
			updateTask = new UpdateTask();
			try {
				updateTimer.schedule(updateTask, monitorRate, monitorRate);
			} catch (IllegalStateException ex) {
				// task already cancelled
			}
		}
	}

	public void stop() {
		synchronized (this) {
			if (updateTask != null) {
				updateTask.cancel();
				updateTask = null;
			}
		}
	}

	private void setWorkflowRun(String workflowRun) throws InvalidRunIdException {
		this.workflowRun = workflowRun;
		if (workflowRun == null || isFinished(workflowRun))
			stop();
		else
			start();
	}

	/**
	 * Returns true if a workflow run has been recorded by the monitor as finished.
	 * <p>
	 * If the workflow run has not been recorded as finished false is returned and the state of the
	 * workflow is checked and recorded for future calls of this method.
	 *
	 * @param workflowRun
	 *            the ID of the run to check
	 * @return true if a workflow run has been recorded by the monitor as finished
	 * @throws InvalidRunIdException
	 *             if the runId is invalid
	 */
	private boolean isFinished(String workflowRun) throws InvalidRunIdException {
		boolean finished = false;
		if (finishedRuns.contains(workflowRun))
			finished = true;
		else {
			State state = runService.getState(workflowRun);
			if (state == State.COMPLETED || state == State.CANCELLED
					|| state == State.FAILED)
				finishedRuns.add(workflowRun);
		}
		return finished;
	}

	private class UpdateTask extends TimerTask {
		@Override
		public void run() {
			try {
				updatable.update();
				if (isFinished(workflowRun)) {
					updatable.update();
					stop();
				}
			} catch (InvalidRunIdException e) {
				logger.warn("workflow run could not be queried", e);
				stop();
			}
		}
	}

	private class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof WorkflowRunSelectionEvent) {
				WorkflowRunSelectionEvent event = (WorkflowRunSelectionEvent) message;
				try {
					setWorkflowRun(event.getSelectedWorkflowRun());
				} catch (InvalidRunIdException e) {
					logger.warn("Selected workflow run ID is invalid", e);
				}
			} else if (message instanceof PerspectiveSelectionEvent) {
				PerspectiveSelectionEvent event = (PerspectiveSelectionEvent) message;
				PerspectiveSPI selectedPerspective = event
						.getSelectedPerspective();
				if ("net.sf.taverna.t2.ui.perspectives.results.ResultsPerspective"
						.equals(selectedPerspective.getID())) {
					try {
						if (workflowRun != null && !isFinished(workflowRun))
							start();
					} catch (InvalidRunIdException e) {
						logger.warn("Selected workflow run ID is invalid", e);
					}
				} else
					stop();
			}
		}
	}
}
