/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.servicepanel;

import javax.swing.ImageIcon;

import uk.org.taverna.commons.services.ServiceRegistry;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * Service panel factory
 *
 * @author Stian Soiland-Reyes
 */
public class ServicePanelComponentFactory implements UIComponentFactorySPI {

	private ServiceDescriptionRegistry serviceDescriptionRegistry;
	private EditManager editManager;
	private MenuManager menuManager;
	private SelectionManager selectionManager;
	private ServiceRegistry serviceRegistry;

	public UIComponentSPI getComponent() {
		return new ServicePanel(serviceDescriptionRegistry, editManager, menuManager, selectionManager, serviceRegistry);
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Service panel";
	}

	public void setServiceDescriptionRegistry(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

}
