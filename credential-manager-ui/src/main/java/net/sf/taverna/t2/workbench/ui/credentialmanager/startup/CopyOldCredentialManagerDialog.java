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

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

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

    // Old Credential Manager master password field 
    private JPasswordField jpfPassword;
    
    // Stores password entered
    private String password;

    // Text for the user to explain what this dialog is about
    private String instructions;
    
    public CopyOldCredentialManagerDialog(String previousTavernaVersion)
    {
        super((Frame)null, "Older version of Credential Manager detected", true);
        instructions = "<html><body>Taverna has detected an older version ("+previousTavernaVersion+") of Credential Manager<br> that may contain your credentals. " +
		"Do you wish to import them?<br><br>If yes, please provide your old master password below.<br>Alternatively, you can always add them manually later.</body></html>";
        initComponents();
    } 
    
    /**
     * Initialise the dialog's GUI components.
     */
    private void initComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        JLabel jlInstructions = new JLabel(instructions);
//        JTextArea jtaInstructions = new JTextArea(instructions);
//        jtaInstructions.setEditable(false);
//        //jtaInstructions.setBackground(this.getBackground());
//        jtaInstructions.setFont(new Font(null, Font.PLAIN, 11));
        jlInstructions.setBorder(new EmptyBorder(5, 5, 5, 5));
        JPanel jpInstructions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        jpInstructions.add(jlInstructions);
        
        JLabel jlPassword = new JLabel("Old master password");
        jlPassword.setBorder(new EmptyBorder(5, 5, 5, 5));  
        jpfPassword = new JPasswordField(15);
        
        JButton jbImport = new JButton("Import credentials");
        jbImport.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                importPressed();
            }
        });

        JButton jbDontImport = new JButton("Do not import anything now");
        jbDontImport.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                dontImportPressed();
            }
        });

        // Central panel with username/password fields and a "Do you want to Save?" checkbox
        JPanel jpMain = new JPanel(new BorderLayout());
        
        JPanel jpPassword = new JPanel(new GridLayout(2, 2, 5, 5));
        jpPassword.add(jlPassword);
        jpPassword.add(jpfPassword);
        jpMain.add(jpPassword, BorderLayout.CENTER);
                
        jpPassword.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
        
        JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        jpButtons.add(jbImport);
        jpButtons.add(jbDontImport);

        jpPassword.setMinimumSize(new Dimension(300,100));

        getContentPane().add(jpInstructions, BorderLayout.NORTH);
        getContentPane().add(jpMain, BorderLayout.CENTER);
        getContentPane().add(jpButtons, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });

        setResizable(false);

        getRootPane().setDefaultButton(jbImport);

        pack();
    }

    
    /**
     * Get the password set in the dialog.
     */
    public String getPassword()
    {
    	return password;
    }

    /**
     * Check if an non-empty password was entered.
     */
    private boolean checkControls()
    {    	
    	password = new String(jpfPassword.getPassword());
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
            closeDialog();
        }
    }

    private void dontImportPressed()
    {
    	// Set password to null to indicate that "Do not import" button was pressed
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

	public void setPassword(String password) {
		this.password = password;
		jpfPassword.setText(password);
	}
}

