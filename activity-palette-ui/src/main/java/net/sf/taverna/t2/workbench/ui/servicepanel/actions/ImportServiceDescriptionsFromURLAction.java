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
package net.sf.taverna.t2.workbench.ui.servicepanel.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;

/**
 * Action to import a list of service descriptions from an URL pointing 
 * to an xml file into the Service Registry. Users have an option to 
 * completely replace the current services or just add the ones from the 
 * file to the current services.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ImportServiceDescriptionsFromURLAction extends AbstractAction{

	private static final String IMPORT_SERVICES_FROM_URL = "Import services from URL";

	private Logger logger = Logger.getLogger(ExportServiceDescriptionsAction.class);
	
	public ImportServiceDescriptionsFromURLAction(){
		super(IMPORT_SERVICES_FROM_URL);
	}
	
	public void actionPerformed(ActionEvent e) {
	
		JComponent parentComponent = null;
		if (e.getSource() instanceof JComponent) {
			parentComponent = (JComponent) e.getSource();
		}

		Object[] options = { "Add to current services",
				"Replace current services", "Cancel" };
		int choice = JOptionPane
				.showOptionDialog(
						parentComponent,
						"Do you want to add the imported services to the current ones or replace the current ones?",
						"Import services", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (choice != JOptionPane.CANCEL_OPTION) {

			final String urlString = (String) JOptionPane.showInputDialog(parentComponent,
					"Enter the URL of the service descriptions file to import",
					"Service Descriptions URL", JOptionPane.QUESTION_MESSAGE, null, null,
					"http://");
			if (urlString != null) {
				
				// TODO: Open in separate thread to avoid hanging UI
				try {
					// Did user want to replace or add services?
					if (choice == JOptionPane.YES_OPTION) {
						addServices(urlString);
					} else {
						replaceServices(urlString);
					}
				} catch (Exception ex) {
					logger.error(
							"Service descriptions import: failed to import services from "
									+ urlString, ex);
					JOptionPane.showMessageDialog(parentComponent,
							"Failed to import services from "
									+ urlString, "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		if (parentComponent instanceof JButton) {
			// lose the focus from the button after performing the action
			parentComponent.requestFocusInWindow();
		}
	}

	private void replaceServices(String urlString) throws Exception {
		
		ServiceDescriptionRegistryImpl serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
		.getInstance();
		
		Set<ServiceDescriptionProvider> providers = serviceDescriptionRegistry.getServiceDescriptionProviders();
		Set<ServiceDescriptionProvider> providersCopy = new HashSet<ServiceDescriptionProvider>(providers);

		for (ServiceDescriptionProvider provider : providersCopy) {
			// remove all configurable service providers
			if (provider instanceof ConfigurableServiceProvider<?>) {
				serviceDescriptionRegistry
						.removeServiceDescriptionProvider(provider);
			}
		}

		// import all providers from the file
		addServices(urlString);		
	}

	private void addServices(String urlString) throws Exception {
		
		URL url = new URL(urlString);
		
		ServiceDescriptionRegistry serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
		.getInstance();

		serviceDescriptionRegistry.loadServiceProviders(url);		
		
		serviceDescriptionRegistry.saveServiceDescriptions();
	}

}

