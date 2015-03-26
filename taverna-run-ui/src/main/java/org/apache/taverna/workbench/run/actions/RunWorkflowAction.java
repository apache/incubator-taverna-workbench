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
package org.apache.taverna.workbench.run.actions;

import static java.awt.Frame.ICONIFIED;
import static java.awt.Frame.NORMAL;
import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_R;
import static javax.swing.KeyStroke.getKeyStroke;
import static javax.swing.SwingUtilities.invokeLater;
import static net.sf.taverna.t2.reference.ui.InvalidDataflowReport;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.runIcon;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import net.sf.taverna.t2.reference.ui.CopyWorkflowInProgressDialog;
import net.sf.taverna.t2.reference.ui.CopyWorkflowSwingWorker;
import net.sf.taverna.t2.reference.ui.WorkflowLaunchWindow;
import net.sf.taverna.t2.reference.ui.referenceactions.ReferenceActionSPI;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.ClosedDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.SwingWorkerCompletionWaiter;
import org.apache.taverna.workbench.ui.Workbench;

import org.apache.log4j.Logger;

import org.apache.taverna.databundle.DataBundles;
import org.apache.taverna.platform.execution.api.ExecutionEnvironment;
import org.apache.taverna.platform.execution.api.InvalidExecutionIdException;
import org.apache.taverna.platform.execution.api.InvalidWorkflowException;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunProfile;
import org.apache.taverna.platform.run.api.RunProfileException;
import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.platform.run.api.RunStateException;
import org.apache.taverna.robundle.Bundle;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * Run the current workflow (with workflow input dialogue if needed) and add it
 * to the list of runs.
 * <p>
 * Note that running a workflow will force a clone of the WorkflowBundle, allowing further edits to
 * the current WorkflowBundle without obstructing the run.
 */
@SuppressWarnings("serial")
public class RunWorkflowAction extends AbstractAction {
	private static Logger logger = Logger.getLogger(RunWorkflowAction.class);

