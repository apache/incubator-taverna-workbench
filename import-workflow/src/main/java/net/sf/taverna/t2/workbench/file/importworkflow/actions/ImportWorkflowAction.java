package net.sf.taverna.t2.workbench.file.importworkflow.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import net.sf.taverna.t2.workbench.ui.Utils;

public class ImportWorkflowAction extends AbstractAction {
	
	protected static class Singleton {
		protected static ImportWorkflowAction instance = new ImportWorkflowAction();
	}

	public static ImportWorkflowAction getInstance() {
		return Singleton.instance;
	}

	protected ImportWorkflowAction() {
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
		//wizard.setImportEnabled(true);
		wizard.setNestedEnabled(false);
		wizard.setVisible(true);
	}

}
