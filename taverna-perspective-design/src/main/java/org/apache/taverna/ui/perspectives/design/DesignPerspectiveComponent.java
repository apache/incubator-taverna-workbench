/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.ui.perspectives.design;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.zaria.UIComponentFactorySPI;

/**
 * @author David Withers
 */
public class DesignPerspectiveComponent extends JSplitPane {
	private static final long serialVersionUID = 6199239532713982318L;

	private final UIComponentFactorySPI graphViewComponentFactory;
	private final UIComponentFactorySPI servicePanelComponentFactory;
	private final UIComponentFactorySPI contextualViewComponentFactory;
	private final UIComponentFactorySPI workflowExplorerFactory;
	@SuppressWarnings("unused")
	private final UIComponentFactorySPI reportViewComponentFactory;
	private final SelectionManager selectionManager;

	private final FileManager fileManager;
	private final MenuManager menuManager;
	private final EditManager editManager;

	public DesignPerspectiveComponent(
			UIComponentFactorySPI graphViewComponentFactory,
			UIComponentFactorySPI servicePanelComponentFactory,
			UIComponentFactorySPI contextualViewComponentFactory,
			UIComponentFactorySPI workflowExplorerFactory,
			UIComponentFactorySPI reportViewComponentFactory,
			FileManager fileManager, SelectionManager selectionManager,
			MenuManager menuManager, EditManager editManager) {
		this.graphViewComponentFactory = graphViewComponentFactory;
		this.servicePanelComponentFactory = servicePanelComponentFactory;
		this.contextualViewComponentFactory = contextualViewComponentFactory;
		this.workflowExplorerFactory = workflowExplorerFactory;
		this.reportViewComponentFactory = reportViewComponentFactory;
		this.fileManager = fileManager;
		this.selectionManager = selectionManager;
		this.menuManager = menuManager;
		this.editManager = editManager;

		setBorder(null);
		setOrientation(HORIZONTAL_SPLIT);
		setDividerLocation(300);
		setLeftComponent(createLeftComponent());
		setRightComponent(createRightComponent());
	}

	private Component createLeftComponent() {
		JSplitPane leftComponent = new JSplitPane(VERTICAL_SPLIT);
		leftComponent.setBorder(null);
		leftComponent.setDividerLocation(400);

		leftComponent.setLeftComponent((Component) servicePanelComponentFactory
				.getComponent());

		JTabbedPane rightComponent = new JTabbedPane();
		rightComponent.addTab("Workflow explorer",
				(Component) workflowExplorerFactory.getComponent());
		rightComponent.addTab("Details",
				(Component) contextualViewComponentFactory.getComponent());
		// rightComponent.addTab("Validation report", (Component)
		// reportViewComponentFactory.getComponent());
		leftComponent.setRightComponent(rightComponent);

		return leftComponent;
	}

	private Component createRightComponent() {
		JPanel diagramComponent = new JPanel(new BorderLayout());
		diagramComponent.add(new WorkflowSelectorComponent(selectionManager),
				NORTH);
		diagramComponent.add(
				(Component) graphViewComponentFactory.getComponent(), CENTER);

		JPanel rightComonent = new JPanel(new BorderLayout());
		rightComonent.add(new WorkflowBundleSelectorComponent(selectionManager,
				fileManager, menuManager, editManager), NORTH);
		rightComonent.add(diagramComponent, CENTER);
		return rightComonent;
	}
}
