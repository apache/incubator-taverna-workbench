/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.file.importworkflow.menu;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.importworkflow.actions.AddNestedWorkflowAction;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.views.graph.menu.InsertMenu;

/**
 * An action to add a nested workflow activity + a wrapping processor to the
 * workflow.
 *
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 *
 */
public class AddNestedWorkflowMenuAction extends AbstractMenuAction {

	private static final String ADD_NESTED_WORKFLOW = "Nested workflow";

	private static final URI ADD_NESTED_WORKFLOW_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuAddNestedWorkflow");

	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private SelectionManager selectionManager;

	public AddNestedWorkflowMenuAction() {
		super(InsertMenu.INSERT, 400, ADD_NESTED_WORKFLOW_URI);
	}

	@Override
	protected Action createAction() {
		AddNestedWorkflowAction a = new AddNestedWorkflowAction(editManager, fileManager,
				menuManager, colourManager, workbenchConfiguration, selectionManager);
		// Override name to avoid "Add "
		a.putValue(Action.NAME, ADD_NESTED_WORKFLOW);
		a.putValue(Action.SHORT_DESCRIPTION, ADD_NESTED_WORKFLOW);
		a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_N, InputEvent.SHIFT_DOWN_MASK
						| InputEvent.ALT_DOWN_MASK));
		return a;

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
