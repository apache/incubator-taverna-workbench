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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.ui.menu.DefaultMenuBar;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.AbstractDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class WorkflowsMenu extends AbstractMenuCustom {

	private EditManager editManager = EditManager.getInstance();
	private EditManagerObserver editManagerObserver = new EditManagerObserver();
	private static FileManager fileManager = FileManager.getInstance();
	private FileManagerObserver fileManagerObserver = new FileManagerObserver();

	private JMenu workflowsMenu;

	public WorkflowsMenu() {
		super(DefaultMenuBar.DEFAULT_MENU_BAR, 900);
		fileManager.addObserver(fileManagerObserver);
		editManager.addObserver(editManagerObserver);
	}

	@Override
	protected Component createCustomComponent() {
		DummyAction action = new DummyAction("Workflows");
		action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_W);

		workflowsMenu = new JMenu(action);

		updateWorkflowsMenu();
		return workflowsMenu;
	}

	public void updateWorkflowsMenu() {
		SwingUtilities.invokeLater(new Runnable() {
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
		Dataflow currentDataflow = fileManager.getCurrentDataflow();
		for (final Dataflow dataflow : fileManager.getOpenDataflows()) {
		
			
			String name = findWorkflowName(dataflow);
			if (fileManager.isDataflowChanged(dataflow)) {
				name = "*" + name;
			}
			// A counter
			name = ++i + " " + name;

			SwitchWorkflowAction switchWorkflowAction = new SwitchWorkflowAction(
					name, dataflow);
			if (i < 10) {
				switchWorkflowAction.putValue(Action.MNEMONIC_KEY, 
						new Integer(KeyEvent.VK_0 + i));
			}
			
			JRadioButtonMenuItem switchWorkflowMenuItem = new JRadioButtonMenuItem(
					switchWorkflowAction);
			workflowsGroup.add(switchWorkflowMenuItem);
			if (dataflow.equals(currentDataflow)) {
				switchWorkflowMenuItem.setSelected(true);
			}
			workflowsMenu.add(switchWorkflowMenuItem);
		}
		if (i == 0) {
			workflowsMenu.add(new NoWorkflowsOpen());
		}
		workflowsMenu.setEnabled(true);

		workflowsMenu.revalidate();
	}

	public static String findWorkflowName(final Dataflow dataflow) {
		Object source = fileManager.getDataflowSource(dataflow);
		String name = dataflow.getLocalName(); 	// Fallback
		
		if (source instanceof File){
			name = ((File)source).getAbsolutePath();
		} else if (source instanceof URL){
			name = source.toString();
		} else if (source != null) {
			// Check if it has implemented a toString() method
			Method toStringMethod = null;
			Method toStringMethodFromObject = null;
			try {
				toStringMethod = source.getClass().getMethod("toString");
				toStringMethodFromObject = Object.class.getMethod("toString");
			} catch (Exception e) {
				throw new IllegalStateException("Source did not implement Object.toString() " + source);
			}
			if (toStringMethod != toStringMethodFromObject) {
				name = source.toString();
			} 
		}
		return name;
	}

	private final class EditManagerObserver implements
			Observer<EditManagerEvent> {
		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEditEvent) {
				updateWorkflowsMenu();
			}
		}
	}

	private final class FileManagerObserver implements
			Observer<FileManagerEvent> {
		public void notify(Observable<FileManagerEvent> sender,
				FileManagerEvent message) throws Exception {
			if (message instanceof AbstractDataflowEvent) {
				updateWorkflowsMenu();
				// TODO: Don't rebuild whole menu
			}
		}
	}

	private final class NoWorkflowsOpen extends AbstractAction {
		private NoWorkflowsOpen() {
			super("No workflows open");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			// No-op
		}
	}

	private final class SwitchWorkflowAction extends AbstractAction {
		private final Dataflow dataflow;

		private SwitchWorkflowAction(String name, Dataflow dataflow) {
			super(name);
			this.dataflow = dataflow;
		}

		public void actionPerformed(ActionEvent e) {
			fileManager.setCurrentDataflow(dataflow);
		}
	}
}
