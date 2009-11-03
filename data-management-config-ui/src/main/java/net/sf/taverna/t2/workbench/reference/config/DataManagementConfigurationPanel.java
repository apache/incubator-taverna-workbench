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
package net.sf.taverna.t2.workbench.reference.config;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.workbench.helper.Helper;
import net.sf.taverna.t2.workbench.run.DataflowRunsComponent;
import net.sf.taverna.t2.workbench.run.DataflowRunsComponentFactory;

import org.apache.log4j.Logger;

public class DataManagementConfigurationPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private DataManagementConfiguration configuration = DataManagementConfiguration
			.getInstance();
	private final static Logger logger = Logger
			.getLogger(DataManagementConfigurationPanel.class);

	JCheckBox enableProvenance;
	JCheckBox enableInMemory;
	private JButton helpButton;
	private JButton resetButton;
	private JButton applyButton;
	private DialogTextArea storageText;
	private DialogTextArea enableInMemoryTextDisabled;

	public DataManagementConfigurationPanel() {

		GridBagLayout gridbag = generateGridBagLayout();

		setLayout(gridbag);

		resetFields();

	}

	private GridBagLayout generateGridBagLayout() {
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		enableProvenance = new JCheckBox("Enable provenance capture");
		DialogTextArea enableProvenanceText = new DialogTextArea(
				"Disabling provenance will prevent you from being able to view intermediate results, but does give a performance benefit.");
		enableProvenanceText.setLineWrap(true);
		enableProvenanceText.setWrapStyleWord(true);
		enableProvenanceText.setEditable(false);
		enableProvenanceText.setOpaque(false);
		enableProvenanceText.setFont(enableProvenanceText.getFont().deriveFont(
				Font.PLAIN, 10));

		enableInMemory = new JCheckBox("In-memory storage");
		DialogTextArea enableInMemoryText = new DialogTextArea(
				"Data will not be stored between workbench sessions. This option is intended for testing only. Only use if your workflows have a low memory requirement. Provenance information is still recorded to a database.");
		enableInMemoryText.setLineWrap(true);
		enableInMemoryText.setWrapStyleWord(true);
		enableInMemoryText.setEditable(false);
		enableInMemoryText.setOpaque(false);
		enableInMemoryText.setFont(enableProvenanceText.getFont().deriveFont(
				Font.PLAIN, 10));

		enableInMemoryTextDisabled = new DialogTextArea(
				"It is not possible to modify the data storage settings whilst there are workflow runs in progress, or past workflow runs open");
		enableInMemoryTextDisabled.setLineWrap(true);
		enableInMemoryTextDisabled.setWrapStyleWord(true);
		enableInMemoryTextDisabled.setEditable(false);
		enableInMemoryTextDisabled.setOpaque(false);
		enableInMemoryTextDisabled.setFont(enableProvenanceText.getFont()
				.deriveFont(Font.BOLD, 10));
		enableInMemoryTextDisabled.setForeground(Color.RED);
		enableInMemoryTextDisabled.setVisible(false);

		storageText = new DialogTextArea(
				"Select how Taverna stores the data and provenance produced when a workflow is run. This includes workflow results and intermediate results.");
		storageText.setLineWrap(true);
		storageText.setWrapStyleWord(true);
		storageText.setEditable(false);
		storageText.setBorder(new EmptyBorder(10, 10, 10, 10));

		JComponent portPanel = createDerbyServerStatusComponent();

		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 10, 0);
		c.gridx = 0;
		c.gridy = GridBagConstraints.RELATIVE;
		c.weightx = 1d;
		c.weighty = 1d;
		c.fill = GridBagConstraints.HORIZONTAL;
		gridbag.setConstraints(storageText, c);
		add(storageText);

		c.ipady = 0;
		c.insets = new Insets(0, 0, 5, 0);
		gridbag.setConstraints(enableProvenance, c);
		add(enableProvenance);

		c.insets = new Insets(0, 20, 15, 20);
		gridbag.setConstraints(enableProvenanceText, c);
		add(enableProvenanceText);

		c.insets = new Insets(0, 0, 5, 0);
		gridbag.setConstraints(enableInMemory, c);
		add(enableInMemory);

		c.insets = new Insets(0, 20, 15, 20);
		gridbag.setConstraints(enableInMemoryText, c);
		add(enableInMemoryText);

		c.insets = new Insets(0, 20, 15, 20);
		gridbag.setConstraints(enableInMemoryTextDisabled, c);
		add(enableInMemoryTextDisabled);

		c.insets = new Insets(0, 20, 15, 20);
		gridbag.setConstraints(portPanel, c);
		add(portPanel);

		JPanel buttonPanel = createButtonPanel();
		c.insets = new Insets(0, 0, 5, 0);
		gridbag.setConstraints(buttonPanel, c);
		add(buttonPanel);
		return gridbag;
	}

	private JComponent createDerbyServerStatusComponent() {

		DialogTextArea textArea = new DialogTextArea();
		Connection connection = null;
		boolean running = false;

		try {
			running = DataManagementHelper.isRunning();
			connection = DataManagementHelper.openConnection();

		} catch (Exception e) {
			running = false;
		} finally {
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger
							.warn(
									"Unable to close connection to database (or return to pool)",
									e);
				}
		}

		if (running) {
			int port = DataManagementConfiguration.getInstance()
					.getCurrentPort();
			textArea.setText("The database is currently running on port: "
					+ port);
		} else {
			textArea
					.setText("Unable to retrieve a database connection - the database isn't available");
		}

		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setAlignmentX(CENTER_ALIGNMENT);
		textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN, 10));
		textArea.setVisible(DataManagementConfiguration.getInstance()
				.getStartInternalDerbyServer());
		return textArea;
	}

	// for testing only
	public static void main(String[] args) {
		JDialog dialog = new JDialog();
		dialog.add(new DataManagementConfigurationPanel());
		dialog.setModal(true);
		dialog.setSize(500, 300);
		dialog.setVisible(true);
		System.exit(0);
	}

	public void resetFields() {

		enableInMemory
				.setSelected(configuration.getProperty(
						DataManagementConfiguration.IN_MEMORY)
						.equalsIgnoreCase("true"));
		enableProvenance.setSelected(configuration.getProperty(
				DataManagementConfiguration.ENABLE_PROVENANCE)
				.equalsIgnoreCase("true"));

		boolean enabled = !workflowInstances();
		enableInMemory.setEnabled(enabled);
		enableInMemoryTextDisabled.setVisible(!enabled);

	}

	private boolean workflowInstances() {
		return DataflowRunsComponent.getInstance().getRunListCount()>0;
	}

	private void applySettings() {
		configuration.setProperty(
				DataManagementConfiguration.ENABLE_PROVENANCE, String
						.valueOf(enableProvenance.isSelected()));
		configuration.setProperty(DataManagementConfiguration.IN_MEMORY, String
				.valueOf(enableInMemory.isSelected()));
	}

	private JPanel createButtonPanel() {
		final JPanel panel = new JPanel();

		helpButton = new JButton(new AbstractAction("Help") {

			public void actionPerformed(ActionEvent arg0) {
				Helper.showHelp(panel);
			}
		});
		panel.add(helpButton);

		resetButton = new JButton(new AbstractAction("Reset") {

			public void actionPerformed(ActionEvent arg0) {
				resetFields();
			}
		});
		panel.add(resetButton);

		applyButton = new JButton(new AbstractAction("Apply") {

			public void actionPerformed(ActionEvent arg0) {
				applySettings();
				resetFields();
			}
		});
		panel.add(applyButton);

		return panel;
	}
}
