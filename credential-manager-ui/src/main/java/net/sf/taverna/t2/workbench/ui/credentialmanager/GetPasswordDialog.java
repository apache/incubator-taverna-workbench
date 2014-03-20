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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * A general dialog for entering a password.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class GetPasswordDialog extends NonBlockedHelpEnabledDialog {

	// Instructions for user explaining the purpose of the password 
	private String instructions = null;
	
    // Password entry password field 
    private JPasswordField passwordField;

    // Stores the password entered 
    private String password = null;

    public GetPasswordDialog(JFrame parent, String title, boolean modal, String instr)
    {
        super(parent, title, modal);
        instructions = instr;
        initComponents();
    }

    public GetPasswordDialog(JDialog parent, String title, boolean modal, String instr)
    {
        super(parent, title, modal);
        instructions = instr;
        initComponents();
    }

    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());
               	
        JLabel passwordLabel = new JLabel("Password");
        passwordField = new JPasswordField(15);

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

             
        JLabel instructionsLabel; // Instructions
        if (instructions != null){
        	instructionsLabel = new JLabel (instructions);
        	instructionsLabel.setFont(new Font(null, Font.PLAIN, 11));
        	instructionsLabel.setBorder(new EmptyBorder(5,5,5,5));
        	getContentPane().add(instructionsLabel, BorderLayout.NORTH);
        }
        
        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        passwordPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

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
     * Check that the password entered is not empty and 
     * store the entered password.
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
    	password = null;
        closeDialog();
    }

    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
}

