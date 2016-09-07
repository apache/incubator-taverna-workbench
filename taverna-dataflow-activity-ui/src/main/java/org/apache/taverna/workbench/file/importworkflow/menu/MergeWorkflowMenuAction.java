package org.apache.taverna.workbench.file.importworkflow.menu;
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

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.file.importworkflow.actions.MergeWorkflowAction;
import org.apache.taverna.workbench.selection.SelectionManager;

public class MergeWorkflowMenuAction extends AbstractMenuAction {

	public static final URI INSERT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");

	public static final URI IMPORT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#insert");

	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private SelectionManager selectionManager;

	public MergeWorkflowMenuAction() {
		super(INSERT_URI, 2000, IMPORT_URI);
	}

	@Override
	protected Action createAction() {
		return new MergeWorkflowAction(editManager, fileManager, menuManager, colourManager,
				workbenchConfiguration, selectionManager);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
