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

package org.apache.taverna.workbench.reference.config;

import static java.awt.Color.RED;
import static java.awt.Font.PLAIN;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.WEST;
import static org.apache.taverna.workbench.helper.Helper.showHelp;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.lang.ui.DialogTextArea;
import org.apache.taverna.configuration.database.DatabaseConfiguration;
import org.apache.taverna.configuration.database.DatabaseManager;

@SuppressWarnings("serial")
public class DataManagementConfigurationPanel extends JPanel {
	private DatabaseConfiguration configuration;
	private DatabaseManager databaseManager;

	private JCheckBox enableProvenance;
	private JCheckBox enableInMemory;
	private JButton helpButton;
	private JButton resetButton;
	private JButton applyButton;
	private JTextArea storageText;
	private JTextArea exposeDatanatureText;
	private JCheckBox exposeDatanatureBox;
	private DialogTextArea enableInMemoryTextDisabled;

	public DataManagementConfigurationPanel(DatabaseConfiguration configuration, DatabaseManager databaseManager) {
		this.configuration = configuration;
		this.databaseManager = databaseManager;

		setLayout(generateLayout());
		resetFields();
	}

	private static final boolean ADD_WARNING_LISTENERS = false;

	private GridBagLayout generateLayout() {
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		enableProvenance = new JCheckBox("Enable provenance capture");
		DialogTextArea enableProvenanceText = new DialogTextArea(
				"Disabling provenance will prevent you from being able to view intermediate results, but does give a performance benefit.");
		Font plain = enableProvenanceText.getFont().deriveFont(PLAIN, 11);
		enableProvenanceText.setLineWrap(true);
		enableProvenanceText.setWrapStyleWord(true);
		enableProvenanceText.setEditable(false);
		enableProvenanceText.setFocusable(false);
		enableProvenanceText.setOpaque(false);
		enableProvenanceText.setFont(plain);

		enableInMemory = new JCheckBox("In-memory storage");
		DialogTextArea enableInMemoryText = new DialogTextArea(
				"Data will not be stored between workbench sessions. If you run workflows passing larger amounts of data, try disabling in-memory storage, which can reduce execution performance, but also Taverna's memory consumption. ");
		enableInMemoryText.setLineWrap(true);
		enableInMemoryText.setWrapStyleWord(true);
		enableInMemoryText.setEditable(false);
		enableInMemoryText.setFocusable(false);
		enableInMemoryText.setOpaque(false);
		enableInMemoryText.setFont(plain);

		enableInMemoryTextDisabled = new DialogTextArea(
				"If you enable in-memory storage of data when provenance collection is turned on then provenance will not be available after you shutdown Taverna as the in-memory data will be lost.");
		enableInMemoryTextDisabled.setLineWrap(true);
		enableInMemoryTextDisabled.setWrapStyleWord(true);
		enableInMemoryTextDisabled.setEditable(false);
		enableInMemoryTextDisabled.setFocusable(false);
		enableInMemoryTextDisabled.setOpaque(false);
		enableInMemoryTextDisabled.setFont(plain);
		enableInMemoryTextDisabled.setForeground(RED);
		enableInMemoryTextDisabled.setVisible(false);

		// Disable warning as inMemory is default
		// To re-enable - also see resetFields()

		if (ADD_WARNING_LISTENERS) {
			enableInMemory.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					enableInMemoryTextDisabled.setVisible(enableProvenance
							.isSelected() && enableInMemory.isSelected());
				}
			});
			enableProvenance.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					enableInMemoryTextDisabled.setVisible(enableProvenance
							.isSelected() && enableInMemory.isSelected());
				}
			});
		}

		storageText = new JTextArea(
				"Select how Taverna stores the data and provenance produced when a workflow is run. This includes workflow results and intermediate results.");
		storageText.setLineWrap(true);
		storageText.setWrapStyleWord(true);
		storageText.setEditable(false);
		storageText.setFocusable(false);
		storageText.setBorder(new EmptyBorder(10, 10, 10, 10));
		storageText.setFont(plain);

		JComponent portPanel = createDerbyServerStatusComponent();

		c.anchor = WEST;
		c.insets = new Insets(0, 0, 10, 0);
		c.gridx = 0;
		c.gridy = RELATIVE;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.fill = HORIZONTAL;
		gridbag.setConstraints(storageText, c);
		add(storageText);

		c.ipady = 0;
		c.insets = new Insets(0, 0, 5, 0);
		c.fill = NONE;
		gridbag.setConstraints(enableProvenance, c);
		add(enableProvenance);

		c.insets = new Insets(0, 20, 15, 20);
		c.fill = HORIZONTAL;
		gridbag.setConstraints(enableProvenanceText, c);
		add(enableProvenanceText);

		c.insets = new Insets(0, 0, 5, 0);
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(enableInMemory, c);
		add(enableInMemory);

		c.insets = new Insets(0, 20, 15, 20);
		c.fill = HORIZONTAL;
		gridbag.setConstraints(enableInMemoryText, c);
		add(enableInMemoryText);

		c.insets = new Insets(0, 20, 15, 20);
		c.fill = HORIZONTAL;
		gridbag.setConstraints(enableInMemoryTextDisabled, c);
		add(enableInMemoryTextDisabled);

		c.insets = new Insets(0, 20, 15, 20);
		gridbag.setConstraints(portPanel, c);
		add(portPanel);

		c.insets = new Insets(0, 0, 5, 0);
		c.fill = NONE;
		exposeDatanatureBox = new JCheckBox(
				"Allow setting of input data encoding");
		gridbag.setConstraints(exposeDatanatureBox, c);
		add(exposeDatanatureBox);

		exposeDatanatureText = new JTextArea(
				"Select if you want to control how Taverna handles files read as input data");
		exposeDatanatureText.setLineWrap(true);
		exposeDatanatureText.setWrapStyleWord(true);
		exposeDatanatureText.setEditable(false);
		exposeDatanatureText.setFocusable(false);
		exposeDatanatureText.setOpaque(false);
		exposeDatanatureText.setFont(plain);

		c.insets = new Insets(0, 20, 15, 20);
		c.fill = HORIZONTAL;
		gridbag.setConstraints(exposeDatanatureText, c);
		add(exposeDatanatureText);

		JPanel buttonPanel = createButtonPanel();
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = BOTH;
		c.insets = new Insets(0, 0, 5, 0);
		gridbag.setConstraints(buttonPanel, c);
		add(buttonPanel);
		return gridbag;
	}

	private JComponent createDerbyServerStatusComponent() {
		DialogTextArea textArea = new DialogTextArea();
		boolean running;

		try (Connection connection = databaseManager.getConnection()) {
			running = databaseManager.isRunning();
		} catch (Exception e) {
			running = false;
		}

		if (running)
			textArea.setText("The database is currently running on port: "
					+ configuration.getCurrentPort() + ".");
		else
			textArea.setText("Unable to retrieve a database connection - "
					+ "the database is not available.");

		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setFocusable(false);
		textArea.setOpaque(false);
		textArea.setAlignmentX(CENTER_ALIGNMENT);
		textArea.setFont(textArea.getFont().deriveFont(PLAIN, 11));
		textArea.setVisible(configuration.getStartInternalDerbyServer());
		return textArea;
	}

	// for testing only