	/**
	 * A map of workflows and their corresponding {@link WorkflowLaunchWindow}s.
	 * We only create one window per workflow and then update its content if the
	 * workflow gets updated
	 */
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
		putValue(SMALL_ICON, runIcon);
		putValue(NAME, "Run workflow...");
		putValue(SHORT_DESCRIPTION, "Run the current workflow");
		putValue(MNEMONIC_KEY, VK_R);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_R, getDefaultToolkit().getMenuShortcutKeyMask()));
		fileManager.addObserver(new Observer<FileManagerEvent>() {
			@Override
			public void notify(Observable<FileManagerEvent> sender, FileManagerEvent message)
					throws Exception {
				if (message instanceof ClosedDataflowEvent)
					workflowLaunchWindowMap
							.remove(((ClosedDataflowEvent) message)
									.getDataflow());
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();
		final Profile profile = selectionManager.getSelectedProfile();
		Set<ExecutionEnvironment> executionEnvironments = runService
				.getExecutionEnvironments(profile);
		if (executionEnvironments.isEmpty()) {
			showErrorDialog(
					"There are no execution environments capable of running this workflow",
					"Can't run workflow");
			return;
		}

		// TODO ask user to choose execution environment
		final ExecutionEnvironment executionEnvironment = executionEnvironments.iterator().next();
		try {
			if (validate(workflowBundle, profile)) {
				if (workflowBundle.getMainWorkflow().getInputPorts().isEmpty()) {
					final Bundle bundle = DataBundles.createBundle();
					invokeLater(new Runnable() {
						@Override
						public void run() {
							runWorkflow(workflowBundle, profile,
									executionEnvironment, bundle);
						}
					});
				} else // workflow had inputs - show the input dialog
					invokeLater(new Runnable() {
						@Override
						public void run() {
							showInputDialog(workflowBundle, profile,
									executionEnvironment);
						}
					});
				}
		} catch (Exception ex) {
			String message = "Could not run workflow " + workflowBundle.getName();
			logger.warn(message);
			showErrorDialog(ex.getMessage(), message);
		}
	}

	// TODO update to use Scufl2 validation
	private boolean validate(WorkflowBundle workflowBundle, Profile selectedProfile) {
		//CheckWorkflowStatus.checkWorkflow(selectedProfile, workbench, editManager,
		//		fileManager,reportManager);
		return true;
	}

	private void runWorkflow(WorkflowBundle workflowBundle, Profile profile,
			ExecutionEnvironment executionEnvironment, Bundle workflowInputs) {
		try {
			RunProfile runProfile = createRunProfile(workflowBundle, profile,
					executionEnvironment, workflowInputs);
			if (runProfile != null) {
				String runId = runService.createRun(runProfile);
				runService.start(runId);
			}
		} catch (InvalidWorkflowException | RunProfileException | InvalidRunIdException
				| RunStateException | InvalidExecutionIdException e) {
			String message = "Could not run workflow " + workflowBundle.getName();
			logger.warn(message, e);
			showErrorDialog(e.getMessage(), message);
		}
	}

	private RunProfile createRunProfile(WorkflowBundle workflowBundle, Profile profile,
			ExecutionEnvironment executionEnvironment, Bundle inputDataBundle) {
		/*
		 * Make a copy of the workflow to run so user can still modify the
		 * original workflow
		 */
		WorkflowBundle workflowBundleCopy = null;

		/*
		 * CopyWorkflowSwingWorker will make a copy of the workflow and pop up a
		 * modal dialog that will block the GUI while CopyWorkflowSwingWorker is
		 * doing it to let the user know that something is being done. Blocking
		 * of the GUI is needed here so that the user cannot modify the original
		 * workflow while it is being copied.
		 */
		CopyWorkflowSwingWorker copyWorkflowSwingWorker = new CopyWorkflowSwingWorker(
				workflowBundle);

		CopyWorkflowInProgressDialog dialog = new CopyWorkflowInProgressDialog();
		copyWorkflowSwingWorker.addPropertyChangeListener(new SwingWorkerCompletionWaiter(dialog));
		copyWorkflowSwingWorker.execute();

		/*
		 * Give a chance to the SwingWorker to finish so we do not have to
		 * display the dialog if copying of the workflow is quick (so it won't
		 * flicker on the screen)
		 */
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// do nothing
		}

		if (!copyWorkflowSwingWorker.isDone())
			dialog.setVisible(true); // this will block the GUI
		// see if user cancelled the dialog
		boolean userCancelled = dialog.hasUserCancelled();

		if (userCancelled) {
			// Stop the CopyWorkflowSwingWorker if it is still working
			copyWorkflowSwingWorker.cancel(true);
			return null;
		}

		// Get the workflow copy from the copyWorkflowSwingWorker
		try {
			workflowBundleCopy = copyWorkflowSwingWorker.get();
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Failed to get the workflow copy", e);
		}

		if (workflowBundleCopy == null) {
			showErrorDialog("Unable to make a copy of the workflow to run",
					"Workflow copy failed");
			return null;
		}

		return new RunProfile(executionEnvironment, workflowBundleCopy,
				workflowBundleCopy.getMainWorkflow().getName(),
				profile.getName(), inputDataBundle);
	}

	private void showInputDialog(final WorkflowBundle workflowBundle,
			final Profile profile,
			final ExecutionEnvironment executionEnvironment) {
		// Get the WorkflowLauchWindow
		WorkflowLaunchWindow launchWindow = null;
		synchronized (workflowLaunchWindowMap) {
			WorkflowLaunchWindow savedLaunchWindow = workflowLaunchWindowMap
					.get(workflowBundle);
			if (savedLaunchWindow == null) {
				launchWindow = new WorkflowLaunchWindow(
						workflowBundle.getMainWorkflow(), editManager,
						fileManager, reportManager, workbench,
						new ArrayList<ReferenceActionSPI>(), null) {
					@Override
					public void handleLaunch(Bundle workflowInputs) {
						runWorkflow(workflowBundle, profile,
								executionEnvironment, workflowInputs);
						//TODO T2 now makes the launch window vanish
						setState(ICONIFIED); // minimise the window
					}

					@Override
					public void handleCancel() {
						// Keep the window so we do not have to rebuild it again
						setVisible(false);
					}
				};

				/*
				 * Add this window to the map of the workflow input/launch
				 * windows
				 */
				workflowLaunchWindowMap.put(workflowBundle, launchWindow);
				launchWindow.setLocationRelativeTo(null);
			} else
				launchWindow = savedLaunchWindow;

			// Display the window
			launchWindow.setVisible(true);
			/*
			 * On Win XP setting the window visible seems not to be enough to
			 * bring the window up if it was minimised previously so we restore
			 * it here
			 */
			if (launchWindow.getState() == ICONIFIED)
				launchWindow.setState(NORMAL); // restore the window
		}
	}
}
