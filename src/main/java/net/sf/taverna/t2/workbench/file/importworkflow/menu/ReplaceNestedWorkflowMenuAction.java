package net.sf.taverna.t2.workbench.file.importworkflow.menu;

import java.net.URI;

import javax.swing.Action;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.importworkflow.actions.ReplaceNestedWorkflowAction;
import net.sf.taverna.t2.workbench.selection.SelectionManager;

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
