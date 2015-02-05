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

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static net.sf.taverna.t2.security.credentialmanager.CredentialManager.KeystoreType.TRUSTSTORE;
import static net.sf.taverna.t2.workbench.ui.credentialmanager.CMStrings.ERROR_TITLE;
import static net.sf.taverna.t2.workbench.ui.credentialmanager.CredentialManagerUI.TRUST_CERT_ENTRY_TYPE;

import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.KeystoreChangedEvent;

import org.apache.log4j.Logger;

/**
 * The table model used to display the Keystore's trusted certificate entries.
 * 
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class TrustedCertsTableModel extends AbstractTableModel implements
		Observer<KeystoreChangedEvent> {
	private static final Logger logger = Logger
			.getLogger(TrustedCertsTableModel.class);

	// Column names
	private String[] columnNames;
	// Table data
	private Object[][] data;
	private CredentialManager credManager;

	public TrustedCertsTableModel(CredentialManager credentialManager) {
		credManager = credentialManager;
		if (credentialManager == null) {
			// Failed to instantiate Credential Manager - warn the user and exit
			String sMessage = "Failed to instantiate Credential Manager. ";
			logger.error("CM GUI: "+ sMessage);
			showMessageDialog(new JFrame(), sMessage, ERROR_TITLE,
					ERROR_MESSAGE);
			return;
        }

       	data = new Object[0][0];
        columnNames = new String[] {
        	"Entry Type", // type of the Keystore entry
        	"Owner", // owner's common name
        	"Issuer", // issuer's common name
        	"Serial Number", // public key certificate's serial number
            "Last Modified", // last modified date of the entry
            "Alias" // the invisible column holding the actual alias in the Keystore
        };

        try {
			load();
		} catch (CMException cme) {
			String sMessage = "Failed to load trusted certificates";
			logger.error(sMessage);
			showMessageDialog(new JFrame(), sMessage, ERROR_TITLE,
					ERROR_MESSAGE);
			return;
		}

        // Start observing changes to the Keystore
        credManager.addObserver(this);
    }

    /**
     * Load the TrustCertsTableModel with trusted certificate entries from the Keystore.
     */
	public void load() throws CMException {
		/*
		 * Place trusted certificate entries' aliases in a tree map to sort them
		 */
		Set<String> aliases = new TreeSet<>();
		for (String alias : credManager.getAliases(TRUSTSTORE))
			/*
			 * We are only interested in trusted certificate entries here. Alias
			 * for such entries is constructed as
			 * "trustedcert#<CERT_SERIAL_NUMBER>#<CERT_COMMON_NAME>"
			 */
			if (alias.startsWith("trustedcert#"))
				aliases.add(alias);

		/*
		 * Create one table row for each trusted certificate entry Each row has
		 * 4 fields - type, owner name, last modified data and the invisible
		 * alias
		 */
		data = new Object[aliases.size()][6];

		/*
		 * Iterate through the sorted aliases, retrieving the trusted
		 * certificate entries and populating the table model
		 */
		int i = 0;
		for (String alias : aliases) {
			/*
			 * Populate the type column - it is set with an integer but a custom
			 * cell renderer will cause a suitable icon to be displayed
			 */
			data[i][0] = TRUST_CERT_ENTRY_TYPE;

			/*
			 * Split the alias string to extract owner, issuer and serial number
			 * alias =
			 * "trustedcert#<CERT_SUBJECT_COMMON_NAME>"#"<CERT_ISSUER_COMMON_NAME>"
			 * #"<CERT_SERIAL_NUMBER>
			 */
			String[] aliasComponents = alias.split("#");

			// Populate the owner column extracted from the alias
			data[i][1] = aliasComponents[1];

			// Populate the issuer column extracted from the alias
			data[i][2] = aliasComponents[2];

			// Populate the serial number column extracted from the alias
			data[i][3] = aliasComponents[3];

			// Populate the modified date column
			//data[iCnt][4] = credManager.getEntryCreationDate(CredentialManager.TRUSTSTORE, alias);

			// Populate the invisible alias column
			data[i][5] = alias;

			i++;
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
		if (message.keystoreType.equals(TRUSTSTORE))
			load();
	}
}