//	public static void main(String[] args) {
//		JDialog dialog = new JDialog();
//		dialog.add(new DataManagementConfigurationPanel());
//		dialog.setModal(true);
//		dialog.setSize(500, 300);
//		dialog.setVisible(true);
//		System.exit(0);
//	}

	public void resetFields() {
		enableInMemory.setSelected(configuration.isInMemory());
		enableProvenance.setSelected(configuration.isProvenanceEnabled());
		exposeDatanatureBox.setSelected(configuration.isExposeDatanature());

		if (ADD_WARNING_LISTENERS) {
			enableInMemoryTextDisabled.setVisible(enableProvenance.isSelected()
					&& enableInMemory.isSelected());
		}
	}

	/*private boolean workflowInstances() {
		return DataflowRunsComponent.getInstance().getRunListCount()>0;
	}*/

	private void applySettings() {
		configuration.setProvenanceEnabled(enableProvenance.isSelected());
		configuration.setInMemory(enableInMemory.isSelected());
		configuration.setExposeDatanature(exposeDatanatureBox.isSelected());
	}

	private JPanel createButtonPanel() {
		final JPanel panel = new JPanel();

		helpButton = new JButton(new AbstractAction("Help") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showHelp(panel);
			}
		});
		panel.add(helpButton);

		resetButton = new JButton(new AbstractAction("Reset") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetFields();
			}
		});
		panel.add(resetButton);

		applyButton = new JButton(new AbstractAction("Apply") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				applySettings();
				resetFields();
			}
		});
		panel.add(applyButton);

		return panel;
	}
}
