package net.sf.taverna.t2.workbench.file.importworkflow.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.file.importworkflow.actions.ImportWorkflowAction;

public class ImportWorkflowMenuAction extends AbstractMenuAction {

	public static final URI INSERT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");

	public static final URI IMPORT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");

	public ImportWorkflowMenuAction() {
		super(INSERT_URI, 40, IMPORT_URI);
	}

	@Override
	protected Action createAction() {
		return ImportWorkflowAction.getInstance();
	}

}
