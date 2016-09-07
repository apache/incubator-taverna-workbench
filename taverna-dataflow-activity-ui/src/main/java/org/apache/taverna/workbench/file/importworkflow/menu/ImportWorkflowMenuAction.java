package org.apache.taverna.workbench.file.importworkflow.menu;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.importworkflow.actions.ImportWorkflowAction;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * An action to import nested/merged workflows.
 *
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 *
 */
public class ImportWorkflowMenuAction extends AbstractContextualMenuAction {

	private static final URI insertSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/insert");

	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private SelectionManager selectionManager;

	public ImportWorkflowMenuAction() {
		super(insertSection, 400);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled() && getContextualSelection().getSelection() instanceof Workflow;
	}

	@Override
	protected Action createAction() {
		ImportWorkflowAction myAction = new ImportWorkflowAction(editManager, fileManager,
				menuManager, colourManager, workbenchConfiguration, selectionManager);
		// Just "Workflow" as we go under the "Insert" menu
		myAction.putValue(Action.NAME, "Nested workflow");
		return myAction;
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

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
