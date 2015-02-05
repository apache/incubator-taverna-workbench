package net.sf.taverna.t2.workbench.file.importworkflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.Utils;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;
import net.sf.taverna.t2.workflow.edits.ConfigureEdit;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.core.Workflow;

public class ReplaceNestedWorkflowAction extends ActivityConfigurationAction {
	private static final long serialVersionUID = 1L;

	private final EditManager editManager;
	private final FileManager fileManager;
	private final MenuManager menuManager;

	private final ColourManager colourManager;

	private final WorkbenchConfiguration workbenchConfiguration;

	private final SelectionManager selectionManager;

	public ReplaceNestedWorkflowAction(Activity activity, EditManager editManager,
			FileManager fileManager, MenuManager menuManager,
			ActivityIconManager activityIconManager, ColourManager colourManager,
			ServiceDescriptionRegistry serviceDescriptionRegistry,
			WorkbenchConfiguration workbenchConfiguration, SelectionManager selectionManager) {
		super(activity, activityIconManager, serviceDescriptionRegistry);
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.menuManager = menuManager;
		this.colourManager = colourManager;
		this.workbenchConfiguration = workbenchConfiguration;
		this.selectionManager = selectionManager;
		putValue(NAME, "Replace nested workflow");
	}

	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}
		ImportWorkflowWizard wizard = new ImportWorkflowWizard(
				Utils.getParentFrame(parentComponent), editManager, fileManager, menuManager,
				colourManager, workbenchConfiguration, selectionManager) {
			private static final long serialVersionUID = 1L;

//			@Override
//			protected Edit<?> makeInsertNestedWorkflowEdit(Workflow nestedFlow, String name) {
//				Configuration configuration = new Configuration();
//				configuration.setType(null);
//				// TODO use service registry
//				return new ConfigureEdit<Activity>(getActivity(), null, configuration);
//			}

//			@Override
//			protected Activity getInsertedActivity() {
//				return getActivity();
//			}
		};

		wizard.setMergeEnabled(false);
//		wizard.setCustomDestinationDataflow(fileManager.getCurrentDataflow(),
//				"Existing nested workflow");
//		wizard.setDestinationEnabled(false);
		wizard.setVisible(true);
	}

}
