/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester
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
package org.apache.taverna.workbench.run.cleanup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.workbench.ShutdownSPI;
import uk.org.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.platform.execution.api.InvalidExecutionIdException;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.platform.run.api.RunStateException;

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
				File runStore = new File(
						applicationConfiguration.getApplicationHomeDir(),
						RUN_STORE_DIRECTORY);
				try {
					File file = new File(runStore,
							runService.getRunName(workflowRun) + ".wfRun");
					if (!file.exists())
						runService.save(workflowRun, file);
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
