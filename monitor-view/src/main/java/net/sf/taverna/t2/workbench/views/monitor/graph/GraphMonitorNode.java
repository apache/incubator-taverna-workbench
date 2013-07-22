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
package net.sf.taverna.t2.workbench.views.monitor.graph;

import net.sf.taverna.t2.workbench.models.graph.GraphController;
import uk.org.taverna.platform.report.ProcessorReport;
import uk.org.taverna.platform.report.State;

/**
 * A <code>MonitorNode</code> that updates a <code>Graph</code> when
 * <code>MonitorableProperty</code>s change.
 *
 * @author David Withers
 */
public class GraphMonitorNode {

	private ProcessorReport processorReport;

	private GraphController graphController;

	private String processorId;

	private int queueSize = 0;

	private int sentJobs = 0;

	private int completedJobs = 0;

	private int errors = 0;

	private State state = State.CREATED;

	public GraphMonitorNode(ProcessorReport processorReport, GraphController graphController) {
		this.processorReport = processorReport;
		this.graphController = graphController;
		processorId = GraphMonitor.getProcessorId(processorReport);
	}

	/**
	 * Updates the <code>Graph</code> when changes to
	 * properties are detected.
	 */
	public void update() {
		boolean stateChanged = false;
		State newState = processorReport.getState();
		if (newState != state) {
			state = newState;
			stateChanged = true;
		}
		if (stateChanged || state == State.RUNNING) {
			boolean queueSizeChanged = false;
			boolean sentJobsChanged = false;
			boolean completedJobsChanged = false;
			boolean errorsChanged = false;

			int newQueueSize = processorReport.getJobsQueued();
			newQueueSize = newQueueSize == -1 ? 0 : newQueueSize;
			if (queueSize != newQueueSize) {
				queueSize = newQueueSize;
				queueSizeChanged = true;
			}

			int newSentJobs = processorReport.getJobsStarted();
			if (sentJobs != newSentJobs) {
				sentJobs = newSentJobs;
				sentJobsChanged = true;
			}

			int newCompletedJobs = processorReport.getJobsCompleted();
			if (completedJobs != newCompletedJobs) {
				completedJobs = newCompletedJobs;
				completedJobsChanged = true;
			}

			int newErrors = processorReport.getJobsCompletedWithErrors();
			if (errors != newErrors) {
				errors = newErrors;
				errorsChanged = true;
			}

			if (queueSizeChanged || sentJobsChanged || completedJobsChanged || errorsChanged) {
				if (completedJobsChanged) {
					graphController.setIteration(processorId, completedJobs);
				}
				if (completedJobs > 0) {
					int totalJobs = sentJobs + queueSize;
					graphController.setNodeCompleted(processorId, ((float) (completedJobs))
							/ (float) totalJobs);
				}
				if (sentJobsChanged) {
					// graphController.setEdgeActive(processorId, true);
				}
				if (errorsChanged && errors > 0) {
					graphController.setErrors(processorId, errors);
				}
			}

		}
	}

	public void redraw() {
		queueSize = Math.max(processorReport.getJobsQueued(), 0);
		sentJobs = processorReport.getJobsStarted();
		completedJobs = processorReport.getJobsCompleted();
		errors = processorReport.getJobsCompletedWithErrors();

		graphController.setIteration(processorId, completedJobs);
		if (completedJobs > 0) {
			int totalJobs = sentJobs + queueSize;
			graphController.setNodeCompleted(processorId, ((float) (completedJobs))
					/ (float) totalJobs);
		}
		if (errors > 0) {
			graphController.setErrors(processorId, errors);
		}
	}

}
