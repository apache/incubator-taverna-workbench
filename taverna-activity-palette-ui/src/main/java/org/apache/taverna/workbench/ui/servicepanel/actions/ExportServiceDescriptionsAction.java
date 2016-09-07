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
/*

package org.apache.taverna.workbench.ui.servicepanel.actions;

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;

import org.apache.log4j.Logger;

/**
 * Action to export the current service descritpions from the Service
 * Registry to an xml file.
 *
 * @author Alex Nenadic
 */
//FIXME this file assumes we're writing out as XML
@SuppressWarnings("serial")
public class ExportServiceDescriptionsAction extends AbstractAction {
	private static final String EXTENSION = ".xml";
	private static final String EXPORT_SERVICES = "Export services to file";
	private static final String SERVICE_EXPORT_DIR_PROPERTY = "serviceExportDir";
	private Logger logger = Logger.getLogger(ExportServiceDescriptionsAction.class);
	private final ServiceDescriptionRegistry serviceDescriptionRegistry;

	public ExportServiceDescriptionsAction(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		super(EXPORT_SERVICES);
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public static final boolean INHIBIT = true;

	@Override
	public void actionPerformed(ActionEvent e) {
		JComponent parentComponent = null;
		if (e.getSource() instanceof JComponent)
			parentComponent = (JComponent) e.getSource();

		if (INHIBIT) {
			showMessageDialog(parentComponent,
					"Operation not currently working correctly",
					"Not Implemented", ERROR_MESSAGE);
			return;
		}

		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs.get(SERVICE_EXPORT_DIR_PROPERTY,
				System.getProperty("user.home"));
		fileChooser.setDialogTitle("Select file to export services to");

		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(EXTENSION);
			}

			@Override
			public String getDescription() {
				return ".xml files";
			}
		});

		fileChooser.setCurrentDirectory(new File(curDir));

		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			int returnVal = fileChooser.showSaveDialog(parentComponent);
			if (returnVal == APPROVE_OPTION) {
				prefs.put(SERVICE_EXPORT_DIR_PROPERTY, fileChooser.getCurrentDirectory()
						.toString());
				File file = fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(EXTENSION)) {
					String newName = file.getName() + EXTENSION;
					file = new File(file.getParentFile(), newName);
				}

				try {
					if (file.exists()) {
						String msg = "Are you sure you want to overwrite existing file "
								+ file + "?";
						int ret = showConfirmDialog(parentComponent, msg,
								"File already exists", YES_NO_CANCEL_OPTION);
						if (ret == NO_OPTION) {
							tryAgain = true;
							continue;
						} else if (ret != YES_OPTION) {
							logger.info("Service descriptions export: aborted overwrite of "
									+ file.getAbsolutePath());
							break;
						}
					}
					exportServiceDescriptions(file);
					break;
				} catch (Exception ex) {
					logger.error("Service descriptions export: failed to export services to "
							+ file.getAbsolutePath(), ex);
					showMessageDialog(
							parentComponent,
							"Failed to export services to "
									+ file.getAbsolutePath(), "Error",
							ERROR_MESSAGE);
					break;
				}
			}
		}

		if (parentComponent instanceof JButton)
			// lose the focus from the button after performing the action
			parentComponent.requestFocusInWindow();
	}

	private void exportServiceDescriptions(File file) {
		// TODO: Open in separate thread to avoid hanging UI
		serviceDescriptionRegistry.exportCurrentServiceDescriptions(file);
		logger.info("Service descriptions export: saved to file "
				+ file.getAbsolutePath());
	}
}
