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
import static java.awt.BorderLayout.SOUTH;
import static java.awt.BorderLayout.WEST;
import static java.awt.Font.PLAIN;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.ALERT_TITLE;
import static org.apache.taverna.workbench.ui.credentialmanager.CMStrings.ERROR_TITLE;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.DistinguishedNameParser;
import org.apache.taverna.workbench.helper.NonBlockedHelpEnabledDialog;

/**
 * Allows the user import a key pair from a PKCS #12 file (keystore).
 */
@SuppressWarnings("serial")
class NewKeyPairEntryDialog extends NonBlockedHelpEnabledDialog {
	//private static final Logger logger = Logger.getLogger(NewKeyPairEntryDialog.class);

	/** List of key pairs available for import */
	private JList<String> keyPairsJList;
	/** PKCS #12 keystore */
	private KeyStore pkcs12KeyStore;
	/** Private key part of the key pair chosen by the user for import */
	private Key privateKey;
	/** Certificate chain part of the key pair chosen by the user for import */
	private Certificate[] certificateChain;
	/** Key pair alias to be used for this entry in the Keystore */
	private String alias;
	private final DistinguishedNameParser dnParser;

	public NewKeyPairEntryDialog(JFrame parent, String title, boolean modal,
			KeyStore pkcs12KeyStore, DistinguishedNameParser dnParser)
			throws CMException {
		super(parent, title, modal);
		this.pkcs12KeyStore = pkcs12KeyStore;
		this.dnParser = dnParser;
		initComponents();
	}

	public NewKeyPairEntryDialog(JDialog parent, String title, boolean modal,
			KeyStore pkcs12KeyStore, DistinguishedNameParser dnParser)
			throws CMException {
		super(parent, title, modal);
		this.pkcs12KeyStore = pkcs12KeyStore;
		this.dnParser = dnParser;
		initComponents();
	}

	/**
	 * Get the private part of the key pair.
	 */
	public Key getPrivateKey() {
		return privateKey;
	}

	/**
	 * Get the certificate chain part of the key pair.
	 */
	public Certificate[] getCertificateChain() {
		return certificateChain;
	}

	/**
	 * Get the keystore alias of the key pair.
	 */
	public String getAlias() {
		return alias;
	}

	private void initComponents() throws CMException {
		// Instructions
		JLabel instructionsLabel = new JLabel("Select a key pair to import:");
		instructionsLabel.setFont(new Font(null, PLAIN, 11));
		instructionsLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JPanel instructionsPanel = new JPanel(new BorderLayout());
		instructionsPanel.add(instructionsLabel, WEST);

		// Import button
		final JButton importButton = new JButton("Import");
		importButton.setEnabled(false);
		importButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				importPressed();
			}
		});

		// Certificate details button
		final JButton certificateDetailsButton = new JButton("Details");
		certificateDetailsButton.setEnabled(false);
		certificateDetailsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				certificateDetailsPressed();
			}
		});

        // List to hold keystore's key pairs
		keyPairsJList = new JList<>();
		keyPairsJList.setSelectionMode(SINGLE_SELECTION);
		keyPairsJList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent evt) {
				boolean enabled = keyPairsJList.getSelectedIndex() >= 0;
				importButton.setEnabled(enabled);
				certificateDetailsButton.setEnabled(enabled);
			}
		});

        // Put the key list into a scroll pane
		JScrollPane keyPairsScrollPane = new JScrollPane(keyPairsJList,
				VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		keyPairsScrollPane.getViewport().setBackground(
				keyPairsJList.getBackground());
        
        JPanel keyPairsPanel = new JPanel();
        keyPairsPanel.setLayout(new BoxLayout(keyPairsPanel, Y_AXIS));
		keyPairsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		instructionsPanel.setAlignmentY(LEFT_ALIGNMENT);
		keyPairsPanel.add(instructionsPanel);
		keyPairsScrollPane.setAlignmentY(LEFT_ALIGNMENT);
		keyPairsPanel.add(keyPairsScrollPane);

		// Cancel button
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				cancelPressed();
			}
		});

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		buttonsPanel.add(certificateDetailsButton);
		buttonsPanel.add(importButton);
		buttonsPanel.add(cancelButton);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(keyPairsPanel, CENTER);
		getContentPane().add(buttonsPanel, SOUTH);

		// Populate the list
		populateKeyPairList();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		setResizable(false);
		getRootPane().setDefaultButton(importButton);
		pack();
	}

	/**
	 * Populate the key pair list with the PKCS #12 keystore's key pair aliases.
	 */
	private void populateKeyPairList() throws CMException {
		try {
			List<String> keyPairAliases = new ArrayList<>();

			Enumeration<String> aliases = pkcs12KeyStore.aliases();
			while (aliases.hasMoreElements()) {
				String alias = aliases.nextElement();

				if (pkcs12KeyStore.isKeyEntry(alias)) {
					pkcs12KeyStore.getKey(alias, new char[] {});
					Certificate[] certs = pkcs12KeyStore
							.getCertificateChain(alias);
					if (certs != null && certs.length != 0)
						keyPairAliases.add(alias);
				}
			}

            if (!keyPairAliases.isEmpty()) {
                keyPairsJList.setListData(keyPairAliases.toArray(new String[0]));
                keyPairsJList.setSelectedIndex(0);
			} else
                // No key pairs were found - warn the user
				showMessageDialog(this,
						"No private key pairs were found in the file",
						ALERT_TITLE, WARNING_MESSAGE);
		} catch (GeneralSecurityException ex) {
            throw new CMException("Problem occured while reading the PKCS #12 file.",
                ex);
        }
    }

	/**
	 * Display the selected key pair's certificate.
	 */
	private void certificateDetailsPressed() {
		try {
			String alias = (String) keyPairsJList.getSelectedValue();

			// Convert the certificate object into an X509Certificate object.
			X509Certificate cert = dnParser.convertCertificate(pkcs12KeyStore
					.getCertificate(alias));

			ViewCertDetailsDialog viewCertificateDialog = new ViewCertDetailsDialog(
					this, "Certificate details", true, (X509Certificate) cert,
					null, dnParser);
			viewCertificateDialog.setLocationRelativeTo(this);
			viewCertificateDialog.setVisible(true);
		} catch (Exception ex) {
			showMessageDialog(this,
					"Failed to obtain certificate details to show",
					ALERT_TITLE, WARNING_MESSAGE);
			closeDialog();
		}
	}

	public void importPressed() {
		String alias = (String) keyPairsJList.getSelectedValue();
		try {
			privateKey = pkcs12KeyStore.getKey(alias, new char[] {});
			certificateChain = pkcs12KeyStore.getCertificateChain(alias);
			this.alias = alias;
		} catch (Exception ex) {
			showMessageDialog(
					this,
					"Failed to load the private key and certificate chain from the PKCS #12 file.",
					ERROR_TITLE, ERROR_MESSAGE);
		}

        closeDialog();
    }

	public void cancelPressed() {
		/*
		 * Set everything to null, just in case some of the values have been set
		 * previously and the user pressed 'cancel' after that.
		 */
		privateKey = null;
		certificateChain = null;
		closeDialog();
	}

	private void closeDialog() {
		setVisible(false);
		dispose();
	}

}
