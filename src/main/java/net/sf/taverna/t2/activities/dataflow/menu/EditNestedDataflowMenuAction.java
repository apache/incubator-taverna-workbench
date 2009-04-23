package net.sf.taverna.t2.activities.dataflow.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.actions.EditNestedDataflowAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

public class EditNestedDataflowMenuAction extends
		AbstractConfigureActivityMenuAction<DataflowActivity> {

	public EditNestedDataflowMenuAction() {
		super(DataflowActivity.class);
	}

	@Override
	protected Action createAction() {
		EditNestedDataflowAction configAction = new EditNestedDataflowAction(findActivity());
		return configAction;
	}


}
