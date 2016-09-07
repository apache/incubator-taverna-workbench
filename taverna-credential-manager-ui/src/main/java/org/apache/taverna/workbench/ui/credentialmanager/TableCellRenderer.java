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
/*

package org.apache.taverna.workbench.ui.credentialmanager;

import static org.apache.taverna.workbench.ui.credentialmanager.CredentialManagerUI.KEY_PAIR_ENTRY_TYPE;
import static org.apache.taverna.workbench.ui.credentialmanager.CredentialManagerUI.PASSWORD_ENTRY_TYPE;
import static org.apache.taverna.workbench.ui.credentialmanager.CredentialManagerUI.TRUST_CERT_ENTRY_TYPE;

import java.awt.Component;
//import java.text.DateFormat;
//import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
//import net.sf.taverna.t2.workbench.ui.credentialmanager.KeyPairsTableModel;
//import net.sf.taverna.t2.workbench.ui.credentialmanager.PasswordsTableModel;
//import net.sf.taverna.t2.workbench.ui.credentialmanager.TrustedCertsTableModel;

/**
 * Custom cell renderer for the cells of the tables displaying
 * Keystore/Truststore contents.
 * 
 * @author Alex Nenadic
 */
public class TableCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -3983986682794010259L;

	private final ImageIcon passwordEntryIcon = new ImageIcon(
			TableCellRenderer.class.getResource("/images/table/key_entry.png"));
	private final ImageIcon keypairEntryIcon = new ImageIcon(
			TableCellRenderer.class
					.getResource("/images/table/keypair_entry.png"));
	private final ImageIcon trustcertEntryIcon = new ImageIcon(
			TableCellRenderer.class
					.getResource("/images/table/trustcert_entry.png"));

	/**
	 * Get the rendered cell for the supplied value and column.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable keyStoreTable,
			Object value, boolean bIsSelected, boolean bHasFocus, int iRow,
			int iCol) {
		JLabel cell = (JLabel) super.getTableCellRendererComponent(
				keyStoreTable, value, bIsSelected, bHasFocus, iRow, iCol);

		if (value != null) {
        	// Type column - display an icon representing the type
			if (iCol == 0)
				configureTypeColumn(value, cell);
            // Last Modified column - format date (if date supplied)        
            /*else if (((keyStoreTable.getModel() instanceof PasswordsTableModel) && (iCol == 3)) || 
            	((keyStoreTable.getModel() instanceof KeyPairsTableModel) && (iCol == 4))||
            	((keyStoreTable.getModel() instanceof TrustedCertsTableModel) && (iCol == 4))){
            	if (value instanceof Date) {
            		// Include timezone
            		cell.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
            			DateFormat.LONG).format((Date) value));
            	} else {
            		cell.setText(value.toString());
            	}
            }*/
            // Other columns - just use their text values
			else
				cell.setText(value.toString());
		}

		cell.setBorder(new EmptyBorder(0, 5, 0, 5));
		return cell;
	}

	private void configureTypeColumn(Object value, JLabel cell) {
		ImageIcon icon = null;
		// The cell is in the first column of Passwords table
		if (PASSWORD_ENTRY_TYPE.equals(value)) {
			icon = passwordEntryIcon; // key (i.e. password) entry image
		}
		// The cell is in the first column of Key Pairs table
		else if (KEY_PAIR_ENTRY_TYPE.equals(value)) {
			icon = keypairEntryIcon; // key pair entry image
		}
		// The cell is in the first column of Trusted Certificates table
		else if (TRUST_CERT_ENTRY_TYPE.equals(value)) {
			icon = trustcertEntryIcon; // trust. certificate entry image
		}

		cell.setIcon(icon);
		cell.setText("");
		cell.setVerticalAlignment(CENTER);
		cell.setHorizontalAlignment(CENTER);
	}
}
