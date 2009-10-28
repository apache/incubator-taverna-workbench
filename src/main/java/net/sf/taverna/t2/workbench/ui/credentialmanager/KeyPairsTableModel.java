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

import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.KeystoreChangedEvent;

/**
 * The table model used to display the Keystore's key pair entries.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class KeyPairsTableModel extends AbstractTableModel implements Observer<KeystoreChangedEvent> {

	// Column names 
    private String[] columnNames;

    // Table data 
    private Object[][] data;
    
	private CredentialManager credManager;
    
	private Logger logger = Logger.getLogger(KeyPairsTableModel.class);

    /**
     * Construct a new KeyPairsTableModel.
     */
    public KeyPairsTableModel()
    {
        credManager = null;
        try{
        	credManager = CredentialManager.getInstance();
        }
        catch (CMException cme){
			// Failed to instantiate Credential Manager - warn the user and exit
			String sMessage = "Failed to instantiate Credential Manager. " + cme.getMessage();
			logger.error("CM GUI: "+ sMessage);
			cme.printStackTrace();
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
            "URLs", // the invisible column holding the list of URLs associated with this entry
            "Alias" // the invisible column holding the actual alias in the Keystore
        	};
        
        try {
			load();
		} catch (CMException cme) {
			String sMessage = "Failed to load key pairs";
			logger.error(sMessage);
			cme.printStackTrace();
			JOptionPane.showMessageDialog(new JFrame(), sMessage,
					"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
        // Start observing changes to the Keystore
        credManager.addObserver(this);
    }
    
    /**
     * Load the KeyPairsTableModel with the key pair entries from the Keystore. 
     */
    public void load() throws CMException {
        
        try{
            // Place key pair entries' aliases in a tree map to sort them
            TreeMap<String, String> sortedAliases = new TreeMap<String, String>();  
            	
            ArrayList<String> aliases = credManager.getAliases(CredentialManager.KEYSTORE);
            
           	for (String alias: aliases){
        		// We are only interested in key pair entries here.
        		// Alias for such entries is constructed as "keypair#<CERT_SERIAL_NUMBER>#<CERT_COMMON_NAME>" where
        		if (alias.startsWith("keypair#")){
        			sortedAliases.put(alias, alias);
        		}
        	}   		
        			
            // Create one table row for each key pair entry
            data = new Object[sortedAliases.size()][7];

            // Iterate through the sorted aliases (if any), retrieving the key pair
            // entries and populating the table model
            int iCnt = 0;
            for (String alias : sortedAliases.values())
            {                
                
                // Populate the type column - it is set with an integer
                // but a custom cell renderer will cause a suitable icon
                // to be displayed
                data[iCnt][0] = CredentialManagerUI.KEY_PAIR_ENTRY_TYPE;

                // Split the alias string to extract owner, issuer and serial number 
                // alias = "keypair#"<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"#"<CERT_SERIAL_NUMBER>
                String[] aliasComponents = alias.split("#");
                
                // Populate the owner column extracted from the alias
                data[iCnt][1] = aliasComponents[1];
                
                // Populate the issuer column extracted from the alias
                data[iCnt][2] = aliasComponents[2];
                
                // Populate the serial number column extracted from the alias
                data[iCnt][3] = aliasComponents[3];
                
                // Populate the modified date column ("UBER" keystore type supports creation date)
               	data[iCnt][4] = credManager.getEntryCreationDate(CredentialManager.KEYSTORE, alias);

                // Populate the invisible URLs list column
                data[iCnt][5] = credManager.getServiceURLsForKeyPair(alias);   
                
                // Populate the invisible alias column
                data[iCnt][6] = alias;  
                
                iCnt++;
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

