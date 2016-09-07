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

package org.apache.taverna.workbench.file.impl.actions;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_L;
import static java.awt.event.KeyEvent.VK_W;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.closeAllIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

@SuppressWarnings("serial")
public class CloseAllWorkflowsAction extends AbstractAction {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CloseWorkflowAction.class);
	private static final String CLOSE_ALL_WORKFLOWS = "Close all workflows";
	private FileManager fileManager;
	private CloseWorkflowAction closeWorkflowAction;

	public CloseAllWorkflowsAction(EditManager editManager, FileManager fileManager) {
		super(CLOSE_ALL_WORKFLOWS, closeAllIcon);
		this.fileManager = fileManager;
		closeWorkflowAction = new CloseWorkflowAction(editManager, fileManager);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_W, getDefaultToolkit().getMenuShortcutKeyMask()
						| SHIFT_DOWN_MASK));
		putValue(MNEMONIC_KEY, VK_L);
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Component parentComponent = null;
		if (event.getSource() instanceof Component)
			parentComponent = (Component) event.getSource();
		closeAllWorkflows(parentComponent);
	}

	public boolean closeAllWorkflows(Component parentComponent) {
		// Close in reverse so we can save nested workflows first
		List<WorkflowBundle> workflowBundles = fileManager.getOpenDataflows();

		Collections.reverse(workflowBundles);

		for (WorkflowBundle workflowBundle : workflowBundles) {
			boolean success = closeWorkflowAction.closeWorkflow(
					parentComponent, workflowBundle);
			if (!success)
				return false;
		}
		return true;
	}
}
