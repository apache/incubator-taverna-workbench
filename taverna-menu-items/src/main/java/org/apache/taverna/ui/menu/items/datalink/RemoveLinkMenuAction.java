package org.apache.taverna.ui.menu.items.datalink;

import java.awt.Component;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.design.actions.RemoveDatalinkAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Workflow;

public class RemoveLinkMenuAction extends AbstractContextualMenuAction {

	private EditManager editManager;
	private SelectionManager selectionManager;

	public RemoveLinkMenuAction() {
		super(LinkSection.linkSection, 10);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof DataLink
				&& getContextualSelection().getParent() instanceof Workflow;
	}

	@Override
	protected Action createAction() {
		Workflow workflow = (Workflow) getContextualSelection().getParent();
		DataLink datalink = (DataLink) getContextualSelection().getSelection();
		Component component = getContextualSelection().getRelativeToComponent();
		return new RemoveDatalinkAction(workflow, datalink, component, editManager, selectionManager);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
