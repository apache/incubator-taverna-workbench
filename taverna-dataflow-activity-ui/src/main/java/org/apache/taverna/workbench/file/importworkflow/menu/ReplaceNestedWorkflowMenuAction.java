package org.apache.taverna.workbench.file.importworkflow.menu;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.activitytools.AbstractConfigureActivityMenuAction;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.importworkflow.actions.ReplaceNestedWorkflowAction;
import org.apache.taverna.workbench.selection.SelectionManager;

public class ReplaceNestedWorkflowMenuAction extends AbstractConfigureActivityMenuAction {

	private static final URI NESTED_ACTIVITY = URI.create("http://ns.taverna.org.uk/2010/activity/nested-workflow");

	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;
	private ActivityIconManager activityIconManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private ServiceDescriptionRegistry serviceDescriptionRegistry;
	private SelectionManager selectionManager;

	public ReplaceNestedWorkflowMenuAction() {
		super(NESTED_ACTIVITY);
	}

	@Override
	protected Action createAction() {
		ReplaceNestedWorkflowAction configAction = new ReplaceNestedWorkflowAction(findActivity(),
				editManager, fileManager, menuManager, activityIconManager, colourManager,
				serviceDescriptionRegistry, workbenchConfiguration, selectionManager);
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

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setServiceDescriptionRegistry(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
