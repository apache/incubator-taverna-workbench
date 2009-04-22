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
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;
import net.sf.taverna.t2.workbench.ui.servicepanel.ServicePanel;
import net.sf.taverna.t2.workbench.ui.servicepanel.actions.AddServiceProviderAction;

/**
 * A menu that provides a set up menu actions for adding new service providers to
 * the Service Panel.
 * <p>
 * The Actions are discovered from the {@link ServiceDescriptionProvider}s found through the
 * SPI.
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 * 
 * @see ServiceDescription
 * @see ServicePanel
 * @see ServiceDescriptionRegistry#addServiceDescriptionProvider(net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider)
 * 
 */
@SuppressWarnings("serial")
public class AddServiceProviderMenu extends JButton {
	
	private final static String ADD_SERVICE_PROVIDER_MENU_NAME = "Add Service";

	public AddServiceProviderMenu() {
		super();
		
		final Component c = createCustomComponent();
		this.setAction(new AbstractAction(ADD_SERVICE_PROVIDER_MENU_NAME){

			public void actionPerformed(ActionEvent e) {
				((JPopupMenu) c).show(AddServiceProviderMenu.this, 0, AddServiceProviderMenu.this.getHeight());
			}});
	}

	private ServiceDescriptionRegistry serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
			.getInstance();

	@SuppressWarnings("unchecked")
	private Component createCustomComponent() {
		JPopupMenu addServiceMenu = new JPopupMenu("New service");
		addServiceMenu.setToolTipText("Add a new service provider");
		boolean isEmpty = true;
		for (ConfigurableServiceProvider provider : getServiceDescriptionRegistry()
				.getUnconfiguredServiceProviders()) {
			AddServiceProviderAction addAction = new AddServiceProviderAction(
					provider, this);
			addAction.setServiceDescriptionRegistry(getServiceDescriptionRegistry());
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
