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
package net.sf.taverna.t2.workbench.ui.credentialmanager;

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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog used for editing or entering new service URI, username or password for
 * a password entry.
 *
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class NewEditPasswordEntryDialog extends NonBlockedHelpEnabledDialog
{
	// 'Edit' mode constant - the dialog is in the 'edit' entry mode
	private static final String EDIT_MODE = "EDIT";

	// 'New' mode constant - the dialog is in the 'new' entry mode
	private static final String NEW_MODE = "NEW";

	// Mode of this dialog - NEW_MODE for entering new password entry and EDIT_MODE for editting an existing password entry */
	String mode;

    // Service URI field
    private JTextField serviceURIField;

    // Username field
    private JTextField usernameField;

    // First password entry field
    private JPasswordField passwordField;

    // Password confirmation entry field
    private JPasswordField passwordConfirmField;

    // Stores service URI entered
    private URI serviceURI;
    // Stores previous service URI for EDIT_MODE
    private URI serviceURIOld;

    // Stores username entered
    private String username;

    // Stores password entered
    private String password;

    private Logger logger = Logger.getLogger(NewEditPasswordEntryDialog.class);

    private CredentialManager credentialManager;

    public NewEditPasswordEntryDialog(JFrame parent, String title,
			boolean modal, URI currentURI, String currentUsername,
			String currentPassword, CredentialManager credentialManager)
    {
        super(parent, title, modal);
        serviceURI = currentURI;
        username = currentUsername;
        password = currentPassword;
		this.credentialManager = credentialManager;
        if (serviceURI == null && username == null && password == null) // if passed values are all null
        {
        	mode = NEW_MODE; // dialog is for entering a new password entry
        }
        else{
            mode = EDIT_MODE; // dialog is for editing an existing entry
            serviceURIOld = currentURI;
        }
        initComponents();
    }

    public NewEditPasswordEntryDialog(JDialog parent, String title, boolean modal, URI currentURI, String currentUsername, String currentPassword, CredentialManager credentialManager)
    {
        super(parent, title, modal);
        serviceURI = currentURI;
        username = currentUsername;
        password = currentPassword;
		this.credentialManager = credentialManager;
       if (serviceURI == null && username == null && password == null) // if passed values are all null
        {
        	mode = NEW_MODE; // dialog is for entering new password entry
        }
        else{
            mode = EDIT_MODE; // dialog is for editing existing entry
            serviceURIOld = currentURI;
        }
        initComponents();
    }

    private void initComponents()
    {
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
        if (mode.equals(EDIT_MODE)){
            serviceURIField.setText(serviceURI.toASCIIString());
            usernameField.setText(username);
            passwordField.setText(password);
            passwordConfirmField.setText(password);
        }

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                okPressed();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                cancelPressed();
            }
        });

        JPanel passwordPanel = new JPanel(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 0.0;

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        passwordPanel.add(serviceURILabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        passwordPanel.add(serviceURIField, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        passwordPanel.add(usernameLabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        passwordPanel.add(usernameField, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        passwordPanel.add(passwordLabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        passwordPanel.add(passwordField, gbc);

		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        passwordPanel.add(passwordConfirmLabel, gbc);

		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        passwordPanel.add(passwordConfirmField, gbc);

        passwordPanel.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        getContentPane().add(passwordPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
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
    public String getUsername()
    {
        return username;
    }

    /**
     * Get the service URI entered in the dialog.
     */
    public URI getServiceURI()
    {
        return serviceURI;
    }

    /**
     * Get the password entered in the dialog.
     */
    public String getPassword()
    {
    	return password;
    }

    /**
     * Checks that the user has entered a non-empty service URI, a non-empty username,
     * a non-empty password and that an entry with the same URI already does not already
     * exist in the Keystore. Store the new password.
     */
    private boolean checkControls()
    {
    	String serviceURIString = new String(serviceURIField.getText());
    	if (serviceURIString.length() == 0) {
            JOptionPane.showMessageDialog(this,
                "Service URI cannot be empty",
                "Credential Manager Warning",
                JOptionPane.WARNING_MESSAGE);

            return false;
    	}
    	try {
			serviceURI = new URI(serviceURIString);
		} catch (URISyntaxException e) {
            JOptionPane.showMessageDialog(this,
                    "Service URI is not a valid URI",
                    "Credential Manager Warning",
                    JOptionPane.WARNING_MESSAGE);

                return false;
		}

    	username = new String(usernameField.getText());
    	if (username.length() == 0){
            JOptionPane.showMessageDialog(this,
                "Username cannot be empty",
                "Credential Manager Warning",
                JOptionPane.WARNING_MESSAGE);

            return false;
    	}

    	String firstPassword = new String(passwordField.getPassword());
        String confirmPassword = new String(passwordConfirmField.getPassword());

    	if ((firstPassword.length() > 0) && (firstPassword.equals(confirmPassword))) { // passwords the same and non-empty
    		password = firstPassword;
        }
        else if ((firstPassword.length() == 0) && (firstPassword.equals(confirmPassword))){ // passwords match but are empty

            JOptionPane.showMessageDialog(this,
                "Password cannot be empty",
                "Credential Manager Warning",
                JOptionPane.WARNING_MESSAGE);

            return false;
        }
        else{ // passwords do not match
            JOptionPane.showMessageDialog(this,
                "Passwords do not match",
                "Credential Manager Warning",
                JOptionPane.WARNING_MESSAGE);

            return false;
        }

		// Check if the entered service URL is already associated with another password entry in the Keystore
    	List<URI> uriList = null;
    	try {
			uriList = credentialManager.getServiceURIsForAllUsernameAndPasswordPairs();
		} catch (CMException cme) {
			// Failed to instantiate Credential Manager - warn the user and exit
			String exMessage = "Failed to instantiate Credential Manager to check for duplicate service URIs.";
			logger.error(exMessage, cme);
			JOptionPane.showMessageDialog(new JFrame(), exMessage,
					"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

       	if (uriList != null){ // should not be null really (although can be empty). Check anyway.
       		if (mode.equals(EDIT_MODE)){ // edit mode
            	// Remove the current entry's service URI from the list
                uriList.remove(serviceURIOld);
       		}

   			if (uriList.contains(serviceURI)){ // found another entry for this service URI
        		// Warn the user and exit
            	JOptionPane.showMessageDialog(
                		this,
                		"The entered service URI is already associated with another password entry",
            			"Credential Manager Alert",
            			JOptionPane.WARNING_MESSAGE);
            	return false;
			}
       	}

    	return true;
    }

    private void okPressed()
    {
        if (checkControls()) {
            closeDialog();
        }
    }

    private void cancelPressed()
    {
    	// Set all fields to null to indicate that cancel button was pressed
    	serviceURI = null;
    	username = null;
    	password = null;
        closeDialog();
    }

    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
}

