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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

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

/**
 * Dialog used for editing or entering new service URL, username or password of a password entry.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class NewEditPasswordEntryDialog extends JDialog
{
	// 'Edit' mode constant - the dialog is in the 'edit' entry mode
	private static final String EDIT_MODE = "Edit";
	
	// 'New' mode constant - the dialog is in the 'new' entry mode
	private static final String NEW_MODE = "NEW";
	
	// Mode of this dialog - NEW_MODE for entering new password entry and EDIT_MODE for editting an existing password entry */
	String mode;
  
    // Service URL field
    private JTextField jtfServiceURL;
    
    // Username field
    private JTextField jtfUsername;
    
    // First password entry field
    private JPasswordField jpfFirstPassword;

    // Password confirmation entry field
    private JPasswordField jpfConfirmPassword;

    // Stores service URL entered 
    private String serviceURL;    
    
    // Stores username entered 
    private String username;
    
    // Stores password entered
    private String password;
    
    private Logger logger = Logger.getLogger(NewEditPasswordEntryDialog.class);

    /**
     * Creates new NewEditPasswordEntryDialog dialog where parent is a frame.
     */
    public NewEditPasswordEntryDialog(JFrame parent, String title,
			boolean modal, String currentURL, String currentUsername,
			String currentPassword)
    {
        super(parent, title, modal);        
        serviceURL = currentURL;
        username = currentUsername;
        password = currentPassword;
        if (serviceURL == null && username == null && password == null) // if passed values are all null
        {
        	mode = NEW_MODE; // dialog is for entering a new password entry
        }
        else{
            mode = EDIT_MODE; // dialog is for editing an existing entry
        }
        initComponents();
    }

    /**
     * Creates new NewEditPasswordEntryDialog dialog where parent is a dialog.
     */
    public NewEditPasswordEntryDialog(JDialog parent, String title, boolean modal, String currentURL, String currentUsername, String currentPassword)
    {
        super(parent, title, modal);
        serviceURL = currentURL;
        username = currentUsername;
        password = currentPassword;
        if (serviceURL == null && username == null && password == null) // if passed values are all null
        {
        	mode = NEW_MODE; // dialog is for entering new password entry
        }
        else{
            mode = EDIT_MODE; // dialog is for editing existing entry
        }
        initComponents();
    }
    
    
    /**
     * Get the username set in the dialog.
     *
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }
    
    /**
     * Get the service URL set in the dialog.
     *
     * @return the service URL
     */
    public String getServiceURL()
    {
        return serviceURL;
    }
    
    /**
     * Get the password set in the dialog.
     *
     * @return the password
     */
    public String getPassword()
    {
    	return password;
    }
    
    
    /**
     * Initialise the dialog's GUI components.
     */
    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        JLabel jlServiceURL = new JLabel("Service URL");
        jlServiceURL.setBorder(new EmptyBorder(0,5,0,0));
        
        JLabel jlUsername = new JLabel("Username");
        jlUsername.setBorder(new EmptyBorder(0,5,0,0));
        
        JLabel jlFirstPassword = new JLabel("Password");
        jlFirstPassword.setBorder(new EmptyBorder(0,5,0,0));
        
        JLabel jlConfirmPassword = new JLabel("Confirm password");
        jlConfirmPassword.setBorder(new EmptyBorder(0,5,0,0));
               
        jtfServiceURL = new JTextField(15);
        //jtfServiceURL.setBorder(new EmptyBorder(0,0,0,5));

        jtfUsername = new JTextField(15);
        //jtfUsername.setBorder(new EmptyBorder(0,0,0,5));

        jpfFirstPassword = new JPasswordField(15);
        //jpfFirstPassword.setBorder(new EmptyBorder(0,0,0,5));

        jpfConfirmPassword = new JPasswordField(15);
        //jpfConfirmPassword.setBorder(new EmptyBorder(0,0,0,5));

        
        //If in EDIT_MODE - populate the fields with current values
        if (mode.equals(EDIT_MODE)){
            jtfServiceURL.setText(serviceURL);
            jtfUsername.setText(username);     
            jpfFirstPassword.setText(password);
            jpfConfirmPassword.setText(password);
        }
        
        JButton jbOK = new JButton("OK");
        jbOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                okPressed();
            }
        });

        JButton jbCancel = new JButton("Cancel");
        jbCancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                cancelPressed();
            }
        });

        JPanel jpPassword = new JPanel(new GridLayout(4, 2, 5, 5));
        jpPassword.add(jlServiceURL);
        jpPassword.add(jtfServiceURL);
        jpPassword.add(jlUsername);
        jpPassword.add(jtfUsername);
        jpPassword.add(jlFirstPassword);
        jpPassword.add(jpfFirstPassword);
        jpPassword.add(jlConfirmPassword);
        jpPassword.add(jpfConfirmPassword);
        
        jpPassword.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
        
        jpPassword.setMinimumSize(new Dimension(300,100));

        JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        jpButtons.add(jbOK);
        jpButtons.add(jbCancel);

        getContentPane().add(jpPassword, BorderLayout.CENTER);
        getContentPane().add(jpButtons, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });

        setResizable(false);

        getRootPane().setDefaultButton(jbOK);

        pack();
    }

    /**
     * Checks for the following:
     * <ul>
     *     <li>That the user has supplied a non empty service URL
     *     <li>That the user has supplied a non empty username
     *     <li>That the user has supplied and confirmed a non empty password
     *     <li>That the entry with the same URL already does not exist in the Keystore
     * </ul>
     * and stores the new password in this object.
     *
     * @return true - if the user's dialog entry matches the above criteria, false otherwise
     */
    private boolean checkControls()
    {
    	serviceURL = new String(jtfServiceURL.getText());
    	if (serviceURL.length() == 0) {
            JOptionPane.showMessageDialog(this,
                "Service URL cannot be empty", 
                "Credential Manager Warning",
                JOptionPane.WARNING_MESSAGE);
               
            return false;
    	}
    	
    	username = new String(jtfUsername.getText());
    	if (username.length() == 0){
            JOptionPane.showMessageDialog(this,
                "Username cannot be empty", 
                "Credential Manager Warning",
                JOptionPane.WARNING_MESSAGE);
               
            return false;
    	}
    	   	
    	String sFirstPassword = new String(jpfFirstPassword.getPassword());
        String sConfirmPassword = new String(jpfConfirmPassword.getPassword());

    	if ((sFirstPassword.length() > 0) && (sFirstPassword.equals(sConfirmPassword))) { // passwords the same and non-empty
    		password = sFirstPassword;
        }
        else if ((sFirstPassword.length() == 0) && (sFirstPassword.equals(sConfirmPassword))){ // passwords match but are empty

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
    	
		// Check if the entered URL is already associated with another key pair entry in the Keystore
    	CredentialManager credManager = null;
    	try {
			credManager = CredentialManager.getInstance();
		} catch (CMException cme) {
			// Failed to instantiate Credential Manager - warn the user and exit
			String exMessage = "Failed to instantiate Credential Manager";
			logger.error(exMessage, cme);
			cme.printStackTrace();
			JOptionPane.showMessageDialog(new JFrame(), exMessage,
					"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
   	
    	HashMap<String, ArrayList<String>> urlMap = credManager.getServiceURLsforKeyPairs();
       	if (urlMap != null){ // should not be null really (although can be empty). Check anyway.
        	Set<String> aliases = urlMap.keySet();
        	for (Iterator<String> i = aliases.iterator(); i.hasNext(); ){
        		String alias = (String) i.next();
        		// Check if url list for this alias contains the newly entered url
        		ArrayList<String> urls = (ArrayList<String>) urlMap.get(alias);
        		if (urls.contains(serviceURL)){
            		// Warn the user and exit
                	JOptionPane.showMessageDialog(
                    		this, 
                    		"The entered URL is already associated with another key pair entry",
                			"Credential Manager Alert",
                			JOptionPane.INFORMATION_MESSAGE);
                	return false;
        		}    		
        	 }
       	}
    	
    	return true;
    }

    /**
     * OK button pressed or otherwise activated.
     */
    private void okPressed()
    {
        if (checkControls()) {
            closeDialog();
        }
    }

    /**
     * Cancel button pressed or otherwise activated.
     */
    private void cancelPressed()
    {
    	// Set all fields to null to indicate that cancel button was pressed
    	serviceURL = null;
    	username = null;
    	password = null;
        closeDialog();
    }

    /**
     * Close the dialog.
     */
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
}

