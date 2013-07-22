/*******************************************************************************
 * Copyright (C) 2011 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.ui.perspectives.design;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;

/**
 * @author David Withers
 */
public class DesignPerspectiveComponent extends JSplitPane {

	private static final long serialVersionUID = 1L;

	private final UIComponentFactorySPI graphViewComponentFactory;
	private final UIComponentFactorySPI servicePanelComponentFactory;
	private final UIComponentFactorySPI contextualViewComponentFactory;
	private final UIComponentFactorySPI workflowExplorerFactory;
	private final UIComponentFactorySPI reportViewComponentFactory;
	private final SelectionManager selectionManager;

	private final FileManager fileManager;
	private final MenuManager menuManager;
	private final EditManager editManager;

	public DesignPerspectiveComponent(UIComponentFactorySPI graphViewComponentFactory,
			UIComponentFactorySPI servicePanelComponentFactory,
			UIComponentFactorySPI contextualViewComponentFactory,
			UIComponentFactorySPI workflowExplorerFactory,
			UIComponentFactorySPI reportViewComponentFactory, FileManager fileManager,
			SelectionManager selectionManager, MenuManager menuManager, EditManager editManager) {
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
		setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		setDividerLocation(200);
		setLeftComponent(createLeftComponent());
		setRightComponent(createRightComponent());
	}

	private Component createLeftComponent() {
		JSplitPane leftComponent = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		leftComponent.setBorder(null);
		leftComponent.setDividerLocation(100);

		leftComponent.setLeftComponent((Component) servicePanelComponentFactory.getComponent());

		JTabbedPane rightComponent = new JTabbedPane();
		rightComponent.addTab("Workflow explorer",
				(Component) workflowExplorerFactory.getComponent());
		rightComponent.addTab("Details", (Component) contextualViewComponentFactory.getComponent());
		// rightComponent.addTab("Validation report", (Component)
		// reportViewComponentFactory.getComponent());
		leftComponent.setRightComponent(rightComponent);

		return leftComponent;
	}

	private Component createRightComponent() {
		JPanel diagramComponent = new JPanel(new BorderLayout());
		diagramComponent.add(new WorkflowSelectorComponent(selectionManager), BorderLayout.NORTH);
		diagramComponent.add((Component) graphViewComponentFactory.getComponent(),
				BorderLayout.CENTER);

		JPanel rightComonent = new JPanel(new BorderLayout());
		rightComonent.add(new WorkflowBundleSelectorComponent(selectionManager, fileManager,
				menuManager, editManager), BorderLayout.NORTH);
		rightComonent.add(diagramComponent, BorderLayout.CENTER);
		return rightComonent;
	}

}
