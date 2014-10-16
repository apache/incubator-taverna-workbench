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

import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;

import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;

import org.apache.log4j.Logger;

/**
 * Action to import a list of service descriptions from an URL pointing
 * to an xml file into the Service Registry. Users have an option to
 * completely replace the current services or just add the ones from the
 * file to the current services.
 *
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class ImportServiceDescriptionsFromURLAction extends AbstractAction{
	private static final String IMPORT_SERVICES_FROM_URL = "Import services from URL";
	private static final Logger logger = Logger.getLogger(ExportServiceDescriptionsAction.class);

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public ImportServiceDescriptionsFromURLAction(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super(IMPORT_SERVICES_FROM_URL);
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	private static final Object[] CHOICES = { "Add to current services",
			"Replace current services", "Cancel" };

	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent parentComponent = null;
		if (e.getSource() instanceof JComponent)
			parentComponent = (JComponent) e.getSource();

		if (ExportServiceDescriptionsAction.INHIBIT) {
			showMessageDialog(parentComponent,
					"Operation not currently working correctly",
					"Not Implemented", ERROR_MESSAGE);
			return;
		}

		int choice = showOptionDialog(
				parentComponent,
				"Do you want to add the imported services to the current ones or replace the current ones?",
				"Import services", YES_NO_CANCEL_OPTION, QUESTION_MESSAGE,
				null, CHOICES, CHOICES[0]);

		if (choice != CANCEL_OPTION) {
			final String urlString = (String) showInputDialog(parentComponent,
					"Enter the URL of the service descriptions file to import",
					"Service Descriptions URL", QUESTION_MESSAGE, null, null,
					"http://");
			try {
				if (urlString != null && !urlString.isEmpty())
					// Did user want to replace or add services?
					importServices(urlString, choice == YES_OPTION);
			} catch (Exception ex) {
				logger.error(
						"Service descriptions import: failed to import services from "
								+ urlString, ex);
				showMessageDialog(parentComponent,
						"Failed to import services from " + urlString, "Error",
						ERROR_MESSAGE);
			}
		}

		if (parentComponent instanceof JButton)
			// lose the focus from the button after performing the action
			parentComponent.requestFocusInWindow();
	}

	private void importServices(final String urlString, final boolean addToCurrent)
			throws Exception {
		// TODO: Open in separate thread to avoid hanging UI
		URL url = new URL(urlString);

		if (!addToCurrent)
			for (ServiceDescriptionProvider provider : new HashSet<>(
					serviceDescriptionRegistry.getServiceDescriptionProviders()))
				// remove all configurable service providers
				if (provider instanceof ConfigurableServiceProvider)
					serviceDescriptionRegistry
							.removeServiceDescriptionProvider(provider);

		// import all providers from the URL
		serviceDescriptionRegistry.loadServiceProviders(url);
		serviceDescriptionRegistry.saveServiceDescriptions();
	}
}
