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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog used by users to change their
 * master password for the Credential Manager.
 */
@SuppressWarnings("serial")
public class ChangeMasterPasswordDialog extends NonBlockedHelpEnabledDialog {

    // Old password entry field
    private JPasswordField oldPasswordField;

    // New password entry field
    private JPasswordField newPasswordField;

    // New password confirmation entry field
    private JPasswordField newPasswordConfirmField;

    // The entered new password
    private String password = null;

    // Instructions to the users as to what to do in the dialog
    private String instructions;

	private final CredentialManager credentialManager;

	public ChangeMasterPasswordDialog(JFrame parent, String title,
			boolean modal, String instructions, CredentialManager credentialManager)    {
        super(parent, title, modal, null);
        this.instructions = instructions;
		this.credentialManager = credentialManager;
        initComponents();
    }

    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        JLabel instructionsLabel = new JLabel (instructions);
    	instructionsLabel.setFont(new Font(null, Font.PLAIN, 11));

    	JPanel instructionsPanel = new JPanel();
    	instructionsPanel.setLayout(new BoxLayout(instructionsPanel, BoxLayout.Y_AXIS));
    	instructionsPanel.add(instructionsLabel);
    	instructionsPanel.setBorder(new EmptyBorder(10,5,10,0));

        JLabel oldPasswordLabel = new JLabel("Old master password");
        oldPasswordLabel.setBorder(new EmptyBorder(0,5,0,0));

        JLabel newPasswordLabel = new JLabel("New master password");
        newPasswordLabel.setBorder(new EmptyBorder(0,5,0,0));

        JLabel newPasswordConfirmLabel = new JLabel("Confirm new master password");
        newPasswordConfirmLabel.setBorder(new EmptyBorder(0,5,0,0));

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
        mainPanel.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
        mainPanel.add(instructionsPanel, BorderLayout.NORTH);
        mainPanel.add(jpPassword, BorderLayout.CENTER);

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
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
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
    public String getPassword()
    {
    	return password;
    }

    /**
     * Check that the user has provided the correct old master password,
     * that the user has supplied the new password and confirmed it and
     * that it is not empty. If all is OK, stores the new password in
     * the password field.
     *
     */
    private boolean checkPassword()
    {
    	String oldPassword = new String (oldPasswordField.getPassword());

    	 if (oldPassword.length()== 0) { //old password must not be empty
             JOptionPane.showMessageDialog(this,
                     "You must provide your current master password",
                     "Credential Manager Warning",
                     JOptionPane.WARNING_MESSAGE);
             return false;
         }
    	try {
			if (!credentialManager.confirmMasterPassword(oldPassword)){
			    JOptionPane.showMessageDialog(this,
			            "You have provided an incorrect master password",
			            "Credential Manager Warning",
			            JOptionPane.WARNING_MESSAGE);
			        return false;
			}
		} catch (Exception e) {
		    JOptionPane.showMessageDialog(this,
		            "Credential Manager could not verify your current master password",
		            "Credential Manager Warning",
		            JOptionPane.WARNING_MESSAGE);
			return false;
		}

        String newPassword = new String(newPasswordField.getPassword());
        String newPasswordConfirm = new String(newPasswordConfirmField.getPassword());

        if ((newPassword.equals(newPasswordConfirm)) && (newPassword.length()!= 0)) { //passwords match and not empty
            password = newPassword;
            return true;
        }
        else if ((newPassword.equals(newPasswordConfirm)) && (newPassword.length() == 0)) { //passwords match but are empty
            JOptionPane.showMessageDialog(this,
                    "The new master password cannot be empty",
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
    }

    private void okPressed()
    {
        if (checkPassword()) {
            closeDialog();
        }
    }

    private void cancelPressed()
    {
    	// Set the password to null as it might have changed in the meantime
    	// if user entered something then cancelled.
    	password = null;
        closeDialog();
    }

    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
}



