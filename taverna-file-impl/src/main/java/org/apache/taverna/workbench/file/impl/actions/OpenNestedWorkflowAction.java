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

package org.apache.taverna.workbench.file.impl.actions;

import java.awt.Component;
import java.io.File;

import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.FileType;
import org.apache.taverna.workbench.file.exceptions.OpenException;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * An action for opening a nested workflow from a file.
 * 
 * @author Alex Nenadic
 */
public class OpenNestedWorkflowAction extends OpenWorkflowAction {
	private static final long serialVersionUID = -5398423684000142379L;
	private static Logger logger = Logger
			.getLogger(OpenNestedWorkflowAction.class);

	public OpenNestedWorkflowAction(FileManager fileManager) {
		super(fileManager);
	}

	/**
	 * Opens a nested workflow from a file (should be one file even though the
	 * method takes a list of files - this is because it overrides the
	 * {@link OpenWorkflowAction#openWorkflows(Component, File[], FileType, OpenCallback)
	 * openWorkflows(...)} method).
	 */
	@Override
	public void openWorkflows(final Component parentComponent, File[] files,
			FileType fileType, OpenCallback openCallback) {
		ErrorLoggingOpenCallbackWrapper callback = new ErrorLoggingOpenCallbackWrapper(
				openCallback);
		for (File file : files)
			try {
				callback.aboutToOpenDataflow(file);
				WorkflowBundle workflowBundle = fileManager.openDataflow(
						fileType, file);
				callback.openedDataflow(file, workflowBundle);
			} catch (final RuntimeException ex) {
				logger.warn("Could not open workflow from " + file, ex);
				if (!callback.couldNotOpenDataflow(file, ex))
					showErrorMessage(parentComponent, file, ex);
			} catch (final OpenException ex) {
				logger.warn("Could not open workflow from " + file, ex);
				if (!callback.couldNotOpenDataflow(file, ex))
					showErrorMessage(parentComponent, file, ex);
				return;
			}
	}
}
