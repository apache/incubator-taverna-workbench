/**
 *
 */
package net.sf.taverna.t2.workbench.file.impl;

import static java.lang.Thread.sleep;
import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.ui.SwingWorkerCompletionWaiter;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * @author alanrw
 */
public class OpenDataflowRunnable implements Runnable {
	private final FileManagerImpl fileManager;
	private final FileType fileType;
	private final Object source;
	private WorkflowBundle dataflow;
	private OpenException e;

	public OpenDataflowRunnable(FileManagerImpl fileManager, FileType fileType,
			Object source) {
		this.fileManager = fileManager;
		this.fileType = fileType;
		this.source = source;
	}

	@Override
	public void run() {
		OpenDataflowSwingWorker openDataflowSwingWorker = new OpenDataflowSwingWorker(
				fileType, source, fileManager);
		OpenDataflowInProgressDialog dialog = new OpenDataflowInProgressDialog();
		openDataflowSwingWorker
				.addPropertyChangeListener(new SwingWorkerCompletionWaiter(
						dialog));
		openDataflowSwingWorker.execute();

		/*
		 * Give a chance to the SwingWorker to finish so we do not have to
		 * display the dialog
		 */
		try {
			sleep(500);
		} catch (InterruptedException e) {
		    this.e = new OpenException("Opening was interrupted");
		}
		if (!openDataflowSwingWorker.isDone())
			dialog.setVisible(true); // this will block the GUI
		boolean userCancelled = dialog.hasUserCancelled(); // see if user cancelled the dialog

		if (userCancelled) {
			// Stop the OpenDataflowSwingWorker if it is still working
			openDataflowSwingWorker.cancel(true);
			dataflow = null;
			this.e = new OpenException("Opening was cancelled");
			// exit
			return;
		}
		dataflow = openDataflowSwingWorker.getDataflow();
		this.e = openDataflowSwingWorker.getException();
	}

	public WorkflowBundle getDataflow() {
		return dataflow;
	}

	public OpenException getException() {
		return this.e;
	}
}
