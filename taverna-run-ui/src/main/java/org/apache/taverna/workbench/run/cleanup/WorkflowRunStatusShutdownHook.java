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
package org.apache.taverna.workbench.run.cleanup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.platform.execution.api.InvalidExecutionIdException;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.platform.run.api.RunStateException;
import org.apache.taverna.workbench.ShutdownSPI;

/**
 * Shutdown hook that detects running and paused workflows.
 * 
 * @author David Withers
 */
public class WorkflowRunStatusShutdownHook implements ShutdownSPI {
	private static final String RUN_STORE_DIRECTORY = "workflow-runs";

	private RunService runService;
	private ApplicationConfiguration applicationConfiguration;

	@Override
	public int positionHint() {
		return 40;
	}

	@Override
	public boolean shutdown() {
		boolean shutdown = true;
		List<String> workflowRuns = runService.getRuns();
		List<String> runningWorkflows = new ArrayList<>();
		List<String> pausedWorkflows = new ArrayList<>();
		for (String workflowRun : workflowRuns)
			try {
				switch (runService.getState(workflowRun)) {
				case PAUSED:
				case RUNNING:
					pausedWorkflows.add(workflowRun);
				default:
					break;
				}
			} catch (InvalidRunIdException e) {
			}
		if (runningWorkflows.size() + pausedWorkflows.size() > 0) {
			WorkflowRunStatusShutdownDialog dialog = new WorkflowRunStatusShutdownDialog(
					runningWorkflows.size(), pausedWorkflows.size());
			dialog.setVisible(true);
			shutdown = dialog.confirmShutdown();
		}
		if (shutdown) {
			for (String workflowRun : pausedWorkflows)
				try {
					runService.cancel(workflowRun);
				} catch (InvalidRunIdException | RunStateException
						| InvalidExecutionIdException e) {
				}
			for (String workflowRun : runningWorkflows)
				try {
					runService.cancel(workflowRun);
				} catch (InvalidRunIdException | RunStateException
						| InvalidExecutionIdException e) {
				}
			for (String workflowRun : workflowRuns) {
				Path runStore = applicationConfiguration.getApplicationHomeDir().resolve(
						RUN_STORE_DIRECTORY);
				try {
					Path runFile = runStore.resolve(
							runService.getRunName(workflowRun) + ".wfRun");
					if (Files.notExists(runFile)) {
						runService.save(workflowRun, runFile);
					}
				} catch (InvalidRunIdException | IOException e) {
				}
			}
		}
		return shutdown;
	}

	public void setRunService(RunService runService) {
		this.runService = runService;
	}

	public void setApplicationConfiguration(ApplicationConfiguration appConfig) {
		this.applicationConfiguration = appConfig;
	}
}
