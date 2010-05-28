/**
 * 
 */
package net.sf.taverna.t2.workbench.file.impl;

import net.sf.taverna.t2.workbench.file.FileType;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdesktop.swingworker.SwingWorkerCompletionWaiter;

/**
 * @author alanrw
 *
 */
public class OpenDataflowRunnable implements Runnable {
	
	private final FileManagerImpl fileManager;
	private final FileType fileType;
	private final Object source;
	
	private Dataflow dataflow;

	public OpenDataflowRunnable (FileManagerImpl fileManager, FileType fileType, Object source) {
		this.fileManager = fileManager;
		this.fileType = fileType;
		this.source = source;				
	}
	
	public void run() {
		OpenDataflowSwingWorker openDataflowSwingWorker = new OpenDataflowSwingWorker(fileType, source, fileManager);
		OpenDataflowInProgressDialog dialog = new OpenDataflowInProgressDialog();
		openDataflowSwingWorker.addPropertyChangeListener(
			     new SwingWorkerCompletionWaiter(dialog));
		openDataflowSwingWorker.execute();
		
		// Give a chance to the SwingWorker to finish so we do not have to display 
		// the dialog
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// do nothing
		}
		if (!openDataflowSwingWorker.isDone()){
			dialog.setVisible(true); // this will block the GUI
		}
		dataflow = openDataflowSwingWorker.getDataflow();
		}

public Dataflow getDataflow() {
	return dataflow;
}

}
