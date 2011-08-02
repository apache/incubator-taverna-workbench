package net.sf.taverna.t2.activities.stringconstant.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.actions.StringConstantActivityConfigurationAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;

public class ConfigureStringConstantMenuAction extends
		AbstractConfigureActivityMenuAction<StringConstantActivity> {


	private EditManager editManager;
	private FileManager fileManager;

	public ConfigureStringConstantMenuAction() {
		super(StringConstantActivity.class);
	}

	@Override
	protected Action createAction() {
		StringConstantActivityConfigurationAction configAction = new StringConstantActivityConfigurationAction(
				findActivity(), getParentFrame(), editManager, fileManager);
		configAction.putValue(Action.NAME, StringConstantActivityConfigurationAction.CONFIGURE_STRINGCONSTANT);
		addMenuDots(configAction);
		return configAction;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

}
