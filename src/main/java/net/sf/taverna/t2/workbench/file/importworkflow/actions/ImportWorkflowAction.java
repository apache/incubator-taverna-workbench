package net.sf.taverna.t2.workbench.file.importworkflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.activities.dataflow.servicedescriptions.DataflowActivityIcon;
import net.sf.taverna.t2.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import net.sf.taverna.t2.workbench.ui.Utils;

/**
 * A general version of {@link AddNestedWorkflowAction} and
 * {@link MergeWorkflowAction} that allows the user to choose which action to
 * perform.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class ImportWorkflowAction extends AbstractAction {
	private static final long serialVersionUID = -2242979457902699028L;

	protected static class Singleton {
		protected static ImportWorkflowAction instance = new ImportWorkflowAction();
	}

	public static ImportWorkflowAction getInstance() {
		return Singleton.instance;
	}

	public ImportWorkflowAction() {
		super("Import workflow", DataflowActivityIcon.getDataflowIcon());
	}

	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}
		ImportWorkflowWizard wizard = new ImportWorkflowWizard(Utils
				.getParentFrame(parentComponent));
		wizard.setVisible(true);
	}

}
