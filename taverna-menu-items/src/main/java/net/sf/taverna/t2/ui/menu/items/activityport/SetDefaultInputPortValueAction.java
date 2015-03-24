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
package net.sf.taverna.t2.ui.menu.items.activityport;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.selection.events.PerspectiveSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import uk.org.taverna.commons.services.ServiceRegistry;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;

/**
 * An action that sets a default value to a processor's input port, in case
 * the input port is selected on the Graph View.
 *
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class SetDefaultInputPortValueAction extends AbstractAction {

	/* Current workflow's selection model event observer. */
	private Observer<DataflowSelectionMessage> workflowSelectionObserver = new DataflowSelectionObserver();

	private final EditManager editManager;
	private final SelectionManager selectionManager;
	private final ServiceRegistry serviceRegistry;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public SetDefaultInputPortValueAction(EditManager editManager,
			SelectionManager selectionManager, ServiceRegistry serviceRegistry) {
		super();
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		this.serviceRegistry = serviceRegistry;
		putValue(SMALL_ICON, WorkbenchIcons.inputValueIcon);
		putValue(NAME, "Constant value");
		putValue(SHORT_DESCRIPTION, "Add a constant value for an input port");
		setEnabled(false);

		selectionManager.addObserver(new SelectionManagerObserver());

	}

	public void actionPerformed(ActionEvent e) {
		WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();
		DataflowSelectionModel dataFlowSelectionModel = selectionManager
				.getDataflowSelectionModel(workflowBundle);
		// Get selected port
		Set<Object> selectedWFComponents = dataFlowSelectionModel.getSelection();
		if (selectedWFComponents.size() > 1) {
			JOptionPane.showMessageDialog(null,
					"Only one workflow component should be selected for this action.", "Warning",
					JOptionPane.WARNING_MESSAGE);
		} else {
			Object selectedWFComponent = selectedWFComponents.toArray()[0];
			if (selectedWFComponent instanceof InputProcessorPort) {
				new AddInputPortDefaultValueAction(workflowBundle.getMainWorkflow(),
						(InputProcessorPort) selectedWFComponent, null, editManager,
						selectionManager, serviceRegistry).actionPerformed(e);
			}
		}
	}

	/**
	 * Check if action should be enabled or disabled and update its status.
	 */
	public void updateStatus() {
		WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();
		DataflowSelectionModel selectionModel = selectionManager
				.getDataflowSelectionModel(workflowBundle);

		// List of all selected objects in the graph view
		Set<Object> selection = selectionModel.getSelection();

		if (selection.isEmpty()) {
			setEnabled(false);
		} else {
			// Take the first selected item - we only support single selections anyway
			Object selected = selection.toArray()[0];
			if (selected instanceof InputProcessorPort) {
				// If this input port is not already connected to something - enable the button
				setEnabled(scufl2Tools.datalinksTo((InputProcessorPort) selected).isEmpty());
			}
		}
	}

	/**
	 * Observes events on workflow Selection Manager, i.e. when a workflow
	 * node is selected in the graph view, and enables/disables this action accordingly.
	 */
	private final class DataflowSelectionObserver implements Observer<DataflowSelectionMessage> {

		public void notify(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) throws Exception {
			updateStatus();
		}
	}

	private final class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {

		private static final String DESIGN_PERSPECTIVE_ID = "net.sf.taverna.t2.ui.perspectives.design.DesignPerspective";

		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof WorkflowBundleSelectionEvent) {
				WorkflowBundleSelectionEvent workflowBundleSelectionEvent = (WorkflowBundleSelectionEvent) message;
				WorkflowBundle oldFlow = workflowBundleSelectionEvent
						.getPreviouslySelectedWorkflowBundle();
				WorkflowBundle newFlow = workflowBundleSelectionEvent.getSelectedWorkflowBundle();
				// Update the buttons status as current dataflow has changed
				updateStatus();

				// Remove the workflow selection model listener from the previous (if any)
				// and add to the new workflow (if any)
				if (oldFlow != null) {
					selectionManager.getDataflowSelectionModel(oldFlow).removeObserver(
							workflowSelectionObserver);
				}

				if (newFlow != null) {
					selectionManager.getDataflowSelectionModel(newFlow).addObserver(
							workflowSelectionObserver);
				}
			} else if (message instanceof PerspectiveSelectionEvent) {
				PerspectiveSelectionEvent perspectiveSelectionEvent = (PerspectiveSelectionEvent) message;
				if (DESIGN_PERSPECTIVE_ID.equals(perspectiveSelectionEvent.getSelectedPerspective().getID())) {
					updateStatus();
				} else {
					setEnabled(false);
				}
			}
		}

	}

}
