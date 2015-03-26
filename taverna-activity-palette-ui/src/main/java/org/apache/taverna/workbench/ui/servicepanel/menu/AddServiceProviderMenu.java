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

package org.apache.taverna.workbench.ui.servicepanel.menu;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

import org.apache.taverna.servicedescriptions.ConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.workbench.ui.servicepanel.ServicePanel;
import org.apache.taverna.workbench.ui.servicepanel.actions.AddServiceProviderAction;

/**
 * A menu that provides a set up menu actions for adding new service providers
 * to the Service Panel.
 * <p>
 * The Actions are discovered from the {@link ServiceDescriptionProvider}s found
 * through the SPI.
 *
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 * @author Alan R Williams
 *
 * @see ServiceDescription
 * @see ServicePanel
 * @see ServiceDescriptionRegistry#addServiceDescriptionProvider(ServiceDescriptionProvider)
 */
@SuppressWarnings("serial")
public class AddServiceProviderMenu extends JButton {
	public static class ServiceProviderComparator implements
			Comparator<ServiceDescriptionProvider> {
		@Override
		public int compare(ServiceDescriptionProvider o1,
				ServiceDescriptionProvider o2) {
			return o1.getName().toLowerCase().compareTo(
					o2.getName().toLowerCase());
		}
	}

	private final static String ADD_SERVICE_PROVIDER_MENU_NAME = "Import new services";

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public AddServiceProviderMenu(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super();
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;

		final Component c = createCustomComponent();
		setAction(new AbstractAction(ADD_SERVICE_PROVIDER_MENU_NAME) {
			@Override
			public void actionPerformed(ActionEvent e) {
				((JPopupMenu) c).show(AddServiceProviderMenu.this, 0,
						AddServiceProviderMenu.this.getHeight());
			}
		});
	}

	private Component createCustomComponent() {
		JPopupMenu addServiceMenu = new JPopupMenu(
				ADD_SERVICE_PROVIDER_MENU_NAME);
		addServiceMenu.setToolTipText("Add a new service provider");
		boolean isEmpty = true;
		List<ConfigurableServiceProvider> providers = new ArrayList<>(
				serviceDescriptionRegistry.getUnconfiguredServiceProviders());
		Collections.sort(providers,  new ServiceProviderComparator());
		for (ConfigurableServiceProvider provider : providers) {
			/*
			 * Skip BioCatalogue's ConfigurableServiceProviderS as they should
			 * not be used to add servcie directlry but rather though the
			 * Service Catalogue perspective
			 */
			if (provider.getId().toLowerCase().contains("servicecatalogue"))
				continue;

			AddServiceProviderAction addAction = new AddServiceProviderAction(
					provider, this);
			addAction.setServiceDescriptionRegistry(serviceDescriptionRegistry);
			addServiceMenu.add(addAction);
			isEmpty = false;
		}
		if (isEmpty)
			addServiceMenu.setEnabled(false);
		return addServiceMenu;
	}
}
