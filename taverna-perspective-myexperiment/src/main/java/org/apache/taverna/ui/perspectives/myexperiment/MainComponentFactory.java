package org.apache.taverna.ui.perspectives.myexperiment;
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

/**
 * @author Sergejs Aleksejevs, Jiten Bhagat
 */

import javax.swing.ImageIcon;

import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.ui.perspectives.myexperiment.model.MyExperimentClient;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.zaria.UIComponentFactorySPI;
import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;

public class MainComponentFactory implements UIComponentFactorySPI {

	private EditManager editManager;
	private FileManager fileManager;
	private MyExperimentClient myExperimentClient;
	private MenuManager menuManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private SelectionManager selectionManager;	

	public UIComponentSPI getComponent() {
		return new MainComponent(editManager, fileManager, myExperimentClient, menuManager, colourManager, workbenchConfiguration, selectionManager);
	}

	public ImageIcon getIcon() {
		return WorkbenchIcons.databaseIcon;
	}

	public String getName() {
		return "myExperiment Main Component Factory";
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMyExperimentClient(MyExperimentClient myExperimentClient) {
		this.myExperimentClient = myExperimentClient;
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
