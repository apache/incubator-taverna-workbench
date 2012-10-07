/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.WorkflowPerspective;

public class DesignPerspective implements PerspectiveSPI, WorkflowPerspective {

	private DesignPerspectiveComponent designPerspectiveComponent;

	private UIComponentFactorySPI graphViewComponentFactory;
	private UIComponentFactorySPI servicePanelComponentFactory;
	private UIComponentFactorySPI contextualViewComponentFactory;
	private UIComponentFactorySPI workflowExplorerFactory;
	private UIComponentFactorySPI reportViewComponentFactory;

	@Override
	public String getID() {
		return this.getClass().getName();
	}

	@Override
	public JComponent getPanel() {
		if (designPerspectiveComponent == null) {
			designPerspectiveComponent = new DesignPerspectiveComponent(
					graphViewComponentFactory, servicePanelComponentFactory,
					contextualViewComponentFactory, workflowExplorerFactory, reportViewComponentFactory);
		}
		return designPerspectiveComponent;
	}

	@Override
	public ImageIcon getButtonIcon() {
		return WorkbenchIcons.editIcon;
	}

	@Override
	public String getText() {
		return "Design";
	}

	@Override
	public int positionHint() {
		return 10;
	}

	public void setGraphViewComponentFactory(UIComponentFactorySPI graphViewComponentFactory) {
		this.graphViewComponentFactory = graphViewComponentFactory;
	}

	public void setServicePanelComponentFactory(UIComponentFactorySPI servicePanelComponentFactory) {
		this.servicePanelComponentFactory = servicePanelComponentFactory;
	}

	public void setContextualViewComponentFactory(UIComponentFactorySPI contextualViewComponentFactory) {
		this.contextualViewComponentFactory = contextualViewComponentFactory;
	}

	public void setWorkflowExplorerFactory(UIComponentFactorySPI workflowExplorerFactory) {
		this.workflowExplorerFactory = workflowExplorerFactory;
	}

	public void setReportViewComponentFactory(UIComponentFactorySPI reportViewComponentFactory) {
		this.reportViewComponentFactory = reportViewComponentFactory;
	}

}
