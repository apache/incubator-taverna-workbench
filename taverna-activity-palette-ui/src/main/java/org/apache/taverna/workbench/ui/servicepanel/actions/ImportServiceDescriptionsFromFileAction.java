/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.workbench.ui.servicepanel.actions;

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashSet;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import org.apache.taverna.servicedescriptions.ConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;

/**
 * Action to import a list of service descriptions from an xml file
 * into the Service Registry. Users have an option to completely
 * replace the current services or just add the ones from the file
 * to the current services.
 *
 * @author Alex Nenadic
 */
//FIXME this file assumes we're writing out as XML
@SuppressWarnings("serial")
public class ImportServiceDescriptionsFromFileAction extends AbstractAction{
	private static final String EXTENSION = ".xml";
	private static final String IMPORT_SERVICES = "Import services from file";
	private static final String SERVICE_IMPORT_DIR_PROPERTY = "serviceImportDir";
	private static final Logger logger = Logger.getLogger(ExportServiceDescriptionsAction.class);

	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public ImportServiceDescriptionsFromFileAction(
			ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super(IMPORT_SERVICES);
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
			JFileChooser fileChooser = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(getClass());
			String curDir = prefs.get(SERVICE_IMPORT_DIR_PROPERTY, System.getProperty("user.home"));

			fileChooser.setDialogTitle("Select file to import services from");

			fileChooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory()
							|| f.getName().toLowerCase().endsWith(EXTENSION);
				}

				@Override
				public String getDescription() {
					return EXTENSION + " files";
				}
			});

			fileChooser.setCurrentDirectory(new File(curDir));

			if (fileChooser.showOpenDialog(parentComponent) == APPROVE_OPTION) {
				prefs.put(SERVICE_IMPORT_DIR_PROPERTY, fileChooser
						.getCurrentDirectory().toString());
				File file = fileChooser.getSelectedFile();

				try {
					// Did user want to replace or add services?
					importServices(file, choice == YES_OPTION);
				} catch (Exception ex) {
					logger.error(
							"Service descriptions import: failed to import services from "
									+ file.getAbsolutePath(), ex);
					showMessageDialog(parentComponent,
							"Failed to import services from " + file.getAbsolutePath(), "Error",
							ERROR_MESSAGE);
				}
			}
		}

		if (parentComponent instanceof JButton)
			// lose the focus from the button after performing the action
			parentComponent.requestFocusInWindow();
	}

	private void importServices(final File file, final boolean addToCurrent)
			throws Exception {
		// TODO: Open in separate thread to avoid hanging UI

		if (!addToCurrent)
			for (ServiceDescriptionProvider provider : new HashSet<>(
					serviceDescriptionRegistry.getServiceDescriptionProviders()))
				// remove all configurable service providers
				if (provider instanceof ConfigurableServiceProvider)
					serviceDescriptionRegistry
							.removeServiceDescriptionProvider(provider);

		// import all providers from the file
		serviceDescriptionRegistry.loadServiceProviders(file);
		serviceDescriptionRegistry.saveServiceDescriptions();
	}
}

