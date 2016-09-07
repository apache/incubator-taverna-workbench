/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.ports;

import java.awt.Component;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.design.actions.EditDataflowOutputPortAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;

public class EditDataflowOutputPortMenuAction extends
		AbstractContextualMenuAction {

	private EditManager editManager;
	private SelectionManager selectionManager;

	public EditDataflowOutputPortMenuAction() {
		super(WorkflowOutputPortSection.outputPort, 10);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof OutputWorkflowPort
				&& getContextualSelection().getParent() instanceof Workflow;
	}

	@Override
	protected Action createAction() {
		Workflow workflow = (Workflow) getContextualSelection().getParent();
		OutputWorkflowPort outport = (OutputWorkflowPort) getContextualSelection()
				.getSelection();
		Component component = getContextualSelection().getRelativeToComponent();
		return new EditDataflowOutputPortAction(workflow, outport, component, editManager, selectionManager);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
