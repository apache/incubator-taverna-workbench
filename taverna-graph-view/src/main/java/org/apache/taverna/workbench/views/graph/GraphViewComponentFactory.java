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
package org.apache.taverna.workbench.views.graph;

import javax.swing.ImageIcon;

import org.apache.taverna.services.ServiceRegistry;

import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.zaria.UIComponentFactorySPI;
import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;
import org.apache.taverna.workbench.views.graph.config.GraphViewConfiguration;

/**
 * @author David Withers
 */
public class GraphViewComponentFactory implements UIComponentFactorySPI {
	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;
	private SelectionManager selectionManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private GraphViewConfiguration graphViewConfiguration;
	private ServiceRegistry serviceRegistry;

	@Override
	public UIComponentSPI getComponent() {
		return new GraphViewComponent(colourManager, editManager, fileManager,
				menuManager, graphViewConfiguration, workbenchConfiguration,
				selectionManager, serviceRegistry);
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Graph View";
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

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setWorkbenchConfiguration(
			WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public void setGraphViewConfiguration(
			GraphViewConfiguration graphViewConfiguration) {
		this.graphViewConfiguration = graphViewConfiguration;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
}
