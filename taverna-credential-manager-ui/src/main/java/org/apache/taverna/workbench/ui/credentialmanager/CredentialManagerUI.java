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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.PAGE_END;
import static java.awt.Dialog.ModalExclusionType.APPLICATION_EXCLUDE;
import static java.awt.Toolkit.getDefaultToolkit;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static org.apache.taverna.security.credentialmanager.CredentialManager.KeystoreType.KEYSTORE;
import static org.apache.taverna.security.credentialmanager.CredentialManager.KeystoreType.TRUSTSTORE;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.ALERT_TITLE;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.ERROR_TITLE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.security.credentialmanager.CredentialManager.KeystoreType;
import org.apache.taverna.security.credentialmanager.DistinguishedNameParser;
import org.apache.taverna.security.credentialmanager.UsernamePassword;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;

/**
 * Provides a UI for the Credential Manager for users to manage their
 * credentials saved by the Credential Manager in Taverna's Keystore and
 * Trustore. Credentials include username and passwords pairs, key pairs, proxy
 * key pairs and trusted certificates of CA's and s. Credentials are stored in
 * two Bouncy Castle "UBER"-type keystores: the Keystore (containing passwords
 * and (normal and proxy) key pairs) and the Truststore (containing trusted
 * certificates).
 *
 * Inspired by the Portlecle tool (http://portecle.sourceforge.net/)
 * and Firefox's Certificate Manager.
 *
 * @author Alex Nenadic
 */

@SuppressWarnings("serial")
public class CredentialManagerUI extends JFrame {
	private static Logger logger = Logger.getLogger(CredentialManagerUI.class);
	/** Default tabbed pane width */
	private static final int DEFAULT_FRAME_WIDTH = 650;
	/** Default tabbed pane height */
	private static final int DEFAULT_FRAME_HEIGHT = 400;
	/** Credential Manager icon (when frame is minimised)*/
	private static final Image credManagerIconImage = getDefaultToolkit()
			.createImage(
					CredentialManagerUI.class
							.getResource("/images/cred_manager_transparent.png"));

	/**
	 * Credential Manager to manage all operations on the Keystore and
	 * Truststore
	 */
	public final CredentialManager credManager;
	private final DistinguishedNameParser dnParser;
	
	////////////// Tabs //////////////

	/**
	 * Tabbed pane to hold tables containing various entries in the Keystore and
	 * Truststore
	 */
	private JTabbedPane keyStoreTabbedPane;
	/** Tab 1: holds passwords table */
	private JPanel passwordsTab = new JPanel(new BorderLayout(10, 10));
	/** Tab 1: name */
	public static final String PASSWORDS = "Passwords";
	/** Tab 2: holds key pairs (user certificates) table */
	private JPanel keyPairsTab = new JPanel(new BorderLayout(10, 10));
	/** Tab 2: name */
	public static final String KEYPAIRS = "Your Certificates";
	/** Tab 3: holds trusted certificates table */
	private JPanel trustedCertificatesTab = new JPanel(new BorderLayout(10, 10));
	/** Tab 3: name */
	public static final String TRUSTED_CERTIFICATES = "Trusted Certificates";

	////////////// Tables //////////////

	/** Password entries' table */
	private JTable passwordsTable;
	/** Key pair entries' table */
	private JTable keyPairsTable;
	/** Trusted certificate entries' table */
	private JTable trustedCertsTable;
	/** Password entry column type */
	public static final String PASSWORD_ENTRY_TYPE = "Password";
	/** Key pair entry column type */
	public static final String KEY_PAIR_ENTRY_TYPE = "Key Pair";
	/** Trusted cert entry column type */
	public static final String TRUST_CERT_ENTRY_TYPE = "Trusted Certificate";

	/**
	 * Overrides the Object's clone method to prevent the singleton object to be
	 * cloned.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	/**
	 * Creates a new Credential Manager UI's frame.
	 */
	public CredentialManagerUI(CredentialManager credentialManager,
			DistinguishedNameParser dnParser) {
		credManager = credentialManager;
		this.dnParser = dnParser;
		setModalExclusionType(APPLICATION_EXCLUDE);
		// Initialise the UI components
		initComponents();
	}

