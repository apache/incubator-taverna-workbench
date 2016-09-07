package org.apache.taverna.workbench.file.importworkflow.actions;
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

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.taverna.activities.dataflow.servicedescriptions.DataflowActivityIcon;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.importworkflow.gui.ImportWorkflowWizard;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.Utils;

/**
 * An action for adding a nested workflow.
 *
 * @author Stian Soiland-Reyes
 *
 */
public class AddNestedWorkflowAction extends AbstractAction {
	private static final long serialVersionUID = -2242979457902699028L;
	private final EditManager editManager;
	private final FileManager fileManager;
	private final MenuManager menuManager;
	private final ColourManager colourManager;
	private final WorkbenchConfiguration workbenchConfiguration;
	private final SelectionManager selectionManager;

	public AddNestedWorkflowAction(EditManager editManager, FileManager fileManager,
			MenuManager menuManager, ColourManager colourManager,
			WorkbenchConfiguration workbenchConfiguration, SelectionManager selectionManager) {
		super("Add nested workflow", DataflowActivityIcon.getDataflowIcon());
		this.editManager = editManager;
		this.fileManager = fileManager;
		this.menuManager = menuManager;
		this.colourManager = colourManager;
		this.workbenchConfiguration = workbenchConfiguration;
		this.selectionManager = selectionManager;
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
				colourManager, workbenchConfiguration, selectionManager);
		wizard.setMergeEnabled(false);
		wizard.setVisible(true);
	}

}
