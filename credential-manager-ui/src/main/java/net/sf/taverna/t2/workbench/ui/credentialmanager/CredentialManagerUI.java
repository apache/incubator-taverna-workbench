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
//import java.awt.Component;
//import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CMUtil;
import net.sf.taverna.t2.security.credentialmanager.CMX509Util;
import net.sf.taverna.t2.security.credentialmanager.ChangeMasterPasswordDialog;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.GetMasterPasswordDialog;
import net.sf.taverna.t2.security.credentialmanager.SetMasterPasswordDialog;

import net.sf.taverna.t2.workbench.ui.credentialmanager.CredentialManagerUI;
import net.sf.taverna.t2.workbench.ui.credentialmanager.CryptoFileFilter;
import net.sf.taverna.t2.workbench.ui.credentialmanager.EditKeyPairEntryDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.GetPasswordDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.KeyPairsTableModel;
import net.sf.taverna.t2.workbench.ui.credentialmanager.NewEditPasswordEntryDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.NewKeyPairEntryDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.NewTrustCertsDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.PasswordsTableModel;
import net.sf.taverna.t2.workbench.ui.credentialmanager.TableCellRenderer;
import net.sf.taverna.t2.workbench.ui.credentialmanager.TableHeaderRenderer;
import net.sf.taverna.t2.workbench.ui.credentialmanager.TrustedCertsTableModel;
import net.sf.taverna.t2.workbench.ui.credentialmanager.ViewCertDetailsDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.ViewUsernamePasswordEntryDialog;

import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

/**
 * Provides a UI for the Credential Manager for users to manage their credentials 
 * saved by the Credential Manager in Taverna's Keystore and Trustore. Credentials
 * include username and passwords pairs, key pairs, proxy key pairs and trusted 
 * certificates of CA's and s.
 * Credentials are stored in two Bouncy Castle "UBER"-type keystores: the
 * Keystore (containing passwords and (normal and proxy) key pairs) and the Truststore 
 * (containing trusted certificates).
 * 
 * @author Alex Nenadic
 */

@SuppressWarnings("serial")
public class CredentialManagerUI extends JFrame {

	// Credential Manager UI singleton
	private static CredentialManagerUI INSTANCE;
	
	// Logger 
	private static Logger logger = Logger.getLogger(CredentialManagerUI.class);

	// Default tabbed pane width - dictates width of this frame 
	private static final int DEFAULT_FRAME_WIDTH = 600;

	// Default tabbed pane height - dictates height of this frame 
	private static final int DEFAULT_FRAME_HEIGHT = 400;

	// Credential Manager icon (when frame minimised) 
	private static final Image credManagerIconImage = Toolkit.getDefaultToolkit()
			.createImage(CredentialManagerUI.class.getResource("/images/cred_manager_transparent.png"));

	// Credential Manager - manages all operations on the Keystore and Truststore
	public static CredentialManager credManager;
	
	// Keystore tab and table controls
	
	// Tabbed pane to hold keystore entries tables 
	private JTabbedPane keyStoreTabbedPane;

	// Tab 1: holds passwords table 
	private JPanel passwordsTab = new JPanel(new BorderLayout(10, 10));

	// Tab 1: name 
	public static final String PASSWORDS = "Passwords";

	// Tab 2: holds key pairs (user certificates) table 
	private JPanel keyPairsTab = new JPanel(new BorderLayout(10, 10));

	// Tab 2: name 
	public static final String KEYPAIRS = "Your Certificates";
	
	// Tab 3: holds key pairs (user certificates) table 
	private JPanel proxiesTab = new JPanel(new BorderLayout(10, 10));

	// Tab 3: name 
	public static final String PROXIES = "Proxies";

	// Tab 4: holds trusted certificates table 
	private JPanel trustedCertificatesTab = new JPanel(new BorderLayout(10, 10));

	// Tab 4: name 
	public static final String TRUSTED_CERTIFICATES = "Trusted Certificates";
	
	// Tables

	// Password entries' table 
	private JTable passwordsTable;

	// Key Pair entries' table 
	private JTable keyPairsTable;
	
	// Proxy entries' table
	private JTable proxiesTable;

	// Trusted Certificate entries' table 
	private JTable trustedCertsTable;

	// Value to place in the Type column for a password entry
	public static final String PASSWORD_ENTRY_TYPE = "Password";

	// Value to place in the Type column for a key pair entry 
	public static final String KEY_PAIR_ENTRY_TYPE = "Key Pair";
	
	// Value to place in the Type column for a proxy entry 
	public static final String PROXY_ENTRY_TYPE = "Proxy";

