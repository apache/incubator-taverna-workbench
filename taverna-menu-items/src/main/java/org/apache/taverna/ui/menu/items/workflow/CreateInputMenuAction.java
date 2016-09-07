/**********************************************************************
 **********************************************************************/
package org.apache.taverna.ui.menu.items.workflow;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.ui.menu.items.contextualviews.InsertSection;
import org.apache.taverna.workbench.design.actions.AddDataflowInputAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.Workflow;

public class CreateInputMenuAction extends AbstractContextualMenuAction {

	private EditManager editManager;
	private SelectionManager selectionManager;

	public CreateInputMenuAction() {
		super(InsertSection.insertSection, 10);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Workflow;
	}

	@Override
	protected Action createAction() {
		return new AddDataflowInputAction((Workflow) getContextualSelection()
				.getSelection(), getContextualSelection()
				.getRelativeToComponent(), editManager, selectionManager);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
