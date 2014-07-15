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
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.impl.CMUtils;
import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.ViewCertDetailsDialog;

/**
 * Allows the user import a key pair from a PKCS #12 file (keystore).
 */
@SuppressWarnings("serial")
class NewKeyPairEntryDialog extends NonBlockedHelpEnabledDialog {

	//private Logger logger = Logger.getLogger(NewKeyPairEntryDialog.class);
	
	// List of key pairs available for import 
    private JList keyPairsJList;

    // PKCS #12 keystore 
    private KeyStore pkcs12KeyStore;

    // Private key part of the key pair chosen by the user for import 
    private Key privateKey;

    // Certificate chain part of the key pair chosen by the user for import 
    private Certificate[] certificateChain;

    // Key pair alias to be used for this entry in the Keystore 
    private String alias;
    
    public NewKeyPairEntryDialog(JFrame parent, String title, boolean modal, KeyStore pkcs12KeyStore)
        throws CMException
    {
        super(parent, title, modal);
        this.pkcs12KeyStore = pkcs12KeyStore;
        initComponents();
    }

    public NewKeyPairEntryDialog(JDialog parent, String title, boolean modal, KeyStore pkcs12KeyStore)
        throws CMException
    {
        super(parent, title, modal);
        this.pkcs12KeyStore = pkcs12KeyStore;
        initComponents();
    }

    /**
     * Get the private part of the key pair.
     */
    public Key getPrivateKey()
    {
        return privateKey;
    }

    /**
     * Get the certificate chain part of the key pair.
     */
    public Certificate[] getCertificateChain()
    {
        return certificateChain;
    }

    /**
     * Get the keystore alias of the key pair.
     */
    public String getAlias()
    {
        return alias;
    }
    
    private void initComponents()
        throws CMException
    {
        // Instructions
        JLabel instructionsLabel = new JLabel("Select a key pair to import:");
        instructionsLabel.setFont(new Font(null, Font.PLAIN, 11));
        instructionsLabel.setBorder(new EmptyBorder(5,5,5,5));
        JPanel instructionsPanel = new JPanel(new BorderLayout());
        instructionsPanel.add(instructionsLabel, BorderLayout.WEST);

        // Import button
        final JButton importButton = new JButton("Import");
        importButton.setEnabled(false);
        importButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                importPressed();
            }
        });

        // Certificate details button
        final JButton certificateDetailsButton = new JButton("Details");
        certificateDetailsButton.setEnabled(false);
        certificateDetailsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                certificateDetailsPressed();
            }
        });

        // List to hold keystore's key pairs
        keyPairsJList = new JList();
        keyPairsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        keyPairsJList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {	
                if (keyPairsJList.getSelectedIndex() == -1) {
                    importButton.setEnabled(false);
                    certificateDetailsButton.setEnabled(false);
                }
                else {
                    importButton.setEnabled(true);
                    certificateDetailsButton.setEnabled(true);
                }
            }
        });

        // Put the key list into a scroll pane
        JScrollPane keyPairsScrollPane = new JScrollPane(keyPairsJList,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        keyPairsScrollPane.getViewport().setBackground(keyPairsJList.getBackground());
        
        JPanel keyPairsPanel = new JPanel();
        keyPairsPanel.setLayout(new BoxLayout(keyPairsPanel, BoxLayout.Y_AXIS));
        keyPairsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));      
   
        instructionsPanel.setAlignmentY(JPanel.LEFT_ALIGNMENT);
        keyPairsPanel.add(instructionsPanel);
        keyPairsScrollPane.setAlignmentY(JPanel.LEFT_ALIGNMENT);
        keyPairsPanel.add(keyPairsScrollPane);

        // Cancel button
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                cancelPressed();
            }
        });

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(certificateDetailsButton);
        buttonsPanel.add(importButton);
        buttonsPanel.add(cancelButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(keyPairsPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        // Populate the list
        populateKeyPairList();

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

    /**
     * Populate the key pair list with the PKCS #12 keystore's key
     * pair aliases.
     */
    private void populateKeyPairList()
        throws CMException
    {
        try {
        	ArrayList<String> keyPairAliases = new ArrayList<String>();

        	Enumeration<String> aliases = pkcs12KeyStore.aliases();
        	while (aliases.hasMoreElements())
            {
                String alias = aliases.nextElement();

                if (pkcs12KeyStore.isKeyEntry(alias)) { // is it a key entry?
                	pkcs12KeyStore.getKey(alias, new char[] {});
                    Certificate[] certs = pkcs12KeyStore.getCertificateChain(alias);
                    if (certs != null && certs.length != 0) {
                        keyPairAliases.add(alias);
                    }
                }
            }

            if (keyPairAliases.size() > 0) {
                keyPairsJList.setListData(keyPairAliases.toArray());
                keyPairsJList.setSelectedIndex(0);
            }
            else {
                // No key pairs were found - warn the user
            	JOptionPane.showMessageDialog(
                		this, 
                		"No private key pairs were found in the file",
            			"Credential Manager Alert",
            			JOptionPane.WARNING_MESSAGE);
            }
        }
        catch (GeneralSecurityException ex) {
            throw new CMException("Problem occured while reading the PKCS #12 file.",
                ex);
        }
    }

    /**
     * Display the selected key pair's certificate.
     */
    private void certificateDetailsPressed()
    {
        try {        	
            
        	String alias = (String) keyPairsJList.getSelectedValue();

            //Convert the certificate object into an X509Certificate object.
             X509Certificate cert = CMUtils.convertCertificate(pkcs12KeyStore.getCertificate(alias));

            ViewCertDetailsDialog viewCertificateDialog = new ViewCertDetailsDialog(this,
            		"Certificate details", 
            		true, 
            		(X509Certificate) cert,
            		null);
            viewCertificateDialog.setLocationRelativeTo(this);
            viewCertificateDialog.setVisible(true);
            
        }
        catch (Exception ex) {      	
            JOptionPane.showMessageDialog(this,
                    "Failed to obtain certificate details to show", 
                    "Credential Manager Alert",
                    JOptionPane.WARNING_MESSAGE);
            closeDialog();
        }
    }

    public void importPressed()
    {
        String alias = (String) keyPairsJList.getSelectedValue();
        try {
            privateKey = pkcs12KeyStore.getKey(alias, new char[] {});
            certificateChain = pkcs12KeyStore.getCertificateChain(alias);
            this.alias = alias;
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load the private key and certificate chain from the PKCS #12 file.", 
                    "Credential Manager Error",
                    JOptionPane.ERROR_MESSAGE);
            closeDialog();
        }

        closeDialog();
    }
    
    public void cancelPressed()
    {
    	// Set everything to null, just in case some of the values have been set previously and
    	// the user pressed 'cancel' after that.
    	privateKey = null;
    	certificateChain = null;
        closeDialog();
    }

    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
    
}

