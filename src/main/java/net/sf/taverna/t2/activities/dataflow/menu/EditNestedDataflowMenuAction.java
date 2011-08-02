package net.sf.taverna.t2.activities.dataflow.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.actions.EditNestedDataflowAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.file.FileManager;

public class EditNestedDataflowMenuAction extends
		AbstractConfigureActivityMenuAction<DataflowActivity> {

	private FileManager fileManager;

	public EditNestedDataflowMenuAction() {
		super(DataflowActivity.class);
	}

	@Override
	protected Action createAction() {
		EditNestedDataflowAction configAction = new EditNestedDataflowAction(findActivity(), fileManager);
		return configAction;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

}
