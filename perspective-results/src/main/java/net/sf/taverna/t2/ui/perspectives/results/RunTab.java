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

import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.pauseIcon;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.tickIcon;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.workingIcon;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.ui.tabselector.Tab;
import net.sf.taverna.t2.workbench.selection.SelectionManager;

import org.apache.log4j.Logger;

import uk.org.taverna.platform.execution.api.InvalidExecutionIdException;
import uk.org.taverna.platform.report.State;
import uk.org.taverna.platform.run.api.InvalidRunIdException;
import uk.org.taverna.platform.run.api.RunService;
import uk.org.taverna.platform.run.api.RunStateException;

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
