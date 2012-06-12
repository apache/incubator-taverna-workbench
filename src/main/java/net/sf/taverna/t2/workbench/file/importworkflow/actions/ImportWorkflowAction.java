package net.sf.taverna.t2.workbench.file.importworkflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.activities.dataflow.servicedescriptions.DataflowActivityIcon;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import net.sf.taverna.t2.workbench.ui.Utils;

/**
 * A general version of {@link AddNestedWorkflowAction} and {@link MergeWorkflowAction} that allows
 * the user to choose which action to perform.
 *
 * @author Stian Soiland-Reyes
 *
 */
public class ImportWorkflowAction extends AbstractAction {
	private static final long serialVersionUID = -2242979457902699028L;
	private final EditManager editManager;
	private final FileManager fileManager;
	private final MenuManager menuManager;
	private final ColourManager colourManager;
	private final WorkbenchConfiguration workbenchConfiguration;

	public ImportWorkflowAction(EditManager editManager, FileManager fileManager,
			MenuManager menuManager, ColourManager colourManager,
			WorkbenchConfiguration workbenchConfiguration) {
		super("Import workflow", DataflowActivityIcon.getDataflowIcon());
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.menuManager = menuManager;
		this.colourManager = colourManager;
		this.workbenchConfiguration = workbenchConfiguration;
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
				colourManager, workbenchConfiguration);
		wizard.setVisible(true);
	}

}
