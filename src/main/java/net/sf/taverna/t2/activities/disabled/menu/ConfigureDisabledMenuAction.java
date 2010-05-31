package net.sf.taverna.t2.activities.disabled.menu;

import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import javax.swing.Action;

import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;
import net.sf.taverna.t2.activities.disabled.actions.DisabledActivityConfigurationAction;

public class ConfigureDisabledMenuAction extends
		AbstractConfigureActivityMenuAction<DisabledActivity> {

	public ConfigureDisabledMenuAction() {
		super(DisabledActivity.class);
	}
	
	@Override
	protected Action createAction() {
	    return new DisabledActivityConfigurationAction(findActivity(), getParentFrame());
	}

}
