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

package org.apache.taverna.workbench.edits.impl.menu;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.edits.impl.menu.UndoMenuSection.UNDO_SECTION_URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Undo the last {@link Edit} done on the current workflow using the
 * {@link EditManager}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class UndoMenuAction extends AbstractMenuAction {
	private static Logger logger = Logger.getLogger(UndoMenuAction.class);
	private final EditManager editManager;
	private SelectionManager selectionManager;
	private AbstractUndoAction undoAction;

	public UndoMenuAction(EditManager editManager) {
		super(UNDO_SECTION_URI, 10);
		this.editManager = editManager;
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		undoAction = new AbstractUndoAction("Undo", editManager) {
			@Override
			protected boolean isActive(WorkflowBundle workflowBundle) {
				return editManager.canUndoDataflowEdit(workflowBundle);
			}

			@Override
			protected void performUndoOrRedo(WorkflowBundle workflowBundle) {
				try {
					editManager.undoDataflowEdit(workflowBundle);
				} catch (RuntimeException e) {
					logger.warn("Could not undo for " + workflowBundle, e);
					showMessageDialog(null, "Could not undo for workflow "
							+ workflowBundle + ":\n" + e, "Could not undo",
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
