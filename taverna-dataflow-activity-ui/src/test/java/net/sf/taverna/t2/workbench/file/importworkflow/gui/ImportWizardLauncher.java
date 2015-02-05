package net.sf.taverna.t2.workbench.file.importworkflow.gui;

import javax.swing.UIManager;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.impl.EditManagerImpl;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.impl.FileManagerImpl;


public class ImportWizardLauncher {

	public static void main(String[] args) throws Exception {

		UIManager.setLookAndFeel(UIManager
				.getSystemLookAndFeelClassName());

		EditManager editManager = new EditManagerImpl();
		FileManager fileManager = new FileManagerImpl(editManager);

		ImportWorkflowWizard s = new ImportWorkflowWizard(null, editManager, fileManager, null, null, null, null);
		s.setVisible(true);
	}
}
