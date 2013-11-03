/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester
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
package net.sf.taverna.t2.workbench.run.actions;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.reference.ui.CopyWorkflowInProgressDialog;
import net.sf.taverna.t2.reference.ui.CopyWorkflowSwingWorker;
import net.sf.taverna.t2.reference.ui.InvalidDataflowReport;
import net.sf.taverna.t2.reference.ui.WorkflowLaunchWindow;
import net.sf.taverna.t2.reference.ui.referenceactions.ReferenceActionSPI;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.SwingWorkerCompletionWaiter;
import net.sf.taverna.t2.workbench.ui.Workbench;

import org.apache.log4j.Logger;
import org.purl.wf4ever.robundle.Bundle;

import uk.org.taverna.databundle.DataBundles;
import uk.org.taverna.platform.execution.api.ExecutionEnvironment;
import uk.org.taverna.platform.execution.api.InvalidExecutionIdException;
import uk.org.taverna.platform.execution.api.InvalidWorkflowException;
import uk.org.taverna.platform.run.api.InvalidRunIdException;
import uk.org.taverna.platform.run.api.RunProfile;
import uk.org.taverna.platform.run.api.RunProfileException;
import uk.org.taverna.platform.run.api.RunService;
import uk.org.taverna.platform.run.api.RunStateException;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.profiles.Profile;

/**
 * Run the current workflow (with workflow input dialogue if needed) and add it
 * to the list of runs.
 * <p>
 * Note that running a workflow will force a clone of the WorkflowBundle, allowing further edits to
 * the current WorkflowBundle without obstructing the run.
 */
