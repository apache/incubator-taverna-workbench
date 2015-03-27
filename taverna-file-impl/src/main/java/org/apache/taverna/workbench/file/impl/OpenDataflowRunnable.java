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

package org.apache.taverna.workbench.file.impl;

import static java.lang.Thread.sleep;
import org.apache.taverna.workbench.file.FileType;
import org.apache.taverna.workbench.file.exceptions.OpenException;
import org.apache.taverna.workbench.ui.SwingWorkerCompletionWaiter;
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
