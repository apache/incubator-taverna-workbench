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

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class SaveAllWorkflowsAction extends AbstractAction {

	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			updateEnabled();
		}
	}

	private final SaveWorkflowAction saveWorkflowAction = new SaveWorkflowAction();

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(SaveAllWorkflowsAction.class);

	private static final String SAVE_ALL_WORKFLOWS = "Save all workflows";

	private FileManager fileManager = FileManager.getInstance();
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	public SaveAllWorkflowsAction() {
		super(SAVE_ALL_WORKFLOWS, WorkbenchIcons.saveAllIcon);
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
						| InputEvent.SHIFT_DOWN_MASK));
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);

		fileManager.addObserver(fileManagerObserver);
		updateEnabled();
	}

	public void updateEnabled() {
		setEnabled(!(fileManager.getOpenDataflows().isEmpty()));
	}

	public void actionPerformed(ActionEvent ev) {
		Component parentComponent = null;
		if (ev.getSource() instanceof Component) {
			parentComponent = (Component) ev.getSource();
		}
		saveAllDataflows(parentComponent);
	}

	public void saveAllDataflows(Component parentComponent) {
		// Save in reverse so we save nested workflows first
		List<Dataflow> dataflows = fileManager.getOpenDataflows();
		Collections.reverse(dataflows);

		for (Dataflow dataflow : dataflows) {
			boolean success = saveWorkflowAction.saveDataflow(parentComponent,
					dataflow);
			if (!success) {
				break;
			}
		}
	}

}
