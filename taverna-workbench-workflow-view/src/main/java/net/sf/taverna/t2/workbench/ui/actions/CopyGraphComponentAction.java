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
package net.sf.taverna.t2.workbench.ui.actions;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.event.KeyEvent.VK_C;
import static java.awt.event.KeyEvent.VK_Y;
import static javax.swing.KeyStroke.getKeyStroke;
import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.copyIcon;
import static net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView.copyProcessor;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.ui.menu.DesignOnlyAction;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;

/**
 * An action that copies the selected graph component.
 * 
 * @author Alan R Williams
 * 
 */
@SuppressWarnings("serial")
public class CopyGraphComponentAction extends AbstractAction implements
		DesignOnlyAction {
	/** Current workflow's selection model event observer. */
	private Observer<DataflowSelectionMessage> workflowSelectionObserver = new DataflowSelectionObserver();
	private final SelectionManager selectionManager;

	public CopyGraphComponentAction(SelectionManager selectionManager) {
		super("Copy", copyIcon);
		this.selectionManager = selectionManager;
		putValue(SHORT_DESCRIPTION, "Copy selected component");
		putValue(MNEMONIC_KEY, VK_Y);

		putValue(
				ACCELERATOR_KEY,
				getKeyStroke(VK_C, getDefaultToolkit().getMenuShortcutKeyMask()));
		setEnabled(false);

		selectionManager.addObserver(new SelectionManagerObserver());
	}

	/**
	 * Check if action should be enabled or disabled and update its status.
	 */
	public void updateStatus() {
		WorkflowBundle workflowBundle = selectionManager
				.getSelectedWorkflowBundle();
		DataflowSelectionModel selectionModel = selectionManager
				.getDataflowSelectionModel(workflowBundle);

		// List of all selected objects in the graph view
		Set<Object> selection = selectionModel.getSelection();

		if (selection.isEmpty())
			setEnabled(false);
		else {
			/*
			 * Take the first selected item - we only support single selections
			 * anyway
			 */
			Object selected = selection.toArray()[0];
			setEnabled (selected instanceof Processor);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		copyProcessor(selectionManager);
	}

	/**
	 * Observes events on workflow Selection Manager, i.e. when a workflow node
	 * is selected in the graph view, and enables/disables this action
	 * accordingly.
	 */
	private final class DataflowSelectionObserver extends
			SwingAwareObserver<DataflowSelectionMessage> {
		@Override
		public void notifySwing(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) {
			updateStatus();
		}
	}

	private final class SelectionManagerObserver extends
			SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof WorkflowBundleSelectionEvent) {
				WorkflowBundleSelectionEvent event = (WorkflowBundleSelectionEvent) message;
				WorkflowBundle oldFlow = event
						.getPreviouslySelectedWorkflowBundle();
				WorkflowBundle newFlow = event.getSelectedWorkflowBundle();
				// Update the buttons status as current dataflow has changed
				updateStatus();

				/*
				 * Remove the workflow selection model listener from the
				 * previous (if any) and add to the new workflow (if any)
				 */
				if (oldFlow != null)
					selectionManager.getDataflowSelectionModel(oldFlow)
							.removeObserver(workflowSelectionObserver);
				if (newFlow != null)
					selectionManager.getDataflowSelectionModel(newFlow)
							.addObserver(workflowSelectionObserver);
			}
		}
	}
}
