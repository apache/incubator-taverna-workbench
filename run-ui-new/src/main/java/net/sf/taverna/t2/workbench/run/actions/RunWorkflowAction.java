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
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.impl.InvocationContextImpl;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ui.CopyWorkflowInProgressDialog;
import net.sf.taverna.t2.reference.ui.CopyWorkflowSwingWorker;
import net.sf.taverna.t2.reference.ui.InvalidDataflowReport;
import net.sf.taverna.t2.reference.ui.WorkflowLaunchWindow;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.run.ResultsPerspectiveComponent;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorkerCompletionWaiter;


/**
 * Run the current workflow (with workflow input dialogue if needed) and add it to the
 * list of runs.
 * <p>
 * Note that running a workflow will force a serialization and deserialization
 * of the Dataflow to make a copy of the workflow, allowing further edits to the
 * current Dataflow without obstructing the run.
 * 
 */
public class RunWorkflowAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RunWorkflowAction.class);

	private ResultsPerspectiveComponent resultsPerspectiveComponent;

	private PerspectiveSPI resultsPerspective;
	
	// A map of workflows and their corresponding WorkflowLaunchWindowS
	// We only create one window per workflow and then update its content if the workflow gets updated
	public static WeakHashMap<Dataflow, WorkflowLaunchWindow> workflowLaunchWindowMap = new WeakHashMap<Dataflow, WorkflowLaunchWindow>();

	public RunWorkflowAction() {
		resultsPerspectiveComponent = ResultsPerspectiveComponent.getInstance();
		putValue(SMALL_ICON, WorkbenchIcons.runIcon);
		putValue(NAME, "Run workflow...");
		putValue(SHORT_DESCRIPTION, "Run the current workflow");
		putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	public void actionPerformed(ActionEvent e) {
		Object model = ModelMap.getInstance().getModel(
				ModelMapConstants.CURRENT_DATAFLOW);
		if (!(model instanceof Dataflow)) {
			return;
		}
		final Dataflow dataflow = (Dataflow) model;
		//Thread t = new Thread("Preparing to run workflow "
		//		+ dataflow.getLocalName()) {
		//	public void run() {
				try {
					runDataflow(dataflow);
				} catch (Exception ex) {
					String message = "Could not run workflow "
							+ dataflow.getLocalName();
					logger.warn(message);
					InvalidDataflowReport.showErrorDialog(ex.getMessage(), message);			
				}
		//	}
		//};
		//t.setDaemon(true);
		//t.start();		
	}

	protected void runDataflow(final Dataflow dataflowOriginal) {
		
		// If the workflow has no input ports - we can run immediately
		if (dataflowOriginal.getInputPorts().isEmpty()) {

			// Make a copy of the workflow to run so user can still
			// modify the original workflow
			Dataflow dataflowCopy = null;

			// CopyWorkflowSwingWorker will make a copy of the workflow and pop up a
			// modal dialog that will block the GUI while CopyWorkflowSwingWorker is 
			// doing it to let the user know that something is being done. Blocking 
			// of the GUI is needed here so that the user cannot modify the original 
			// workflow while it is being copied.
			CopyWorkflowSwingWorker copyWorkflowSwingWorker = new CopyWorkflowSwingWorker(dataflowOriginal);

			CopyWorkflowInProgressDialog dialog = new CopyWorkflowInProgressDialog();
			copyWorkflowSwingWorker.addPropertyChangeListener(
				     new SwingWorkerCompletionWaiter(dialog));
			copyWorkflowSwingWorker.execute();
			
			// Give a chance to the SwingWorker to finish so we do not have to display 
			// the dialog if copying of the workflow is quick (so it won't flicker on the screen)
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// do nothing
			}
			if (!copyWorkflowSwingWorker.isDone()){
				dialog.setVisible(true); // this will block the GUI
			}
			boolean userCancelled = dialog.hasUserCancelled(); // see if user cancelled the dialog
			
			if (userCancelled){
				// Stop the CopyWorkflowSwingWorker if it is still working
				copyWorkflowSwingWorker.cancel(true);
				// exit
				return;
			}
			else{ 
				// Get the workflow copy from the copyWorkflowSwingWorker
				try {
					dataflowCopy = copyWorkflowSwingWorker.get();
				} catch (Exception e) {
					dataflowCopy = null;
					logger.error("Failed to get the workflow copy", e);
				}
				
				if (dataflowCopy != null) {
					// TODO check if the database has been created and create if
					// needed if provenance turned on then add an
					// IntermediateProvLayer to
					// each Processor
					final ReferenceService referenceService = resultsPerspectiveComponent
							.getReferenceService();
					ProvenanceConnector provenanceConnector = null;

					// FIXME: All these run-stuff should be done in a general
					// way so
					// it could also be used when running workflows
					// non-interactively
					if (DataManagementConfiguration.getInstance()
							.isProvenanceEnabled()) {
						String connectorType = DataManagementConfiguration
								.getInstance().getConnectorType();

						for (ProvenanceConnectorFactory factory : ProvenanceConnectorFactoryRegistry
								.getInstance().getInstances()) {
							if (connectorType.equalsIgnoreCase(factory
									.getConnectorType())) {
								provenanceConnector = factory
										.getProvenanceConnector();
							}
						}

						// slight change, the init is outside but it also means
						// that
						// the init call has to ensure that the dbURL is set
						// correctly
						try {
							if (provenanceConnector != null) {
								provenanceConnector.init();
								provenanceConnector
										.setReferenceService(referenceService);
							}
						} catch (Exception except) {

						}
					}
					final InvocationContextImpl context = new InvocationContextImpl(
							referenceService, provenanceConnector);
					// Workflow run id will be set on the context from the
					// facade
					if (provenanceConnector != null) {
						provenanceConnector.setInvocationContext(context);
					}

					final WorkflowInstanceFacade facade;
					try {
						facade = new EditsImpl().createWorkflowInstanceFacade(
								dataflowCopy, context, "");
					} catch (InvalidDataflowException ex) {
						InvalidDataflowReport.invalidDataflow(ex
								.getDataflowValidationReport());
						return;
					}
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							switchToResultsPerspective();
							resultsPerspectiveComponent.runWorkflow(facade,
									(Map<String, T2Reference>) null);
						}
					});
				} else { // something went wrong when copying the workflow
					InvalidDataflowReport.showErrorDialog(
							"Unable to make a copy of the workflow to run",
							"Workflow copy failed");
				}
			}
		} 
		else { // workflow had inputs - show the input dialog
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					showInputDialog(dataflowOriginal);
				}
			});
		}
	}

	private void switchToResultsPerspective() {
		if (resultsPerspective == null) {
			for (PerspectiveSPI perspective : Workbench.getInstance()
					.getPerspectives().getPerspectives()) {
				if (perspective.getText().equalsIgnoreCase("Results (new)")) {
					resultsPerspective = perspective;
					break;
				}
			}
		}
		if (resultsPerspective != null) {
			ModelMap.getInstance().setModel(
					ModelMapConstants.CURRENT_PERSPECTIVE, resultsPerspective);
		}
	}

	@SuppressWarnings("serial")
	private void showInputDialog(final Dataflow dataflowOriginal) {
		
		// Get the WorkflowLauchWindow
		WorkflowLaunchWindow launchWindow = null;
		synchronized(dataflowOriginal)
		{
			if (workflowLaunchWindowMap.get(dataflowOriginal) == null) {
				launchWindow = new WorkflowLaunchWindow(dataflowOriginal,
						resultsPerspectiveComponent.getReferenceService()) {

					@Override
					public void handleLaunch(
							Map<String, T2Reference> workflowInputs) {
						setState(Frame.ICONIFIED); // minimise the window
						switchToResultsPerspective();
						resultsPerspectiveComponent.runWorkflow(getFacade(), workflowInputs);
					}

					@Override
					public void handleCancel() {
						// Keep the window so we do not have to rebuild it again
						setVisible(false);
					}
				};

				// Add this window to the map of the workflow input/launch
				// windows
				workflowLaunchWindowMap.put(dataflowOriginal, launchWindow);
				
				launchWindow.setLocationRelativeTo(null);
			} 
			else {
				launchWindow = workflowLaunchWindowMap.get(dataflowOriginal);
				// Update the Reference Service in the case it has changed in
				// the meantime
				// (e.g. user switched from in-memory to database)
				launchWindow.setReferenceService(resultsPerspectiveComponent
						.getReferenceService());
			}
			
			// Display the window
			launchWindow.setVisible(true);
			// On Win XP setting the window visible seems not to be enough to 
			// bring the window up if it was minimised previously so we restore it here
			if (launchWindow.getState() == Frame.ICONIFIED){
				launchWindow.setState(Frame.NORMAL); // restore the window
			}
		}
	}

}
