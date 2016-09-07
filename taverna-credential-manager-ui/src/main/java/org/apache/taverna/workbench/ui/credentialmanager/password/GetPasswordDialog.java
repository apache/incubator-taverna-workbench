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

package org.apache.taverna.workbench.ui.credentialmanager.password;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.FlowLayout.LEFT;
import static java.awt.FlowLayout.RIGHT;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.taverna.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog for entering user's username and password.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class GetPasswordDialog extends NonBlockedHelpEnabledDialog {
	/**
	 * Whether we should ask user to save their username and password using
	 * Credential Manager
	 */
	private boolean shouldAskUserToSave;
	/** Username field */
	private JTextField usernameField;
	/** Password field */
	private JPasswordField passwordField;
	/**
	 * Whether user wished to save the username and password using Credential
	 * Manager
	 */
	private JCheckBox saveCheckBox;
	/** The entered username */
	private String username;
	/** The entered password */
	private String password;
	/** Instructions to the user */
	private String instructions;

	public GetPasswordDialog(String instructions, boolean shouldAskUserToSave) {
		super((Frame) null, "Enter username and password", true);
		this.instructions = instructions;
		this.shouldAskUserToSave = shouldAskUserToSave;
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());

		JLabel instructionsLabel = new JLabel(instructions);
		instructionsLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JPanel jpInstructions = new JPanel(new FlowLayout(LEFT));
		jpInstructions.add(instructionsLabel);

		JLabel usernameLabel = new JLabel("Username");
		usernameLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

		usernameField = new JTextField(15);
		passwordField = new JPasswordField(15);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				okPressed();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelPressed();
			}
		});

        // Central panel with username/password fields and a "Do you want to Save?" checkbox
		JPanel mainPanel = new JPanel(new BorderLayout());

		JPanel passwordPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		passwordPanel.add(usernameLabel);
		passwordPanel.add(usernameField);
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordField);
		mainPanel.add(passwordPanel, CENTER);

		// If user wants to save this username and password
		saveCheckBox = new JCheckBox();
		saveCheckBox.setBorder(new EmptyBorder(5, 5, 5, 5));
		saveCheckBox.setSelected(true);
		saveCheckBox
				.setText("Use Credential Manager to save this username and password");
		if (shouldAskUserToSave) {
			JPanel jpSaveCheckBox = new JPanel(new FlowLayout(LEFT));
			jpSaveCheckBox.add(saveCheckBox);
			mainPanel.add(jpSaveCheckBox, SOUTH);
		}

		passwordPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10,
				10), new EtchedBorder()));

		JPanel buttonsPanel = new JPanel(new FlowLayout(RIGHT));
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

		passwordPanel.setMinimumSize(new Dimension(300, 100));

		getContentPane().add(jpInstructions, NORTH);
		getContentPane().add(mainPanel, CENTER);
		getContentPane().add(buttonsPanel, SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		setResizable(false);
		getRootPane().setDefaultButton(okButton);
		pack();
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * Check if user wishes to save username and pasword using the Credential
	 * Manager.
	 */
	public boolean shouldSaveUsernameAndPassword() {
		return saveCheckBox.isSelected();
	}

	private boolean checkControls() {
		username = usernameField.getText();
		if (username.length() == 0) {
			showMessageDialog(this, "Username cannot be empty", "Warning",
					WARNING_MESSAGE);
			return false;
		}

		password = new String(passwordField.getPassword());
		if (password.length() == 0) { // password empty
			showMessageDialog(this, "Password cannot be empty", "Warning",
					WARNING_MESSAGE);

			return false;
		}

		return true;
	}

	private void okPressed() {
		if (checkControls())
			closeDialog();
	}

	private void cancelPressed() {
		// Set all fields to null to indicate that cancel button was pressed
		username = null;
		password = null;
		closeDialog();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}

	public void setUsername(String username) {
		this.username = username;
		usernameField.setText(username);
	}

	public void setPassword(String password) {
		this.password = password;
		passwordField.setText(password);
	}
}
