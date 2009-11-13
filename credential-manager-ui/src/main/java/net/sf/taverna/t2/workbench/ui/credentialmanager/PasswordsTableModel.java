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

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.KeystoreChangedEvent;
import net.sf.taverna.t2.workbench.ui.credentialmanager.CredentialManagerUI;

import org.apache.log4j.Logger;

/**
 * The table model used to display the Keystore's username/password 
 * pair entries.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class PasswordsTableModel extends AbstractTableModel implements Observer<KeystoreChangedEvent> {

	// Column names 
    private String[] columnNames;

    // Table data
    private Object[][] data;

	private CredentialManager credManager;
	
	private Logger logger = Logger.getLogger(PasswordsTableModel.class);

    /**
     * Construct a new PasswordsTableModel.
     */
    public PasswordsTableModel(){
    	
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
                "Service URL", // the service url, part of the actual alias in the Keystore
                "Username", // username for the service, part of the password entry in the Keystore
                "Last Modified", // last modified date of the entry
                "Password", // the invisible column holding the password value of the password entry in the Keystore
        		"Alias" // the invisible column holding the Keystore alias of the entry
        };
        
        try {
			load();
		} catch (CMException cme) {
			String sMessage = "Failed to load username and password pairs";
			logger.error(sMessage);
			JOptionPane.showMessageDialog(new JFrame(), sMessage,
					"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
        
        // Start observing changes to the Keystore
        credManager.addObserver(this);
    }
    
    /**
     * Load the PasswordsTableModel with the password entries from the Keystore. 
     */
    public void load() throws CMException
    {
    	try{
            // Place password entries' aliases in a tree map to sort them
            TreeMap<String, String> sortedAliases = new TreeMap<String, String>();  
            	
            ArrayList<String> aliases = credManager.getAliases(CredentialManager.KEYSTORE);
            
           	for (String alias: aliases){
        		// We are only interested in username/password entries here.
        		// Alias for such entries is constructed as "password#"<SERVICE_URL> where
        		// service URL is the service this username/password pair is to be used for.
        		if (alias.startsWith("password#")){
        			sortedAliases.put(alias, alias);
        		}
        	}

            // Create one table row for each password entry
            data = new Object[sortedAliases.size()][6];

            // Iterate through the sorted aliases, retrieving the password
            // entries and populating the table model
            int iCnt = 0;
            for (String alias : sortedAliases.values()){
                
                // Populate the type column - it is set with an integer
                // but a custom cell renderer will cause a suitable icon
                // to be displayed
                data[iCnt][0] = CredentialManagerUI.PASSWORD_ENTRY_TYPE;

                // Populate the service URL column as a substring of alias 
                // from the first occurrence of '#' till the end of the string
                String serviceURL = alias.substring(alias.indexOf('#')+1);
                data[iCnt][1] = serviceURL;
                
                // Get the username and password pair from the Keystore. They
                // are returned in a single string in format <USERNAME><SEPARATOR_CHARACTER><PASSWORD>
                String[] unpassPair = credManager.getUsernameAndPasswordForService(serviceURL);
            	String username = unpassPair[0];
            	String password = unpassPair[1];

                // Populate the username column
                data[iCnt][2] = username;
               
                // Populate the last modified date column ("UBER" keystore type supports creation date)
                data[iCnt][3] = credManager.getEntryCreationDate(CredentialManager.KEYSTORE, alias);
                
                // Populate the invisible password column
                data[iCnt][4] = password; 
                
                // Populate the invisible alias column
                data[iCnt][5] = alias;
                
                iCnt++;
            }

            fireTableDataChanged();
    	}
    	catch (CMException cme){
			throw cme;
    	}
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

