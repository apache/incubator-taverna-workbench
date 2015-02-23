package net.sf.taverna.t2.activities.dataflow.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.actions.EditNestedDataflowAction;
import net.sf.taverna.t2.activities.dataflow.servicedescriptions.DataflowTemplateService;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.selection.SelectionManager;

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
