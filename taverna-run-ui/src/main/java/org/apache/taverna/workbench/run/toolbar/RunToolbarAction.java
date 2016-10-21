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
package org.apache.taverna.workbench.run.toolbar;

import static org.apache.taverna.workbench.run.toolbar.RunToolbarSection.RUN_TOOLBAR_SECTION;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.run.actions.RunWorkflowAction;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.Workbench;
import org.apache.taverna.platform.run.api.RunService;

public class RunToolbarAction extends AbstractMenuAction {
	private static final URI RUN_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#runToolbarRun");

	private EditManager editManager;
	private FileManager fileManager;
	private ReportManager reportManager;
	private Workbench workbench;
	private RunService runService;
	private SelectionManager selectionManager;

	public RunToolbarAction() {
		super(RUN_TOOLBAR_SECTION, 10, RUN_URI);
	}

	@Override
	protected Action createAction() {
		return new RunWorkflowAction(editManager, fileManager, reportManager,
				workbench, runService, selectionManager);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

	public void setRunService(RunService runService) {
		this.runService = runService;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}
}
