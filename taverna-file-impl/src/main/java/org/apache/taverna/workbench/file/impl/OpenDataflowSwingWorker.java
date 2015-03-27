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

import javax.swing.SwingWorker;

import org.apache.taverna.workbench.file.FileType;
import org.apache.taverna.workbench.file.exceptions.OpenException;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

public class OpenDataflowSwingWorker extends
		SwingWorker<WorkflowBundle, Object> {
	@SuppressWarnings("unused")
	private Logger logger = Logger.getLogger(OpenDataflowSwingWorker.class);
	private FileType fileType;
	private Object source;
	private FileManagerImpl fileManagerImpl;
	private WorkflowBundle workflowBundle;
	private OpenException e = null;

	public OpenDataflowSwingWorker(FileType fileType, Object source,
			FileManagerImpl fileManagerImpl) {
		this.fileType = fileType;
		this.source = source;
		this.fileManagerImpl = fileManagerImpl;
	}

	@Override
	protected WorkflowBundle doInBackground() throws Exception {
		try {
			workflowBundle = fileManagerImpl.performOpenDataflow(fileType,
					source);
		} catch (OpenException e) {
			this.e = e;
		}
		return workflowBundle;
	}

	public WorkflowBundle getDataflow() {
		return workflowBundle;
	}

	public OpenException getException() {
		return e;
	}
}
