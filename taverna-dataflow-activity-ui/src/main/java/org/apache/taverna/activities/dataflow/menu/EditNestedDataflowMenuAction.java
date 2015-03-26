package org.apache.taverna.activities.dataflow.menu;

import javax.swing.Action;

import org.apache.taverna.activities.dataflow.actions.EditNestedDataflowAction;
import org.apache.taverna.activities.dataflow.servicedescriptions.DataflowTemplateService;
import org.apache.taverna.workbench.activitytools.AbstractConfigureActivityMenuAction;
import org.apache.taverna.workbench.selection.SelectionManager;

public class EditNestedDataflowMenuAction extends AbstractConfigureActivityMenuAction {

	private SelectionManager selectionManager;

	public EditNestedDataflowMenuAction() {
		super(DataflowTemplateService.ACTIVITY_TYPE);
	}

	@Override
	protected Action createAction() {
		EditNestedDataflowAction configAction = new EditNestedDataflowAction(findActivity(), selectionManager);
		return configAction;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
