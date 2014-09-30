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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.security.auth.x500.X500Principal;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import net.sf.taverna.t2.security.credentialmanager.DistinguishedNameParser;
import net.sf.taverna.t2.security.credentialmanager.ParsedDistinguishedName;
import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.ViewCertDetailsDialog;

/**
 * Allows the user to import one or more trusted certificates from a file.
 */
@SuppressWarnings("serial")
public class NewTrustCertsDialog extends NonBlockedHelpEnabledDialog {

    private JList trustedCertsJList;

	// List of trusted certs read from the file and available for import
    private ArrayList<X509Certificate> availableTrustedCerts = new ArrayList<X509Certificate>();
    
    // List of trusted certs selected for import
    private ArrayList<X509Certificate> selectedTrustedCerts;
    
    private final DistinguishedNameParser dnParser;
    
    public NewTrustCertsDialog(JFrame parent, String title, boolean modal, ArrayList<X509Certificate> lCerts, DistinguishedNameParser dnParser)
    {
        super(parent, title, modal);
        availableTrustedCerts = lCerts;
        this.dnParser = dnParser;
        initComponents();
    }
    
    public NewTrustCertsDialog(JDialog parent, String title, boolean modal, ArrayList<X509Certificate> lCerts, DistinguishedNameParser dnParser)
    {
        super(parent, title, modal);
        availableTrustedCerts = lCerts;
        this.dnParser = dnParser;
        initComponents();
    }

    private void initComponents()
    {
        // Instructions
        JLabel instructionsLabel = new JLabel("Select one or more certificates for import:");
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
        final JButton certificateDetailsButton = new JButton("Certificate Details");
        certificateDetailsButton.setEnabled(false);
        certificateDetailsButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                certificateDetailsPressed();
            }
        });

        // List with trusted certs' aliases
        trustedCertsJList = new JList();
        trustedCertsJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
        trustedCertsJList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {           	
                if (trustedCertsJList.getSelectedIndex() == -1) {
                    importButton.setEnabled(false);
                    certificateDetailsButton.setEnabled(false);
                }
                else {
                    importButton.setEnabled(true);
                    certificateDetailsButton.setEnabled(true);
                }
            }
        });
        // Populate the list - get the certificate subjects' CNs
        ArrayList<String> cns = new ArrayList<String>();
        for (int i = 0; i < availableTrustedCerts.size(); i++){        	
    		
        	String subjectDN = ((X509Certificate) availableTrustedCerts.get(i)).getSubjectX500Principal().getName(X500Principal.RFC2253);
                ParsedDistinguishedName parsedDN = dnParser.parseDN(subjectDN);    		
        	String subjectCN = parsedDN.getCN();
        	cns.add(i, subjectCN);
        }
        trustedCertsJList.setListData(cns.toArray());
        trustedCertsJList.setSelectedIndex(0);

        // Put the list into a scroll pane
        JScrollPane trustedCertsScrollPanel = new JScrollPane(trustedCertsJList,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        trustedCertsScrollPanel.getViewport().setBackground(trustedCertsJList.getBackground());
        
        JPanel trustedCertsPanel = new JPanel(); 
        trustedCertsPanel.setLayout(new BoxLayout(trustedCertsPanel, BoxLayout.Y_AXIS));
        trustedCertsPanel.setBorder(new CompoundBorder(new CompoundBorder(
            new EmptyBorder(5, 5, 5, 5), new EtchedBorder()), new EmptyBorder(
            5, 5, 5, 5)));
   
        instructionsPanel.setAlignmentY(JPanel.LEFT_ALIGNMENT);
        trustedCertsPanel.add(instructionsPanel);
        trustedCertsScrollPanel.setAlignmentY(JPanel.LEFT_ALIGNMENT);
        trustedCertsPanel.add(trustedCertsScrollPanel);
        certificateDetailsButton.setAlignmentY(JPanel.RIGHT_ALIGNMENT);
        trustedCertsPanel.add(certificateDetailsButton);

        // Cancel button
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                cancelPressed();
            }
        });

        JPanel jpButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        jpButtons.add(importButton);
        jpButtons.add(cancelButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(trustedCertsPanel, BorderLayout.CENTER);
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

    /**
     * Shows the selected key pair's certificate.
     */
    private void certificateDetailsPressed()
    {
        try {        	
        	
        	int i = trustedCertsJList.getSelectedIndex();
            
        	X509Certificate cert = (X509Certificate) availableTrustedCerts.get(i);

            ViewCertDetailsDialog viewCertificateDialog = new ViewCertDetailsDialog(this,
            		"Certificate details", 
            		true, 
            		cert,
            		null,
                        dnParser);
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

    /**
     * Get the trusted certificates selected for import.
     */
    public ArrayList<X509Certificate> getTrustedCertificates()
    {
    	return selectedTrustedCerts;
    }

   
    /**
     * Store the selected trusted certs.
     */
    public void importPressed()
    {
    	int[] selectedValues = trustedCertsJList.getSelectedIndices();
    	selectedTrustedCerts = new ArrayList<X509Certificate>();
    	for (int i= 0; i < selectedValues.length; i++){
    		selectedTrustedCerts.add(availableTrustedCerts.get(selectedValues[i]));
    	}

        closeDialog();
    }

    public void cancelPressed()
    {
    	// Set selectedTrustCerts to null to indicate that user has cancelled the import
    	selectedTrustedCerts = null;
        closeDialog();
    }

    private void closeDialog()
    {
        setVisible(false);
        dispose();
    }
}

