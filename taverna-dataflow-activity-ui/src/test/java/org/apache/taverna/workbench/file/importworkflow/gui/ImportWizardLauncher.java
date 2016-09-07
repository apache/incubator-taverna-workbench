package org.apache.taverna.workbench.file.importworkflow.gui;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
