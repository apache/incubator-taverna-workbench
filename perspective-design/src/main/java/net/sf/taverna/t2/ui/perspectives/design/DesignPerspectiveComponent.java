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

import java.awt.Component;

import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;

/**
 *
 *
 * @author David Withers
 */
public class DesignPerspectiveComponent extends JSplitPane {

	private static final long serialVersionUID = 1L;

	private final UIComponentFactorySPI graphViewComponentFactory;
	private final UIComponentFactorySPI servicePanelComponentFactory;
	private final UIComponentFactorySPI contextualViewComponentFactory;
	private final UIComponentFactorySPI workflowExplorerFactory;
	private final UIComponentFactorySPI reportViewComponentFactory;

	public DesignPerspectiveComponent(UIComponentFactorySPI graphViewComponentFactory,
			UIComponentFactorySPI servicePanelComponentFactory,
			UIComponentFactorySPI contextualViewComponentFactory,
			UIComponentFactorySPI workflowExplorerFactory,
			UIComponentFactorySPI reportViewComponentFactory) {
		this.graphViewComponentFactory = graphViewComponentFactory;
		this.servicePanelComponentFactory = servicePanelComponentFactory;
		this.contextualViewComponentFactory = contextualViewComponentFactory;
		this.workflowExplorerFactory = workflowExplorerFactory;
		this.reportViewComponentFactory = reportViewComponentFactory;

		setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		setDividerLocation(0.49949545913218973);
		setLeftComponent(createLeftComponent());
		setRightComponent(createRightComponent());
	}

	/**
	 * @return
	 */
	private Component createLeftComponent() {
		JSplitPane leftComponent = new JSplitPane();
		leftComponent.setOrientation(JSplitPane.VERTICAL_SPLIT);
		leftComponent.setDividerLocation(0.381635581061693);

		leftComponent.setLeftComponent((Component) servicePanelComponentFactory.getComponent());

		JTabbedPane rightComponent = new JTabbedPane();
//		rightComponent.addTab("Workflow explorer", (Component) workflowExplorerFactory.getComponent());
//		rightComponent.addTab("Details", (Component) contextualViewComponentFactory.getComponent());
//		rightComponent.addTab("Validation report",  (Component) reportViewComponentFactory.getComponent());
		leftComponent.setRightComponent(rightComponent);

		return leftComponent;
	}

	/**
	 * @return
	 */
	private Component createRightComponent() {
		return (Component) graphViewComponentFactory.getComponent();
	}

}
