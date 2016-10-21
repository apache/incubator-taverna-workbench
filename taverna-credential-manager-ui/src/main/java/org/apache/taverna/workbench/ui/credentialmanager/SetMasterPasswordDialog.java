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

import org.apache.taverna.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog used for user to set a master password for Credential Manager.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class SetMasterPasswordDialog extends NonBlockedHelpEnabledDialog {
	/** Password entry field */
	private JPasswordField passwordField;
	/** Password confirmation entry field */
	private JPasswordField passwordConfirmField;
	/** The entered password */
	private String password = null;
	/** Instructions for the user */
	private String instructions;

	public SetMasterPasswordDialog(JFrame parent, String title, boolean modal,
			String instructions) {
		super(parent, title, modal);
		this.instructions = instructions;
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

		JLabel passwordLabel = new JLabel("Master password");
		passwordLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

		JLabel passwordConfirmLabel = new JLabel("Confirm master password");
		passwordConfirmLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

		passwordField = new JPasswordField(15);
		passwordConfirmField = new JPasswordField(15);

		JPanel passwordPanel = new JPanel(new GridLayout(2, 2, 5, 5));
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordField);
		passwordPanel.add(passwordConfirmLabel);
		passwordPanel.add(passwordConfirmField);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10),
				new EtchedBorder()));
		mainPanel.add(instructionsPanel, NORTH);
		mainPanel.add(passwordPanel, CENTER);

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

	public String getPassword() {
		return password;
	}

	/**
	 * Check that the user has entered a non-empty password and store the new
	 * password.
	 */
	private boolean checkPassword() {
		String firstPassword = new String(passwordField.getPassword());
		String confirmPassword = new String(passwordConfirmField.getPassword());

		if (!firstPassword.equals(confirmPassword)) {
			showMessageDialog(this, "The passwords do not match", WARN_TITLE,
					WARNING_MESSAGE);
			return false;
		}
		if (firstPassword.isEmpty()) {
			// passwords match but are empty
			showMessageDialog(this, "The password cannot be empty", WARN_TITLE,
					WARNING_MESSAGE);
			return false;
		}

		// passwords match and not empty
		password = firstPassword;
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