public class RunWorkflowAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RunWorkflowAction.class);

	// A map of workflows and their corresponding WorkflowLaunchWindowS
	// We only create one window per workflow and then update its content if the
	// workflow gets updated
	private static HashMap<WorkflowBundle, WorkflowLaunchWindow> workflowLaunchWindowMap = new HashMap<>();

	private final EditManager editManager;
	private final FileManager fileManager;
	private final ReportManager reportManager;
	private final Workbench workbench;
	private final RunService runService;
	private final SelectionManager selectionManager;

	public RunWorkflowAction(EditManager editManager, FileManager fileManager,
			ReportManager reportManager, Workbench workbench, RunService runService,
			SelectionManager selectionManager) {
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.reportManager = reportManager;
		this.workbench = workbench;
		this.runService = runService;
		this.selectionManager = selectionManager;
		putValue(SMALL_ICON, WorkbenchIcons.runIcon);
		putValue(NAME, "Run workflow...");
		putValue(SHORT_DESCRIPTION, "Run the current workflow");
		putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit
				.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileManager.addObserver(new Observer<FileManagerEvent>() {
			public void notify(Observable<FileManagerEvent> sender, FileManagerEvent message)
					throws Exception {
				if (message instanceof ClosedDataflowEvent) {
					workflowLaunchWindowMap.remove(((ClosedDataflowEvent) message).getDataflow());
				}
			}
		});
	}

	public void actionPerformed(ActionEvent e) {
		final WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();
		final Profile profile = selectionManager.getSelectedProfile();
		Set<ExecutionEnvironment> executionEnvironments = runService
				.getExecutionEnvironments(profile);
		if (executionEnvironments.isEmpty()) {
			InvalidDataflowReport.showErrorDialog(
					"There are no execution environments capable of running this workflow",
					"Can't run workflow");
		} else {
			// TODO ask user to choose execution environment
			final ExecutionEnvironment executionEnvironment = executionEnvironments.iterator().next();
			// TODO update to use Scufl2 validation
			// if (CheckWorkflowStatus.checkWorkflow(selectedProfile, workbench, editManager,
			// fileManager,reportManager)) {
			try {
				final Bundle bundle = DataBundles.createBundle();
				if (workflowBundle.getMainWorkflow().getInputPorts().isEmpty()) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							runWorkflow(workflowBundle, profile, executionEnvironment, bundle);
						}
					});
				} else { // workflow had inputs - show the input dialog
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							showInputDialog(workflowBundle, profile, executionEnvironment);
						}
					});
				}
			} catch (Exception ex) {
				String message = "Could not run workflow " + workflowBundle.getName();
				logger.warn(message);
				InvalidDataflowReport.showErrorDialog(ex.getMessage(), message);
			}
			// }
		}
	}

	private void runWorkflow(WorkflowBundle workflowBundle, Profile profile,
			ExecutionEnvironment executionEnvironment, Bundle workflowInputs) {
		try {
			RunProfile runProfile = createRunProfile(workflowBundle, profile,
					executionEnvironment, workflowInputs);
			if (runProfile != null) {
				String runId = runService.createRun(runProfile);
				selectionManager.setSelectedWorkflowRun(runId);
				runService.start(runId);
			}
		} catch (InvalidWorkflowException | RunProfileException | InvalidRunIdException
				| RunStateException | InvalidExecutionIdException e) {
			String message = "Could not run workflow " + workflowBundle.getName();
			logger.warn(message, e);
			InvalidDataflowReport.showErrorDialog(e.getMessage(), message);
		}
	}

	private RunProfile createRunProfile(WorkflowBundle workflowBundle, Profile profile,
			ExecutionEnvironment executionEnvironment, Bundle inputDataBundle) {
		// Make a copy of the workflow to run so user can still
		// modify the original workflow
		WorkflowBundle workflowBundleCopy = null;

		// CopyWorkflowSwingWorker will make a copy of the workflow and pop
		// up a modal dialog that will block the GUI while
		// CopyWorkflowSwingWorker is doing it to let the user know that something is being done.
		// Blocking of the GUI is needed here so that the user cannot modify the
		// original workflow while it is being copied.
		CopyWorkflowSwingWorker copyWorkflowSwingWorker = new CopyWorkflowSwingWorker(
				workflowBundle);

		CopyWorkflowInProgressDialog dialog = new CopyWorkflowInProgressDialog();
		copyWorkflowSwingWorker.addPropertyChangeListener(new SwingWorkerCompletionWaiter(dialog));
		copyWorkflowSwingWorker.execute();

		// Give a chance to the SwingWorker to finish so we do not have to display
		// the dialog if copying of the workflow is quick (so it won't flicker on the screen)
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// do nothing
		}
		if (!copyWorkflowSwingWorker.isDone()) {
			dialog.setVisible(true); // this will block the GUI
		}
		// see if user cancelled the dialog
		boolean userCancelled = dialog.hasUserCancelled();

		if (userCancelled) {
			// Stop the CopyWorkflowSwingWorker if it is still working
			copyWorkflowSwingWorker.cancel(true);
		} else {
			// Get the workflow copy from the copyWorkflowSwingWorker
			try {
				workflowBundleCopy = copyWorkflowSwingWorker.get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Failed to get the workflow copy", e);
			}

			if (workflowBundleCopy == null) {
				InvalidDataflowReport.showErrorDialog(
						"Unable to make a copy of the workflow to run", "Workflow copy failed");
			}
		}

		if (workflowBundleCopy != null) {
			return new RunProfile(executionEnvironment, workflowBundleCopy, workflowBundleCopy
					.getMainWorkflow().getName(), profile.getName(), inputDataBundle);
		} else {
			return null;
		}
	}

	@SuppressWarnings("serial")
	private void showInputDialog(final WorkflowBundle workflowBundle, final Profile profile,
			final ExecutionEnvironment executionEnvironment) {
		// Get the WorkflowLauchWindow
		WorkflowLaunchWindow launchWindow = null;
		synchronized (workflowLaunchWindowMap) {
			WorkflowLaunchWindow savedLaunchWindow = workflowLaunchWindowMap.get(workflowBundle);
			if (savedLaunchWindow == null) {
				launchWindow = new WorkflowLaunchWindow(workflowBundle.getMainWorkflow(),
						editManager, fileManager, reportManager, workbench, new ArrayList<ReferenceActionSPI>(), null) {

					@Override
					public void handleLaunch(Bundle workflowInputs) {
						runWorkflow(workflowBundle, profile, executionEnvironment, workflowInputs);
						setState(Frame.ICONIFIED); // minimise the window
					}

					@Override
					public void handleCancel() {
						// Keep the window so we do not have to rebuild it again
						setVisible(false);
					}
				};

				// Add this window to the map of the workflow input/launch
				// windows
				workflowLaunchWindowMap.put(workflowBundle, launchWindow);

				launchWindow.setLocationRelativeTo(null);
			} else {
				launchWindow = savedLaunchWindow;
			}

			// Display the window
			launchWindow.setVisible(true);
			// On Win XP setting the window visible seems not to be enough to
			// bring the window up if it was minimised previously so we restore
			// it here
			if (launchWindow.getState() == Frame.ICONIFIED) {
				launchWindow.setState(Frame.NORMAL); // restore the window
			}
		}
	}

}
