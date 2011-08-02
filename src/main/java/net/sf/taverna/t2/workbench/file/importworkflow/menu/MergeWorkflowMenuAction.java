package net.sf.taverna.t2.workbench.file.importworkflow.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.importworkflow.actions.MergeWorkflowAction;

public class MergeWorkflowMenuAction extends AbstractMenuAction {

	public static final URI INSERT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");

	public static final URI IMPORT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");

	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;

	public MergeWorkflowMenuAction() {
		super(INSERT_URI, 2000, IMPORT_URI);
	}

	@Override
	protected Action createAction() {
		return new MergeWorkflowAction(editManager, fileManager, menuManager);
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
