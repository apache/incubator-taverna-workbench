package net.sf.taverna.t2.activities.dataflow.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.actions.OpenNestedDataflowFromFileAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

public class OpenNestedDataflowFromFileMenuAction extends
		AbstractConfigureActivityMenuAction<DataflowActivity> {

	public OpenNestedDataflowFromFileMenuAction() {
		super(DataflowActivity.class);
	}

	@Override
	protected Action createAction() {
		OpenNestedDataflowFromFileAction configAction = new OpenNestedDataflowFromFileAction(findActivity());
		addMenuDots(configAction);
		return configAction;
	}


}
