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

import static javax.swing.border.BevelBorder.RAISED;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Custom cell renderer for the headers of the tables displaying 
 * the Keystore/Truststore contents.
 */
@SuppressWarnings("serial")
public class TableHeaderRenderer extends DefaultTableCellRenderer {
	private final ImageIcon entryTypeIcon = new ImageIcon(
			TableHeaderRenderer.class
					.getResource("/images/table/entry_heading.png"));

	@Override
	public Component getTableCellRendererComponent(JTable jtKeyStoreTable,
			Object value, boolean bIsSelected, boolean bHasFocus, int iRow,
			int iCol) {
        // Get header renderer
        JLabel header = (JLabel) jtKeyStoreTable.getColumnModel().getColumn(iCol).getHeaderRenderer();

        // The entry type header contains an icon for every table
        if (iCol == 0) {
            header.setText("");
            header.setIcon(entryTypeIcon); // entry type icon (header for the first column of the table)
            header.setHorizontalAlignment(CENTER);
            header.setVerticalAlignment(CENTER);
            header.setToolTipText("Entry type");
        }
        // All other headers contain text
        else {
            header.setText((String) value);
            header.setHorizontalAlignment(LEFT);
            
            // Passwords table
            if (jtKeyStoreTable.getModel() instanceof PasswordsTableModel){
                if (iCol == 1) //Service URL column
					header.setToolTipText("URL of the service username and password will be used for");
				else if (iCol == 2) // Username column
					header.setToolTipText("Username for the service");
			}
            // Key pairs table
			else if (jtKeyStoreTable.getModel() instanceof KeyPairsTableModel) {
				if (iCol == 1) // Owner
					header.setToolTipText("Certificate's owner");
				else if (iCol == 2) // Issuer
					header.setToolTipText("Certificate's issuer");
				else if (iCol == 3) // Serial number
					header.setToolTipText("Certificate's serial number");
            }       
            // Trusted certs table
			else if (jtKeyStoreTable.getModel() instanceof TrustedCertsTableModel) {
				if (iCol == 1) // Owner
					header.setToolTipText("Certificate's owner");
				else if (iCol == 2) // Issuer
					header.setToolTipText("Certificate's issuer");
				else if (iCol == 3) // Serial number
					header.setToolTipText("Certificate's serial number");
            }         
        }
		header.setBorder(new CompoundBorder(new BevelBorder(RAISED),
				new EmptyBorder(0, 5, 0, 5)));
		return header;
	}
}
