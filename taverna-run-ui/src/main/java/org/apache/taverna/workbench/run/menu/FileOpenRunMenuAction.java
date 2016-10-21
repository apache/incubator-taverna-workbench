/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.workbench.run.menu;

import static org.apache.taverna.workbench.run.menu.FileRunMenuSection.FILE_RUN_SECTION_URI;

import java.io.File;
import java.net.URI;

import javax.swing.Action;
import org.apache.taverna.configuration.app.ApplicationConfiguration;

import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.run.actions.OpenWorkflowRunAction;

public class FileOpenRunMenuAction extends AbstractMenuAction {
	private static final String RUN_STORE_DIRECTORY = "workflow-runs";
	private static final URI FILE_OPEN_RUN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileOpenRun");

	private RunService runService;
	private ApplicationConfiguration applicationConfiguration;

	public FileOpenRunMenuAction() {
		super(FILE_RUN_SECTION_URI, 20, FILE_OPEN_RUN_URI);
	}

	@Override
	protected Action createAction() {
		File runStore = new File(
				applicationConfiguration.getApplicationHomeDir(),
				RUN_STORE_DIRECTORY);
		return new OpenWorkflowRunAction(runService, runStore);
	}

	public void setRunService(RunService runService) {
		this.runService = runService;
	}

	public void setApplicationConfiguration(
			ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}
}
