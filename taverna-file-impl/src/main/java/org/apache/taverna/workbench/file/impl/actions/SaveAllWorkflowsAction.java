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
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_S;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.saveAllIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.FileManagerEvent;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

@SuppressWarnings("serial")
public class SaveAllWorkflowsAction extends AbstractAction {
	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		@Override
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			updateEnabled();
		}
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(SaveAllWorkflowsAction.class);
	private static final String SAVE_ALL_WORKFLOWS = "Save all workflows";

	private final SaveWorkflowAction saveWorkflowAction;
	private FileManager fileManager;
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	public SaveAllWorkflowsAction(EditManager editManager,
			FileManager fileManager) {
		super(SAVE_ALL_WORKFLOWS, saveAllIcon);
		this.fileManager = fileManager;
		saveWorkflowAction = new SaveWorkflowAction(editManager, fileManager);
		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_S, getDefaultToolkit().getMenuShortcutKeyMask()
						| SHIFT_DOWN_MASK));
		putValue(MNEMONIC_KEY, VK_A);

		fileManager.addObserver(fileManagerObserver);
		updateEnabled();
	}

	public void updateEnabled() {
		setEnabled(!(fileManager.getOpenDataflows().isEmpty()));
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		Component parentComponent = null;
		if (ev.getSource() instanceof Component)
			parentComponent = (Component) ev.getSource();
		saveAllDataflows(parentComponent);
	}

	public void saveAllDataflows(Component parentComponent) {
		// Save in reverse so we save nested workflows first
		List<WorkflowBundle> workflowBundles = fileManager.getOpenDataflows();
		Collections.reverse(workflowBundles);

		for (WorkflowBundle workflowBundle : workflowBundles)
			if (!saveWorkflowAction.saveDataflow(parentComponent,
					workflowBundle))
				break;
	}
}
