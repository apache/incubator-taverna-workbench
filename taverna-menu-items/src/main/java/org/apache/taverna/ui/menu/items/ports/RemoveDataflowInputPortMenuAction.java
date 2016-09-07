package org.apache.taverna.ui.menu.items.ports;

import java.awt.Component;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.design.actions.RemoveDataflowInputPortAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;

public class RemoveDataflowInputPortMenuAction extends
		AbstractContextualMenuAction {

	private EditManager editManager;
	private SelectionManager selectionManager;

	public RemoveDataflowInputPortMenuAction() {
		super(WorkflowInputPortSection.inputPort, 10);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof InputWorkflowPort
				&& getContextualSelection().getParent() instanceof Workflow;
	}

	@Override
	protected Action createAction() {
		Workflow workflow = (Workflow) getContextualSelection().getParent();
		InputWorkflowPort inport = (InputWorkflowPort) getContextualSelection()
				.getSelection();
		Component component = getContextualSelection().getRelativeToComponent();
		return new RemoveDataflowInputPortAction(workflow, inport, component, editManager, selectionManager);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
