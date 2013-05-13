package net.sf.taverna.t2.activities.dataflow.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.actions.EditNestedDataflowAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.file.FileManager;

public class EditNestedDataflowMenuAction extends AbstractConfigureActivityMenuAction {

	private FileManager fileManager;

	public EditNestedDataflowMenuAction() {
		super(URI.create("http://ns.taverna.org.uk/2010/activity/nested-workflow"));
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
