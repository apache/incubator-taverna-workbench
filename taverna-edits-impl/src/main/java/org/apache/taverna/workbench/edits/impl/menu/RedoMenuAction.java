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
package org.apache.taverna.workbench.edits.impl.menu;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.edits.impl.menu.UndoMenuSection.UNDO_SECTION_URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Redo the previous {@link Edit} done on the current workflow using the
 * {@link EditManager}.
 *
 * @author Stian Soiland-Reyes
 */
public class RedoMenuAction extends AbstractMenuAction {
	private static Logger logger = Logger.getLogger(RedoMenuAction.class);
	private final EditManager editManager;
	private SelectionManager selectionManager;
	private AbstractUndoAction undoAction;

	public RedoMenuAction(EditManager editManager) {
		super(UNDO_SECTION_URI, 20);
		this.editManager = editManager;
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		undoAction = new AbstractUndoAction("Redo", editManager) {
			@Override
			protected boolean isActive(WorkflowBundle workflowBundle) {
				return editManager.canRedoDataflowEdit(workflowBundle);
			}

			@Override
			protected void performUndoOrRedo(WorkflowBundle workflowBundle) {
				try {
					editManager.redoDataflowEdit(workflowBundle);
				} catch (EditException | RuntimeException e) {
					logger.warn("Could not redo for " + workflowBundle, e);
					showMessageDialog(null, "Could not redo for workflow "
							+ workflowBundle + ":\n" + e, "Could not redo",
							ERROR_MESSAGE);
				}
			}
		};
		undoAction.setSelectionManager(selectionManager);
		return undoAction;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
		if (undoAction != null)
			undoAction.setSelectionManager(selectionManager);
	}
}
