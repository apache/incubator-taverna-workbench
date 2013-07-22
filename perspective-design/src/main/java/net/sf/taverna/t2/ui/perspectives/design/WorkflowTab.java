/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.ui.perspectives.design;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.lang.ui.tabselector.Tab;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.file.events.SavedDataflowEvent;
import net.sf.taverna.t2.workbench.file.exceptions.UnsavedException;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Tab for selecting current workflow.
 *
 * @author David Withers
 */
public class WorkflowTab extends Tab<WorkflowBundle> {

	private static final long serialVersionUID = 1L;

	private final Component component;
	private final SelectionManager selectionManager;
	private final FileManager fileManager;
	private final EditManager editManager;
	private final Action closeMenuAction;

	private boolean saved = true;
	private EditManagerObserver editManagerObserver;
	private FileManagerObserver fileManagerObserver;

	public WorkflowTab(Component component, final WorkflowBundle workflowBundle,
			final SelectionManager selectionManager, final FileManager fileManager,
			EditManager editManager, Action closeMenuAction) {
		super(workflowBundle.getMainWorkflow().getName(), workflowBundle);
		this.component = component;
		this.selectionManager = selectionManager;
		this.fileManager = fileManager;
		this.editManager = editManager;
		this.closeMenuAction = closeMenuAction;
		editManagerObserver = new EditManagerObserver();
		fileManagerObserver = new FileManagerObserver();
		editManager.addObserver(editManagerObserver);
		fileManager.addObserver(fileManagerObserver);
	}

	protected void clickTabAction() {
		selectionManager.setSelectedWorkflowBundle(selection);
	}

	protected void closeTabAction() {
		if (!saved && closeMenuAction != null) {
			selectionManager.setSelectedWorkflowBundle(selection);
			closeMenuAction.actionPerformed(new ActionEvent(component, 0, ""));
		} else {
			try {
				fileManager.closeDataflow(selection, false);
			} catch (UnsavedException e) {
			}
		}
	}

	private class EditManagerObserver extends SwingAwareObserver<EditManagerEvent> {
		@Override
		public void notifySwing(Observable<EditManagerEvent> sender,
				EditManagerEvent message) {
			if (message instanceof AbstractDataflowEditEvent) {
				AbstractDataflowEditEvent event = (AbstractDataflowEditEvent) message;
				if (event.getDataFlow() == selection) {
					setSaved(false);
				}
			}
		}
	}

	private class FileManagerObserver extends SwingAwareObserver<FileManagerEvent> {
		public void notifySwing(Observable<FileManagerEvent> sender, FileManagerEvent message) {
			if (message instanceof ClosedDataflowEvent) {
				ClosedDataflowEvent event = (ClosedDataflowEvent) message;
				if (event.getDataflow() == selection) {
					fileManager.removeObserver(fileManagerObserver);
					editManager.removeObserver(editManagerObserver);
				}
			} else if (message instanceof SavedDataflowEvent) {
				SavedDataflowEvent event = (SavedDataflowEvent) message;
				if (event.getDataflow() == selection) {
					setSaved(true);
				}
			}
		}
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
		String name = getName();
		if (saved && name.startsWith("*")) {
			setName(name.substring(1));
		} else if (!saved && !name.startsWith("*")) {
			setName("*" + name);
		}
	}

}
