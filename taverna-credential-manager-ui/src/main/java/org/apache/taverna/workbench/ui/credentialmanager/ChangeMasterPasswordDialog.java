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
package org.apache.taverna.workbench.ui.credentialmanager;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Font.PLAIN;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.WARN_TITLE;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog used by users to change their master password for the Credential
 * Manager.
 */
@SuppressWarnings("serial")
public class ChangeMasterPasswordDialog extends NonBlockedHelpEnabledDialog {
	/** Old password entry field */
	private JPasswordField oldPasswordField;
	/** New password entry field */
	private JPasswordField newPasswordField;
	/** New password confirmation entry field */
	private JPasswordField newPasswordConfirmField;
	/** The entered new password */
	private String password = null;
	/** Instructions to the users as to what to do in the dialog */
	private String instructions;
	private final CredentialManager credentialManager;

	public ChangeMasterPasswordDialog(JFrame parent, String title,
			boolean modal, String instructions,
			CredentialManager credentialManager) {
		super(parent, title, modal, null);
		this.instructions = instructions;
		this.credentialManager = credentialManager;
		initComponents();
	}

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());

		JLabel instructionsLabel = new JLabel(instructions);
		instructionsLabel.setFont(new Font(null, PLAIN, 11));

		JPanel instructionsPanel = new JPanel();
		instructionsPanel.setLayout(new BoxLayout(instructionsPanel, Y_AXIS));
		instructionsPanel.add(instructionsLabel);
		instructionsPanel.setBorder(new EmptyBorder(10, 5, 10, 0));

		JLabel oldPasswordLabel = new JLabel("Old master password");
		oldPasswordLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

		JLabel newPasswordLabel = new JLabel("New master password");
		newPasswordLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

		JLabel newPasswordConfirmLabel = new JLabel(
				"Confirm new master password");
		newPasswordConfirmLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

		oldPasswordField = new JPasswordField(15);
		newPasswordField = new JPasswordField(15);
		newPasswordConfirmField = new JPasswordField(15);

		JPanel jpPassword = new JPanel(new GridLayout(0, 2, 5, 5));
		jpPassword.add(oldPasswordLabel);
		jpPassword.add(oldPasswordField);
		jpPassword.add(newPasswordLabel);
		jpPassword.add(newPasswordField);
		jpPassword.add(newPasswordConfirmLabel);
		jpPassword.add(newPasswordConfirmField);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10),
				new EtchedBorder()));
		mainPanel.add(instructionsPanel, NORTH);
		mainPanel.add(jpPassword, CENTER);

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
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

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

	/**
	 * Get the password set in the dialog or null if none was set.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Check that the user has provided the correct old master password, that
	 * the user has supplied the new password and confirmed it and that it is
	 * not empty. If all is OK, stores the new password in the password field.
	 * 
	 */
	private boolean checkPassword() {
		String oldPassword = new String(oldPasswordField.getPassword());

		if (oldPassword.length() == 0) {
			// old password must not be empty
			showMessageDialog(this,
					"You must provide your current master password",
					WARN_TITLE, WARNING_MESSAGE);
			return false;
		}

		try {
			if (!credentialManager.confirmMasterPassword(oldPassword)) {
				showMessageDialog(this,
						"You have provided an incorrect master password",
						WARN_TITLE, WARNING_MESSAGE);
				return false;
			}
		} catch (Exception e) {
			showMessageDialog(
					this,
					"Credential Manager could not verify your current master password",
					WARN_TITLE, WARNING_MESSAGE);
			return false;
		}

		String newPassword = new String(newPasswordField.getPassword());
		String newPasswordConfirm = new String(
				newPasswordConfirmField.getPassword());

		if (!newPassword.equals(newPasswordConfirm)) {
			// passwords do not match
			showMessageDialog(this, "Passwords do not match", WARN_TITLE,
					WARNING_MESSAGE);
			return false;
		}

		if (newPassword.isEmpty()) {
			// passwords match but are empty
			showMessageDialog(this, "The new master password cannot be empty",
					WARN_TITLE, WARNING_MESSAGE);
			return false;
		}

		// passwords match and not empty
		password = newPassword;
		return true;
	}

	private void okPressed() {
		if (checkPassword())
			closeDialog();
	}

	private void cancelPressed() {
		/*
		 * Set the password to null as it might have changed in the meantime if
		 * user entered something then cancelled.
		 */
		password = null;
		closeDialog();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}
}
