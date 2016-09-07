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

package org.apache.taverna.ui.perspectives.design;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.lang.ui.tabselector.Tab;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.EditManager.AbstractDataflowEditEvent;
import org.apache.taverna.workbench.edits.EditManager.EditManagerEvent;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.events.ClosedDataflowEvent;
import org.apache.taverna.workbench.file.events.FileManagerEvent;
import org.apache.taverna.workbench.file.events.SavedDataflowEvent;
import org.apache.taverna.workbench.file.exceptions.UnsavedException;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Tab for selecting current workflow.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class WorkflowTab extends Tab<WorkflowBundle> {
	private static final String SAVED_MARKER = "*";

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

	@Override
	protected void clickTabAction() {
		selectionManager.setSelectedWorkflowBundle(selection);
	}

	@Override
	protected void closeTabAction() {
		if (!saved && closeMenuAction != null) {
			selectionManager.setSelectedWorkflowBundle(selection);
			closeMenuAction.actionPerformed(new ActionEvent(component, 0, ""));
		} else
			try {
				fileManager.closeDataflow(selection, false);
			} catch (UnsavedException e) {
			}
	}

	private class EditManagerObserver extends
			SwingAwareObserver<EditManagerEvent> {
		@Override
		public void notifySwing(Observable<EditManagerEvent> sender,
				EditManagerEvent message) {
			if (message instanceof AbstractDataflowEditEvent) {
				AbstractDataflowEditEvent event = (AbstractDataflowEditEvent) message;
				if (event.getDataFlow() == selection)
					setSaved(false);
			}
		}
	}

	private class FileManagerObserver extends
			SwingAwareObserver<FileManagerEvent> {
		@Override
		public void notifySwing(Observable<FileManagerEvent> sender,
				FileManagerEvent message) {
			if (message instanceof ClosedDataflowEvent) {
				ClosedDataflowEvent event = (ClosedDataflowEvent) message;
				if (event.getDataflow() == selection) {
					fileManager.removeObserver(fileManagerObserver);
					editManager.removeObserver(editManagerObserver);
				}
			} else if (message instanceof SavedDataflowEvent) {
				SavedDataflowEvent event = (SavedDataflowEvent) message;
				if (event.getDataflow() == selection)
					setSaved(true);
			}
		}
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
		String name = getName();
		if (saved && name.startsWith(SAVED_MARKER))
			setName(name.substring(SAVED_MARKER.length()));
		else if (!saved && !name.startsWith(SAVED_MARKER))
			setName(SAVED_MARKER + name);
	}
}
