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

/**
 * Dialog used for viewing service URL, username and password.
 * 
 * @author Alex Nenadic
 */
public class ViewUsernamePasswordEntryDialog
    extends JDialog
{
	private static final long serialVersionUID = -7224904997349644853L;

	// Service URL field 
    private JTextField jtfServiceURL;
    
    // Username field 
    private JTextField jtfUsername;
    
    // Password field 
    private JTextField jtfPassword;

    // Service URL value
    private String serviceURL;    
    
    // Service username value
    private String username;
    
    // Service password value
    private String password;

    /**
     * Creates new ViewPasswordEntryDialog dialog where the parent is a frame.
     */
    public ViewUsernamePasswordEntryDialog(JFrame parent, String currentURL, String currentUsername, String currentPassword)
    {
        super(parent, "View username and password for a service", true);
        serviceURL = currentURL;
        username = currentUsername;
        password = currentPassword;
        initComponents();
    }

    /**
     * Creates new ViewPasswordDialog dialog where the parent is a dialog.
     */
    public ViewUsernamePasswordEntryDialog(JDialog parent, String currentURL, String currentUsername, String currentPassword)
    {
        super(parent, "View username and password for a service", true);
        serviceURL = currentURL;
        username = currentUsername;
        password = currentPassword;
        initComponents();
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
        JLabel jlPassword = new JLabel("Password");
        jlPassword.setBorder(new EmptyBorder(0,5,0,0));

        //Populate the fields with values and disable user input
        jtfServiceURL = new JTextField();
        jtfServiceURL.setText(serviceURL);
        jtfServiceURL.setEditable(false);
        //jtfServiceURL.setBorder(new EmptyBorder(0,0,0,5));
        
        jtfUsername = new JTextField(15);
        jtfUsername.setText(username);
        jtfUsername.setEditable(false);
       // jtfUsername.setBorder(new EmptyBorder(0,0,0,5));
        
        jtfPassword = new JTextField(15);
        jtfPassword.setText(password);
        jtfPassword.setEditable(false);
        //jtfPassword.setBorder(new EmptyBorder(0,0,0,5));

        JButton jbOK = new JButton("OK");
        jbOK.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
            	closeDialog();
            }
        });
        
//        JButton jbViewPassword = new JButton("View password");
//        jbViewPassword.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent evt)
//            {
//            	showPassword();
//            }
//        });
        
        JPanel jpPassword = new JPanel(new GridBagLayout());
        
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weighty = 0.0;
		
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        jpPassword.add(jlServiceURL, gbc);
        
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        jpPassword.add(jtfServiceURL, gbc);
        
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        jpPassword.add(jlUsername, gbc);
        
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        jpPassword.add(jtfUsername, gbc);
        
		gbc.weightx = 0.0;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 0);
        jpPassword.add(jlPassword, gbc);
        
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(5, 10, 0, 5);
        jpPassword.add(jtfPassword, gbc);
        
        
        jpPassword.setBorder(new CompoundBorder(
                new EmptyBorder(10, 10, 10, 10), new EtchedBorder()));
        
        JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        jpButtons.add(jbOK);
       // jpButtons.add(jbViewPassword);
        
        getContentPane().add(jpPassword, BorderLayout.CENTER);
        getContentPane().add(jpButtons, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent evt)
            {
                closeDialog();
            }
        });

       // setResizable(false);

        getRootPane().setDefaultButton(jbOK);

        pack();
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


