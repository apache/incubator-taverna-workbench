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

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		viewHolder = new JPanel();

		JPanel onPanel = new JPanel();
//		BoxLayout layout = new BoxLayout(onPanel, BoxLayout.X_AXIS);
//
		onPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

//		JPanel dropPanel = new JPanel();
//		BoxLayout layout2 = new BoxLayout(dropPanel, BoxLayout.X_AXIS);
//		dropPanel.setLayout(layout2);

		JPanel buttonPanel = new JPanel();
		BoxLayout layout3 = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel storageConfig = new JLabel(
				"Enable provenance capture from workflow runs");

		final JCheckBox checkBox = new JCheckBox();


		if (provenanceConfiguration.getProperty("enabled").equalsIgnoreCase(
				"yes")) {
			checkBox.setSelected(true);
		}

		onPanel.add(storageConfig);
		onPanel.add(checkBox);


		final JComboBox connectorChooser = new JComboBox();
		
//		connectorChooser.setMaximumSize(new Dimension(150,20));

		String property = provenanceConfiguration.getProperty("connector");
		for (ProvenanceConnector connector : ProvenanceConnectorRegistry
				.getInstance().getInstances()) {
			connectorChooser.addItem(connector);
			if (property != null) {
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
					// probably OK, just no config view so set the view to null and continue
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

		JButton deleteData = new JButton("Delete current provenance");
		deleteData.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				// get the selected ProvenanceConnector and call the delete
				// function
			}

		});

		final JButton applyButton = new JButton("Apply");
		applyButton.setToolTipText("Save current settings");
		// saves the user selected options in the ProvenanceConfiguration
		// properties file
		applyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				provenanceConnector = (ProvenanceConnector) connectorChooser
						.getSelectedItem();
				if (checkBox.isSelected()) {
					if (provenanceConnector!=null) {
						logger.info("Provenance has been enabled");
						String name = provenanceConnector.getName();
						
						provenanceConfiguration.setProperty("connector", name);
						provenanceConfiguration.setProperty("enabled", "yes");
					}
					// execute the selected connectors config action
					if (configureAction != null) {
						configureAction.actionPerformed(null);
						provenanceConnector.setDbURL(ProvenanceConfiguration.getInstance().getProperty("dbURL"));
					}

				} else if (!checkBox.isSelected()) {
					logger.info("Provenance has been disabled");
					provenanceConfiguration.setProperty("enabled", "no");
					if (provenanceConnector!=null) {
						String name = provenanceConnector.getName();
						provenanceConfiguration.setProperty("connector", name);
						
					}
					// execute the selected connectors config action
					if (configureAction != null) {
						configureAction.actionPerformed(null);
						provenanceConnector.setDbURL(ProvenanceConfiguration.getInstance().getProperty("dbURL"));
					}
				}
			}

		});

		buttonPanel.add(applyButton);

		JButton clearButton = new JButton("Clear Database");
		clearButton.setToolTipText("Remove the provenance but keep the database and the tables");
		clearButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				if (provenanceConnector != null) {
					int n = JOptionPane
							.showConfirmDialog(
									applyButton,
									"Are you sure you want to clear all the provenance in the database?",
									"Clear Database",
									JOptionPane.YES_NO_OPTION);

					if (n == JOptionPane.YES_OPTION) {
						provenanceConnector.setDbURL(ProvenanceConfiguration.getInstance().getProperty("dbURL"));
						provenanceConnector.clearDatabase();
					}

				}
			}

		});

		buttonPanel.add(clearButton);
		
		JButton createButton = new JButton("Create Database");
		createButton.setToolTipText("Create the provenance database including all the tables required");
		createButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				if (provenanceConnector != null) {
					int n = JOptionPane
							.showConfirmDialog(
									applyButton,
									"Are you sure you want to create the provenance database?",
									"Create Database",									
									JOptionPane.YES_NO_OPTION);

					if (n == JOptionPane.YES_OPTION) {
						provenanceConnector.setDbURL(ProvenanceConfiguration.getInstance().getProperty("dbURL"));
						provenanceConnector.createDatabase();
					}

				}
			}
			
		});
		
		buttonPanel.add(createButton);
		
		JButton deleteButton = new JButton("Delete Database");
		deleteButton.setToolTipText("Completely remove the provenance database");
		deleteButton.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				if (provenanceConnector != null) {
					int n = JOptionPane
							.showConfirmDialog(
									applyButton,
									"Are you sure you want to delete the provenance database?",
									"Delete Database",
									JOptionPane.YES_NO_OPTION);

					if (n == JOptionPane.YES_OPTION) {
						provenanceConnector.setDbURL(ProvenanceConfiguration.getInstance().getProperty("dbURL"));
						provenanceConnector.deleteDatabase();
					}

				}
			}
			
		});
		//FIXME does not work correctly with the derby-connector
//		buttonPanel.add(deleteButton);
		add(onPanel);
		add(Box.createVerticalStrut(5));
		JPanel boxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		
		labelPanel.add (new JLabel("Select the database type for provenance storage from the drop down"));
		add(labelPanel);
		add(Box.createVerticalStrut(5));//		labelPanel.setMaximumSize(new Dimension(300,20));
		boxPanel.add(connectorChooser);
//		boxPanel.add(Box.createHorizontalGlue());
		add(boxPanel);
		add(Box.createVerticalStrut(5));
//		add(dropPanel);
		viewHolder.setBorder(BorderFactory.createBevelBorder(1));
		add(viewHolder);
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
