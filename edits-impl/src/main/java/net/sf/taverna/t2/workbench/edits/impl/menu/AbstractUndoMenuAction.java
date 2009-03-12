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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.AbstractDataflowEditEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.WorkflowPerspective;
import net.sf.taverna.t2.workflowmodel.Dataflow;

/**
 * A AbstractMenuAction used as a superclass for {@link UndoMenuAction} and
 * {@link RedoMenuAction}.
 * <p>
 * The action {@link Observer observes} the {@link EditManager} and
 * {@link ModelMap} to enable/disable the action depending on if undo/redo is
 * possible or not.
 * 
 * @author Stian Soiland-Reyes
 */
public abstract class AbstractUndoMenuAction extends AbstractMenuAction {

	public AbstractUndoMenuAction(int position) {
		super(UndoMenuSection.UNDO_SECTION_URI, position);
	}

	protected abstract class AbstractUndoAction extends AbstractAction {

		EditManager editManager = EditManager.getInstance();

		ModelMap modelMap = ModelMap.getInstance();

		public AbstractUndoAction(String label) {
			super(label);
			if (label.equals("Undo")){
				this.putValue(Action.SMALL_ICON, WorkbenchIcons.undoIcon);
				this.putValue(Action.SHORT_DESCRIPTION, "Undo an action");
			}
			else if (label.equals("Redo")){
				this.putValue(Action.SMALL_ICON, WorkbenchIcons.redoIcon);
				this.putValue(Action.SHORT_DESCRIPTION, "Redo an action");
			}
			editManager.addObserver(new EditManagerObserver());
			modelMap.addObserver(new ModelMapObserver());
			updateStatus();
		}

		/**
		 * {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent e) {
			Dataflow dataflow = getCurrentDataflow();
			if (dataflow != null) {
				performUndoOrRedo(dataflow);
			}
		}

		/**
		 * Check if action should be enabled or disabled and update its status.
		 */
		public void updateStatus() {
			Dataflow dataflow = getCurrentDataflow();
			if (dataflow == null) {
				setEnabled(false);
			}
			setEnabled(isActive(dataflow));
		}

		/**
		 * Retrieve the current dataflow from the {@link ModelMap}, or
		 * <code>null</code> if no workflow is active.
		 * 
		 * @return The current {@link Dataflow}
		 */
		protected Dataflow getCurrentDataflow() {
			return (Dataflow) modelMap
					.getModel(ModelMapConstants.CURRENT_DATAFLOW);
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
		protected abstract boolean isActive(Dataflow dataflow);

		/**
		 * Called by {@link #actionPerformed(ActionEvent)} when the current
		 * dataflow is not <code>null</code>.
		 * 
		 * @param dataflow
		 *            {@link Dataflow} on which to undo or redo
		 */
		protected abstract void performUndoOrRedo(Dataflow dataflow);

		/**
		 * Update the status if there's been an edit done on the current
		 * workflow.
		 * 
		 */
		protected class EditManagerObserver implements
				Observer<EditManagerEvent> {
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

		/**
		 * Update the status when the current dataflow or the current perspective changes.
		 * 
		 */
		protected class ModelMapObserver implements Observer<ModelMapEvent> {
			public void notify(Observable<ModelMapEvent> sender,
					ModelMapEvent message) throws Exception {
				if (message.getModelName()
						.equals(ModelMapConstants.CURRENT_DATAFLOW)) {
					updateStatus();
				}
				else if (message.getModelName().equals(
						ModelMapConstants.CURRENT_PERSPECTIVE)) {
					if (message.getNewModel() instanceof WorkflowPerspective) {
						updateStatus();
					}
					else{
						setEnabled(false);
					}
				}
			}
		}
	}

}