	// Value to place in the Type column for a trusted certificate entry 
	public static final String TRUST_CERT_ENTRY_TYPE = "Trusted Certificate";
	
	
	/**
	 * Returns a CredentialManagerUI singleton.
	 */
	public static CredentialManagerUI getInstance(){
		synchronized (CredentialManagerUI.class) {
			if (INSTANCE == null){
				// Pop up a warning about Java Cryptography Extension (JCE) 
				// Unlimited Strength Jurisdiction Policy 
				CMUtil.warnUserAboutJCEPolicy();
				String password;
				if (CredentialManager.isInitialised()){
					// Ask user to provide a master password for Credential Manager
					GetMasterPasswordDialog getPasswordDialog = new GetMasterPasswordDialog("Enter master password for Credential Manager");
					getPasswordDialog.setLocationRelativeTo(null);
					getPasswordDialog.setVisible(true);
					password = getPasswordDialog.getPassword();
				}
				else{ // Credential Manager has not been initialised so far
					File keystoreFile = new File(CMUtil.getSecurityConfigurationDirectory(),CredentialManager.T2KEYSTORE_FILE); 				
					if (keystoreFile.exists()){ // If keystore exists then password has been set some time before
						// Ask user to provide a master password for Credential Manager
						GetMasterPasswordDialog getPasswordDialog = new GetMasterPasswordDialog("Enter master password for Credential Manager");
						getPasswordDialog.setLocationRelativeTo(null);
						getPasswordDialog.setVisible(true);
						password = getPasswordDialog.getPassword();
						if (password == null){ //user cancelled
							return null;
						}
					}
					else{ // Keystore does not exist - ask user to set the master password for Credential Manager
						SetMasterPasswordDialog setPasswordDialog = new SetMasterPasswordDialog((JFrame) null, "Set master password", true, "Set master password for Credential Manager");
						setPasswordDialog.setLocationRelativeTo(null);
						setPasswordDialog.setVisible(true);
						password = setPasswordDialog.getPassword();
						if (password == null){ //user cancelled
							return null;
						}
					}		
				}
				try {
					INSTANCE = new CredentialManagerUI(password);
				} catch (CMException cme) {
					// Failed to instantiate Credential Manager - warn the user and exit
					String exMessage = cme.getMessage();
					logger.error(exMessage);
					JOptionPane.showMessageDialog(new JFrame(), exMessage,
							"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
					INSTANCE = null;
					return null;
				}
			}
			else{ // CredentialManagerUI (and therefore Credential Manager) have been instantiated before - ask user to confirm master password
				GetMasterPasswordDialog getPasswordDialog = new GetMasterPasswordDialog("Enter master password for Credential Manager");
				getPasswordDialog.setLocationRelativeTo(null);
				getPasswordDialog.setVisible(true);
				String password = getPasswordDialog.getPassword();
				if (password == null){ //user cancelled
					return null;
				}
				if (! CredentialManager.confirmMasterPassword(password)){
					JOptionPane.showMessageDialog(null, "Incorrect password.", "Credential Manager Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
		}
		return INSTANCE;
	}

	/**
	 * Overrides the Object’s clone method to prevent the singleton object to be
	 * cloned.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	/**
	 * Creates a new Credential Manager UI's frame.
	 */
	private CredentialManagerUI(String password) throws CMException
	{

		// Instantiate Credential Manager that will perform all 
		// operations on the Keystore and Truststore
		// We are using a special method that takes a password to instantiate 
		// Credential Manager as we needed to give user the option from the UI to cancel
		credManager = CredentialManager.getInstance(password);
		
        // Initialise the UI components
		initComponents();		
	}
	
	private void initComponents(){

		// Initialise the tabbed pane that contains the tabs with tabular
		// representations of the Keystore's content.
		keyStoreTabbedPane = new JTabbedPane();
		// Initialise the tab containing the table for username/password entries from the
		// Keystore
		passwordsTable = initTable(PASSWORDS, passwordsTab);
		// Initialise the tab containing the table for key pair entries from the
		// Keystore
		keyPairsTable = initTable(KEYPAIRS, keyPairsTab);
		// Initialise the tab containing the table for proxy entries from the
		// Keystore
		proxiesTable = initTable(PROXIES, proxiesTab);
		// Initialise the tab containing the table for trusted certificate
		// entries from the Truststore
		trustedCertsTable = initTable(TRUSTED_CERTIFICATES,
				trustedCertificatesTab);
		// Set the size of the tabbed pane to the preferred size - the size of
		// the main application frame depends on it.
		keyStoreTabbedPane.setPreferredSize(new Dimension(DEFAULT_FRAME_WIDTH,
				DEFAULT_FRAME_HEIGHT));
		
		// Button for changing Credential Manager's master password
		JButton changeMasterPasswordButton = new JButton("Change master password");
		changeMasterPasswordButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				changeMasterPassword();
			}});
		JPanel changeMasterPasswordPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		changeMasterPasswordPanel.add(changeMasterPasswordButton);
		
		// Add change master password to the main application frame
		getContentPane().add(changeMasterPasswordPanel, BorderLayout.NORTH);
		// Add tabbed pane to the main application frame
		getContentPane().add(keyStoreTabbedPane, BorderLayout.CENTER);

		// Handle application close
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				closeFrame();
			}
		});
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		pack();

		// Centre the frame in the centre of the desktop
		setLocationRelativeTo(null);

		// Set the frame's icon
		setIconImage(credManagerIconImage);

		// Set the frame's title
		setTitle("Credential Manager");

		//setModal(true);
		//setVisible(true);
	}
	
	protected void changeMasterPassword() {

		ChangeMasterPasswordDialog changePasswordDialog = new ChangeMasterPasswordDialog(this, "Change master password", true , "Change master password for Credential Manager");
		changePasswordDialog.setLocationRelativeTo(null);
		changePasswordDialog.setVisible(true);
		String password = changePasswordDialog.getPassword();
		if (password == null){ // user cancelled
			return; // do nothing
		}
		else{
			try {
				credManager.changeMasterPassword(password);
				JOptionPane.showMessageDialog(new JFrame(), "Master password changed sucessfully",
						"Credential Manager Error", JOptionPane.INFORMATION_MESSAGE);
			} catch (CMException cme) {
				// Failed to change the master password for Credential Manager - warn the user
				String exMessage = "Failed to change master password for Credential Manager";
				logger.error(exMessage);
				JOptionPane.showMessageDialog(new JFrame(), exMessage,
						"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Initialise the tab on the tabbed pane and its tabular content.
	 */
	private JTable initTable(String tableType, JPanel tab) {

		JTable table = null;

		if (tableType.equals(PASSWORDS)) { // Passwords table
			// The Passwords table's data model
			PasswordsTableModel passwordsTableModel = new PasswordsTableModel();
			// The table itself
			table = new JTable(passwordsTableModel);

			// Set the password and alias columns of the Passwords table to be
			// invisible by removing them from the column model (they will still present
			// in the table model)
			// Remove the last column first
			TableColumn aliasColumn = table.getColumnModel().getColumn(5);
			table.getColumnModel().removeColumn(aliasColumn);
			TableColumn passwordColumn = table.getColumnModel().getColumn(4);
			table.getColumnModel().removeColumn(passwordColumn);

			// Buttons
			JButton newPasswordButton = new JButton("New");
			newPasswordButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					newPassword();
				}});
			JButton viewPasswordButton = new JButton("Details");
			viewPasswordButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					viewPassword();
				}});
			JButton editPasswordButton = new JButton("Edit");
			editPasswordButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					editPassword();
				}});
			JButton deletePasswordButton = new JButton("Delete");
			deletePasswordButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					deletePassword();
				}});
			
			// Panel to hold the buttons
			JPanel bp = new JPanel();
			bp.add(viewPasswordButton);
			bp.add(editPasswordButton);
			bp.add(newPasswordButton);
			bp.add(deletePasswordButton);

			// Add button panel to the tab
			tab.add(bp, BorderLayout.PAGE_END);

		} 
		else if (tableType.equals(KEYPAIRS)) { // Key Pairs tab
			// The Key Pairs table's data model
			KeyPairsTableModel keyPairsTableModel = new KeyPairsTableModel();
			// The table itself
			table = new JTable(keyPairsTableModel);

			// Set the alias and service URLs columns of the KayPairs table to be
			// invisible by removing them from the column model (they will still present
			// in the table model)
			// Remove the last column first
			TableColumn aliasColumn = table.getColumnModel().getColumn(6); 
			table.getColumnModel().removeColumn(aliasColumn);
			TableColumn serviceURLsColumn = table.getColumnModel().getColumn(5);
			table.getColumnModel().removeColumn(serviceURLsColumn);

			// Buttons
			JButton viewKeyPairButton = new JButton("Details");
			viewKeyPairButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					viewCertificate();
				}});
			JButton editServiceURLKeyPairButton = new JButton("Edit");
			editServiceURLKeyPairButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					editKeyPair();
				}});
			JButton importKeyPairButton = new JButton("Import");
			importKeyPairButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					newKeyPair();
				}});
			JButton exportKeyPairButton = new JButton("Export");
			exportKeyPairButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					exportKeyPair();
				}});
			JButton deleteKeyPairButton = new JButton("Delete");
			deleteKeyPairButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					deleteKeyPair();
				}});

			// Panel to hold the buttons
			JPanel bp = new JPanel();
			bp.add(viewKeyPairButton);
			bp.add(editServiceURLKeyPairButton);
			bp.add(importKeyPairButton);
			bp.add(exportKeyPairButton);
			bp.add(deleteKeyPairButton);

			// Add button panel to the tab
			tab.add(bp, BorderLayout.PAGE_END);

		} 
		else if (tableType.equals(PROXIES)) { // Proxies tab
			// The Proxies table's data model
			ProxiesTableModel proxiesTableModel = new ProxiesTableModel();
			
			// The table itself
			table = new JTable(proxiesTableModel);

			// Set alias column of the Proxies table to be
			// invisible by removing it from the column model (it will still present
			// in the table model)
			TableColumn aliasColumn = table.getColumnModel().getColumn(5); 
			table.getColumnModel().removeColumn(aliasColumn);

			// Buttons
			JButton viewProxyButton = new JButton("Details");
			viewProxyButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					viewProxyCertificate();
				}});
			JButton deleteProxyButton = new JButton("Delete");
			deleteProxyButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					deleteProxy();
				}});
			

			// Panel to hold the buttons
			JPanel bp = new JPanel();
			bp.add(viewProxyButton);
			bp.add(deleteProxyButton);
			
			// Add button panel to the tab
			tab.add(bp, BorderLayout.PAGE_END);

		} 
		else if (tableType.equals(TRUSTED_CERTIFICATES)) { // Trusted Certificates tab
			// The Trusted Certificate table's data model
			TrustedCertsTableModel trustedCertificatesTableModel = new TrustedCertsTableModel();
			// The table itself
			table = new JTable(trustedCertificatesTableModel);

			// Set the alias column of the Trusted Certs table to be invisible
			// by removing it from the column model (it is still present in the
			// table model)
			TableColumn aliasColumn = table.getColumnModel().getColumn(5);
			table.getColumnModel().removeColumn(aliasColumn);

			// Buttons
			JButton viewTrustedCertificateButton = new JButton("Details");
			viewTrustedCertificateButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					viewCertificate();
				}});
			JButton importTrustedCertificateButton = new JButton("Import");
			importTrustedCertificateButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					importTrustedCertificate();
				}});
			JButton exportTrustedCertificateButton = new JButton("Export");
			exportTrustedCertificateButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					exportTrustedCertificate();
				}});
			JButton deleteTrustedCertificateButton = new JButton("Delete");
			deleteTrustedCertificateButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					deleteTrustedCertificate();
				}});

			// Panel to hold the buttons
			JPanel bp = new JPanel();
			bp.add(viewTrustedCertificateButton);
			bp.add(importTrustedCertificateButton);
			bp.add(exportTrustedCertificateButton);
			bp.add(deleteTrustedCertificateButton);

			// Add button panel to the tab
			tab.add(bp, BorderLayout.PAGE_END);
		}

		table.setShowGrid(false);
		table.setRowMargin(0);
		table.getColumnModel().setColumnMargin(0);
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// Top accommodates entry icons with 2 pixels spare space (images are 16x16 pixels)
		table.setRowHeight(18);

		// Add custom renderrers for the table headers and cells
		for (int iCnt = 0; iCnt < table.getColumnCount(); iCnt++) {
			TableColumn column = table.getColumnModel().getColumn(iCnt);
			column.setHeaderRenderer(new TableHeaderRenderer());
			column.setCellRenderer(new TableCellRenderer());
		}

		// Make the first column small and not resizable (it holds icons to
		// represent different entry types)
		TableColumn typeCol = table.getColumnModel().getColumn(0);
		typeCol.setResizable(false);
		typeCol.setMinWidth(20);
		typeCol.setMaxWidth(20);
		typeCol.setPreferredWidth(20);

		// Set the size for the second column
		// (i.e. Service URL column of Passwords and Key Pairs tables, and
		// Certificate Name column of the Trusted Certificates table)
		TableColumn secondCol = table.getColumnModel().getColumn(1);
		secondCol.setMinWidth(20);
		secondCol.setMaxWidth(10000);
		secondCol.setPreferredWidth(300);

		// Don't care about the size of other columns

		// Put the table into a scroll pane
		JScrollPane jspTableScrollPane = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspTableScrollPane.getViewport().setBackground(table.getBackground());

		// Put the scroll pane on the tab panel
		tab.add(jspTableScrollPane, BorderLayout.CENTER);
		jspTableScrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));

		// Add mouse listeners to show an entry's details if it is
		// double-clicked
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				tableDoubleClick(evt);
			}
		});

		// Add the tab to the tabbed pane
		keyStoreTabbedPane.addTab(tableType, tab);

		return table;
	}
	
	/**
	 * Displays the details of the username/password pair entry - this includes 
	 * showing the plaintext password and service URL for this entry.
	 */
	private void viewPassword() {
		// Which username/password pair entry has been selected, if any?
		int iRow = passwordsTable.getSelectedRow();
		if (iRow == -1) { // no row currently selected
			return;
		}

		// Get current values for service URL, username and password
		String serviceURL = (String) passwordsTable.getValueAt(iRow, 1); // current entry's service URL

		String username = (String) passwordsTable.getValueAt(iRow, 2); // current entry's username

		// Because the password column is not visible we call
		// the getValueAt method on the table model rather than at the JTable
		String password = (String) passwordsTable.getModel().getValueAt(iRow,4); // current entry's password value

		// Let the user view service URL, username and password of the entry
		ViewUsernamePasswordEntryDialog viewServicePassDialog = new ViewUsernamePasswordEntryDialog(
				this, serviceURL, username, password);

		viewServicePassDialog.setLocationRelativeTo(this);
		viewServicePassDialog.setVisible(true);
	}
	
	/**
	 * Lets a user insert a new username/password/service URL tuple to the Keystore.
	 */
	private void newPassword() {
		
		String serviceURL = null; // service URL
		String username = null; // username
		String password = null; // password

		// Loop until the user cancels or enters everything correctly
		while (true) { 
			
			// Let the user insert a new password entry (by specifying service
			// URL, username and password)
			NewEditPasswordEntryDialog newPasswordDialog = new NewEditPasswordEntryDialog(
					this, "New username and password for a service", true, serviceURL, username,
					password);
			newPasswordDialog.setLocationRelativeTo(this);
			newPasswordDialog.setVisible(true);

			serviceURL = newPasswordDialog.getServiceURL(); // get service URL
			username = newPasswordDialog.getUsername(); // get username
			password = newPasswordDialog.getPassword(); // get password

			if (password == null) { // user cancelled - any of the above three fields is null 		
				// do nothing
				return;
			}

			// Check if a password entry with the given service URL
			// already exists in the Keystore.
			// We ask this here as the user may wish to overwrite the
			// existing password entry.
			// Checking for key pair entries' URLs is done in the
			// NewEditPasswordEntry dialog.
			
			// Get list of service URLs for all the password entries in the Keystore
			ArrayList<String> serviceURLs = null;
			try{
				serviceURLs = credManager.getServiceURLsforUsernameAndPasswords(); 
			}
			catch(CMException  cme){
				String exMessage = "Failed to get service URLs for all username and password pairs to check if the entered service URL already exists";
				JOptionPane.showMessageDialog(this, exMessage,
						"Credential Manager Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (serviceURLs.contains(serviceURL)) { // if such a URL already exists 
				// Ask if the user wants to overwrite it
				int iSelected = JOptionPane.showConfirmDialog(this,
						"Credential Manager already contains a password entry with the same service URL.\n"
								+ "Do you want to overwrite it?",
						"Credential Manager Alert",
						JOptionPane.YES_NO_OPTION);

				// Add the new password entry in the Keystore 
				if (iSelected == JOptionPane.YES_OPTION) {
					try{
						credManager.saveUsernameAndPasswordForService(username, password, serviceURL);
						break;
					}
					catch (CMException cme){
						String exMessage = "Credential Manager failed to insert a new username and password pair";
						JOptionPane.showMessageDialog(this, exMessage,
								"Credential Manager Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			// Otherwise show the same window with the entered service
			// URL, username and password values
			} 
			else {
				// Add the new password entry in the Keystore
				try{
					credManager.saveUsernameAndPasswordForService(username, password, serviceURL);
					break;
				}
				catch (CMException cme){
					String exMessage = "Credential Manager failed to insert a new username and password pair";
					JOptionPane.showMessageDialog(this, exMessage,
							"Credential Manager Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} 
	}
	
	/**
	 * Lets a user insert a new username/password pair for a given service URL 
	 * to the Keystore.
	 */
	public void newPasswordForService(String serviceURL) {
		
		// Make sure password tab is selected as this method may
		// be called from outside of Credential Manager UI.
		keyStoreTabbedPane.setSelectedComponent(passwordsTab);
		
		String username = null; // username
		String password = null; // password

		// Loop until the user cancels or enters everything correctly
		while (true) { 
			
			// Let the user insert a new password entry for the given service URL 
			// (by specifying username and password)
			NewEditPasswordEntryDialog newPasswordDialog = new NewEditPasswordEntryDialog(
					this, "New username and password for a service", true, serviceURL, username,
					password);
			newPasswordDialog.setLocationRelativeTo(this);
			newPasswordDialog.setVisible(true);

			serviceURL = newPasswordDialog.getServiceURL(); // get service URL
			username = newPasswordDialog.getUsername(); // get username
			password = newPasswordDialog.getPassword(); // get password

			if (password == null) { // user cancelled - any of the above three fields is null 		
				// do nothing
				return;
			}

			// Check if a password entry with the given service URL
			// already exists in the Keystore.
			// We ask this here as the user may wish to overwrite the
			// existing password entry.
			// Checking for key pair entries' URLs is done in the
			// NewEditPasswordEntry dialog.
			
			// Get list of service URLs for all the password entries in the Keystore
			ArrayList<String> serviceURLs = null;
			try{
				serviceURLs = credManager.getServiceURLsforUsernameAndPasswords(); 
			}
			catch(CMException  cme){
				String exMessage = "Failed to get service URLs for all username and password pairs to check if the entered service URL already exists";
				JOptionPane.showMessageDialog(this, exMessage,
						"Credential Manager Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (serviceURLs.contains(serviceURL)) { // if such a URL already exists 
				// Ask if the user wants to overwrite it
				int iSelected = JOptionPane.showConfirmDialog(this,
						"Credential Manager already contains a password entry with the same service URL.\n"
								+ "Do you want to overwrite it?",
						"Credential Manager Alert",
						JOptionPane.YES_NO_OPTION);

				// Add the new password entry in the Keystore 
				if (iSelected == JOptionPane.YES_OPTION) {
					try{
						credManager.saveUsernameAndPasswordForService(username, password, serviceURL);
						break;
					}
					catch (CMException cme){
						String exMessage = "Credential Manager failed to insert a new username and password pair";
						JOptionPane.showMessageDialog(this, exMessage,
								"Credential Manager Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			// Otherwise show the same window with the entered service
			// URL, username and password values
			} 
			else {
				// Add the new password entry in the Keystore
				try{
					credManager.saveUsernameAndPasswordForService(username, password, serviceURL);
					break;
				}
				catch (CMException cme){
					String exMessage = "Credential Manager failed to insert a new username and password pair";
					JOptionPane.showMessageDialog(this, exMessage,
							"Credential Manager Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		} 
	}


	/**
	 * Lets a user edit a username and password entry or their related service URL 
	 * to the Keystore.
	 */
	private void editPassword() {

		// Which password entry has been selected?
		int iRow = passwordsTable.getSelectedRow();
		if (iRow == -1) { // no row currently selected
			return;
		}

		// Get current values for service URL, username and password
		String serviceURL = (String) passwordsTable.getValueAt(iRow, 1); // current entry's service URL

		String username = (String) passwordsTable.getValueAt(iRow, 2); // current entry's username

		// Because the password column is not visible we call
		// the getValueAt method on the table model rather than at the JTable
		String password = (String) passwordsTable.getModel().getValueAt(iRow, 4); // current entry's password value

		while (true) { // loop until user cancels or enters everything correctly
			// Let the user edit service URL, username or password of a password entry
			NewEditPasswordEntryDialog editPasswordDialog = new NewEditPasswordEntryDialog(
					this, "Edit username and password for a service", true, serviceURL, username,
					password);

			editPasswordDialog.setLocationRelativeTo(this);
			editPasswordDialog.setVisible(true);

			// New values
			String nServiceURL = editPasswordDialog.getServiceURL(); // get new service URL
			String nUsername = editPasswordDialog.getUsername(); // get new username
			String nPassword = editPasswordDialog.getPassword(); // get new password

			if (nPassword == null) { // user cancelled - any of the above three fields is null
				// do nothing
				return;
			}

			// Is anything actually modified?
			boolean isModified = (!serviceURL.equals(nServiceURL)
					|| !username.equals(nUsername) || !password
					.equals(nPassword));

			if (isModified) {
				// Check if a different password entry with the new URL
				// (i.e. alias) already exists in the Keystore
				// We ask this here as the user may wish to overwrite that
				// other password entry.
				
				// Get list of URLs for all passwords in the Keystore
				ArrayList<String> serviceURLs = null;
				try{
					serviceURLs = credManager.getServiceURLsforUsernameAndPasswords(); 
				}
				catch(CMException  cme){
					String exMessage = "Failed to get service URLs for all username and password pairs to check if the modified entry already exists";
					JOptionPane.showMessageDialog(this, exMessage,
							"Credential Manager Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			
				// If the modified service URL already exists and is not the currently selected one
				if ( (!nServiceURL.equals(serviceURL)) && serviceURLs.contains(nServiceURL)) { 

					int iSelected = JOptionPane.showConfirmDialog(this,
							"The Keystore already contains username and password pair for the entered service URL.\n"
									+ "Do you want to overwrite it?",
							"Credential Manager Alert",
							JOptionPane.YES_NO_OPTION);

					if (iSelected == JOptionPane.YES_OPTION) {

						// Overwrite that other entry entry and save the new
						// one in its place.
						// Also remove the current one that we are editing -
						// as it is replacing the other entry.
						try{
							credManager.deleteUsernameAndPasswordForService(serviceURL);
							credManager.saveUsernameAndPasswordForService(nUsername, nPassword, nServiceURL);
							break;
						}
						catch (CMException cme){
							String exMessage = "Failed to update the username and password pair in the Keystore";
							JOptionPane.showMessageDialog(this, exMessage,
									"Credential Manager Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				// Otherwise show the same window with the entered
				// service URL, username and password values
				} 
				else {
					try{
						if ( !nServiceURL.equals(serviceURL)) {
							credManager.deleteUsernameAndPasswordForService(serviceURL);
						}
						credManager.saveUsernameAndPasswordForService(nUsername, nPassword, nServiceURL);
						break;
					}
					catch (CMException cme){
						String exMessage = "Failed to update the username and password pair in the Keystore";
						JOptionPane.showMessageDialog(this, exMessage,
								"Credential Manager Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			else{ // nothing actually modified by OK pressed
				break;
			}
		}
	}

	/**
	 * Lets the user delete the selected username and password entries 
	 * from the Keystore.
	 */
	private void deletePassword() {

		// Which entries have been selected?
		int[] iRows = passwordsTable.getSelectedRows();
		if (iRows.length == 0) { // no password entry selected
			return;
		}

		// Ask user to confirm the deletion
		int iSelected = JOptionPane
				.showConfirmDialog(
						null,
						"Are you sure you want to delete the selected username and password entries?",
						"Credential Manager Alert",
						JOptionPane.YES_NO_OPTION);

		if (iSelected != JOptionPane.YES_OPTION) {
			return;
		}
					
		for (int i = iRows.length - 1; i >= 0; i--) { // delete from backwards
			// Get service URL for the current entry 
			String serviceURL = (String) passwordsTable.getValueAt(iRows[i], 1); // current entry's service URL
			try {
				// Delete the password entry from the Keystore
				credManager.deleteUsernameAndPasswordForService(serviceURL);
			} 
			catch (CMException cme) {
				String exMessage = "Failed to delete the username and password pair from the Keystore";
				JOptionPane.showMessageDialog(this, exMessage,
						"Credential Manager Error",
						JOptionPane.ERROR_MESSAGE);
			} 
		}		
	}
	
	/**
	 * Shows the contents of a (user or trusted) certificate.
	 */
	@SuppressWarnings("unchecked")
	private void viewCertificate() {

		int iRow = -1;
		String alias = null;
		X509Certificate certToView = null;
		ArrayList<String> serviceURLs = null;
		String keystoreType = null;
		
		// Are we showing user's public key certificate?
		if (keyPairsTab.isShowing()){
			keystoreType = CredentialManager.KEYSTORE;
			iRow = keyPairsTable.getSelectedRow();
			
			if (iRow != - 1){
				// Because the alias column is not visible we call the
				// getValueAt method on the table model rather than at the JTable
				alias = (String) keyPairsTable.getModel().getValueAt(iRow, 6); // current entry's Keystore alias

		    	// Get the list of service URLs for the entry
		        serviceURLs = (ArrayList<String>) keyPairsTable.getModel().getValueAt(iRow, 5);   
			}
		}
		// Are we showing trusted certificate?
		else if(trustedCertificatesTab.isShowing()){
			keystoreType = CredentialManager.TRUSTSTORE;
			iRow = trustedCertsTable.getSelectedRow();
			
			if (iRow != - 1){

				// Get the selected trusted certificate entry's Truststore alias
				// Alias column is invisible so we get the value from the table model
				alias = (String) trustedCertsTable.getModel()
						.getValueAt(iRow, 5); 
			}
		}
		
		if (iRow != -1) { // something has been selected
			try {
				// Get the entry's certificate
				certToView = CMX509Util.convertCertificate(credManager
						.getCertificate(keystoreType, alias));

				// Supply the certificate and list of URLs to the view
				// certificate dialog. 
				ViewCertDetailsDialog viewCertDetailsDialog = new ViewCertDetailsDialog(
						this, "Certificate details", true, certToView,
						serviceURLs);
				viewCertDetailsDialog.setLocationRelativeTo(this);
				viewCertDetailsDialog.setVisible(true);
			} 
			catch (CMException cme) {
				String exMessage = "Failed to get certificate details to display to the user";
				logger.error(exMessage);
				JOptionPane.showMessageDialog(this, exMessage,
						"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
			} 
		} 
	}
	
	/**
	 * Lets a user import a key pair from a PKCS #12 keystore file to the Keystore.
	 */
	private void newKeyPair() {

		// Let the user choose a PKCS #12 file (keystore) containing a public
		// and private key pair to import
		File importFile = selectImportExportFile(
				"PKCS #12 file to import from", // title
				new String[] { ".p12", ".pfx" }, // array of file extensions
				// for the file filter
				"PKCS#12 Files (*.p12, *.pfx)", // description of the filter
				"Import"); // text for the file chooser's approve button

		if (importFile == null) {
			return;
		}

		// The PKCS #12 keystore is not a file
		if (!importFile.isFile()) {
			JOptionPane.showMessageDialog(this, "Your selection is not a file",
					"Credential Manager Alert", JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Get the user to enter the password that was used to encrypt the
		// private key contained in the PKCS #12 file
		GetPasswordDialog dGetPassword = new GetPasswordDialog(this,
				"Import key pair entry", true,
				"Enter the password that was used to encrypt the PKCS #12 file");
		dGetPassword.setLocationRelativeTo(this);
		dGetPassword.setVisible(true);

		String pkcs12Password = dGetPassword.getPassword();

		if (pkcs12Password == null) { // user cancelled
			return;
		} else if (pkcs12Password.length() == 0) { // empty password
			// FIXME: Maybe user did not have the password set for the private key???
			return;
		}

		try {
			// Load the PKCS #12 keystore from the file using BC provider
			KeyStore pkcs12 = credManager.loadPKCS12Keystore(importFile, pkcs12Password);

			// Display the import key pair dialog supplying all the private keys
			// stored in the PKCS #12 file
			// and a field for the user to enter service URL associated with the
			// selected key pair
			// Typically there will be only one private key inside, but could be
			// more
			NewKeyPairEntryDialog dImportKeyPair = new NewKeyPairEntryDialog(
					this, "Credential Manager", true, pkcs12);
			dImportKeyPair.setLocationRelativeTo(this);
			dImportKeyPair.setVisible(true);

			// Get the private key and certificate chain of the key pair
			Key privateKey = dImportKeyPair.getPrivateKey();
			Certificate[] certChain = dImportKeyPair.getCertificateChain();

			// Get the service URLs
			ArrayList<String> serviceURLs = dImportKeyPair.getServiceURLs();

			if (privateKey == null || certChain == null) {
				// User did not select a key pair for import or cancelled
				return;
			}

			// Check if a key pair entry with the same alias already exists in
			// the Keystore
			if (credManager.containsKeyPair(privateKey, certChain)) {
				int iSelected = JOptionPane
						.showConfirmDialog(
								this,
								"The keystore already contains the key pair entry with the same private key.\nDo you want to overwrite it?",
								"Credential Manager Alert",
								JOptionPane.YES_NO_OPTION);

				if (iSelected != JOptionPane.YES_OPTION) {
					return;
				}
			}

			// Place the private key and certificate chain into the Keystore
			// and save the service URLs list associated with this key pair
			credManager.saveKeyPair(privateKey, certChain, serviceURLs);

			// Display success message
			JOptionPane
					.showMessageDialog(this, "Key pair import successful",
							"Credential Manager Alert",
							JOptionPane.INFORMATION_MESSAGE);
		} 
		catch (Exception ex) { // too many exceptions to catch separately
			String exMessage = "Failed to import the key pair entry to the Keystore. "
					+ ex.getMessage();
			logger.error(exMessage);
			JOptionPane.showMessageDialog(this, exMessage,
					"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	
	/**
	 * Lets a user edit service URLs for a given key pair entry.
	 */
	@SuppressWarnings("unchecked")
	private void editKeyPair() {
		// Which key pair entry has been selected?
		int iRow = keyPairsTable.getSelectedRow();
		if (iRow == -1) { // no row currently selected
			return;
		}

		// Because the alias column is not visible we call the
		// getValueAt method on the table model rather than at the JTable
		String alias = (String) keyPairsTable.getModel().getValueAt(iRow, 6); // current entry's Keystore alias

    	// Get the list of URLs for the alias
		ArrayList<String> serviceURLs = (ArrayList<String>) keyPairsTable.getModel().getValueAt(iRow, 5);   
  
		// Let the user edit the list of service urls this key pair is
		// associated to
		EditKeyPairEntryDialog dEditKeyPair = new EditKeyPairEntryDialog(this,
				"Edit key pair's service URLs", true, serviceURLs);

		dEditKeyPair.setLocationRelativeTo(this);
		dEditKeyPair.setVisible(true);

		ArrayList<String> newServiceURLs = dEditKeyPair.getServiceURLs(); // new service URLs list

		if (newServiceURLs == null) { // user cancelled
			return;
		}

		// Is anything actually modified?
		boolean isModified = (!serviceURLs.equals(newServiceURLs));

		if (isModified) {
			try {
				// Add the new list of URLs for the alias
				credManager.saveServiceURLsForKeyPair(alias, newServiceURLs);
			} 
			catch (CMException cme) {
				String exMessage = "Failed to update service URLs for the key pair entry";
				logger.error(exMessage);
				JOptionPane.showMessageDialog(this, exMessage,
						"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
			} 
		} 
	}

	/**
	 * Lets a user export user's private and public key pair to a PKCS #12
	 * keystore file.
	 */
	private void exportKeyPair() {
		
		// Which key pair entry has been selected?
		int iRow = keyPairsTable.getSelectedRow();
		if (iRow == -1) { // no row currently selected
			return;
		}

		// Get the key pair entry's Keystore alias
		String alias = (String) keyPairsTable.getModel().getValueAt(iRow, 6); 

		// Let the user choose a PKCS #12 file (keystore) to export public and
		// private key pair to
		File exportFile = selectImportExportFile("Select a file to export to", // title
				new String[] { ".p12", ".pfx" }, // array of file extensions
				// for the file filter
				"PKCS#12 Files (*.p12, *.pfx)", // description of the filter
				"Export"); // text for the file chooser's approve button

		if (exportFile == null) {
			return;
		}

		// If file already exist - ask the user if he wants to overwrite it
		if (exportFile.isFile()) {
			int iSelected = JOptionPane
					.showConfirmDialog(
							this,
							"The file with the given name already exists.\nDo you want to overwrite it?",
							"Credential Manager Alert",
							JOptionPane.YES_NO_OPTION);

			if (iSelected == JOptionPane.NO_OPTION) {
				return;
			}
		}

		// Get the user to enter the password for the PKCS #12 keystore file
		GetPasswordDialog dGetPassword = new GetPasswordDialog(this,
				"Credential Manager", true,
				"Enter the password for protecting the exported key pair");
		dGetPassword.setLocationRelativeTo(this);
		dGetPassword.setVisible(true);

		String pkcs12Password = dGetPassword.getPassword();

		if (pkcs12Password == null) { // user cancelled or empty password
			// Warn the user
			JOptionPane
					.showMessageDialog(
							this,
							"You must supply a password for protecting the exported key pair.",
							"Credential Manager Alert",
							JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// Export the key pair
		try {
			credManager.exportKeyPair(alias, exportFile, pkcs12Password);
			JOptionPane.showMessageDialog(this, "Key pair export successful",
							"Credential Manager Alert",
							JOptionPane.INFORMATION_MESSAGE);
		} 
		catch (CMException cme) {
			JOptionPane.showMessageDialog(this, cme.getMessage(),
					"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
		} 
	}

	/**
	 * Lets a user delete selected key pair entries from the Keystore.
	 */
	private void deleteKeyPair() {
		
		// Which entries have been selected?
		int[] iRows = keyPairsTable.getSelectedRows();
		if (iRows.length == 0) { // no key pair entry selected
			return;
		}

		// Ask user to confirm the deletion
		int iSelected = JOptionPane
				.showConfirmDialog(
						null,
						"Are you sure you want to delete the selected key pairs?",
						"Credential Manager Alert",
						JOptionPane.YES_NO_OPTION);

		if (iSelected != JOptionPane.YES_OPTION) {
			return;
		}
					
		for (int i = iRows.length - 1; i >= 0; i--) { // delete from backwards
			// Get the alias for the current entry 
			String alias = (String) keyPairsTable.getModel().getValueAt(
					iRows[i], 6);
			try {
				// Delete the key pair entry from the Keystore
				// and URLs associated with this key pair entry
				credManager.deleteKeyPair(alias);
			} 
			catch (CMException cme) {
				String exMessage = "Failed to delete the key pair(s) from the Keystore";
				JOptionPane.showMessageDialog(this, exMessage,
						"Credential Manager Error",
						JOptionPane.ERROR_MESSAGE);
			} 
		}
	}
	
	/**
	 * Shows the contents of a proxy certificate.
	 */
	private void viewProxyCertificate() {

		int iRow = proxiesTable.getSelectedRow();
		
		if (iRow != -1) { // something has been selected
			String alias = (String) proxiesTable.getModel().getValueAt(iRow, 5);
			
			if (alias.startsWith("cagridproxy#")){ // cagrid proxy
				try {
					// Get the entry's certificate
					X509Certificate certToView = CMX509Util.convertCertificate(credManager
							.getCertificate(CredentialManager.KEYSTORE, alias));

					String authNServiceURL = alias.substring(alias.indexOf('#') + 1, alias.indexOf(' '));
					String dorianServiceURL = alias.substring(alias.indexOf(' ') +1); 
					// Supply the certificate and list of URLs to the view
					// certificate dialog. 
					ViewCaGridProxyCertDetailsDialog viewCaGridProxyCertDetailsDialog = new ViewCaGridProxyCertDetailsDialog(
							this, "Certificate details", true, certToView, authNServiceURL, dorianServiceURL);
					viewCaGridProxyCertDetailsDialog.setLocationRelativeTo(this);
					viewCaGridProxyCertDetailsDialog.setVisible(true);
				} 
				catch (CMException cme) {
					String exMessage = "Failed to get proxy certificate details to display to the user";
					logger.error(exMessage);
					JOptionPane.showMessageDialog(this, exMessage,
							"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
				} 
			}
			//else{} // some other proxy
		} 
	}
	
	/**
	 * Lets a user delete selected proxy entries from the Keystore.
	 */
	private void deleteProxy() {
		
		// Which entries have been selected?
		int[] iRows = proxiesTable.getSelectedRows();
		if (iRows.length == 0) { // no key pair entry selected
			return;
		}

		// Ask user to confirm the deletion
		int iSelected = JOptionPane
				.showConfirmDialog(
						null,
						"Are you sure you want to delete the selected proxy(ies)?",
						"Credential Manager Alert",
						JOptionPane.YES_NO_OPTION);

		if (iSelected != JOptionPane.YES_OPTION) {
			return;
		}
					
		for (int i = iRows.length - 1; i >= 0; i--) { // delete from backwards
			// Get the alias for the current entry 
			String alias = (String) proxiesTable.getModel().getValueAt(
					iRows[i], 5);
			try {
				// Delete the proxy entry from the Keystore
				credManager.deleteCaGridProxy(alias);
			} 
			catch (CMException cme) {
				String exMessage = "Failed to delete the proxy(ies) from the Keystore";
				logger.error(exMessage);
				JOptionPane.showMessageDialog(this, exMessage,
						"Credential Manager Error",
						JOptionPane.ERROR_MESSAGE);
			} 
		}
	}

	/**
	 * Lets a user import a trusted certificate from a PEM or DER encoded file
	 * into the Truststore.
	 */
	private void importTrustedCertificate() {
		// Let the user choose a file containing trusted certificate(s) to
		// import
		File certFile = selectImportExportFile(
				"Certificate file to import from", // title	
				new String[] { ".pem", "crt", ".cer", ".der", ".p7c" }, // file extensions filters
				"Certificate Files (*.pem, *.crt, , *.cer, *.der, *.p7c)", // filter description
				"Import"); // text for the file chooser's approve button

		if (certFile == null) {
			return;
		}

		// Load the certificate(s) from the file
		ArrayList<X509Certificate> trustCertsList = new ArrayList<X509Certificate>();
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(certFile);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			// The following should be able to load PKCS #7 certificate chain files
			// as well as ASN.1 DER or PEM-encoded (sequences of) certificates
			Collection<? extends Certificate> c = cf.generateCertificates(fis);
			Iterator<? extends Certificate> i = c.iterator();
			while (i.hasNext()) {
				trustCertsList.add((X509Certificate) i.next());
			}
		} 
		catch (Exception cex) {
			// Do nothing
		} 
		finally {
			try {
				fis.close();
			} catch (Exception ex) {
				// ignore
			}
		}

		if (trustCertsList.size() == 0) { // Could not load certificates as
			// any of the above types
			try {
				// Try as openssl PEM format - which sligtly differs from the
				// one supported by JCE
				fis = new FileInputStream(certFile);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				PEMReader pr = new PEMReader(new InputStreamReader(fis), null,
						cf.getProvider().getName());
				Object cert;
				while ((cert = pr.readObject()) != null) {
					if (cert instanceof X509Certificate) {
						trustCertsList.add((X509Certificate) cert);
					}
				}
			} catch (Exception cex) {
				// do nothing
			} finally {
				try {
					fis.close();
				} catch (Exception ex) {
					// ignore
				}
			}
		}

		if (trustCertsList.size() == 0) { // Failed to load certifcate(s)
			// using any of the known encodings
			JOptionPane
					.showMessageDialog(
							this,
							"Failed to load certificate(s) using any of the known encodings -\nfile format not recognised.",
							"Credential Manager Error",
							JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Show the list of certificates contained in the file for the user to
		// select the ones to import
		NewTrustCertsDialog dImportTrustCerts = new NewTrustCertsDialog(this,
				"Credential Manager", true, trustCertsList);

		dImportTrustCerts.setLocationRelativeTo(this);
		dImportTrustCerts.setVisible(true);
		ArrayList<X509Certificate> selectedTrustCerts = dImportTrustCerts
				.getTrustedCertificates(); // user-selected trusted certs to import

		// If user cancelled or did not select any cert to import
		if ((selectedTrustCerts) == null || (selectedTrustCerts.size() == 0)) { 
			return ;
		}

		try {
			
			for (int i = selectedTrustCerts.size() - 1; i >= 0; i--) {
				// Import the selected trusted certificates
				credManager.saveTrustedCertificate(selectedTrustCerts.get(i));
			}

			// Display success message
			JOptionPane
					.showMessageDialog(this,
							"Trusted certificate(s) import successful",
							"Credential Manager Alert",
							JOptionPane.INFORMATION_MESSAGE);
		} 
		catch (CMException cme) {
			String exMessage = "Failed to import trusted certificate(s) to the Truststore";
			logger.error(exMessage);
			JOptionPane.showMessageDialog(this, exMessage,
					"Credential Manager Error", JOptionPane.ERROR_MESSAGE);
		} 
	}

	
	/**
	 * Lets the user export one (at the moment) or more (in future) trusted
	 * certificate entries to a PEM-encoded file.
	 * 
	 * @return True if the export is successful, false otherwise
	 */
	private boolean exportTrustedCertificate() {

		// Which trusted certificate has been selected?
		int iRow = trustedCertsTable.getSelectedRow();
		if (iRow == -1) { // no row currently selected
			return false;
		}

		// Get the trust certificate entry's Keystore alias
		String alias = (String) trustedCertsTable.getModel().getValueAt(iRow, 3); 
		// the alias column is invisible so we get the value from the table model

		// Let the user choose a file to export public and private key pair to
		File exportFile = selectImportExportFile("Select a file to export to", // title
				new String[] { ".pem" }, // array of file extensions for the
				// file filter
				"Certificate Files (*.pem)", // description of the filter
				"Export"); // text for the file chooser's approve button

		if (exportFile == null) {
			return false;
		}

		// If file already exist - ask the user if he wants to overwrite it
		if (exportFile.isFile()) {
			int iSelected = JOptionPane
					.showConfirmDialog(
							this,
							"The file with the given name already exists.\nDo you want to overwrite it?",
							"Credential Manager Alert",
							JOptionPane.YES_NO_OPTION);

			if (iSelected == JOptionPane.NO_OPTION) {
				return false;
			}
		}

		// Export the trusted certificate
		PEMWriter pw = null;
		try {
			// Get the trusted certificate
			Certificate certToExport = credManager.getCertificate(
					CredentialManager.TRUSTSTORE, alias);
			pw = new PEMWriter(new FileWriter(exportFile));
			pw.writeObject(certToExport);

			JOptionPane
					.showMessageDialog(this,
							"Trusted certificate export successful",
							"Credential Manager Alert",
							JOptionPane.INFORMATION_MESSAGE);

			return true;
		} 
		catch (Exception ex) {
			String exMessage = "Failed to export the trusted certificate from the Truststore.";
			logger.error(exMessage);
			JOptionPane.showMessageDialog(
							this,
							exMessage,
							"Credential Manager Error",
							JOptionPane.ERROR_MESSAGE);
			return false;
		} 
		finally {
			if (pw != null) {
				try {
					pw.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}
	
	/**
	 * Lets a user delete the selected trusted certificate entries from the Truststore.
	 */
	private void deleteTrustedCertificate() {
		
		// Which entries have been selected?
		int[] iRows = trustedCertsTable.getSelectedRows();
		if (iRows.length == 0) { // no trusted cert entry selected
			return;
		}

		// Ask user to confirm the deletion
		int iSelected = JOptionPane
				.showConfirmDialog(
						null,
						"Are you sure you want to delete the selected trusted certificate(s)?",
						"Credential Manager Alert",
						JOptionPane.YES_NO_OPTION);

		if (iSelected != JOptionPane.YES_OPTION) {
			return;
		}
					
		for (int i = iRows.length - 1; i >= 0; i--) { // delete from backwards
			// Get the alias for the current entry 
			String alias = (String) trustedCertsTable.getModel().getValueAt(
					iRows[i], 5);
			try {
				// Delete the trusted certificate entry from the Truststore
				credManager.deleteTrustedCertificate(alias);
			} 
			catch (CMException cme) {
				String exMessage = "Failed to delete the trusted certificate(s) from the Truststore";
				logger.error(exMessage);
				JOptionPane.showMessageDialog(this, exMessage,
						"Credential Manager Error",
						JOptionPane.ERROR_MESSAGE);
			} 
		}
	}
	
	/**
	 * Handles double click on the Keystore tables. If it has occurred, show the
	 * details of the entry clicked upon.
	 */
	private void tableDoubleClick(MouseEvent evt) {
		if (evt.getClickCount() > 1) { // is it double click?

			// What row and column were clicked upon (if any)?
			Point point = new Point(evt.getX(), evt.getY());
			int iRow = ((JTable) evt.getSource()).rowAtPoint(point);
			if (iRow == -1) {
				return;
			}
			// Which table the click occured on?
			if (((JTable) evt.getSource()).getModel() instanceof PasswordsTableModel) { // Passwords table
				viewPassword();
			} 
			else if (((JTable) evt.getSource()).getModel() instanceof KeyPairsTableModel) { // Key pairs table
				viewCertificate();
			} 
			else if (((JTable) evt.getSource()).getModel() instanceof ProxiesTableModel) { //Proxies table
				viewProxyCertificate();
			}
			else { // Trusted certificates table
				viewCertificate();
			}
		}
	}
	
	/**
	 * Lets the user select a file to export to or import from a key pair or a
	 * certificate. The file types are filtered according to their extensions:
	 * .p12 or .pfx are PKCS #12 keystore files containing private key and its
	 * public key (+cert chain) .crt are ASN.1 PEM-encoded files containing one
	 * (or more concatenated) public key certificate(s) .der are ASN.1
	 * DER-encoded files containing one public key certificate .cer are
	 * CER-encoded files containing one ore more DER-encoded certificates
	 */
	private File selectImportExportFile(String title, String[] filter,
			String description, String approveButtonText) {

		JFileChooser chooser = new JFileChooser();
		chooser
				.addChoosableFileFilter(new CryptoFileFilter(filter,
						description));
		chooser.setDialogTitle(title);
		chooser.setMultiSelectionEnabled(false);

		int rtnValue = chooser.showDialog(this, approveButtonText);
		if (rtnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			return selectedFile;
		}
		return null;
	}
	
	/**
	 * Exits the UI's frame.
	 */
	private void closeFrame() {
		setVisible(false);
		dispose();
	}

	
//	/**
//	 * Set cursor to busy and disable application input. This can be reversed by
//	 * a subsequent call to setCursorFree.
//	 */
//	private void setCursorBusy() {
//		// Block all mouse events using glass pane
//		Component glassPane = getRootPane().getGlassPane();
//		glassPane.addMouseListener(new MouseAdapter() {
//		});
//		glassPane.setVisible(true);
//
//		// Set cursor to busy
//		glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//	}
//
//	
//	/**
//	 * Set cursor to free and enable application input. Called after a call to
//	 * setCursorBusy.
//	 */
//	private void setCursorFree() {
//		// Accept mouse events
//		Component glassPane = getRootPane().getGlassPane();
//		glassPane.setVisible(false);
//
//		// Revert cursor to default
//		glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
//	}
//			
//	/**
//	 * Action helper class.
//	 */
//	private abstract class AbstractAction extends javax.swing.AbstractAction {
//		protected abstract void act();
//
//		public void actionPerformed(ActionEvent evt) {
//			setCursorBusy();
//			repaint();
//			new Thread(new Runnable() {
//				public void run() {
//					try {
//						act();
//					} finally {
//						setCursorFree();
//					}
//				}
//			}).start();
//		}
//	}

}
