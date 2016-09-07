package org.apache.taverna.workbench.views.monitor.graph;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static java.lang.Math.max;
import org.apache.taverna.workbench.models.graph.GraphController;
import org.apache.taverna.platform.report.ProcessorReport;

/**
 * A <code>MonitorNode</code> that updates a <code>Graph</code> when
 * <code>ProcessorReport</code> property changes.
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

	public GraphMonitorNode(String id, ProcessorReport processorReport,
			GraphController graphController) {
		this.processorReport = processorReport;
		this.graphController = graphController;
		processorId = id;
	}

	/**
	 * Updates the <code>Graph</code> when changes to properties are detected.
	 */
	public void update() {
		synchronized (graphController) {
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

			if (queueSizeChanged || sentJobsChanged || completedJobsChanged
					|| errorsChanged) {
				if (completedJobsChanged)
					graphController.setIteration(processorId, completedJobs);
				if (completedJobs > 0)
					graphController.setNodeCompleted(processorId,
							(completedJobs / (float) (sentJobs + queueSize)));
				if (sentJobsChanged) {
					// graphController.setEdgeActive(processorId, true);
				}
				if (errorsChanged && errors > 0)
					graphController.setErrors(processorId, errors);
			}
		}
	}

	public void redraw() {
		synchronized (graphController) {
			queueSize = max(processorReport.getJobsQueued(), 0);
			sentJobs = processorReport.getJobsStarted();
			completedJobs = processorReport.getJobsCompleted();
			errors = processorReport.getJobsCompletedWithErrors();

			graphController.setIteration(processorId, completedJobs);
			if (completedJobs > 0)
				graphController.setNodeCompleted(processorId,
						(completedJobs / (float) (sentJobs + queueSize)));
			if (errors > 0)
				graphController.setErrors(processorId, errors);
		}
	}
}
