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
/*

package org.apache.taverna.workbench.run.actions;

import static java.awt.event.KeyEvent.VK_V;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.searchIcon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.ui.Workbench;

@SuppressWarnings("serial")
public class ValidateWorkflowAction extends AbstractAction {
	private static final String VALIDATE_WORKFLOW = "Validate workflow";

	protected Action subAction;

	public ValidateWorkflowAction(EditManager editManager,
			FileManager fileManager, ReportManager reportManager,
			Workbench workbench) {
		super(VALIDATE_WORKFLOW, searchIcon);
		putValue(MNEMONIC_KEY, VK_V);
		// subAction = new ReportOnWorkflowAction("", true, false, editManager,
		// fileManager, reportManager, workbench);
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if (subAction != null)
			subAction.actionPerformed(ev);
	}
}
