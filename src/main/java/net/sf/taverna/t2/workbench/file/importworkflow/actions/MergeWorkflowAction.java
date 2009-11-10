package net.sf.taverna.t2.workbench.file.importworkflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import net.sf.taverna.t2.workbench.ui.Utils;

/**
 * An action for merging two workflows
 * 
 * @author Stian Soiland-Reyes
 *
 */
public class MergeWorkflowAction extends AbstractAction {
	private static final long serialVersionUID = -2242979457902699028L;

	protected static class Singleton {
		protected static MergeWorkflowAction instance = new MergeWorkflowAction();
	}

	public static MergeWorkflowAction getInstance() {
		return Singleton.instance;
	}

	public MergeWorkflowAction() {
		super("Merge workflow");
	}

	public void actionPerformed(ActionEvent e) {
		final Component parentComponent;
		if (e.getSource() instanceof Component) {
			parentComponent = (Component) e.getSource();
		} else {
			parentComponent = null;
		}
		ImportWorkflowWizard wizard = new ImportWorkflowWizard(Utils.getParentFrame(parentComponent));
		wizard.setNestedEnabled(false);
		wizard.setVisible(true);
	}

}
