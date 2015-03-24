/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
import java.net.URI;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.lang.ui.tabselector.Tab;
import net.sf.taverna.t2.lang.ui.tabselector.TabSelectorComponent;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.events.ClosedDataflowEvent;
import net.sf.taverna.t2.workbench.file.events.FileManagerEvent;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Component for managing selection of workflow bundles.
 *
 * @author David Withers
 */
public class WorkflowBundleSelectorComponent extends TabSelectorComponent<WorkflowBundle> {
	private static final long serialVersionUID = 7291973052895544750L;
	private static final URI FILE_CLOSE_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#fileClose");

	private final SelectionManager selectionManager;
	private final FileManager fileManager;
	private final EditManager editManager;
	private final MenuManager menuManager;

	private Action closeMenuAction;

	public WorkflowBundleSelectorComponent(SelectionManager selectionManager,
			FileManager fileManager, MenuManager menuManager,
			EditManager editManager) {
		this.selectionManager = selectionManager;
		this.fileManager = fileManager;
		this.menuManager = menuManager;
		this.editManager = editManager;
		fileManager.addObserver(new FileManagerObserver());
		selectionManager.addObserver(new SelectionManagerObserver());
	}

	private class FileManagerObserver extends
			SwingAwareObserver<FileManagerEvent> {
		@Override
		public void notifySwing(Observable<FileManagerEvent> sender,
				FileManagerEvent message) {
			if (message instanceof ClosedDataflowEvent) {
				ClosedDataflowEvent event = (ClosedDataflowEvent) message;
				removeObject(event.getDataflow());
			}
		}
	}

	private class SelectionManagerObserver extends
			SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof WorkflowBundleSelectionEvent) {
				WorkflowBundleSelectionEvent workflowBundleSelectionEvent = (WorkflowBundleSelectionEvent) message;
				WorkflowBundle workflowBundle = workflowBundleSelectionEvent
						.getSelectedWorkflowBundle();
				selectObject(workflowBundle);
			}
		}
	}

	@Override
	protected Tab<WorkflowBundle> createTab(WorkflowBundle workflowBundle) {
		return new WorkflowTab(this, workflowBundle, selectionManager,
				fileManager, editManager, getCloseMenuAction());
	}

	private Action getCloseMenuAction() {
		if (closeMenuAction == null) {
			Component component = menuManager.getComponentByURI(FILE_CLOSE_URI);
			if (component instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem) component;
				closeMenuAction = menuItem.getAction();
			}
		}
		return closeMenuAction;
	}
}
