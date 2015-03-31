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

import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.pauseIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.tickIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.workingIcon;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.taverna.lang.ui.tabselector.Tab;
import org.apache.taverna.workbench.selection.SelectionManager;

import org.apache.log4j.Logger;

import org.apache.taverna.platform.execution.api.InvalidExecutionIdException;
import org.apache.taverna.platform.report.State;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.platform.run.api.RunStateException;

/**
 * Tab for selecting the current workflow run.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class RunTab extends Tab<String> {
	private static Logger logger = Logger.getLogger(RunTab.class);

	private final SelectionManager selectionManager;
	private final RunService runService;
	private final File runStore;

	public RunTab(final String runID, final SelectionManager selectionManager,
			final RunService runService, File runStore) {
		super(getRunName(runService, runID), runID);
		this.selectionManager = selectionManager;
		this.runService = runService;
		this.runStore = runStore;
		updateTabIcon();
	}

	private static String getRunName(RunService runService, String runID) {
		try {
			return runService.getRunName(runID);
		} catch (InvalidRunIdException e) {
			return "Invalid Run";
		}
	}

	@Override
	protected void clickTabAction() {
		selectionManager.setSelectedWorkflowRun(selection);
	}

	@Override
	protected void closeTabAction() {
		try {
			State state = runService.getState(selection);
			if (state == State.RUNNING || state == State.PAUSED) {
				if (showConfirmDialog(
						null,
						"Closing the tab will cancel the workflow run. Do you want to continue?",
						"Workflow is still running", YES_NO_OPTION) == JOptionPane.NO_OPTION)
					return;

				try {
					runService.cancel(selection);
				} catch (RunStateException | InvalidExecutionIdException e) {
					// workflow may have finished by now
				}
			}
			File file = new File(runStore, getName() + ".wfRun");
			try {
				if (!file.exists())
					runService.save(selection, file);
			} catch (IOException e) {
				logger.warn("Failed to save workflow run to " + file, e);
			}
			runService.close(selection);
		} catch (InvalidRunIdException | InvalidExecutionIdException e) {
			// TODO Have to cope with this - execution ID could be invalid but still need to close the tab
			logger.error("problem with invalid id", e);
		}
	}

	public void updateTabIcon() {
		try {
			switch (runService.getState(selection)) {
			case RUNNING:
				setIcon(workingIcon);
				break;
			case COMPLETED:
				setIcon(tickIcon);
				break;
			case PAUSED:
				setIcon(pauseIcon);
				break;
			case CANCELLED:
			case FAILED:
				setIcon(tickIcon);
			default:
				break;
			}
		} catch (InvalidRunIdException e) {
			logger.warn(e);
		}
	}
}
