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

	private JCheckBox checkBox;

	private JComboBox connectorChooser;

	private JButton clearButton;

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
		onPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JPanel buttonPanel = new JPanel();
		BoxLayout layout3 = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel storageConfig = new JLabel(
				"Enable provenance capture from workflow runs");

		checkBox = new JCheckBox();

		connectorChooser = new JComboBox();
		connectorChooser
				.setToolTipText("To enable provenance select a provenance connector from the drop down and ensure that the check box is selected");

		String connectorName = provenanceConfiguration.getProperty("connector");
		String enabled = provenanceConfiguration.getProperty("enabled");
		for (ProvenanceConnector connector : ProvenanceConnectorRegistry
				.getInstance().getInstances()) {
			connectorChooser.addItem(connector);
		}

		connectorChooser.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// get the config view from the contextual view factory registry
				Object selectedItem = connectorChooser.getSelectedItem();

				provenanceConnector = (ProvenanceConnector) selectedItem;
				logger.info("Selected provenance connector: "
						+ provenanceConnector.getClass().getSimpleName());
				showConfigView(provenanceConnector);
			}

		});
		checkBox.addActionListener(new AbstractAction() {

			public void actionPerformed(ActionEvent e) {
				if (checkBox.isSelected()) {
					connectorChooser.setEnabled(true);
					String connectorName = provenanceConfiguration
							.getProperty("connector");
					if (connectorName.equals("none")) {
						if (connectorChooser.getItemCount() >= 1) {
							String name2 = ((ProvenanceConnector)connectorChooser.getItemAt(0)).getName();
							try {
								setChosenConnector(name2);
							} catch (Exception e1) {
								JOptionPane
								.showMessageDialog(
										viewHolder,
										e1,
										"Error", JOptionPane.ERROR_MESSAGE);
							}
						} else {
							// pop up some dialogue
							JOptionPane
									.showMessageDialog(
											viewHolder,
											"No provenance connectors are available.\nPlease check your plugins.",
											"Error", JOptionPane.ERROR_MESSAGE);
							// set enable to 'no'
							provenanceConfiguration
									.setProperty("enabled", "no");
							checkBox.setSelected(false);
							viewHolder.setVisible(false);
						}
					} else {
						try {
							setChosenConnector(connectorName);
						} catch (Exception e1) {
							JOptionPane
							.showMessageDialog(
									viewHolder,
									e1,
									"Error", JOptionPane.ERROR_MESSAGE);
						}
					}
				} else {
					clearButton.setEnabled(false);
					connectorChooser.setEnabled(false);
					provenanceConfiguration.setProperty("enabled", "no");
					viewHolder.setVisible(false);
				}

			}

		});

		onPanel.add(storageConfig);
		onPanel.add(checkBox);

		clearButton = new JButton("Clear Database");
		clearButton
				.setToolTipText("Remove the provenance but keep the database and the tables");
		clearButton.setEnabled(false);
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

		add(onPanel);
		add(Box.createVerticalStrut(5));
		JPanel boxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

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

		add(buttonPanel);
		setSelectedState();

	}

	protected void removeConfigView() {
		viewHolder.removeAll();
		viewHolder.revalidate();
		revalidate();
	}

	private void setSelectedState() {
		String connectorName = provenanceConfiguration.getProperty("connector");
		String enabled = provenanceConfiguration.getProperty("enabled");
		boolean connectorLoaded = false;
		if (!connectorName.equals("none")) {
			try {
				setChosenConnector(connectorName);
			} catch (Exception e) {
				JOptionPane
				.showMessageDialog(
						viewHolder,
						e,
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} else if (connectorChooser.getItemCount() >= 1) {
			String name2 = ((ProvenanceConnector)connectorChooser.getItemAt(0)).getName();
			try {
				setChosenConnector(name2);
			} catch (Exception e1) {
				JOptionPane
				.showMessageDialog(
						viewHolder,
						e1,
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			// pop up some dialogue
			JOptionPane
					.showMessageDialog(
							viewHolder,
							"No provenance connectors are available.\nPlease check your plugins.",
							"Error", JOptionPane.ERROR_MESSAGE);
			// set enable to 'no'
			provenanceConfiguration
					.setProperty("enabled", "no");
			checkBox.setSelected(false);
			viewHolder.setVisible(false);
		}
		if (enabled.equals("no") || enabled.equals("none")) {
			checkBox.setSelected(false);
			clearButton.setEnabled(false);
			viewHolder.setVisible(false);
		} else {
			checkBox.setSelected(true);
			clearButton.setEnabled(true);
			viewHolder.setVisible(true);
		}
	}

	private void showConfigView(ProvenanceConnector provenanceConnector) {
		try {
			ContextualViewFactory viewFactoryForObject = ContextualViewFactoryRegistry
					.getInstance().getViewFactoryForObject(provenanceConnector);
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

	private void setChosenConnector(String connectorName) throws Exception {
		boolean connectorLoaded = false;
		for (int i = 0; i < connectorChooser.getItemCount(); i++) {
			if (((ProvenanceConnector) connectorChooser.getItemAt(i)).getName()
					.equals(connectorName)) {
				connectorLoaded = true;
				provenanceConnector = (ProvenanceConnector) connectorChooser
						.getItemAt(i);
				connectorChooser.setSelectedItem(connectorName);
				provenanceConfiguration.setProperty("enabled", "yes");
				provenanceConfiguration.setProperty("connector",
						provenanceConnector.getName());
				showConfigView(provenanceConnector);
				viewHolder.setVisible(true);
				clearButton.setEnabled(true);
				break;
			}
		}
		if (!connectorLoaded) {
			throw new Exception("Provenance Connector: " + connectorName
					+ " was not loaded\nPlease check your plugins");
		}
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
