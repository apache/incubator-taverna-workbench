package org.apache.taverna.reference.ui;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

public class CopyWorkflowSwingWorker extends
		SwingWorker<WorkflowBundle, String> {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger
			.getLogger(CopyWorkflowSwingWorker.class);

	private WorkflowBundle dataflowOriginal;

	public CopyWorkflowSwingWorker(WorkflowBundle dataflowOriginal) {
		this.dataflowOriginal = dataflowOriginal;
	}

	@Override
	protected WorkflowBundle doInBackground() throws Exception {
		@SuppressWarnings("unused")
		WorkflowBundle dataflowCopy = null;
		return dataflowOriginal;
//		logger.info("CopyWorkflowSwingWorker: copying of the workflow started.");
//		dataflowCopy = WorkflowBundle.cloneWorkflowBean(dataflowOriginal);
//		logger.info("CopyWorkflowSwingWorker: copying of the workflow finished.");
//		return dataflowCopy;
	}
}
