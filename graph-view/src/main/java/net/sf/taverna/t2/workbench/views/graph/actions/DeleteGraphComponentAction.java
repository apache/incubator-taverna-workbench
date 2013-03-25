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
package net.sf.taverna.t2.workbench.views.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.ui.menu.DesignOnlyAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveConditionAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDatalinkAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveProcessorAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.ControlLink;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;

/**
 * An action that deletes the selected graph component.
 *
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class DeleteGraphComponentAction extends AbstractAction implements DesignOnlyAction {

	/* Current workflow's selection model event observer.*/
	private Observer<DataflowSelectionMessage> workflowSelectionObserver = new DataflowSelectionObserver();

	private final EditManager editManager;
	private final SelectionManager selectionManager;

	public DeleteGraphComponentAction(EditManager editManager, final SelectionManager selectionManager) {
		super();
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
		putValue(NAME, "Delete");
		putValue(SHORT_DESCRIPTION, "Delete selected component");
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		setEnabled(false);

		selectionManager.addObserver(new SelectionManagerObserver());
	}

	public void actionPerformed(ActionEvent e) {
		WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();
		DataflowSelectionModel dataFlowSelectionModel = selectionManager.getDataflowSelectionModel(workflowBundle);
		// Get all selected components
		Set<Object> selectedWFComponents = dataFlowSelectionModel.getSelection();
		for (Object selectedWFComponent : selectedWFComponents) {
			if (selectedWFComponent instanceof Processor) {
				Processor processor = (Processor) selectedWFComponent;
				new RemoveProcessorAction(processor.getParent(),
						processor, null, editManager, selectionManager)
						.actionPerformed(e);
			}
			else if (selectedWFComponent instanceof DataLink) {
				DataLink dataLink = (DataLink) selectedWFComponent;
				new RemoveDatalinkAction(dataLink.getParent(),
						dataLink, null, editManager, selectionManager)
						.actionPerformed(e);
			}
			else if (selectedWFComponent instanceof InputWorkflowPort) {
				InputWorkflowPort port = (InputWorkflowPort) selectedWFComponent;
				new RemoveDataflowInputPortAction(port.getParent(),
						port, null, editManager, selectionManager)
						.actionPerformed(e);
			}
			else if (selectedWFComponent instanceof OutputWorkflowPort) {
				OutputWorkflowPort port = (OutputWorkflowPort) selectedWFComponent;
				new RemoveDataflowOutputPortAction(port.getParent(),
						port, null, editManager, selectionManager)
						.actionPerformed(e);
			}
			else if (selectedWFComponent instanceof ControlLink) {
				ControlLink controlLink = (ControlLink) selectedWFComponent;
				new RemoveConditionAction(controlLink.getParent(),
						controlLink, null, editManager, selectionManager)
						.actionPerformed(e);
			}
		}
	}

	/**
	 * Check if action should be enabled or disabled and update its status.
	 */
	public void updateStatus() {
		WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();
		DataflowSelectionModel selectionModel = selectionManager.getDataflowSelectionModel(workflowBundle);

		// List of all selected objects in the graph view
		Set<Object> selection = selectionModel.getSelection();

		if (selection.isEmpty()){
			setEnabled(false);
		}
		else{
			// Take the first selected item - we only support single selections anyway
			Object selected = selection.toArray()[0];
			if ((selected instanceof Processor) ||
					(selected instanceof InputWorkflowPort) ||
					(selected instanceof OutputWorkflowPort) ||
					(selected instanceof DataLink) ||
					(selected instanceof ControlLink)){
				setEnabled(true);
			}
			else{
				setEnabled(false);
			}
		}
	}

	/**
	 * Observes events on workflow Selection Manager, i.e. when a workflow
	 * node is selected in the graph view, and enables/disables this action accordingly.
	 */
	private final class DataflowSelectionObserver extends SwingAwareObserver<DataflowSelectionMessage> {
		@Override
		public void notifySwing(Observable<DataflowSelectionMessage> sender, DataflowSelectionMessage message) {
			updateStatus();
		}
	}

	private final class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender, SelectionManagerEvent message) {
			if (message instanceof WorkflowBundleSelectionEvent) {
				WorkflowBundleSelectionEvent workflowBundleSelectionEvent = (WorkflowBundleSelectionEvent) message;
				WorkflowBundle oldFlow = workflowBundleSelectionEvent.getPreviouslySelectedWorkflowBundle();
				WorkflowBundle newFlow = workflowBundleSelectionEvent.getSelectedWorkflowBundle();
				// Update the buttons status as current dataflow has changed
				updateStatus();

				// Remove the workflow selection model listener from the previous (if any)
				// and add to the new workflow (if any)
				if (oldFlow != null) {
					selectionManager.getDataflowSelectionModel(oldFlow).removeObserver(workflowSelectionObserver);
				}

				if (newFlow != null) {
					selectionManager.getDataflowSelectionModel(newFlow).addObserver(workflowSelectionObserver);
				}
			}
		}
	}

}
