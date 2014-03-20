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
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionRegistryImpl;

/**
 * Action to export the current service descritpions from the Service 
 * Registry to an xml file.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ExportServiceDescriptionsAction extends AbstractAction{

	private static final String EXPORT_SERVICES = "Export services to file";
	private static final String SERVICE_EXPORT_DIR_PROPERTY = "serviceExportDir";
	private Logger logger = Logger.getLogger(ExportServiceDescriptionsAction.class);
	
	public ExportServiceDescriptionsAction(){
		super(EXPORT_SERVICES);
	}
	
	public void actionPerformed(ActionEvent e) {

		JComponent parentComponent = null;
		if (e.getSource() instanceof JComponent){
			parentComponent = (JComponent)e.getSource();
		}
		
		JFileChooser fileChooser = new JFileChooser();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String curDir = prefs.get(SERVICE_EXPORT_DIR_PROPERTY, System.getProperty("user.home"));
		fileChooser.setDialogTitle("Select file to export services to");

		fileChooser.setFileFilter(new FileFilter() {
		   
			public boolean accept(File f) {
		        return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml");
		    }
		    
		    public String getDescription() {
		        return ".xml files";
		    }

		});

		fileChooser.setCurrentDirectory(new File(curDir));

		boolean tryAgain = true;
		while (tryAgain) {
			tryAgain = false;
			int returnVal = fileChooser.showSaveDialog(parentComponent);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				prefs.put(SERVICE_EXPORT_DIR_PROPERTY, fileChooser.getCurrentDirectory()
						.toString());
				File file = fileChooser.getSelectedFile();
				if (!file.getName().toLowerCase().endsWith(".xml")) {
					String newName = file.getName() + ".xml";
					file = new File(file.getParentFile(), newName);
				}

				// TODO: Open in separate thread to avoid hanging UI
				if (file.exists()) {
					String msg = "Are you sure you want to overwrite existing file "
							+ file + "?";
					int ret = JOptionPane.showConfirmDialog(parentComponent,
							msg, "File already exists",
							JOptionPane.YES_NO_CANCEL_OPTION);
					if (ret == JOptionPane.YES_OPTION) {
						try {
							ServiceDescriptionRegistry serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
									.getInstance();
							serviceDescriptionRegistry
									.exportCurrentServiceDescriptions(file);
							logger.info("Service descriptions export: saved to file "
											+ file.getAbsolutePath());
						} catch (Exception ex) {
							logger.error("Service descriptions export: failed to export services to "
											+ file.getAbsolutePath(), ex);
							JOptionPane.showMessageDialog(parentComponent,
									"Failed to export services to " + file.getAbsolutePath(),
									"Error", JOptionPane.ERROR_MESSAGE);
						}
						break;

					} else if (ret == JOptionPane.NO_OPTION) {
						tryAgain = true;
						continue;
					} else {
						logger.info("Service descriptions export: aborted overwrite of "
										+ file.getAbsolutePath());
						break;
					}
				}
				else{
					try {
						ServiceDescriptionRegistry serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
								.getInstance();
						serviceDescriptionRegistry
								.exportCurrentServiceDescriptions(file);
						logger.info("Service descriptions export: saved to file "
										+ file.getAbsolutePath());
					} catch (Exception ex) {
						logger.error("Service descriptions export: failed to export services to "
										+ file.getAbsolutePath(), ex);
						JOptionPane.showMessageDialog(parentComponent,
								"Failed to export services to " + file.getAbsolutePath(),
								"Error", JOptionPane.ERROR_MESSAGE);
					}
					break;
				}
			}
		}
		
		if (parentComponent instanceof JButton){
			// lose the focus from the button after performing the action
			parentComponent.requestFocusInWindow();
		}
	}

}
