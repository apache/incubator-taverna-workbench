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
package net.sf.taverna.t2.workbench.provenance;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.provenance.ProvenanceConnectorRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;

import org.apache.log4j.Logger;

/**
 * Provides user interface for selecting a {@link ProvenanceConnector} to
 * collect data about a workflow run. Allows the user to create and clear the
 * database. Each implementation of {@link ProvenanceConnector} can have its own
 * Configuraton panel specific to its own setup needs
 * 
 * @author Ian Dunlop
 * 
 */
public class ProvenanceConfigurationPanel extends JPanel {

	private static Logger logger = Logger
			.getLogger(ProvenanceConfigurationPanel.class);

	private ProvenanceConfiguration provenanceConfiguration = ProvenanceConfiguration
			.getInstance();

	private ProvenanceConnector provenanceConnector;

	private ContextualView view;
	private JPanel viewHolder;
	private Action configureAction;

	/**
	 * Sets up the UI. Creates a panel with buttons and menus for selecting a
	 * ProvenanceConnector, enabling it and when a connector is selected it
	 * finds its own config panel and displays it
	 */
	public ProvenanceConfigurationPanel() {

		// FIXME this is now really hard coded for derby type of database where
		// no configuration is required, the
		// consensus was that users do not want to click buttons etc. This does
		// leave some problems if someone wants to use
		// eg the mysql type connector which will require user name, password
		// etc. I think all of the config will have to be moved to the
		// individual
		// connector config view rather than this panel doing much. Basically
		// when the check box is selected the connector config view
		// has to do something to make sure it will all work rather than the
		// next couple of lines setting up the database
		// create the database
		// the code in run workflow now has to call create db before doing
		// anything

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		viewHolder = new JPanel();

		JPanel onPanel = new JPanel();
		// BoxLayout layout = new BoxLayout(onPanel, BoxLayout.X_AXIS);
		//
		onPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		// JPanel dropPanel = new JPanel();
		// BoxLayout layout2 = new BoxLayout(dropPanel, BoxLayout.X_AXIS);
		// dropPanel.setLayout(layout2);

		JPanel buttonPanel = new JPanel();
		BoxLayout layout3 = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel storageConfig = new JLabel(
				"Enable provenance capture from workflow runs");

		final JLabel provSelectedLabel = new JLabel(
				"No provenance connector has been enabled");
		provSelectedLabel.setForeground(Color.red);

		final JCheckBox checkBox = new JCheckBox();
		checkBox.setEnabled(false);

		if (provenanceConfiguration.getProperty("enabled").equalsIgnoreCase(
				"yes")) {
			checkBox.setEnabled(true);
			checkBox.setSelected(true);
		}

		final JComboBox connectorChooser = new JComboBox();
		connectorChooser
				.setToolTipText("To enable provenance select a provenance connector from the drop down and ensure that the check box is selected");
		// connectorChooser.setMaximumSize(new Dimension(150,20));

		String property = provenanceConfiguration.getProperty("connector");
		for (ProvenanceConnector connector : ProvenanceConnectorRegistry
				.getInstance().getInstances()) {
			connectorChooser.addItem(connector);
			// FIXME this 'none' check seems a bit dodgy but it looks like that
			// is what the abstract config class returns if there is no property
			if (!property.equalsIgnoreCase("none")) {
				logger.info("Currently selected provenance connector is: "
						+ property);
				checkBox.setEnabled(true);
				if (property.equalsIgnoreCase(connector.getName())) {
					connectorChooser.setSelectedItem(connector);
					provenanceConnector = (ProvenanceConnector) connectorChooser
							.getSelectedItem();
					try {
						ContextualViewFactory viewFactoryForObject = ContextualViewFactoryRegistry
								.getInstance().getViewFactoryForObject(
										provenanceConnector);
						view = viewFactoryForObject
								.getView(provenanceConnector);
					} catch (Exception e) {
						logger.info("there is no configview for the: "
								+ provenanceConnector.getName());
					}
					viewHolder.removeAll();
					viewHolder.revalidate();
					if (view != null) {
						configureAction = view.getConfigureAction(null);
						viewHolder.add(view);
						viewHolder.revalidate();
						revalidate();
					}

				}
			}
		}

		connectorChooser.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// get the config view from the contextual view factory registry
				checkBox.setEnabled(true);
				provenanceConnector = (ProvenanceConnector) connectorChooser
						.getSelectedItem();
				logger.info("Selected provenance connector: "
						+ provenanceConnector.getClass().getSimpleName());
				try {
					ContextualViewFactory viewFactoryForObject = ContextualViewFactoryRegistry
							.getInstance().getViewFactoryForObject(
									provenanceConnector);
					view = viewFactoryForObject.getView(provenanceConnector);
					configureAction = view.getConfigureAction(null);
				} catch (Exception e1) {
					// probably OK, just no config view so set the view to null
					// and continue
					view = null;
				}
				viewHolder.removeAll();
				viewHolder.revalidate();
				if (view != null) {
					viewHolder.add(view);
					viewHolder.revalidate();
					revalidate();
				} else {
					viewHolder.removeAll();
					viewHolder.revalidate();
					revalidate();
				}

			}

		});
		checkBox.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				provenanceConnector = (ProvenanceConnector) connectorChooser
						.getSelectedItem();
				if (checkBox.isSelected()) {
					if (provenanceConnector != null) {
						logger.info("Provenance has been enabled");
						String name = provenanceConnector.getName();
						provSelectedLabel.setText(name
								+ " will be used for provenance storage");
						provSelectedLabel.setForeground(Color.green);

						provenanceConfiguration.setProperty("connector", name);
						provenanceConfiguration.setProperty("enabled", "yes");
					}
					// execute the selected connectors config action
					if (configureAction != null) {
						configureAction.actionPerformed(null);
						provenanceConnector.setDbURL(ProvenanceConfiguration
								.getInstance().getProperty("dbURL"));
					}

				} else if (!checkBox.isSelected()) {
					logger.info("Provenance has been disabled");
					provenanceConfiguration.setProperty("enabled", "no");
					provSelectedLabel
							.setText("No provenance connector has been enabled");
					provSelectedLabel.setForeground(Color.red);
					if (provenanceConnector != null) {
						String name = provenanceConnector.getName();
						provenanceConfiguration.setProperty("connector", name);
					}
					// execute the selected connectors config action
					if (configureAction != null) {
						configureAction.actionPerformed(null);
						provenanceConnector.setDbURL(ProvenanceConfiguration
								.getInstance().getProperty("dbURL"));
					}
				}
			}

		});

		onPanel.add(storageConfig);
		onPanel.add(checkBox);

		// final JButton deleteData = new JButton("Delete current provenance");
		// deleteData.addActionListener(new AbstractAction() {
		//
		// public void actionPerformed(ActionEvent e) {
		// // get the selected ProvenanceConnector and call the delete
		// // function
		// }
		//
		// });

		// final JButton applyButton = new JButton("Apply");
		// applyButton.setToolTipText("Save current settings");
		// // saves the user selected options in the ProvenanceConfiguration
		// // properties file
		// applyButton.addActionListener(new ActionListener() {
		//
		// public void actionPerformed(ActionEvent e) {
		// provenanceConnector = (ProvenanceConnector) connectorChooser
		// .getSelectedItem();
		// if (checkBox.isSelected()) {
		// if (provenanceConnector != null) {
		// logger.info("Provenance has been enabled");
		// String name = provenanceConnector.getName();
		//
		// provenanceConfiguration.setProperty("connector", name);
		// provenanceConfiguration.setProperty("enabled", "yes");
		// }
		// // execute the selected connectors config action
		// if (configureAction != null) {
		// configureAction.actionPerformed(null);
		// provenanceConnector.setDbURL(ProvenanceConfiguration
		// .getInstance().getProperty("dbURL"));
		// }
		//
		// } else if (!checkBox.isSelected()) {
		// logger.info("Provenance has been disabled");
		// provenanceConfiguration.setProperty("enabled", "no");
		// if (provenanceConnector != null) {
		// String name = provenanceConnector.getName();
		// provenanceConfiguration.setProperty("connector", name);
		//
		// }
		// // execute the selected connectors config action
		// if (configureAction != null) {
		// configureAction.actionPerformed(null);
		// provenanceConnector.setDbURL(ProvenanceConfiguration
		// .getInstance().getProperty("dbURL"));
		// }
		// }
		// }
		//
		// });
		// no longer want this apply button, the db will be created
		// automatically when check box is selected
		// buttonPanel.add(applyButton);

		final JButton clearButton = new JButton("Clear Database");
		clearButton
				.setToolTipText("Remove the provenance but keep the database and the tables");
		clearButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				if (provenanceConnector != null) {
					int n = JOptionPane
							.showConfirmDialog(
									clearButton,
									"Are you sure you want to clear all the provenance in the database?",
									"Clear Database", JOptionPane.YES_NO_OPTION);

					if (n == JOptionPane.YES_OPTION) {
						provenanceConnector.setDbURL(ProvenanceConfiguration
								.getInstance().getProperty("dbURL"));
						try {
							provenanceConnector.clearDatabase();
						} catch (Exception e1) {
							JOptionPane.showMessageDialog(clearButton,
									"Problem clearing the database: \n"
											+ e1.toString(), "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}

				}
			}

		});

		buttonPanel.add(clearButton);

		// JButton createButton = new JButton("Create Database");
		// createButton
		// .setToolTipText("Create the provenance database including all the tables required");
		// createButton.addActionListener(new AbstractAction() {
		//
		// public void actionPerformed(ActionEvent e) {
		// if (provenanceConnector != null) {
		// int n = JOptionPane
		// .showConfirmDialog(
		// applyButton,
		// "Are you sure you want to create the provenance database?",
		// "Create Database",
		// JOptionPane.YES_NO_OPTION);
		//
		// if (n == JOptionPane.YES_OPTION) {
		// provenanceConnector.setDbURL(ProvenanceConfiguration
		// .getInstance().getProperty("dbURL"));
		// provenanceConnector.createDatabase();
		// }
		//
		// }
		// }
		//
		// });

		// the db create will be done automatically when the connector is
		// selected
		// buttonPanel.add(createButton);

		// JButton deleteButton = new JButton("Delete Database");
		// deleteButton
		// .setToolTipText("Completely remove the provenance database");
		// deleteButton.addActionListener(new AbstractAction() {
		//
		// public void actionPerformed(ActionEvent e) {
		// if (provenanceConnector != null) {
		// int n = JOptionPane
		// .showConfirmDialog(
		// applyButton,
		// "Are you sure you want to delete the provenance database?",
		// "Delete Database",
		// JOptionPane.YES_NO_OPTION);
		//
		// if (n == JOptionPane.YES_OPTION) {
		// provenanceConnector.setDbURL(ProvenanceConfiguration
		// .getInstance().getProperty("dbURL"));
		// provenanceConnector.deleteDatabase();
		// }
		//
		// }
		// }
		//
		// });
		// FIXME does not work correctly with the derby-connector
		// buttonPanel.add(deleteButton);
		add(onPanel);
		add(Box.createVerticalStrut(5));
		JPanel boxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		if (property != null && checkBox.isSelected()) {
			provSelectedLabel.setText(property
					+ " will be used for provenance storage");
			provSelectedLabel.setForeground(Color.green);
		}

		labelPanel
				.add(new JLabel(
						"Select the database type for provenance storage from the drop down"));
		add(labelPanel);
		add(Box.createVerticalStrut(5));// labelPanel.setMaximumSize(new
		// Dimension(300,20));
		boxPanel.add(connectorChooser);
		// boxPanel.add(Box.createHorizontalGlue());
		add(boxPanel);
		add(Box.createVerticalStrut(5));
		// add(dropPanel);
		viewHolder.setBorder(BorderFactory.createBevelBorder(1));
		add(viewHolder);

		add(Box.createVerticalStrut(5));
		JPanel provSelectedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		provSelectedPanel.add(provSelectedLabel);
		add(provSelectedPanel);
		add(Box.createVerticalStrut(5));
		add(buttonPanel);

	}

	/**
	 * Tester class for checking the gui
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ProvenanceConfigurationPanel panel = new ProvenanceConfigurationPanel();
		JFrame frame = new JFrame();
		frame.add(panel);
		frame.setVisible(true);

	}

}
