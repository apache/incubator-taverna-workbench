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
package org.apache.taverna.workbench.file.impl.actions;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_W;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.closeIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.exceptions.UnsavedException;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

@SuppressWarnings("serial")
public class CloseWorkflowAction extends AbstractAction {
	private static Logger logger = Logger.getLogger(CloseWorkflowAction.class);
	private static final String CLOSE_WORKFLOW = "Close workflow";
	private final SaveWorkflowAction saveWorkflowAction;
	private FileManager fileManager;

	public CloseWorkflowAction(EditManager editManager, FileManager fileManager) {
		super(CLOSE_WORKFLOW, closeIcon);
		this.fileManager = fileManager;
		saveWorkflowAction = new SaveWorkflowAction(editManager, fileManager);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_W, getDefaultToolkit().getMenuShortcutKeyMask()));
		putValue(MNEMONIC_KEY, VK_C);

	}
	@Override
	public void actionPerformed(ActionEvent e) {
		Component parentComponent = null;
		if (e.getSource() instanceof Component)
			parentComponent = (Component) e.getSource();
		closeWorkflow(parentComponent, fileManager.getCurrentDataflow());
	}

	public boolean closeWorkflow(Component parentComponent, WorkflowBundle workflowBundle) {
		if (workflowBundle == null) {
			logger.warn("Attempted to close a null workflow");
			return false;
		}

		try {
			return fileManager.closeDataflow(workflowBundle, true);
		} catch (UnsavedException e1) {
			fileManager.setCurrentDataflow(workflowBundle);
			String msg = "Do you want to save changes before closing the workflow "
					+ fileManager.getDataflowName(workflowBundle) + "?";
			switch (showConfirmDialog(parentComponent, msg, "Save workflow?",
					YES_NO_CANCEL_OPTION)) {
			case NO_OPTION:
				try {
					fileManager.closeDataflow(workflowBundle, false);
					return true;
				} catch (UnsavedException e2) {
					logger.error("Unexpected UnsavedException while "
							+ "closing workflow", e2);
					return false;
				}
			case YES_OPTION:
				boolean saved = saveWorkflowAction.saveDataflow(
						parentComponent, workflowBundle);
				if (!saved)
					return false;
				return closeWorkflow(parentComponent, workflowBundle);
			case CANCEL_OPTION:
			default:
				return false;
			}
		}
	}
}
