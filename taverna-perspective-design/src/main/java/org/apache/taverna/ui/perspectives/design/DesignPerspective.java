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
package org.apache.taverna.ui.perspectives.design;

import static org.apache.taverna.workbench.icons.WorkbenchIcons.editIcon;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;
import org.apache.taverna.workbench.ui.zaria.UIComponentFactorySPI;

public class DesignPerspective implements PerspectiveSPI {
	private DesignPerspectiveComponent designPerspectiveComponent;
	private UIComponentFactorySPI graphViewComponentFactory;
	private UIComponentFactorySPI servicePanelComponentFactory;
	private UIComponentFactorySPI contextualViewComponentFactory;
	private UIComponentFactorySPI workflowExplorerFactory;
	private UIComponentFactorySPI reportViewComponentFactory;
	private FileManager fileManager;
	private SelectionManager selectionManager;
	private MenuManager menuManager;
	private EditManager editManager;

	@Override
	public String getID() {
		return DesignPerspective.class.getName();
	}

	@Override
	public JComponent getPanel() {
		if (designPerspectiveComponent == null)
			designPerspectiveComponent = new DesignPerspectiveComponent(
					graphViewComponentFactory, servicePanelComponentFactory,
					contextualViewComponentFactory, workflowExplorerFactory,
					reportViewComponentFactory, fileManager, selectionManager,
					menuManager, editManager);
		return designPerspectiveComponent;
	}

	@Override
	public ImageIcon getButtonIcon() {
		return editIcon;
	}

	@Override
	public String getText() {
		return "Design";
	}

	@Override
	public int positionHint() {
		return 10;
	}

	public void setGraphViewComponentFactory(
			UIComponentFactorySPI graphViewComponentFactory) {
		this.graphViewComponentFactory = graphViewComponentFactory;
	}

	public void setServicePanelComponentFactory(
			UIComponentFactorySPI servicePanelComponentFactory) {
		this.servicePanelComponentFactory = servicePanelComponentFactory;
	}

	public void setContextualViewComponentFactory(
			UIComponentFactorySPI contextualViewComponentFactory) {
		this.contextualViewComponentFactory = contextualViewComponentFactory;
	}

	public void setWorkflowExplorerFactory(
			UIComponentFactorySPI workflowExplorerFactory) {
		this.workflowExplorerFactory = workflowExplorerFactory;
	}

	public void setReportViewComponentFactory(
			UIComponentFactorySPI reportViewComponentFactory) {
		this.reportViewComponentFactory = reportViewComponentFactory;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}
}
