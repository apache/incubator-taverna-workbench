package org.apache.taverna.workbench.file.importworkflow.gui;

import org.apache.taverna.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import javax.swing.UIManager;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.impl.EditManagerImpl;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.impl.FileManagerImpl;


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
