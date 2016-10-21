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
import static java.awt.BorderLayout.SOUTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.ALERT_TITLE;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.ERROR_TITLE;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.WARN_TITLE;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog used for editing or entering new service URI, username or password for
 * a password entry.
 *
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class NewEditPasswordEntryDialog extends NonBlockedHelpEnabledDialog
{
	private static final Logger logger = Logger
			.getLogger(NewEditPasswordEntryDialog.class);
	/** 'Edit' mode constant - the dialog is in the 'edit' entry mode */
	private static final String EDIT_MODE = "EDIT";
	/** 'New' mode constant - the dialog is in the 'new' entry mode */
	private static final String NEW_MODE = "NEW";

	/**
	 * Mode of this dialog - {@link #NEW_MODE} for entering new password entry
	 * and {@link #EDIT_MODE} for editting an existing password entry
	 */
	String mode;
	/** Service URI field */
	private JTextField serviceURIField;
	/** Username field */
	private JTextField usernameField;
	/** First password entry field */
	private JPasswordField passwordField;
	/** Password confirmation entry field */
	private JPasswordField passwordConfirmField;
	/** Stores service URI entered */
	private URI serviceURI;
	/** Stores previous service URI for {@link #EDIT_MODE} */
	private URI serviceURIOld;
	/** Stores username entered */
	private String username;
    /** Stores password entered*/
    private String password;
    private CredentialManager credentialManager;

	public NewEditPasswordEntryDialog(JFrame parent, String title,
			boolean modal, URI currentURI, String currentUsername,
			String currentPassword, CredentialManager credentialManager) {
		super(parent, title, modal);
		serviceURI = currentURI;
		username = currentUsername;
		password = currentPassword;
		this.credentialManager = credentialManager;
		if (serviceURI == null && username == null && password == null) {
			// if passed values are all null
        	mode = NEW_MODE; // dialog is for entering a new password entry
		} else {
            mode = EDIT_MODE; // dialog is for editing an existing entry
            serviceURIOld = currentURI;
        }
        initComponents();
    }

	public NewEditPasswordEntryDialog(JDialog parent, String title,
			boolean modal, URI currentURI, String currentUsername,
			String currentPassword, CredentialManager credentialManager) {
		super(parent, title, modal);
        serviceURI = currentURI;
        username = currentUsername;
        password = currentPassword;
		this.credentialManager = credentialManager;
		if (serviceURI == null && username == null && password == null) {
			// if passed values are all null
        	mode = NEW_MODE; // dialog is for entering new password entry
		} else {
            mode = EDIT_MODE; // dialog is for editing existing entry
            serviceURIOld = currentURI;
        }
        initComponents();
    }

	private void initComponents() {
		getContentPane().setLayout(new BorderLayout());

        JLabel serviceURILabel = new JLabel("Service URI");
        serviceURILabel.setBorder(new EmptyBorder(0,5,0,0));

        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setBorder(new EmptyBorder(0,5,0,0));

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBorder(new EmptyBorder(0,5,0,0));

        JLabel passwordConfirmLabel = new JLabel("Confirm password");
        passwordConfirmLabel.setBorder(new EmptyBorder(0,5,0,0));

        serviceURIField = new JTextField();
        //jtfServiceURI.setBorder(new EmptyBorder(0,0,0,5));

        usernameField = new JTextField(15);
        //jtfUsername.setBorder(new EmptyBorder(0,0,0,5));

        passwordField = new JPasswordField(15);
        //jpfFirstPassword.setBorder(new EmptyBorder(0,0,0,5));

        passwordConfirmField = new JPasswordField(15);
        //jpfConfirmPassword.setBorder(new EmptyBorder(0,0,0,5));

        //If in EDIT_MODE - populate the fields with current values
		if (mode.equals(EDIT_MODE)) {
			serviceURIField.setText(serviceURI.toASCIIString());
			usernameField.setText(username);
			passwordField.setText(password);
			passwordConfirmField.setText(password);
		}

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

		JPanel passwordPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 0.0;

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        passwordPanel.add(serviceURILabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        passwordPanel.add(serviceURIField, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        passwordPanel.add(usernameLabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        passwordPanel.add(usernameField, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        passwordPanel.add(passwordLabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        passwordPanel.add(passwordField, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.fill = NONE;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        passwordPanel.add(passwordConfirmLabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.fill = HORIZONTAL;
		gbc.anchor = WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
		passwordPanel.add(passwordConfirmField, gbc);

		passwordPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10,
				10), new EtchedBorder()));

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonsPanel.add(okButton);
		buttonsPanel.add(cancelButton);

		getContentPane().add(passwordPanel, CENTER);
		getContentPane().add(buttonsPanel, SOUTH);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

        //setResizable(false);
        getRootPane().setDefaultButton(okButton);
        pack();
    }

	/**
	 * Get the username entered in the dialog.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Get the service URI entered in the dialog.
	 */
	public URI getServiceURI() {
		return serviceURI;
	}

	/**
	 * Get the password entered in the dialog.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Checks that the user has entered a non-empty service URI, a non-empty
	 * username, a non-empty password and that an entry with the same URI
	 * already does not already exist in the Keystore. Store the new password.
	 */
	private boolean checkControls() {
		String serviceURIString = new String(serviceURIField.getText());
		if (serviceURIString.isEmpty()) {
			showMessageDialog(this, "Service URI cannot be empty",
					WARN_TITLE, WARNING_MESSAGE);
			return false;
		}
    	try {
			serviceURI = new URI(serviceURIString);
		} catch (URISyntaxException e) {
			showMessageDialog(this, "Service URI is not a valid URI",
					WARN_TITLE, WARNING_MESSAGE);
			return false;
		}

		username = new String(usernameField.getText());
		if (username.isEmpty()) {
			showMessageDialog(this, "Username cannot be empty", WARN_TITLE,
					WARNING_MESSAGE);
			return false;
		}

		String firstPassword = new String(passwordField.getPassword());
		String confirmPassword = new String(passwordConfirmField.getPassword());

		if (!firstPassword.equals(confirmPassword)) {
			// passwords do not match
			showMessageDialog(this, "Passwords do not match", WARN_TITLE,
					WARNING_MESSAGE);
			return false;
		}
		if (firstPassword.isEmpty()) {
			// passwords match but are empty
			showMessageDialog(this, "Password cannot be empty", WARN_TITLE,
					WARNING_MESSAGE);
			return false;
		}

		// passwords the same and non-empty
		password = firstPassword;

		// Check if the entered service URL is already associated with another password entry in the Keystore
    	List<URI> uriList = null;
    	try {
			uriList = credentialManager.getServiceURIsForAllUsernameAndPasswordPairs();
		} catch (CMException cme) {
			// Failed to instantiate Credential Manager - warn the user and exit
			String exMessage = "Failed to instantiate Credential Manager to check for duplicate service URIs.";
			logger.error(exMessage, cme);
			showMessageDialog(new JFrame(), exMessage, ERROR_TITLE,
					ERROR_MESSAGE);
			return false;
		}

       	if (uriList != null) { // should not be null really (although can be empty). Check anyway.
       		if (mode.equals(EDIT_MODE)) // edit mode
            	// Remove the current entry's service URI from the list
                uriList.remove(serviceURIOld);

   			if (uriList.contains(serviceURI)) { // found another entry for this service URI
        		// Warn the user and exit
				showMessageDialog(
						this,
						"The entered service URI is already associated with another password entry",
						ALERT_TITLE, WARNING_MESSAGE);
				return false;
			}
		}

		return true;
	}

	private void okPressed() {
		if (checkControls())
			closeDialog();
	}

	private void cancelPressed() {
    	// Set all fields to null to indicate that cancel button was pressed
		serviceURI = null;
		username = null;
		password = null;
		closeDialog();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}
}
