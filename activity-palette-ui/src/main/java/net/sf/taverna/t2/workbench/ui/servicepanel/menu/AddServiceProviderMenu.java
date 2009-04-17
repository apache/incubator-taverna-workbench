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
package net.sf.taverna.t2.workbench.ui.servicepanel.menu;

import java.awt.Component;
import java.net.URI;

import javax.swing.JMenu;

import net.sf.taverna.t2.partition.ActivityQueryFactory;
import net.sf.taverna.t2.partition.QueryFactory;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.workbench.ui.servicepanel.actions.AddServiceProviderAction;

/**
 * A menu that provides a set up menu actions for adding new Activity queries to
 * the Activity Palette. <br>
 * The Actions are discovered from the ActivityQueryFactory's found through the
 * QueryFactory SPI.
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 * 
 * @see ActivityQueryFactory
 * @see QueryFactory
 * 
 */
public class AddServiceProviderMenu extends AbstractMenuCustom {

	private static final URI ADD_SERVICE_PROVIDER_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#addServiceProvider");
	private static final URI ACTIVITY_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#activity");

	public AddServiceProviderMenu() {
		super(ACTIVITY_URI, 40, ADD_SERVICE_PROVIDER_URI);
	}

	private ServiceDescriptionRegistry serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
			.getInstance();

	@SuppressWarnings("unchecked")
	protected Component createCustomComponent() {
		JMenu addServiceMenu = new JMenu("New service");
		addServiceMenu.setToolTipText("Add a new service provider");
		boolean isEmpty = true;
		for (ConfigurableServiceProvider provider : getServiceDescriptionRegistry()
				.getUnconfiguredServiceProviders()) {
			AddServiceProviderAction addAction = new AddServiceProviderAction(
					provider);
			addAction
					.setServiceDescriptionRegistry(getServiceDescriptionRegistry());
			addServiceMenu.add(addAction);
			isEmpty = false;
		}
		if (isEmpty) {
			addServiceMenu.setEnabled(false);
		}
		return addServiceMenu;
	}

	public void setServiceDescriptionRegistry(
			ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public ServiceDescriptionRegistry getServiceDescriptionRegistry() {
		return serviceDescriptionRegistry;
	}

}
