/**
 * 
 */
package net.sf.taverna.t2.workbench.report.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdesktop.swingworker.SwingWorkerCompletionWaiter;

/**
 * @author alanrw
 *
 */
public class ReportOnWorkflowAction extends AbstractAction {
	
	private final boolean includeTimeConsuming;
	private final boolean remember;
	private Dataflow specifiedDataflow;

	public ReportOnWorkflowAction(String name, boolean includeTimeConsuming, boolean remember) {
		super(name);
		this.includeTimeConsuming = includeTimeConsuming;
		this.remember = remember;
		this.specifiedDataflow = null;
	}

	public ReportOnWorkflowAction(String name, Dataflow dataflow, boolean includeTimeConsuming, boolean remember) {
		super(name);
		this.specifiedDataflow = dataflow;
		this.includeTimeConsuming = includeTimeConsuming;
		this.remember = remember;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		validateWorkflow();
	}
	
	public void validateWorkflow() {
		Dataflow dataflow;
		if (specifiedDataflow == null) {
			dataflow = FileManager.getInstance().getCurrentDataflow();
		} else {
			dataflow = specifiedDataflow;
		}
		ValidateSwingWorker validateSwingWorker = new ValidateSwingWorker(dataflow, includeTimeConsuming, remember);
		ValidateInProgressDialog dialog = new ValidateInProgressDialog();
		validateSwingWorker.addPropertyChangeListener(
			     new SwingWorkerCompletionWaiter(dialog));
		validateSwingWorker.execute();
		
		// Give a chance to the SwingWorker to finish so we do not have to display 
		// the dialog if copying of the workflow is quick (so it won't flicker on the screen)
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {

		}
		if (!validateSwingWorker.isDone()){
			dialog.setVisible(true); // this will block the GUI
		}
		boolean userCancelled = dialog.hasUserCancelled(); // see if user cancelled the dialog
		
		if (userCancelled) {
			validateSwingWorker.cancel(true);
		}
		
	}

}
