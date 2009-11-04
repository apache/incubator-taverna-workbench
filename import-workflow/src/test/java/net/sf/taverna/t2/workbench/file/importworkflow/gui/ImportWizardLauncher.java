package net.sf.taverna.t2.workbench.file.importworkflow.gui;

import javax.swing.UIManager;


public class ImportWizardLauncher {

	public static void main(String[] args) throws Exception {
		
		UIManager.setLookAndFeel(UIManager
				.getSystemLookAndFeelClassName());
		
		ImportWorkflowWizard s = new ImportWorkflowWizard(null);
		s.setVisible(true);
	}
}
