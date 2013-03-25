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
package net.sf.taverna.t2.workbench.edits.impl.menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.PerspectiveSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.ui.zaria.WorkflowPerspective;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;

/**
 *
 *
 * @author David Withers
 */
public abstract class AbstractUndoAction extends AbstractAction {

	protected EditManager editManager;
	private SelectionManager selectionManager;

	public AbstractUndoAction(String label, EditManager editManager) {
		super(label);
		this.editManager = editManager;
		if (label.equals("Undo")){
			this.putValue(Action.SMALL_ICON, WorkbenchIcons.undoIcon);
			this.putValue(Action.SHORT_DESCRIPTION, "Undo an action");
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_Z,
							Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		else if (label.equals("Redo")){
			this.putValue(Action.SMALL_ICON, WorkbenchIcons.redoIcon);
			this.putValue(Action.SHORT_DESCRIPTION, "Redo an action");
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_Y,
							Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}
		editManager.addObserver(new EditManagerObserver());
		updateStatus();
	}

	/**
	 * {@inheritDoc}
	 */
	public void actionPerformed(ActionEvent e) {
		WorkflowBundle workflowBundle = getCurrentDataflow();
		if (workflowBundle != null) {
			performUndoOrRedo(workflowBundle);
		}
	}

	/**
	 * Check if action should be enabled or disabled and update its status.
	 */
	public void updateStatus() {
		WorkflowBundle workflowBundle = getCurrentDataflow();
		if (workflowBundle == null) {
			setEnabled(false);
		}
		setEnabled(isActive(workflowBundle));
	}

	/**
	 * Retrieve the current dataflow from the {@link ModelMap}, or
	 * <code>null</code> if no workflow is active.
	 *
	 * @return The current {@link Dataflow}
	 */
	protected WorkflowBundle getCurrentDataflow() {
		if (selectionManager == null) {
			return null;
		}
		return selectionManager.getSelectedWorkflowBundle();
	}

	/**
	 * Return <code>true</code> if the action should be enabled when the
	 * given {@link Dataflow} is the current, ie. if it's undoable or
	 * redoable.
	 *
	 * @param dataflow
	 *            Current {@link Dataflow}
	 * @return <code>true</code> if the action should be enabled.
	 */
	protected abstract boolean isActive(WorkflowBundle workflowBundle);

	/**
	 * Called by {@link #actionPerformed(ActionEvent)} when the current
	 * dataflow is not <code>null</code>.
	 *
	 * @param dataflow
	 *            {@link Dataflow} on which to undo or redo
	 */
	protected abstract void performUndoOrRedo(WorkflowBundle workflowBundle);

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
		if (selectionManager != null) {
			selectionManager.addObserver(new SelectionManagerObserver());
		}
	}

	/**
	 * Update the status if there's been an edit done on the current
	 * workflow.
	 *
	 */
	protected class EditManagerObserver implements Observer<EditManagerEvent> {
		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			if (!(message instanceof AbstractDataflowEditEvent)) {
				return;
			}
			AbstractDataflowEditEvent dataflowEdit = (AbstractDataflowEditEvent) message;
			if (dataflowEdit.getDataFlow().equals(
					dataflowEdit.getDataFlow())) {
				// It's an edit that could effect our undoability
				updateStatus();
			}
		}
	}

	private final class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {

		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender, SelectionManagerEvent message) {
			if (message instanceof WorkflowBundleSelectionEvent) {
				updateStatus();
			} else if (message instanceof PerspectiveSelectionEvent) {
				PerspectiveSelectionEvent perspectiveSelectionEvent = (PerspectiveSelectionEvent) message;
				if (perspectiveSelectionEvent.getSelectedPerspective() instanceof WorkflowPerspective) {
					updateStatus();
				} else{
					setEnabled(false);
				}
			}
		}
	}

}
