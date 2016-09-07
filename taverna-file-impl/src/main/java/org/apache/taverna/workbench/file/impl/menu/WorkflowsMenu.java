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

package org.apache.taverna.workbench.file.impl.menu;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_W;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.SwingUtilities.invokeLater;
import static org.apache.taverna.ui.menu.DefaultMenuBar.DEFAULT_MENU_BAR;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.ui.menu.AbstractMenuCustom;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.EditManager.AbstractDataflowEditEvent;
import org.apache.taverna.workbench.edits.EditManager.EditManagerEvent;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.AbstractDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

public class WorkflowsMenu extends AbstractMenuCustom {
	private EditManagerObserver editManagerObserver = new EditManagerObserver();
	private FileManager fileManager;
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	private JMenu workflowsMenu;

	public WorkflowsMenu(EditManager editManager, FileManager fileManager) {
		super(DEFAULT_MENU_BAR, 900);
		this.fileManager = fileManager;
		fileManager.addObserver(fileManagerObserver);
		editManager.addObserver(editManagerObserver);
	}

	@Override
	protected Component createCustomComponent() {
		DummyAction action = new DummyAction("Workflows");
		action.putValue(MNEMONIC_KEY, VK_W);

		workflowsMenu = new JMenu(action);

		updateWorkflowsMenu();
		return workflowsMenu;
	}

	public void updateWorkflowsMenu() {
		invokeLater(new Runnable() {
			@Override
			public void run() {
				updateWorkflowsMenuUI();
			}
		});
	}

	protected void updateWorkflowsMenuUI() {
		workflowsMenu.setEnabled(false);
		workflowsMenu.removeAll();
		ButtonGroup workflowsGroup = new ButtonGroup();

		int i = 0;
		WorkflowBundle currentDataflow = fileManager.getCurrentDataflow();
		for (WorkflowBundle workflowBundle : fileManager.getOpenDataflows()) {
			String name = fileManager.getDataflowName(workflowBundle);
			if (fileManager.isDataflowChanged(workflowBundle))
				name = "*" + name;
			// A counter
			name = ++i + " " + name;

			SwitchWorkflowAction switchWorkflowAction = new SwitchWorkflowAction(
					name, workflowBundle);
			if (i < 10)
				switchWorkflowAction.putValue(MNEMONIC_KEY, new Integer(VK_0
						+ i));

			JRadioButtonMenuItem switchWorkflowMenuItem = new JRadioButtonMenuItem(
					switchWorkflowAction);
			workflowsGroup.add(switchWorkflowMenuItem);
			if (workflowBundle.equals(currentDataflow))
				switchWorkflowMenuItem.setSelected(true);
			workflowsMenu.add(switchWorkflowMenuItem);
		}
		if (i == 0)
			workflowsMenu.add(new NoWorkflowsOpen());
		workflowsMenu.setEnabled(true);
		workflowsMenu.revalidate();
	}

	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {
		@Override
		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEditEvent)
				updateWorkflowsMenu();
		}
	}

	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		@Override
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEvent)
				updateWorkflowsMenu();
			// TODO: Don't rebuild whole menu
		}
	}

	@SuppressWarnings("serial")
	private final class NoWorkflowsOpen extends AbstractAction {
		private NoWorkflowsOpen() {
			super("No workflows open");
			setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// No-op
		}
	}

	@SuppressWarnings("serial")
	private final class SwitchWorkflowAction extends AbstractAction {
		private final WorkflowBundle workflowBundle;

		private SwitchWorkflowAction(String name, WorkflowBundle workflowBundle) {
			super(name);
			this.workflowBundle = workflowBundle;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			fileManager.setCurrentDataflow(workflowBundle);
		}
	}
}
