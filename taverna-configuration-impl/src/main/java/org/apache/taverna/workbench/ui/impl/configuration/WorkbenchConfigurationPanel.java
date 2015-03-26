/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.impl.configuration;

import static java.awt.GridBagConstraints.*;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.helper.Helper.showHelp;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.openIcon;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class WorkbenchConfigurationPanel extends JPanel {
	private static final String RESTART_MSG = "For the new configuration to be fully applied, it is advised to restart Taverna.";
	private static final String DOT_PATH_MSG = "Path to Graphviz executable <code>dot</code>:";
	private static final String CONTEXT_MENU_SIZE_MSG = "Maximum number of services/ports in right-click menu:";
	private static Logger logger = Logger
			.getLogger(WorkbenchConfigurationUIFactory.class);

	private JTextField dotLocation = new JTextField(25);
	private JTextField menuItems = new JTextField(10);
	private JCheckBox warnInternal = new JCheckBox("Warn on internal errors");
	private JCheckBox captureConsole = new JCheckBox(
			"Capture output on stdout/stderr to log file");

	private final WorkbenchConfiguration workbenchConfiguration;

	public WorkbenchConfigurationPanel(
			WorkbenchConfiguration workbenchConfiguration) {
		super();
		this.workbenchConfiguration = workbenchConfiguration;
		initComponents();
	}

	private static JLabel htmlLabel(String html) {
		return new JLabel("<html><body>" + html + "</body></html>");
	}

	private void initComponents() {
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		// Title describing what kind of settings we are configuring here
		JTextArea descriptionText = new JTextArea(
				"General Workbench configuration");
		descriptionText.setLineWrap(true);
		descriptionText.setWrapStyleWord(true);
		descriptionText.setEditable(false);
		descriptionText.setFocusable(false);
		descriptionText.setBorder(new EmptyBorder(10, 10, 10, 10));
		gbc.anchor = WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = HORIZONTAL;
		this.add(descriptionText, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(10, 5, 0, 0);
		gbc.fill = NONE;
		this.add(htmlLabel(DOT_PATH_MSG), gbc);

		dotLocation.setText(workbenchConfiguration.getDotLocation());
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = HORIZONTAL;
		this.add(dotLocation, gbc);

		JButton browseButton = new JButton();
		gbc.gridx = 1;
		gbc.weightx = 0.0;
		gbc.fill = NONE;
		this.add(browseButton, gbc);
		browseButton.setAction(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.setProperty("com.apple.macos.use-file-dialog-packages",
						"false");
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.putClientProperty(
						"JFileChooser.appBundleIsTraversable", "always");
				fileChooser.putClientProperty(
						"JFileChooser.packageIsTraversable", "always");

				fileChooser.setDialogTitle("Browse for dot");

				fileChooser.resetChoosableFileFilters();
				fileChooser.setAcceptAllFileFilterUsed(false);

				fileChooser.setMultiSelectionEnabled(false);

				int returnVal = fileChooser
						.showOpenDialog(WorkbenchConfigurationPanel.this);
				if (returnVal == APPROVE_OPTION)
					dotLocation.setText(fileChooser.getSelectedFile()
							.getAbsolutePath());
			}
		});
		browseButton.setIcon(openIcon);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.insets = new Insets(10, 5, 0, 0);
		gbc.fill = HORIZONTAL;
		this.add(htmlLabel(CONTEXT_MENU_SIZE_MSG), gbc);

		menuItems.setText(Integer.toString(workbenchConfiguration
				.getMaxMenuItems()));
		gbc.gridy++;
		gbc.weightx = 1.0;
		gbc.gridwidth = 1;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = HORIZONTAL;
		this.add(menuItems, gbc);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.fill = HORIZONTAL;
		gbc.insets = new Insets(10, 0, 0, 0);
		warnInternal
				.setSelected(workbenchConfiguration.getWarnInternalErrors());
		this.add(warnInternal, gbc);

		gbc.gridy++;
		gbc.insets = new Insets(0, 0, 10, 0);
		captureConsole.setSelected(workbenchConfiguration.getCaptureConsole());
		this.add(captureConsole, gbc);

		// Add the buttons panel
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = BOTH;
		gbc.anchor = SOUTH;
		this.add(getButtonsPanel(), gbc);
	}

	private Component getButtonsPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER));

		/**
		 * The helpButton shows help about the current component
		 */
		JButton helpButton = new JButton(new AbstractAction("Help") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showHelp(panel);
			}
		});
		panel.add(helpButton);

		/**
		 * The resetButton changes the property values shown to those
		 * corresponding to the configuration currently applied.
		 */
		JButton resetButton = new JButton(new AbstractAction("Reset") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetFields();
			}
		});
		panel.add(resetButton);

		JButton applyButton = new JButton(new AbstractAction("Apply") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String menus = menuItems.getText();
				try {
					workbenchConfiguration.setMaxMenuItems(Integer
							.valueOf(menus));
				} catch (IllegalArgumentException e) {
					String message = "Invalid menu items number " + menus
							+ ":\n" + e.getLocalizedMessage();
					showMessageDialog(panel, message, "Invalid menu items",
							WARNING_MESSAGE);
					return;
				}

				workbenchConfiguration.setCaptureConsole(captureConsole
						.isSelected());
				workbenchConfiguration.setWarnInternalErrors(warnInternal
						.isSelected());
				workbenchConfiguration.setDotLocation(dotLocation.getText());
				try {
					showMessageDialog(panel, RESTART_MSG, "Restart adviced",
							INFORMATION_MESSAGE);
				} catch (Exception e) {
					logger.error("Error storing updated configuration", e);
				}
			}
		});
		panel.add(applyButton);
		return panel;
	}

	/**
	 * Resets the shown field values to those currently set (last saved) in the
	 * configuration.
	 * 
	 * @param configurable
	 */
	private void resetFields() {
		menuItems.setText(Integer.toString(workbenchConfiguration
				.getMaxMenuItems()));
		dotLocation.setText(workbenchConfiguration.getDotLocation());
		warnInternal
				.setSelected(workbenchConfiguration.getWarnInternalErrors());
		captureConsole.setSelected(workbenchConfiguration.getCaptureConsole());
	}
}
