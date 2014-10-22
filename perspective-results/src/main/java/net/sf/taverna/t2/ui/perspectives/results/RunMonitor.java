/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.ui.perspectives.results;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.PerspectiveSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowRunSelectionEvent;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

import org.apache.log4j.Logger;

import uk.org.taverna.platform.report.State;
import uk.org.taverna.platform.run.api.InvalidRunIdException;
import uk.org.taverna.platform.run.api.RunService;

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
