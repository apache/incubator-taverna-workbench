/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.file.impl.menu;

import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_W;
import static javax.swing.Action.MNEMONIC_KEY;
import static javax.swing.SwingUtilities.invokeLater;
import static net.sf.taverna.t2.ui.menu.DefaultMenuBar.DEFAULT_MENU_BAR;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.AbstractDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;

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