	private void initComponents() {
		/*
		 * Initialise the tabbed pane that contains the tabs with tabular
		 * representations of the Keystore's content.
		 */
		keyStoreTabbedPane = new JTabbedPane();
		/*
		 * Initialise the tab containing the table for username/password entries
		 * from the Keystore
		 */
		passwordsTable = initTable(PASSWORDS, passwordsTab);
		/*
		 * Initialise the tab containing the table for key pair entries from the
		 * Keystore
		 */
		keyPairsTable = initTable(KEYPAIRS, keyPairsTab);
		/*
		 * Initialise the tab containing the table for proxy entries from the
		 * Keystore
		 */
		//proxiesTable = initTable(PROXIES, proxiesTab);
		/*
		 * Initialise the tab containing the table for trusted certificate
		 * entries from the Truststore
		 */
		trustedCertsTable = initTable(TRUSTED_CERTIFICATES,
				trustedCertificatesTab);
		/*
		 * Set the size of the tabbed pane to the preferred size - the size of
		 * the main application frame depends on it.
		 */
		keyStoreTabbedPane.setPreferredSize(new Dimension(DEFAULT_FRAME_WIDTH,
				DEFAULT_FRAME_HEIGHT));

		JPanel globalButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton resetJavaAuthCache = new JButton("Clear HTTP authentication");
		resetJavaAuthCache.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearAuthenticationCache();
			}
		});
		globalButtons.add(resetJavaAuthCache);

		// Button for changing Credential Manager's master password
		JButton changeMasterPasswordButton = new JButton(
				"Change master password");
		changeMasterPasswordButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeMasterPassword();
			}
		});
		globalButtons.add(changeMasterPasswordButton);

		// Add change master password to the main application frame
		getContentPane().add(globalButtons, NORTH);
		// Add tabbed pane to the main application frame
		getContentPane().add(keyStoreTabbedPane, CENTER);

		// Handle application close
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeFrame();
			}
		});
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		pack();

		// Centre the frame in the centre of the screen
		setLocationRelativeTo(null);

		// Set the frame's icon
		setIconImage(credManagerIconImage);

		// Set the frame's title
		setTitle("Credential Manager");

		// setModal(true);
		// setVisible(true);
	}

	protected void clearAuthenticationCache() {
		if (!credManager.resetAuthCache())
			showMessageDialog(
					this,
					"Java's internal HTTP authentication cache could not be cleared. \n\n"
							+ "Taverna can only clear the cache using an undocumented Java API \n"
							+ "that might not work if you are using a Java VM other than \n"
							+ "Java 6 from Sun. You can restarting Taverna to clear the cache.",
					"Could not clear authentication cache", ERROR_MESSAGE);
		else
			showMessageDialog(
					this,
					"Java's internal HTTP authentication cache has been cleared. \n\n"
							+ "You might also need to edit or delete individual \n"
							+ "password entries in the credential manager \n"
							+ "if a relevant password has previously been saved.",
					"Cleared authentication cache", INFORMATION_MESSAGE);
	}

	protected void changeMasterPassword() {
		ChangeMasterPasswordDialog changePasswordDialog = new ChangeMasterPasswordDialog(
				this, "Change master password", true,
				"Change master password for Credential Manager", credManager);
		changePasswordDialog.setLocationRelativeTo(null);
		changePasswordDialog.setVisible(true);
		String password = changePasswordDialog.getPassword();
		if (password == null) // user cancelled
			return; // do nothing

		try {
			credManager.changeMasterPassword(password);
			showMessageDialog(this, "Master password changed sucessfully",
					ALERT_TITLE, INFORMATION_MESSAGE);
		} catch (CMException cme) {
			/*
			 * Failed to change the master password for Credential Manager -
			 * warn the user
			 */
			String exMessage = "Failed to change master password for Credential Manager";
			logger.error(exMessage);
			showMessageDialog(this, exMessage, ERROR_TITLE, ERROR_MESSAGE);
		}
	}

	/**
	 * Initialise the tabs and tables with the content from the Keystore and Truststore.
	 */
	private JTable initTable(String tableType, JPanel tab) {
		JTable table = null;

		if (tableType.equals(PASSWORDS)) { // Passwords table
			// The Passwords table's data model
			PasswordsTableModel passwordsTableModel = new PasswordsTableModel(credManager);
			// The table itself
			table = new JTable(passwordsTableModel);

			/*
			 * Set the password and alias columns of the Passwords table to be
			 * invisible by removing them from the column model (they will still
			 * present in the table model)
			 * 
			 * Remove the last column first
			 */
			TableColumn aliasColumn = table.getColumnModel().getColumn(5);
			table.getColumnModel().removeColumn(aliasColumn);
			TableColumn passwordColumn = table.getColumnModel().getColumn(4);
			table.getColumnModel().removeColumn(passwordColumn);
			TableColumn lastModifiedDateColumn = table.getColumnModel().getColumn(3);
			table.getColumnModel().removeColumn(lastModifiedDateColumn);

			// Buttons
			JButton newPasswordButton = new JButton("New");
			newPasswordButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					newPassword();
				}
			});

			final JButton viewPasswordButton = new JButton("Details");
			viewPasswordButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					viewPassword();
				}
			});
			viewPasswordButton.setEnabled(false);

			final JButton editPasswordButton = new JButton("Edit");
			editPasswordButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editPassword();
				}
			});
			editPasswordButton.setEnabled(false);

			final JButton deletePasswordButton = new JButton("Delete");
			deletePasswordButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deletePassword();
				}
			});
			deletePasswordButton.setEnabled(false);

			/*
			 * Selection listener for passwords table to enable/disable action
			 * buttons accordingly
			 */
			class PasswordsTableSelectionListner implements
					ListSelectionListener {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (e.getSource() != passwordsTable.getSelectionModel())
						return;
					if (passwordsTable.getSelectedRow() == -1) {
						// nothing is selected
						viewPasswordButton.setEnabled(false);
						editPasswordButton.setEnabled(false);
						deletePasswordButton.setEnabled(false);
					} else {
						if (!viewPasswordButton.isEnabled())
							viewPasswordButton.setEnabled(true);
						if (!editPasswordButton.isEnabled())
							editPasswordButton.setEnabled(true);
						if (!deletePasswordButton.isEnabled())
							deletePasswordButton.setEnabled(true);
					}
				}
			}
			table.getSelectionModel().addListSelectionListener(new PasswordsTableSelectionListner());

			// Panel to hold the buttons
			JPanel bp = new JPanel();
			bp.add(viewPasswordButton);
			bp.add(editPasswordButton);
			bp.add(newPasswordButton);
			bp.add(deletePasswordButton);

			// Add button panel to the tab
			tab.add(bp, PAGE_END);

		} else if (tableType.equals(KEYPAIRS)) { // Key Pairs tab
			// The Key Pairs table's data model
			KeyPairsTableModel keyPairsTableModel = new KeyPairsTableModel(credManager);
			// The table itself
			table = new JTable(keyPairsTableModel);

			/*
			 * Set the alias and service URIs columns of the KayPairs table to
			 * be invisible by removing them from the column model (they will
			 * still present in the table model)
			 * 
			 * Remove the last column first
			 */
			TableColumn aliasColumn = table.getColumnModel().getColumn(6);
			table.getColumnModel().removeColumn(aliasColumn);
			TableColumn serviceURIsColumn = table.getColumnModel().getColumn(5);
			table.getColumnModel().removeColumn(serviceURIsColumn);
			TableColumn lastModifiedDateColumn = table.getColumnModel().getColumn(4);
			table.getColumnModel().removeColumn(lastModifiedDateColumn);

			// Buttons
			final JButton viewKeyPairButton = new JButton("Details");
			viewKeyPairButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					viewCertificate();
				}
			});
			viewKeyPairButton.setEnabled(false);

			JButton importKeyPairButton = new JButton("Import");
			importKeyPairButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					importKeyPair();
				}
			});

			final JButton exportKeyPairButton = new JButton("Export");
			exportKeyPairButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					exportKeyPair();
				}
			});
			exportKeyPairButton.setEnabled(false);

			final JButton deleteKeyPairButton = new JButton("Delete");
			deleteKeyPairButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteKeyPair();
				}
			});
			deleteKeyPairButton.setEnabled(false);

			/*
			 * Selection listener for key pairs table to enable/disable action
			 * buttons accordingly
			 */
			class KeyPairsTableSelectionListner implements
					ListSelectionListener {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (e.getSource() != keyPairsTable.getSelectionModel())
						return;
					if (keyPairsTable.getSelectedRow() == -1) {
						// nothing is selected
						viewKeyPairButton.setEnabled(false);
						exportKeyPairButton.setEnabled(false);
						deleteKeyPairButton.setEnabled(false);
					} else {
						if (!viewKeyPairButton.isEnabled())
							viewKeyPairButton.setEnabled(true);
						if (!exportKeyPairButton.isEnabled())
							exportKeyPairButton.setEnabled(true);
						if (!deleteKeyPairButton.isEnabled())
							deleteKeyPairButton.setEnabled(true);
					}
				}
			}
			table.getSelectionModel().addListSelectionListener(
					new KeyPairsTableSelectionListner());

			// Panel to hold the buttons
			JPanel bp = new JPanel();
			bp.add(viewKeyPairButton);
			bp.add(importKeyPairButton);
			bp.add(exportKeyPairButton);
			bp.add(deleteKeyPairButton);

			// Add button panel to the tab
			tab.add(bp, PAGE_END);
		} else if (tableType.equals(TRUSTED_CERTIFICATES)) { // Certificates tab

			// The Trusted Certificate table's data model
			TrustedCertsTableModel trustedCertificatesTableModel = new TrustedCertsTableModel(credManager);
			// The table itself
			table = new JTable(trustedCertificatesTableModel);

			/*
			 * Set the alias columns of the Trusted Certs table to be invisible
			 * by removing them from the column model (they will still be
			 * present in the table model)
			 * 
			 * Remove the last column first
			 */
			TableColumn aliasColumn = table.getColumnModel().getColumn(5);
			table.getColumnModel().removeColumn(aliasColumn);
			TableColumn lastModifiedDateColumn = table.getColumnModel().getColumn(4);
			table.getColumnModel().removeColumn(lastModifiedDateColumn);

			// Buttons
			final JButton viewTrustedCertificateButton = new JButton("Details");
			viewTrustedCertificateButton
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							viewCertificate();
						}
					});
			viewTrustedCertificateButton.setEnabled(false);

			JButton importTrustedCertificateButton = new JButton("Import");
			importTrustedCertificateButton
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							importTrustedCertificate();
						}
					});

			final JButton exportTrustedCertificateButton = new JButton("Export");
			exportTrustedCertificateButton
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							exportTrustedCertificate();
						}
					});
			exportTrustedCertificateButton.setEnabled(false);

			final JButton deleteTrustedCertificateButton = new JButton("Delete");
			deleteTrustedCertificateButton
					.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							deleteTrustedCertificate();
						}
					});
			deleteTrustedCertificateButton.setEnabled(false);

			// Selection listener for trusted certs table to enable/disable action buttons accordingly
			class TrustedCertsTableSelectionListener implements
					ListSelectionListener {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if (e.getSource() != trustedCertsTable.getSelectionModel())
						return;
					if (trustedCertsTable.getSelectedRow() == -1) {
						// nothing is selected
						viewTrustedCertificateButton.setEnabled(false);
						exportTrustedCertificateButton.setEnabled(false);
						deleteTrustedCertificateButton.setEnabled(false);
					} else {
						if (!viewTrustedCertificateButton.isEnabled())
							viewTrustedCertificateButton.setEnabled(true);
						if (!exportTrustedCertificateButton.isEnabled())
							exportTrustedCertificateButton.setEnabled(true);
						if (!deleteTrustedCertificateButton.isEnabled())
							deleteTrustedCertificateButton.setEnabled(true);
					}
				}
			}
			table.getSelectionModel().addListSelectionListener(
					new TrustedCertsTableSelectionListener());

			// Panel to hold the buttons
			JPanel bp = new JPanel();
			bp.add(viewTrustedCertificateButton);
			bp.add(importTrustedCertificateButton);
			bp.add(exportTrustedCertificateButton);
			bp.add(deleteTrustedCertificateButton);

			// Add button panel to the tab
			tab.add(bp, PAGE_END);
		} else {
			throw new RuntimeException("Unknown table type " + tableType);
		}

		table.setShowGrid(false);
		table.setRowMargin(0);
		table.getColumnModel().setColumnMargin(0);
		table.getTableHeader().setReorderingAllowed(false);
		table.setAutoResizeMode(AUTO_RESIZE_ALL_COLUMNS);
		// Top accommodates entry icons with 2 pixels spare space (images are
		// 16x16 pixels)
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
		// (i.e. Service URI column of Passwords table, and
		// Certificate Name column of the Kay Pairs and Trusted Certificates tables)
		// We do not care about the size of other columns.
		TableColumn secondCol = table.getColumnModel().getColumn(1);
		secondCol.setMinWidth(20);
		secondCol.setMaxWidth(10000);
		secondCol.setPreferredWidth(300);

		// Put the table into a scroll pane
		JScrollPane jspTableScrollPane = new JScrollPane(table,
				VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jspTableScrollPane.getViewport().setBackground(table.getBackground());

		// Put the scroll pane on the tab panel
		tab.add(jspTableScrollPane, CENTER);
		jspTableScrollPane.setBorder(new EmptyBorder(3, 3, 3, 3));

		/*
		 * Add mouse listeners to show an entry's details if it is
		 * double-clicked
		 */
		table.addMouseListener(new MouseAdapter() {
			@Override
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
	 * showing the plaintext password and service URI for this entry.
	 */
	private void viewPassword() {
		// Which username/password pair entry has been selected, if any?
		int iRow = passwordsTable.getSelectedRow();
		if (iRow == -1) // no row currently selected
			return;

		// Get current values for service URI, username and password
		String serviceURI = (String) passwordsTable.getValueAt(iRow, 1); // current entry's service URI

		String username = (String) passwordsTable.getValueAt(iRow, 2); // current entry's username

		/*
		 * Because the password column is not visible we call the getValueAt
		 * method on the table model rather than at the JTable
		 */
		String password = (String) passwordsTable.getModel()
				.getValueAt(iRow, 4); // current entry's password value

		// Let the user view service URI, username and password of the entry
		ViewUsernamePasswordEntryDialog viewServicePassDialog = new ViewUsernamePasswordEntryDialog(
				this, serviceURI, username, password);

		viewServicePassDialog.setLocationRelativeTo(this);
		viewServicePassDialog.setVisible(true);
	}

	/**
	 * Lets a user insert a new username/password/service URI tuple to the
	 * Keystore.
	 */
	private void newPassword() {
		URI serviceURI = null; // service URI
		String username = null; // username
		String password = null; // password

		// Loop until the user cancels or enters everything correctly
		while (true) {
			/*
			 * Let the user insert a new password entry (by specifying service
			 * URI, username and password)
			 */
			NewEditPasswordEntryDialog newPasswordDialog = new NewEditPasswordEntryDialog(
					this, "New username and password for a service", true,
					serviceURI, username, password, credManager);
			newPasswordDialog.setLocationRelativeTo(this);
			newPasswordDialog.setVisible(true);

			serviceURI = newPasswordDialog.getServiceURI(); // get service URI
			username = newPasswordDialog.getUsername(); // get username
			password = newPasswordDialog.getPassword(); // get password

			if (password == null) { // user cancelled - any of the above three
				// fields is null
				// do nothing
				return;
			}

			/*
			 * Check if a password entry with the given service URI already
			 * exists in the Keystore. We ask this here as the user may wish to
			 * overwrite the existing password entry. Checking for key pair
			 * entries' URIs is done in the NewEditPasswordEntry dialog.
			 */

			/*
			 * Get list of service URIs for all the password entries in the
			 * Keystore
			 */
			List<URI> serviceURIs = null;
			try {
				serviceURIs = credManager
						.getServiceURIsForAllUsernameAndPasswordPairs();
			} catch (CMException cme) {
				showMessageDialog(this, "Failed to get service URIs for all username and password pairs "
						+ "to check if the entered service URI already exists",
						ERROR_TITLE, ERROR_MESSAGE);
				return;
			}
			if (serviceURIs.contains(serviceURI)) { // if such a URI already
				// exists
				// Ask if the user wants to overwrite it
				int answer = showConfirmDialog(
								this,
								"Credential Manager already contains a password entry with the same service URI.\n"
										+ "Do you want to overwrite it?",
								ALERT_TITLE,
								YES_NO_OPTION);

				// Add the new password entry in the Keystore
				try {
					if (answer == YES_OPTION) {
						credManager.addUsernameAndPasswordForService(
								new UsernamePassword(username, password),
								serviceURI);
						break;
					}
				} catch (CMException cme) {
					showMessageDialog(
							this,
							"Credential Manager failed to insert a new username and password pair",
							ERROR_TITLE, ERROR_MESSAGE);
				}
				/*
				 * Otherwise show the same window with the entered service URI,
				 * username and password values
				 */
			} else
				// Add the new password entry in the Keystore
				try {
					credManager.addUsernameAndPasswordForService(new UsernamePassword(username,
							password), serviceURI);
					break;
				} catch (CMException cme) {
					showMessageDialog(
							this,
							"Credential Manager failed to insert a new username and password pair",
							ERROR_TITLE, ERROR_MESSAGE);
				}
		}
	}

	/**
	 * Lets a user insert a new username/password pair for a given service URI
	 * to the Keystore.
	 */
	public void newPasswordForService(URI serviceURI) {
		/*
		 * As this method can be called from outside of Credential Manager UI,
		 * e.g. from wsdl-activity-ui or rshell-activity-ui to pop up a dialog
		 * to ask the user for username and password, we also want to make sure
		 * the main Credential Manager UI Dialog is visible as it may be clearer
		 * to the user what is going on
		 */
		if (!isVisible() || getState() == ICONIFIED)
			setVisible(true);

		// Make sure password tab is selected as this method may
		// be called from outside of Credential Manager UI.
		keyStoreTabbedPane.setSelectedComponent(passwordsTab);

		String username = null; // username
		String password = null; // password

		// Loop until the user cancels or enters everything correctly
		while (true) {

//			if(!this.isVisible()){ // if Cred Man UI is already showing but e.g. obscured by another window or minimised
//				// Do not bring it up!
//			} // actually we now want to show it as it makes it clearer to the user what is going on

			// Let the user insert a new password entry for the given service
			// URI (by specifying username and password)
			NewEditPasswordEntryDialog newPasswordDialog = new NewEditPasswordEntryDialog(
					this, "New username and password for a service", true,
					serviceURI, username, password, credManager);
			newPasswordDialog.setLocationRelativeTo(this);
			newPasswordDialog.setVisible(true);

			serviceURI = newPasswordDialog.getServiceURI(); // get service URI
			username = newPasswordDialog.getUsername(); // get username
			password = newPasswordDialog.getPassword(); // get password

			if (password == null) // user cancelled - any of the above three
				// fields is null
				// do nothing
				return;

			/*
			 * Check if a password entry with the given service URI already
			 * exists in the Keystore. We ask this here as the user may wish to
			 * overwrite the existing password entry. Checking for key pair
			 * entries' URIs is done in the NewEditPasswordEntry dialog.
			 */

			// Get list of service URIs for all the password entries in the
			// Keystore
			List<URI> serviceURIs = null;
			try {
				serviceURIs = credManager
						.getServiceURIsForAllUsernameAndPasswordPairs();
			} catch (CMException cme) {
				showMessageDialog(this, "Failed to get service URIs for all username and password pairs "
						+ "to check if the entered service URI already exists",
						ERROR_TITLE, ERROR_MESSAGE);
				return;
			}
			if (serviceURIs.contains(serviceURI)) { // if such a URI already
				// exists
				// Ask if the user wants to overwrite it
				int answer = showConfirmDialog(
						this,
						"Credential Manager already contains a password entry with the same service URI.\n"
								+ "Do you want to overwrite it?", ALERT_TITLE,
						YES_NO_OPTION);

				// Add the new password entry in the Keystore
				try {
					if (answer == YES_OPTION) {
						credManager.addUsernameAndPasswordForService(
								new UsernamePassword(username, password),
								serviceURI);
						break;
					}
				} catch (CMException cme) {
					String exMessage = "Credential Manager failed to insert a new username and password pair";
					showMessageDialog(this, exMessage, ERROR_TITLE,
							ERROR_MESSAGE);
				}
				// Otherwise show the same window with the entered service
				// URI, username and password values
			} else
				// Add the new password entry in the Keystore
				try {
					credManager.addUsernameAndPasswordForService(new UsernamePassword(username,
							password), serviceURI);
					break;
				} catch (CMException cme) {
					showMessageDialog(this, "Credential Manager failed to insert a new username and password pair",
							ERROR_TITLE,
							ERROR_MESSAGE);
				}
		}
	}

	/**
	 * Lets a user edit a username and password entry or their related service
	 * URI to the Keystore.
	 */
	private void editPassword() {
		// Which password entry has been selected?
		int iRow = passwordsTable.getSelectedRow();
		if (iRow == -1) { // no row currently selected
			return;
		}

		// Get current values for service URI, username and password
		URI serviceURI = URI.create((String) passwordsTable.getValueAt(iRow, 1)); // current entry's service URI

		String username = (String) passwordsTable.getValueAt(iRow, 2); // current entry's username

		/*
		 * Because the password column is not visible we call the getValueAt
		 * method on the table model rather than at the JTable
		 */
		String password = (String) passwordsTable.getModel()
				.getValueAt(iRow, 4); // current entry's password value

		while (true) { // loop until user cancels or enters everything correctly
			// Let the user edit service URI, username or password of a password entry
			NewEditPasswordEntryDialog editPasswordDialog = new NewEditPasswordEntryDialog(
					this, "Edit username and password for a service", true,
					serviceURI, username, password, credManager);

			editPasswordDialog.setLocationRelativeTo(this);
			editPasswordDialog.setVisible(true);

			// New values
			URI newServiceURI = editPasswordDialog.getServiceURI(); // get new service URI
			String newUsername = editPasswordDialog.getUsername(); // get new username
			String newPassword = editPasswordDialog.getPassword(); // get new password

			if (newPassword == null) // user cancelled - any of the above three
				// fields is null
				// do nothing
				return;

			// Is anything actually modified?
			boolean isModified = !serviceURI.equals(newServiceURI)
					|| !username.equals(newUsername)
					|| !password.equals(newPassword);

			if (isModified) {
				/*
				 * Check if a different password entry with the new URI (i.e.
				 * alias) already exists in the Keystore We ask this here as the
				 * user may wish to overwrite that other password entry.
				 */

				// Get list of URIs for all passwords in the Keystore
				List<URI> serviceURIs = null;
				try {
					serviceURIs = credManager
							.getServiceURIsForAllUsernameAndPasswordPairs();
				} catch (CMException cme) {
					showMessageDialog(this, "Failed to get service URIs for all username and password pairs "
							+ "to check if the modified entry already exists",
							ERROR_TITLE,
							ERROR_MESSAGE);
					return;
				}

				// If the modified service URI already exists and is not the
				// currently selected one
				if (!newServiceURI.equals(serviceURI)
						&& serviceURIs.contains(newServiceURI)) {
					int answer = showConfirmDialog(
							this,
							"The Keystore already contains username and password pair for the entered service URI.\n"
									+ "Do you want to overwrite it?",
							ALERT_TITLE, YES_NO_OPTION);

					try {
						if (answer == YES_OPTION) {
							/*
							 * Overwrite that other entry entry and save the new
							 * one in its place. Also remove the current one
							 * that we are editing - as it is replacing the
							 * other entry.
							 */
							credManager
									.deleteUsernameAndPasswordForService(serviceURI);
							credManager.addUsernameAndPasswordForService(
									new UsernamePassword(newUsername,
											newPassword), newServiceURI);
							break;
						}
					} catch (CMException cme) {
						showMessageDialog(
								this,
								"Failed to update the username and password pair in the Keystore",
								ERROR_TITLE, ERROR_MESSAGE);
					}
					// Otherwise show the same window with the entered
					// service URI, username and password values
				} else
					try {
						if (!newServiceURI.equals(serviceURI))
							credManager
									.deleteUsernameAndPasswordForService(serviceURI);
						credManager.addUsernameAndPasswordForService(
								new UsernamePassword(newUsername, newPassword), newServiceURI);
						break;
					} catch (CMException cme) {
						showMessageDialog(
								this,
								"Failed to update the username and password pair in the Keystore",
								ERROR_TITLE, ERROR_MESSAGE);
					}
			} else // nothing actually modified
				break;
		}
	}

	/**
	 * Lets the user delete the selected username and password entries from the
	 * Keystore.
	 */
	private void deletePassword() {
		// Which entries have been selected?
		int[] selectedRows = passwordsTable.getSelectedRows();
		if (selectedRows.length == 0) // no password entry selected
			return;

		// Ask user to confirm the deletion
		if (showConfirmDialog(
				null,
				"Are you sure you want to delete the selected username and password entries?",
				ALERT_TITLE, YES_NO_OPTION) != YES_OPTION)
			return;

		String exMessage = null;
		for (int i = selectedRows.length - 1; i >= 0; i--) { // delete from backwards
			// Get service URI for the current entry
			URI serviceURI = URI.create((String) passwordsTable.getValueAt(selectedRows[i], 1));
			// current entry's service URI
			try {
				// Delete the password entry from the Keystore
				credManager.deleteUsernameAndPasswordForService(serviceURI);
			} catch (CMException cme) {
				exMessage = "Failed to delete the username and password pair from the Keystore";
			}
		}
		if (exMessage != null)
			showMessageDialog(this, exMessage, ERROR_TITLE, ERROR_MESSAGE);
	}

	/**
	 * Shows the contents of a (user or trusted) certificate.
	 */
	private void viewCertificate() {
		int selectedRow = -1;
		String alias = null;
		X509Certificate certToView = null;
		ArrayList<String> serviceURIs = null;
		KeystoreType keystoreType = null;

		// Are we showing user's public key certificate?
		if (keyPairsTab.isShowing()) {
			keystoreType = KEYSTORE;
			selectedRow = keyPairsTable.getSelectedRow();

			if (selectedRow != -1)
				/*
				 * Because the alias column is not visible we call the
				 * getValueAt method on the table model rather than at the
				 * JTable
				 */
				alias = (String) keyPairsTable.getModel().getValueAt(selectedRow, 6); // current entry's Keystore alias
		}
		// Are we showing trusted certificate?
		else if (trustedCertificatesTab.isShowing()) {
			keystoreType = TRUSTSTORE;
			selectedRow = trustedCertsTable.getSelectedRow();

			if (selectedRow != -1)
				/*
				 * Get the selected trusted certificate entry's Truststore alias
				 * Alias column is invisible so we get the value from the table
				 * model
				 */
				alias = (String) trustedCertsTable.getModel().getValueAt(
						selectedRow, 5);
		}

		try {
			if (selectedRow != -1) { // something has been selected
				// Get the entry's certificate
				certToView = dnParser.convertCertificate(credManager
						.getCertificate(keystoreType, alias));

				// Show the certificate's contents to the user
				ViewCertDetailsDialog viewCertDetailsDialog = new ViewCertDetailsDialog(
						this, "Certificate details", true, certToView,
						serviceURIs, dnParser);
				viewCertDetailsDialog.setLocationRelativeTo(this);
				viewCertDetailsDialog.setVisible(true);
			}
		} catch (CMException cme) {
			String exMessage = "Failed to get certificate details to display to the user";
			logger.error(exMessage, cme);
			showMessageDialog(this, exMessage, ERROR_TITLE, ERROR_MESSAGE);
		}
	}

	/**
	 * Lets a user import a key pair from a PKCS #12 keystore file to the
	 * Keystore.
	 */
	private void importKeyPair() {
		/*
		 * Let the user choose a PKCS #12 file (keystore) containing a public
		 * and private key pair to import
		 */
		File importFile = selectImportExportFile(
				"PKCS #12 file to import from", // title
				new String[] { ".p12", ".pfx" }, // array of file extensions
				// for the file filter
				"PKCS#12 Files (*.p12, *.pfx)", // description of the filter
				"Import", // text for the file chooser's approve button
				"keyPairDir"); // preference string for saving the last chosen directory

		if (importFile == null)
			return;

		// The PKCS #12 keystore is not a file
		if (!importFile.isFile()) {
			showMessageDialog(this, "Your selection is not a file",
					ALERT_TITLE, WARNING_MESSAGE);
			return;
		}

		// Get the user to enter the password that was used to encrypt the
		// private key contained in the PKCS #12 file
		GetPasswordDialog getPasswordDialog = new GetPasswordDialog(this,
				"Import key pair entry", true,
				"Enter the password that was used to encrypt the PKCS #12 file");
		getPasswordDialog.setLocationRelativeTo(this);
		getPasswordDialog.setVisible(true);

		String pkcs12Password = getPasswordDialog.getPassword();

		if (pkcs12Password == null) // user cancelled
			return;
		else if (pkcs12Password.isEmpty()) // empty password
			// FIXME: Maybe user did not have the password set for the private key???
			return;

		try {
			// Load the PKCS #12 keystore from the file
			// (this is using the BouncyCastle provider !!!)
			KeyStore pkcs12Keystore = credManager.loadPKCS12Keystore(importFile.toPath(),
					pkcs12Password);

			/*
			 * Display the import key pair dialog supplying all the private keys
			 * stored in the PKCS #12 file (normally there will be only one
			 * private key inside, but could be more as this is a keystore after
			 * all).
			 */
			NewKeyPairEntryDialog importKeyPairDialog = new NewKeyPairEntryDialog(
					this, "Credential Manager", true, pkcs12Keystore, dnParser);
			importKeyPairDialog.setLocationRelativeTo(this);
			importKeyPairDialog.setVisible(true);

			// Get the private key and certificate chain of the key pair
			Key privateKey = importKeyPairDialog.getPrivateKey();
			Certificate[] certChain = importKeyPairDialog.getCertificateChain();

			if (privateKey == null || certChain == null)
				// User did not select a key pair for import or cancelled
				return;

			/*
			 * Check if a key pair entry with the same alias already exists in
			 * the Keystore
			 */
			if (credManager.hasKeyPair(privateKey, certChain)
					&& showConfirmDialog(this,
							"The keystore already contains the key pair entry with the same private key.\n"
									+ "Do you want to overwrite it?",
							ALERT_TITLE, YES_NO_OPTION) != YES_OPTION)
				return;

			// Place the private key and certificate chain into the Keystore
			credManager.addKeyPair(privateKey, certChain);

			// Display success message
			showMessageDialog(this, "Key pair import successful", ALERT_TITLE,
					INFORMATION_MESSAGE);
		} catch (Exception ex) { // too many exceptions to catch separately
			String exMessage = "Failed to import the key pair entry to the Keystore. "
					+ ex.getMessage();
			logger.error(exMessage, ex);
			showMessageDialog(this, exMessage, ERROR_TITLE, ERROR_MESSAGE);
		}
	}

	/**
	 * Lets a user export user's private and public key pair to a PKCS #12
	 * keystore file.
	 */
	private void exportKeyPair() {
		// Which key pair entry has been selected?
		int selectedRow = keyPairsTable.getSelectedRow();
		if (selectedRow == -1) // no row currently selected
			return;

		// Get the key pair entry's Keystore alias
		String alias = (String) keyPairsTable.getModel().getValueAt(selectedRow, 6);

		// Let the user choose a PKCS #12 file (keystore) to export public and
		// private key pair to
		File exportFile = selectImportExportFile("Select a file to export to", // title
				new String[] { ".p12", ".pfx" }, // array of file extensions
				// for the file filter
				"PKCS#12 Files (*.p12, *.pfx)", // description of the filter
				"Export", // text for the file chooser's approve button
				"keyPairDir"); // preference string for saving the last chosen directory

		if (exportFile == null)
			return;

		// If file already exist - ask the user if he wants to overwrite it
		if (exportFile.isFile()
				&& showConfirmDialog(this,
						"The file with the given name already exists.\n"
								+ "Do you want to overwrite it?", ALERT_TITLE,
						YES_NO_OPTION) == NO_OPTION)
			return;

		// Get the user to enter the password for the PKCS #12 keystore file
		GetPasswordDialog getPasswordDialog = new GetPasswordDialog(this,
				"Credential Manager", true,
				"Enter the password for protecting the exported key pair");
		getPasswordDialog.setLocationRelativeTo(this);
		getPasswordDialog.setVisible(true);

		String pkcs12Password = getPasswordDialog.getPassword();

		if (pkcs12Password == null) { // user cancelled or empty password
			// Warn the user
			showMessageDialog(
					this,
					"You must supply a password for protecting the exported key pair.",
					ALERT_TITLE, INFORMATION_MESSAGE);
			return;
		}

		// Export the key pair
		try {
			credManager.exportKeyPair(alias, exportFile.toPath(), pkcs12Password);
			showMessageDialog(this, "Key pair export successful", ALERT_TITLE,
					INFORMATION_MESSAGE);
		} catch (CMException cme) {
			showMessageDialog(this, cme.getMessage(), ERROR_TITLE,
					ERROR_MESSAGE);
		}
	}

	/**
	 * Lets a user delete selected key pair entries from the Keystore.
	 */
	private void deleteKeyPair() {
		// Which entries have been selected?
		int[] selectedRows = keyPairsTable.getSelectedRows();
		if (selectedRows.length == 0) // no key pair entry selected
			return;

		// Ask user to confirm the deletion
		if (showConfirmDialog(null,
				"Are you sure you want to delete the selected key pairs?",
				ALERT_TITLE, YES_NO_OPTION) != YES_OPTION)
			return;

		String exMessage = null;
		for (int i = selectedRows.length - 1; i >= 0; i--) { // delete from backwards
			// Get the alias for the current entry
			String alias = (String) keyPairsTable.getModel().getValueAt(
					selectedRows[i], 6);
			try {
				// Delete the key pair entry from the Keystore
				credManager.deleteKeyPair(alias);
			} catch (CMException cme) {
				logger.warn("failed to delete " + alias, cme);
				exMessage = "Failed to delete the key pair(s) from the Keystore";
			}
		}
		if (exMessage != null)
			showMessageDialog(this, exMessage, ERROR_TITLE, ERROR_MESSAGE);
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
				new String[] { ".pem", ".crt", ".cer", ".der", "p7", ".p7c" }, // file extensions filters
				"Certificate Files (*.pem, *.crt, , *.cer, *.der, *.p7, *.p7c)", // filter descriptions
				"Import", // text for the file chooser's approve button
				"trustedCertDir"); // preference string for saving the last chosen directory
		if (certFile == null)
			return;

		// Load the certificate(s) from the file
		ArrayList<X509Certificate> trustCertsList = new ArrayList<>();
		CertificateFactory cf;
		try {
			cf = CertificateFactory.getInstance("X.509");
		} catch (Exception e) {
			// Nothing we can do! Things are badly misconfigured
			cf = null;
		}

		if (cf != null) {
			try (FileInputStream fis = new FileInputStream(certFile)) {
				for (Certificate cert : cf.generateCertificates(fis))
					trustCertsList.add((X509Certificate) cert);
			} catch (Exception cex) {
				// Do nothing
			}

			if (trustCertsList.size() == 0) {
				// Could not load certificates as any of the above types
				
				try (PEMParser pr = new PEMParser(
								new InputStreamReader(new FileInputStream(certFile)))) {
					/*
					 * Try as openssl PEM format - which sligtly differs from
					 * the one supported by JCE
					 */
					Object cert;
					while ((cert = pr.readObject()) != null)
						if (cert instanceof X509Certificate)
							trustCertsList.add((X509Certificate) cert);
				} catch (Exception cex) {
					// do nothing
				}
			}
		}

		if (trustCertsList.size() == 0) {
			/* Failed to load certifcate(s) using any of the known encodings */
			showMessageDialog(this,
					"Failed to load certificate(s) using any of the known encodings -\n"
							+ "file format not recognised.", ERROR_TITLE,
					ERROR_MESSAGE);
			return;
		}

		// Show the list of certificates contained in the file for the user to
		// select the ones to import
		NewTrustCertsDialog importTrustCertsDialog = new NewTrustCertsDialog(this,
				"Credential Manager", true, trustCertsList, dnParser);

		importTrustCertsDialog.setLocationRelativeTo(this);
		importTrustCertsDialog.setVisible(true);
		List<X509Certificate> selectedTrustCerts = importTrustCertsDialog
				.getTrustedCertificates(); // user-selected trusted certs to import

		// If user cancelled or did not select any cert to import
		if (selectedTrustCerts == null || selectedTrustCerts.isEmpty())
			return;

		try {
			for (X509Certificate cert : selectedTrustCerts)
				// Import the selected trusted certificates
				credManager.addTrustedCertificate(cert);

			// Display success message
			showMessageDialog(this, "Trusted certificate(s) import successful",
					ALERT_TITLE, INFORMATION_MESSAGE);
		} catch (CMException cme) {
			String exMessage = "Failed to import trusted certificate(s) to the Truststore";
			logger.error(exMessage, cme);
			showMessageDialog(this, exMessage, ERROR_TITLE, ERROR_MESSAGE);
		}
	}

	/**
	 * Lets the user export one (at the moment) or more (in future) trusted
	 * certificate entries to a PEM-encoded file.
	 */
	private boolean exportTrustedCertificate() {
		// Which trusted certificate has been selected?
		int selectedRow = trustedCertsTable.getSelectedRow();
		if (selectedRow == -1) // no row currently selected
			return false;

		// Get the trusted certificate entry's Keystore alias
		String alias = (String) trustedCertsTable.getModel()
				.getValueAt(selectedRow, 3);
		// the alias column is invisible so we get the value from the table
		// model

		// Let the user choose a file to export to
		File exportFile = selectImportExportFile("Select a file to export to", // title
				new String[] { ".pem" }, // array of file extensions for the
				// file filter
				"Certificate Files (*.pem)", // description of the filter
				"Export", // text for the file chooser's approve button
				"trustedCertDir"); // preference string for saving the last chosen directory
		if (exportFile == null)
			return false;

		// If file already exist - ask the user if he wants to overwrite it
		if (exportFile.isFile()
				&& showConfirmDialog(this,
						"The file with the given name already exists.\n"
								+ "Do you want to overwrite it?", ALERT_TITLE,
						YES_NO_OPTION) == NO_OPTION)
			return false;

		// Export the trusted certificate
		try (JcaPEMWriter pw = new JcaPEMWriter(new FileWriter(exportFile))) {
		//try (PEMWriter pw = new PEMWriter(new FileWriter(exportFile))) {			
			// Get the trusted certificate
			pw.writeObject(credManager.getCertificate(TRUSTSTORE, alias));
		} catch (Exception ex) {
			String exMessage = "Failed to export the trusted certificate from the Truststore.";
			logger.error(exMessage, ex);
			showMessageDialog(this, exMessage, ERROR_TITLE, ERROR_MESSAGE);
			return false;
		}
		showMessageDialog(this, "Trusted certificate export successful",
				ALERT_TITLE, INFORMATION_MESSAGE);
		return true;
	}

	/**
	 * Lets a user delete the selected trusted certificate entries from the
	 * Truststore.
	 */
	private void deleteTrustedCertificate() {
		// Which entries have been selected?
		int[] selectedRows = trustedCertsTable.getSelectedRows();
		if (selectedRows.length == 0) // no trusted cert entry selected
			return;

		// Ask user to confirm the deletion
		if (showConfirmDialog(
				null,
				"Are you sure you want to delete the selected trusted certificate(s)?",
				ALERT_TITLE, YES_NO_OPTION) != YES_OPTION)
			return;

		String exMessage = null;
		for (int i = selectedRows.length - 1; i >= 0; i--) { // delete from backwards
			// Get the alias for the current entry
			String alias = (String) trustedCertsTable.getModel().getValueAt(
					selectedRows[i], 5);
			try {
				// Delete the trusted certificate entry from the Truststore
				credManager.deleteTrustedCertificate(alias);
			} catch (CMException cme) {
				exMessage = "Failed to delete the trusted certificate(s) from the Truststore";
				logger.error(exMessage, cme);
			}
		}
		if (exMessage != null)
			showMessageDialog(this, exMessage, ERROR_TITLE, ERROR_MESSAGE);
	}

	/**
	 * If double click on a table occured - show the
	 * details of the table entry.
	 */
	private void tableDoubleClick(MouseEvent evt) {
		if (evt.getClickCount() > 1) { // is it a double click?
			// Which row was clicked on (if any)?
			Point point = new Point(evt.getX(), evt.getY());
			int row = ((JTable) evt.getSource()).rowAtPoint(point);
			if (row == -1)
				return;
			// Which table the click occured on?
			if (((JTable) evt.getSource()).getModel() instanceof PasswordsTableModel)
				// Passwords table
				viewPassword();
			else if (((JTable) evt.getSource()).getModel() instanceof KeyPairsTableModel)
				// Key pairs table
				viewCertificate();
			else
				// Trusted certificates table
				viewCertificate();
		}
	}

	/**
	 * Lets the user select a file to export to or import from a key pair or a
	 * certificate.
	 */
	private File selectImportExportFile(String title, String[] filter,
			String description, String approveButtonText, String prefString) {
		Preferences prefs = Preferences
				.userNodeForPackage(CredentialManagerUI.class);
		String keyPairDir = prefs.get(prefString,
				System.getProperty("user.home"));
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.addChoosableFileFilter(new CryptoFileFilter(filter,
				description));
		fileChooser.setDialogTitle(title);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setCurrentDirectory(new File(keyPairDir));

		if (fileChooser.showDialog(this, approveButtonText) != APPROVE_OPTION)
			return null;

		File selectedFile = fileChooser.getSelectedFile();
		prefs.put(prefString, fileChooser.getCurrentDirectory().toString());
		return selectedFile;
	}

	private void closeFrame() {
		setVisible(false);
		dispose();
	}
}
