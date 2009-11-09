package net.sf.taverna.t2.workbench.file.importworkflow.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.file.importworkflow.actions.ReplaceNestedWorkflowAction;

public class ReplaceNestedWorkflowMenuAction extends
		AbstractConfigureActivityMenuAction<DataflowActivity> {

	public ReplaceNestedWorkflowMenuAction() {
		super(DataflowActivity.class);
	}

	@Override
	protected Action createAction() {
		ReplaceNestedWorkflowAction configAction = new ReplaceNestedWorkflowAction(
				findActivity());
		addMenuDots(configAction);
		return configAction;
	}

}
