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
package net.sf.taverna.t2.workbench.ui.credentialmanager.startup;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JButton;
//import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;

/**
 * Dialog for asking user if they wanted to import the content of an older version of Credential Manager files.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class CopyOldCredentialManagerDialog
    extends HelpEnabledDialog
{
	private Logger logger = Logger.getLogger(CopyOldCredentialManagerDialog.class);

    // Old Credential Manager master password field 
    private JPasswordField passwordField;
    
    // Stores password entered
    private String password;

    // Text for the user to explain what this dialog is about
    private String instructions;
    
	//private JCheckBox doNotAskMeToImportAgainCheckBox;

    public CopyOldCredentialManagerDialog(String previousTavernaVersion)
    {
        super((Frame)null, "Older version of Credential Manager detected", true);
        instructions = "<html><body>Taverna has detected an older version ("+previousTavernaVersion+") of Credential Manager<br> that may contain your credentals. " +
		"Do you wish to import them?<br><br>If yes, please provide your old master password.<br>It will be used as your new password as well.<br>Alternatively, you can always add credentials manually later.</body></html>";
        initComponents();
    } 
    
    /**
     * Initialise the dialog's GUI components.
     */
    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        JLabel instructionsLabel = new JLabel(instructions);
        instructionsLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel jpInstructions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jpInstructions.add(instructionsLabel);
        
        JLabel passwordLabel = new JLabel("Old master password");
        passwordLabel.setBorder(new EmptyBorder(5, 5, 5, 5));  
        passwordField = new JPasswordField(15);
        
        JButton importButton = new JButton("Import my credentials");
        importButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                importPressed();
            }
        });

        JButton dontImportButton = new JButton("Do not import anything");
        dontImportButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                dontImportPressed();
            }
        });

        JButton askMeLaterButton = new JButton("Ask me later");
        askMeLaterButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                askMeLaterPressed();
            }
        });
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        JPanel passwordPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
		//doNotAskMeToImportAgainCheckBox = new JCheckBox("Do not ask me again");
		//doNotAskMeToImportAgainCheckBox.setBorder(new EmptyBorder(0,10,0,0));
        passwordPanel.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 5, 10), new EtchedBorder()));
		mainPanel.add(passwordPanel, BorderLayout.CENTER);
		//mainPanel.add(doNotAskMeToImportAgainCheckBox, BorderLayout.SOUTH);                

        JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jpButtons.add(importButton);
        jpButtons.add(dontImportButton);
        jpButtons.add(askMeLaterButton);

        passwordPanel.setMinimumSize(new Dimension(300,100));

        getContentPane().add(jpInstructions, BorderLayout.NORTH);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(jpButtons, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });

        setResizable(false);

        getRootPane().setDefaultButton(importButton);

        pack();
    }

    public String getPassword()
    {
    	return password;
    }

    private boolean checkControls()
    {    	
    	password = new String(passwordField.getPassword());
    	if (password.length() == 0) { // password empty
            JOptionPane.showMessageDialog(this,
                "Password cannot be empty", 
                "Warning",
                JOptionPane.WARNING_MESSAGE);
            return false;        	
        } 	
    	return true;
    }

    private void importPressed()
    {
        if (checkControls()) {
        	// Set the "do not ask again" flag
    		try {
    			FileUtils
    					.touch(CheckForOlderCredentialManagersStartupHook.doNotAskToImportOldCredentialManagerFile);
    		} catch (IOException ioex) {
    			logger
    					.error(
    							"Failed to touch the 'Do not ask me to import old Credential Manager file.",
    							ioex);
    		}
    		closeDialog();
        }
    }

    private void dontImportPressed()
    {
    	// Set password to null to indicate that "Do not import" button was pressed
    	password = null;
    	// Set the "do not ask again" flag
		try {
			FileUtils
					.touch(CheckForOlderCredentialManagersStartupHook.doNotAskToImportOldCredentialManagerFile);
		} catch (IOException ioex) {
			logger
					.error(
							"Failed to touch the 'Do not ask me to import old Credential Manager file.",
							ioex);
		}
        closeDialog();
    }

    private void askMeLaterPressed()
    {
    	// Set password to null to indicate that the user does not wan to import now
    	password = null;
        closeDialog();
    }
    
    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }

	public void setPassword(String password) {
		this.password = password;
		passwordField.setText(password);
	}
}

