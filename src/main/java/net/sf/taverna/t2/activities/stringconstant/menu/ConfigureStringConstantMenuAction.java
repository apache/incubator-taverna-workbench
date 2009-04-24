package net.sf.taverna.t2.activities.stringconstant.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.actions.StringConstantActivityConfigurationAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;

public class ConfigureStringConstantMenuAction extends
		AbstractConfigureActivityMenuAction<StringConstantActivity> {

	private static final String CONFIGURE_STRINGCONSTANT = "Configure value";

	public ConfigureStringConstantMenuAction() {
		super(StringConstantActivity.class);
	}
	
	@Override
	protected Action createAction() {
		StringConstantActivityConfigurationAction configAction = new StringConstantActivityConfigurationAction(
				findActivity(), getParentFrame());
		configAction.putValue(Action.NAME, CONFIGURE_STRINGCONSTANT);
		addMenuDots(configAction);
		return configAction;
	}


}
