/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.workbench.ui.credentialmanager;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static org.apache.taverna.security.credentialmanager.CredentialManager.KeystoreType.KEYSTORE;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.ERROR_TITLE;
import static org.apache.taverna.workbench.ui.credentialmanager.CredentialManagerUI.PASSWORD_ENTRY_TYPE;

import java.net.URI;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.security.credentialmanager.KeystoreChangedEvent;
import org.apache.taverna.security.credentialmanager.UsernamePassword;

import org.apache.log4j.Logger;

/**
 * The table model used to display the Keystore's username/password pair
 * entries.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class PasswordsTableModel extends AbstractTableModel implements
		Observer<KeystoreChangedEvent> {
	private static final Logger logger = Logger
			.getLogger(PasswordsTableModel.class);

	// Column names
	private String[] columnNames;
	// Table data
	private Object[][] data;
	private CredentialManager credManager;

	public PasswordsTableModel(CredentialManager credentialManager) {
		credManager = credentialManager;
		if (credentialManager == null) {
			// Failed to instantiate Credential Manager - warn the user and exit
			String sMessage = "Failed to instantiate Credential Manager. ";
			logger.error("CM GUI: " + sMessage);
			showMessageDialog(new JFrame(), sMessage, ERROR_TITLE,
					ERROR_MESSAGE);
			return;
		}

		data = new Object[0][0];
		columnNames = new String[] { "Entry Type", // type of the Keystore entry
				"Service URL", // the service url, part of the actual alias in
								// the Keystore
				"Username", // username for the service, part of the password
							// entry in the Keystore
				"Last Modified", // last modified date of the entry
				"Password", // the invisible column holding the password value
							// of the password entry in the Keystore
				"Alias" // the invisible column holding the Keystore alias of
						// the entry
		};

		try {
			load();
		} catch (CMException cme) {
			String sMessage = "Failed to load username and password pairs";
			logger.error(sMessage);
			showMessageDialog(new JFrame(), sMessage, ERROR_TITLE,
					ERROR_MESSAGE);
			return;
		}

		// Start observing changes to the Keystore
		credManager.addObserver(this);
	}

	/**
	 * Load the PasswordsTableModel with the password entries from the Keystore.
	 */
	public void load() throws CMException {
		// Place password entries' aliases in a tree map to sort them
		TreeMap<String, String> aliases = new TreeMap<>();

		for (String alias : credManager.getAliases(KEYSTORE))
			/*
			 * We are only interested in username/password entries here. Alias
			 * for such entries is constructed as "password#"<SERVICE_URL> where
			 * service URL is the service this username/password pair is to be
			 * used for.
			 */
			if (alias.startsWith("password#"))
				aliases.put(alias, alias);

		// Create one table row for each password entry
		data = new Object[aliases.size()][6];

		/*
		 * Iterate through the sorted aliases, retrieving the password entries
		 * and populating the table model
		 */
		int iCnt = 0;
		for (String alias : aliases.values()) {
			/*
			 * Populate the type column - it is set with an integer but a custom
			 * cell renderer will cause a suitable icon to be displayed
			 */
			data[iCnt][0] = PASSWORD_ENTRY_TYPE;

			/*
			 * Populate the service URL column as a substring of alias from the
			 * first occurrence of '#' till the end of the string
			 */
			String serviceURL = alias.substring(alias.indexOf('#') + 1);
			data[iCnt][1] = serviceURL;

			/*
			 * Get the username and password pair from the Keystore. They are
			 * returned in a single string in format
			 * <USERNAME><SEPARATOR_CHARACTER><PASSWORD>
			 */
			UsernamePassword usernamePassword = credManager
					.getUsernameAndPasswordForService(URI.create(serviceURL),
							false, "");
			String username = usernamePassword.getUsername();
			String password = usernamePassword.getPasswordAsString();

			// Populate the username column
			data[iCnt][2] = username;

			// Populate the last modified date column ("UBER" keystore type
			// supports creation date)
			// data[iCnt][3] =
			// credManager.getEntryCreationDate(CredentialManager.KEYSTORE,
			// alias);

			// Populate the invisible password column
			data[iCnt][4] = password;

			// Populate the invisible alias column
			data[iCnt][5] = alias;

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
