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

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.security.auth.x500.X500Principal;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CMX509Util;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.KeystoreChangedEvent;

/**
 * The table model used to display the Keystore's proxy key pair entries.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class ProxiesTableModel extends AbstractTableModel implements Observer<KeystoreChangedEvent> {

	// Column names 
    private String[] columnNames;

    // Table data 
    private Object[][] data;
    
	private CredentialManager credManager;
    
	private Logger logger = Logger.getLogger(KeyPairsTableModel.class);

    /**
     * Construct a new KeyPairsTableModel.
     */
    public ProxiesTableModel()
    {
        credManager = null;
        try{
        	credManager = CredentialManager.getInstance();
        }
        catch (CMException cme){
			// Failed to instantiate Credential Manager - warn the user and exit
			String sMessage = "Failed to instantiate Credential Manager. " + cme.getMessage();
			logger.error("CM GUI: "+ sMessage);
			JOptionPane.showMessageDialog(new JFrame(), sMessage,
					"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
			return;
        }
    	
       	data = new Object[0][0];
        columnNames = new String[] {
            	"Entry Type", // type of the Keystore entry
            	"Owner", // owner's common name
            	"Issuer", // issuer's common name
            	"Serial Number", // public key certificate's serial number
            	"Last Modified", // last modified date of the entry
                "Alias", // the invisible column holding the actual alias in the Keystore
        	};
        
        try {
			load();
		} catch (CMException cme) {
			String sMessage = "Failed to load proxies";
			logger.error(sMessage);
			JOptionPane.showMessageDialog(new JFrame(), sMessage,
					"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
        // Start observing changes to the Keystore
        credManager.addObserver(this);
    }
    
    /**
     * Load the ProxiesTableModel with the proxy entries from the Keystore. 
     */
    public void load() throws CMException {
        
        try{
            // Place proxy entries' aliases in a tree map to sort them
            TreeMap<String, String> sortedAliases = new TreeMap<String, String>();  
            	
            ArrayList<String> aliases = credManager.getAliases(CredentialManager.KEYSTORE);
            
           	for (String alias: aliases){
        		// We are only interested in proxy entries here.
        		// Alias for such entries is constructed as "proxy#<CERT_SERIAL_NUMBER>#<CERT_COMMON_NAME>" where
        		if (alias.startsWith("cagridproxy#") || alias.startsWith("proxy#")){ // for different proxies
        			sortedAliases.put(alias, alias);
        		}
        	}    		
        			
            // Create one table row for each proxy entry
            data = new Object[sortedAliases.size()][6];

            // Iterate through the sorted aliases (if any), retrieving the proxy
            // entries and populating the table model
            int iCnt = 0;
            for (String alias : sortedAliases.values())
            {
            	if (alias.startsWith("cagridproxy#")){
                                        
                    // Populate the type column - it is set with an integer
                    // but a custom cell renderer will cause a suitable icon
                    // to be displayed
                    data[iCnt][0] = CredentialManagerUI.PROXY_ENTRY_TYPE;

                    X509Certificate cert = CMX509Util.convertCertificate(credManager.getCertificate(CredentialManager.KEYSTORE, alias));

                    // Populate the "Owner:Serial Number" column extracted from the alias
        			String ownerDN = cert.getSubjectX500Principal().getName(X500Principal.RFC2253);
        			CMX509Util util = new CMX509Util();
        			util.parseDN(ownerDN);
                    data[iCnt][1] = util.getCN(); // owner's common name
                    
                    // Populate the issuer column
                    String issuerDN = cert.getIssuerX500Principal().getName(X500Principal.RFC2253);
                    util.parseDN(issuerDN);
                    data[iCnt][2] = util.getCN();
        			
        			// Get the hexadecimal representation of the certificate's serial number
        			String serialNumber = new BigInteger(1, cert.getSerialNumber().toByteArray()).toString(16).toUpperCase();
                    data[iCnt][3] = serialNumber;
                    
                    // Populate the modified date column ("UBER" keystore type supports creation date)
                   	data[iCnt][4] = credManager.getEntryCreationDate(CredentialManager.KEYSTORE, alias);
                    
                    // Populate the invisible alias column
                    data[iCnt][5] = alias; 
                    
                    iCnt++;
            	}
            	// else if (alias.startsWith("proxy#")) {} // for some other proxies
            }
        }
        catch (CMException cme){
            throw (cme);
        }

        fireTableDataChanged();
    }
    
    /**
     * Get the number of columns in the table.
     */
    public int getColumnCount()
    {
        return columnNames.length;
    }

    /**
     * Get the number of rows in the table.
     */
    public int getRowCount()
    {
        return data.length;
    }

    /**
     * Get the name of the column at the given position.
     */
    public String getColumnName(int iCol)
    {
        return columnNames[iCol];
    }

    /**
     * Get the cell value at the given row and column position.
     */
    public Object getValueAt(int iRow, int iCol)
    {
        return data[iRow][iCol];
    }

    /**
     * Get the class at of the cells at the given column position.
     */
    public Class<? extends Object> getColumnClass(int iCol)
    {
        return getValueAt(0, iCol).getClass();
    }

    /**
     * Is the cell at the given row and column position editable?
     */
    public boolean isCellEditable(int iRow, int iCol)
    {
        // The table is always read-only
        return false;
    }

	public void notify(Observable<KeystoreChangedEvent> sender,
			KeystoreChangedEvent message) throws Exception {

		// reload the table
		if (message.keystoreType.equals(CredentialManager.KEYSTORE)){
			load();
		}
	}    

}


