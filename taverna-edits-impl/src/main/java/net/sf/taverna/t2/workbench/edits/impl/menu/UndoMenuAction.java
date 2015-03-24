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
package net.sf.taverna.t2.workbench.edits.impl.menu;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.workbench.edits.impl.menu.UndoMenuSection.UNDO_SECTION_URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;

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
