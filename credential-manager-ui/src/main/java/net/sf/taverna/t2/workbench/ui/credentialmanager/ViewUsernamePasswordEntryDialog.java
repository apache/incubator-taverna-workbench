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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog used for viewing service URL, username and password.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class ViewUsernamePasswordEntryDialog
    extends NonBlockedHelpEnabledDialog
{
	// Service URL field 
    private JTextField serviceURLField;
    
    // Username field 
    private JTextField usernameField;
    
    // Password field 
    private JTextField passwordField;

    // Service URL value
    private String serviceURL;    
    
    // Service username value
    private String username;
    
    // Service password value
    private String password;

    public ViewUsernamePasswordEntryDialog(JFrame parent, String currentURL, String currentUsername, String currentPassword)
    {
        super(parent, "View username and password for a service", true);
        serviceURL = currentURL;
        username = currentUsername;
        password = currentPassword;
        initComponents();
    }

    public ViewUsernamePasswordEntryDialog(JDialog parent, String currentURL, String currentUsername, String currentPassword)
    {
        super(parent, "View username and password for a service", true);
        serviceURL = currentURL;
        username = currentUsername;
        password = currentPassword;
        initComponents();
    }

    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        JLabel serviceURLLabel = new JLabel("Service URL");
        serviceURLLabel.setBorder(new EmptyBorder(0,5,0,0));
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setBorder(new EmptyBorder(0,5,0,0));
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBorder(new EmptyBorder(0,5,0,0));

        //Populate the fields with values and disable user input
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
        okButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
            	closeDialog();
            }
        });
        
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 0.0;
		
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        fieldsPanel.add(serviceURLLabel, gbc);
        
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        fieldsPanel.add(serviceURLField, gbc);
        
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        fieldsPanel.add(usernameLabel, gbc);
        
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        fieldsPanel.add(usernameField, gbc);
        
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        fieldsPanel.add(passwordLabel, gbc);
        
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        fieldsPanel.add(passwordField, gbc);
        
        
        fieldsPanel.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(okButton);
        
        getContentPane().add(fieldsPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });

       // setResizable(false);

        getRootPane().setDefaultButton(okButton);

        pack();
    }

    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
}


