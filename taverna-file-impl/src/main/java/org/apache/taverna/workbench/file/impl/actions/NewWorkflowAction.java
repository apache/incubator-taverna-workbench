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

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_N;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.newIcon;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;

import org.apache.taverna.workbench.file.FileManager;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class NewWorkflowAction extends AbstractAction {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(NewWorkflowAction.class);
	private static final String NEW_WORKFLOW = "New workflow";
	private FileManager fileManager;

	public NewWorkflowAction(FileManager fileManager) {
		super(NEW_WORKFLOW, newIcon);
		this.fileManager = fileManager;
		putValue(SHORT_DESCRIPTION, NEW_WORKFLOW);
		putValue(MNEMONIC_KEY, KeyEvent.VK_N);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_N, getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		fileManager.newDataflow();
	}
}
