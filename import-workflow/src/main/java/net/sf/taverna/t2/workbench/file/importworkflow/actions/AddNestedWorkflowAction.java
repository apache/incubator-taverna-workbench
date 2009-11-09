package net.sf.taverna.t2.workbench.file.importworkflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.servicedescriptions.DataflowActivityIcon;
import net.sf.taverna.t2.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import net.sf.taverna.t2.workbench.ui.Utils;

/**
 * An action for adding a nested workflow.
 * 
 * @author Stian Soiland-Reyes
 *
 */
public class AddNestedWorkflowAction extends AbstractAction {
	private static final long serialVersionUID = -2242979457902699028L;

	protected static class Singleton {
		protected static AddNestedWorkflowAction instance = new AddNestedWorkflowAction();
	}

	public static AddNestedWorkflowAction getInstance() {
		return Singleton.instance;
	}

	public AddNestedWorkflowAction() {
		super("Add nested workflow", DataflowActivityIcon.getDataflowIcon());
		
	}

	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}
		ImportWorkflowWizard wizard = new ImportWorkflowWizard(Utils.getParentFrame(parentComponent));
		wizard.setMergeEnabled(false);
		wizard.setVisible(true);
	}

}
