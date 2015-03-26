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

package org.apache.taverna.workbench.ui.credentialmanager;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.taverna.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog used for viewing service URL, username and password.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class ViewUsernamePasswordEntryDialog extends
		NonBlockedHelpEnabledDialog {
	/** Service URL field */
	private JTextField serviceURLField;
	/** Username field */
	private JTextField usernameField;
	/** Password field */
	private JTextField passwordField;
	/** Service URL value */
	private String serviceURL;
	/** Service username value */
	private String username;
	/** Service password value */
	private String password;

	public ViewUsernamePasswordEntryDialog(JFrame parent, String currentURL,
			String currentUsername, String currentPassword) {
		super(parent, "View username and password for a service", true);
		serviceURL = currentURL;
		username = currentUsername;
		password = currentPassword;
		initComponents();
	}

	public ViewUsernamePasswordEntryDialog(JDialog parent, String currentURL,
			String currentUsername, String currentPassword) {
		super(parent, "View username and password for a service", true);
		serviceURL = currentURL;
		username = currentUsername;
		password = currentPassword;
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());

		JLabel serviceURLLabel = new JLabel("Service URL");
		serviceURLLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setBorder(new EmptyBorder(0, 5, 0, 0));
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

		// Populate the fields with values and disable user input
		serviceURLField = new JTextField();
		serviceURLField.setText(serviceURL);
		serviceURLField.setEditable(false);

		usernameField = new JTextField(15);
		usernameField.setText(username);
		usernameField.setEditable(false);

		passwordField = new JTextField(15);
		passwordField.setText(password);
		passwordField.setEditable(false);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				closeDialog();
			}
		});

		JPanel fieldsPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 0.0;

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
		fieldsPanel.add(serviceURLLabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
		fieldsPanel.add(serviceURLField, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
		fieldsPanel.add(usernameLabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
		fieldsPanel.add(usernameField, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
		fieldsPanel.add(passwordLabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
		fieldsPanel.add(passwordField, gbc);

		fieldsPanel.setBorder(new CompoundBorder(
				new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonsPanel.add(okButton);

		getContentPane().add(fieldsPanel, CENTER);
		getContentPane().add(buttonsPanel, SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		// setResizable(false);
		getRootPane().setDefaultButton(okButton);
		pack();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}
}
