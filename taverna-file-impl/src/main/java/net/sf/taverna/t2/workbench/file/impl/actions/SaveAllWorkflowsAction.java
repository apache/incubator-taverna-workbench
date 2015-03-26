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
package net.sf.taverna.t2.workbench.file.impl.actions;

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
