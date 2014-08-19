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
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;

import org.apache.log4j.Logger;

/**
 * Action to import a list of service descriptions from an xml file into the
 * Service Registry. Users have an option to completely replace the current
 * services or just add the ones from the file to the current services.
 * 
 * @author Alex Nenadic
 * 
 */
@SuppressWarnings("serial")
public class ImportServiceDescriptionsFromFileAction extends AbstractAction {

	private static final String IMPORT_SERVICES = "Import services from file";
	private static final String SERVICE_IMPORT_DIR_PROPERTY = "serviceImportDir";
	private final Logger logger = Logger
			.getLogger(ExportServiceDescriptionsAction.class);

	public ImportServiceDescriptionsFromFileAction() {
		super(IMPORT_SERVICES);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {

		JComponent parentComponent = null;
		if (e.getSource() instanceof JComponent) {
			parentComponent = (JComponent) e.getSource();
		}

		final Object[] options = { "Add to current services",
				"Replace current services", "Cancel" };
		final int choice = JOptionPane
				.showOptionDialog(
						parentComponent,
						"Do you want to add the imported services to the current ones or replace the current ones?",
						"Import services", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (choice != JOptionPane.CANCEL_OPTION) {

			final JFileChooser fileChooser = new JFileChooser();
			final Preferences prefs = Preferences
					.userNodeForPackage(getClass());
			final String curDir = prefs.get(SERVICE_IMPORT_DIR_PROPERTY,
					System.getProperty("user.home"));

			fileChooser.setDialogTitle("Select file to import services from");

			fileChooser.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(final File f) {
					return f.isDirectory()
							|| f.getName().toLowerCase().endsWith(".xml");
				}

				@Override
				public String getDescription() {
					return ".xml files";
				}
			});

			fileChooser.setCurrentDirectory(new File(curDir));

			final int returnVal = fileChooser.showOpenDialog(parentComponent);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				prefs.put(SERVICE_IMPORT_DIR_PROPERTY, fileChooser
						.getCurrentDirectory().toString());
				final File file = fileChooser.getSelectedFile();

				// TODO: Open in separate thread to avoid hanging UI
				try {
					// Did user want to replace or add services?
					if (choice == JOptionPane.YES_OPTION) {
						addServices(file);
					} else {
						replaceServices(file);
					}
				} catch (final Exception ex) {
					logger.error(
							"Service descriptions import: failed to import services from "
									+ file.getAbsolutePath(), ex);
					JOptionPane.showMessageDialog(
							parentComponent,
							"Failed to import services from "
									+ file.getAbsolutePath(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}

		if (parentComponent instanceof JButton) {
			// lose the focus from the button after performing the action
			parentComponent.requestFocusInWindow();
		}
	}

	private void replaceServices(final File file) throws Exception {
		final ServiceDescriptionRegistryImpl serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
				.getInstance();

		final Set<ServiceDescriptionProvider> providers = serviceDescriptionRegistry
				.getServiceDescriptionProviders();
		final Set<ServiceDescriptionProvider> providersCopy = new HashSet<ServiceDescriptionProvider>(
				providers);

		for (final ServiceDescriptionProvider provider : providersCopy) {
			// remove all configurable service providers
			if (provider instanceof ConfigurableServiceProvider<?>) {
				serviceDescriptionRegistry
						.removeServiceDescriptionProvider(provider);
			}
		}

		// import all providers from the file
		addServices(file);
	}

	private void addServices(final File file) throws Exception {
		final ServiceDescriptionRegistry serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
				.getInstance();

		serviceDescriptionRegistry.loadServiceProviders(file);

		serviceDescriptionRegistry.saveServiceDescriptions();
	}

}
