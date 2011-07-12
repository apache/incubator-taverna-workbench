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
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Dialog used for getting a master password for Credential Manager
 * from the users.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class GetMasterPasswordDialog extends NonBlockedHelpEnabledDialog {
	
    // Password entry field 
    private JPasswordField passwordField;

    // The entered password
    private String password = null;
    
    // Text giving user the instructions what to do in the dialog
    private String instructions;

    public GetMasterPasswordDialog(String instructions)
    {
        super((Frame)null, "Enter master password", true);
        this.instructions = instructions;
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
        
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBorder(new EmptyBorder(0,5,0,0));

        passwordField = new JPasswordField(15);
        JPanel passwordPanel = new JPanel(new GridLayout(1, 1, 5, 5));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
        mainPanel.add(instructionsPanel, BorderLayout.NORTH);
        mainPanel.add(passwordPanel, BorderLayout.CENTER);

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
     * Get the password entered in the dialog.
     */
    public String getPassword()
    {
    	return password;
    }
    
    /**
     * Check that the entered password is not empty and store the entered password.
     */
    private boolean checkPassword()
    {
       password = new String(passwordField.getPassword());

       if (password.length() == 0) { //password is empty          
            JOptionPane.showMessageDialog(this,
                    "The password cannot be empty", 
                    "Credential Manager Warning",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        else { //password is not empty
        	return true;
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



