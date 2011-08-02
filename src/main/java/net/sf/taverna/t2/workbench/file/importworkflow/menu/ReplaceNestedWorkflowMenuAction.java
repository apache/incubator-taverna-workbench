package net.sf.taverna.t2.workbench.file.importworkflow.menu;

import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.importworkflow.actions.ReplaceNestedWorkflowAction;

public class ReplaceNestedWorkflowMenuAction extends
		AbstractConfigureActivityMenuAction<DataflowActivity> {

	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;

	public ReplaceNestedWorkflowMenuAction() {
		super(DataflowActivity.class);
	}

	@Override
	protected Action createAction() {
		ReplaceNestedWorkflowAction configAction = new ReplaceNestedWorkflowAction(
				findActivity(), editManager, fileManager, menuManager);
		addMenuDots(configAction);
		return configAction;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

}
