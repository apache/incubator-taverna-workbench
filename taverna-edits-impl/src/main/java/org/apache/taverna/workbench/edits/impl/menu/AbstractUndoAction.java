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

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_Y;
import static java.awt.event.KeyEvent.VK_Z;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.redoIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.undoIcon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.EditManager.AbstractDataflowEditEvent;
import org.apache.taverna.workbench.edits.EditManager.EditManagerEvent;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.PerspectiveSelectionEvent;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.selection.events.WorkflowBundleSelectionEvent;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public abstract class AbstractUndoAction extends AbstractAction {
	protected EditManager editManager;
	private SelectionManager selectionManager;

	public AbstractUndoAction(String label, EditManager editManager) {
		super(label);
		this.editManager = editManager;
		if (label.equals("Undo")) {
			this.putValue(SMALL_ICON, undoIcon);
			this.putValue(SHORT_DESCRIPTION, "Undo an action");
			putValue(
					ACCELERATOR_KEY,
					getKeyStroke(VK_Z, getDefaultToolkit()
							.getMenuShortcutKeyMask()));
		} else if (label.equals("Redo")) {
			this.putValue(SMALL_ICON, redoIcon);
			this.putValue(SHORT_DESCRIPTION, "Redo an action");
			putValue(
					ACCELERATOR_KEY,
					getKeyStroke(VK_Y, getDefaultToolkit()
							.getMenuShortcutKeyMask()));
		}
		editManager.addObserver(new EditManagerObserver());
		updateStatus();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		WorkflowBundle workflowBundle = getCurrentDataflow();
		if (workflowBundle != null)
			performUndoOrRedo(workflowBundle);
	}

	/**
	 * Check if action should be enabled or disabled and update its status.
	 */
	public void updateStatus() {
		WorkflowBundle workflowBundle = getCurrentDataflow();
		if (workflowBundle == null)
			setEnabled(false);
		setEnabled(isActive(workflowBundle));
	}

	/**
	 * Retrieve the current dataflow from the {@link ModelMap}, or
	 * <code>null</code> if no workflow is active.
	 * 
	 * @return The current {@link Dataflow}
	 */
	protected WorkflowBundle getCurrentDataflow() {
		if (selectionManager == null)
			return null;
		return selectionManager.getSelectedWorkflowBundle();
	}

	/**
	 * Return <code>true</code> if the action should be enabled when the given
	 * {@link Dataflow} is the current, ie. if it's undoable or redoable.
	 * 
	 * @param dataflow
	 *            Current {@link Dataflow}
	 * @return <code>true</code> if the action should be enabled.
	 */
	protected abstract boolean isActive(WorkflowBundle workflowBundle);

	/**
	 * Called by {@link #actionPerformed(ActionEvent)} when the current dataflow
	 * is not <code>null</code>.
	 * 
	 * @param dataflow
	 *            {@link Dataflow} on which to undo or redo
	 */
	protected abstract void performUndoOrRedo(WorkflowBundle workflowBundle);

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
		if (selectionManager != null)
			selectionManager.addObserver(new SelectionManagerObserver());
	}

	/**
	 * Update the status if there's been an edit done on the current workflow.
	 * 
	 */
	protected class EditManagerObserver implements Observer<EditManagerEvent> {
		@Override
		public void notify(Observable<EditManagerEvent> sender,
				EditManagerEvent message) throws Exception {
			if (!(message instanceof AbstractDataflowEditEvent))
				return;
			AbstractDataflowEditEvent dataflowEdit = (AbstractDataflowEditEvent) message;
			if (dataflowEdit.getDataFlow().equals(dataflowEdit.getDataFlow()))
				// It's an edit that could effect our undoability
				updateStatus();
		}
	}

	private final class SelectionManagerObserver extends
			SwingAwareObserver<SelectionManagerEvent> {
		private static final String DESIGN_PERSPECTIVE_ID = "net.sf.taverna.t2.ui.perspectives.design.DesignPerspective";

		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof WorkflowBundleSelectionEvent)
				updateStatus();
			else if (message instanceof PerspectiveSelectionEvent) {
				PerspectiveSelectionEvent perspectiveSelectionEvent = (PerspectiveSelectionEvent) message;
				if (DESIGN_PERSPECTIVE_ID.equals(perspectiveSelectionEvent
						.getSelectedPerspective().getID()))
					updateStatus();
				else
					setEnabled(false);
			}
		}
	}
}
