/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.credentialmanager;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.security.credentialmanager.CredentialManager.KeystoreType.KEYSTORE;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.ERROR_TITLE;
import static org.apache.taverna.workbench.ui.credentialmanager.CredentialManagerUI.KEY_PAIR_ENTRY_TYPE;

import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.security.credentialmanager.KeystoreChangedEvent;

import org.apache.log4j.Logger;

/**
 * The table model used to display the Keystore's key pair entries.
 *
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class KeyPairsTableModel extends AbstractTableModel implements Observer<KeystoreChangedEvent> {
	private static final Logger logger = Logger.getLogger(KeyPairsTableModel.class);

	/** Column names*/
    private String[] columnNames;
    /** Table data*/
    private Object[][] data;
	private CredentialManager credManager;

    public KeyPairsTableModel(CredentialManager credentialManager) {
        credManager = credentialManager;

		if (credManager == null) {
			/* Failed to instantiate Credential Manager - warn the user and exit */
			String sMessage = "Failed to instantiate Credential Manager. ";
			logger.error("CM GUI: " + sMessage);
			showMessageDialog(new JFrame(), sMessage,
					ERROR_TITLE, ERROR_MESSAGE);
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
			logger.error(sMessage, cme);
			showMessageDialog(new JFrame(), sMessage,
					ERROR_TITLE, ERROR_MESSAGE);
			return;
		}

        // Start observing changes to the Keystore
        credManager.addObserver(this);
    }

    /**
     * Load the table model with the key pair entries from the Keystore.
     */
    public void load() throws CMException {
    	// Place key pair entries' aliases in a tree map to sort them
    	TreeMap<String, String> sortedAliases = new TreeMap<>();

		for (String alias: credManager.getAliases(KEYSTORE))
			/*
			 * We are only interested in key pair entries here.
			 * 
			 * Alias for such entries is constructed as
			 * "keypair#<CERT_SERIAL_NUMBER>#<CERT_COMMON_NAME>" where
			 */
			if (alias.startsWith("keypair#"))
				sortedAliases.put(alias, alias);

		// Create one table row for each key pair entry
		data = new Object[sortedAliases.size()][7];

		/*
		 * Iterate through the sorted aliases (if any), retrieving the key pair
		 * entries and populating the table model
		 */
		int iCnt = 0;
		for (String alias : sortedAliases.values()) {
			/*
			 * Populate the type column - it is set with an integer but a custom
			 * cell renderer will cause a suitable icon to be displayed
			 */
			data[iCnt][0] = KEY_PAIR_ENTRY_TYPE;

			/*
			 * Split the alias string to extract owner, issuer and serial number
			 * alias =
			 * "keypair#"<SUBJECT_COMMON_NAME>"#"<ISSUER_COMMON_NAME>"#"<SERIAL_NUMBER>
			 */
			String[] aliasComponents = alias.split("#");

			// Populate the owner column extracted from the alias
			data[iCnt][1] = aliasComponents[1];

			// Populate the issuer column extracted from the alias
			data[iCnt][2] = aliasComponents[2];

			// Populate the serial number column extracted from the alias
			data[iCnt][3] = aliasComponents[3];

			// Populate the modified date column ("UBER" keystore type supports creation date)
			//data[iCnt][4] = credManager.getEntryCreationDate(CredentialManager.KEYSTORE, alias);

			// Populate the invisible URLs list column
			//data[iCnt][5] = credManager.getServiceURLsForKeyPair(alias);

			// Populate the invisible alias column
			data[iCnt][6] = alias;

			iCnt++;
		}

		fireTableDataChanged();
    }

	/**
	 * Get the number of columns in the table.
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Get the number of rows in the table.
	 */
	@Override
	public int getRowCount() {
		return data.length;
	}

	/**
	 * Get the name of the column at the given position.
	 */
	@Override
	public String getColumnName(int iCol) {
		return columnNames[iCol];
	}

	/**
	 * Get the cell value at the given row and column position.
	 */
	@Override
	public Object getValueAt(int iRow, int iCol) {
		return data[iRow][iCol];
	}

	/**
	 * Get the class at of the cells at the given column position.
	 */
	@Override
	public Class<? extends Object> getColumnClass(int iCol) {
		return getValueAt(0, iCol).getClass();
	}

	/**
	 * Is the cell at the given row and column position editable?
	 */
	@Override
	public boolean isCellEditable(int iRow, int iCol) {
		// The table is always read-only
		return false;
	}

	@Override
	public void notify(Observable<KeystoreChangedEvent> sender,
			KeystoreChangedEvent message) throws Exception {
		// reload the table
		if (message.keystoreType.equals(KEYSTORE))
			load();
	}
}
