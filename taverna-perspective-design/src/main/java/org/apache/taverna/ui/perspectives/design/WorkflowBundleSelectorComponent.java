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
package org.apache.taverna.ui.perspectives.design;

import java.awt.Component;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.lang.ui.tabselector.Tab;
import org.apache.taverna.lang.ui.tabselector.TabSelectorComponent;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.ClosedDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.selection.events.WorkflowBundleSelectionEvent;
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
