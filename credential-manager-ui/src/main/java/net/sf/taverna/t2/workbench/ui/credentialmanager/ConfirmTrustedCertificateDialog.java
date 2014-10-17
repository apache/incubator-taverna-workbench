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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.BorderLayout.SOUTH;
import static java.awt.Color.WHITE;
import static java.awt.Font.BOLD;
import static java.awt.Font.PLAIN;
import static java.awt.GridBagConstraints.LINE_START;
import static javax.security.auth.x500.X500Principal.RFC2253;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.DistinguishedNameParser;
import net.sf.taverna.t2.security.credentialmanager.ParsedDistinguishedName;
import net.sf.taverna.t2.workbench.helper.NonBlockedHelpEnabledDialog;

import org.apache.log4j.Logger;

/**
 * Displays the details of a X.509 certificate and asks user if they want to
 * trust it. This is normally invoked by the Taverna's TrustManager when trying 
 * to confirm the trust in the remote server during SSL handshake.
 */
@SuppressWarnings("serial")
public class ConfirmTrustedCertificateDialog extends NonBlockedHelpEnabledDialog {
	private static Logger logger = Logger.getLogger(ConfirmTrustedCertificateDialog.class);

	/** The certificate to display */
	private X509Certificate cert;
	/** User's decision as whether to trust this service's certificate or not */
	private boolean shouldTrust;
	/**
	 * Should the decision also be saved in Credential Manager? Actually - it is
	 * always saved now as it was really hard to implement trusting for one
	 * connection only - so we can either "trust" or "not" trust but not
	 * "trust once".
	 */
	private boolean shouldSave = false;
	private final DistinguishedNameParser dnParser;

	public ConfirmTrustedCertificateDialog(Frame parent, String title,
			boolean modal, X509Certificate crt, DistinguishedNameParser dnParser) {
		super(parent, title, modal);
		this.cert = crt;
		this.dnParser = dnParser;
		initComponents();
	}

	public ConfirmTrustedCertificateDialog(Dialog parent, String title,
			boolean modal, X509Certificate crt, DistinguishedNameParser dnParser)
			throws CMException {
		super(parent, title, modal);
		this.cert = crt;
		this.dnParser = dnParser;
		initComponents();
	}

	private void initComponents(){		
		// title panel
		JPanel titlePanel = new JPanel(new BorderLayout());
		titlePanel.setBackground(WHITE);
		JLabel titleLabel = new JLabel("View service's certificate");
		titleLabel.setFont(titleLabel.getFont().deriveFont(BOLD, 13.5f));
		titleLabel.setBorder(new EmptyBorder(10, 10, 0, 10));

		DialogTextArea titleMessage = new DialogTextArea();
		titleMessage.setMargin(new Insets(5, 20, 10, 10));
		titleMessage.setFont(titleMessage.getFont().deriveFont(11f));
		titleMessage.setEditable(false);
		titleMessage.setFocusable(false);
		titlePanel.setBorder( new EmptyBorder(10, 10, 0, 10));
		titlePanel.add(titleLabel, NORTH);
		titlePanel.add(titleMessage, CENTER);
		
		// Certificate details:

		ParsedDistinguishedName subjectDN = dnParser.parseDN(cert
				.getSubjectX500Principal().getName(RFC2253));
		ParsedDistinguishedName issuerDN = dnParser.parseDN(cert
				.getIssuerX500Principal().getName(RFC2253));
		JPanel certificatePanel = createCertificateDetailsPanel(subjectDN, issuerDN);
		titleMessage.setText("The service host " + subjectDN.getCN() + " requires HTTPS connection and has identified itself with the certificate below.\n" +
				"Do you want to trust this service? (Refusing to trust means you will not be able to invoke services on this host from a workflow.)");

		// OK button
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

//		final JButton trustButton = new JButton("Trust once");
//		trustButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				trustPressed();
//			}
//		});
		
		//final JButton trustAlwaysButton = new JButton("Trust always");
		final JButton trustAlwaysButton = new JButton("Trust");
		trustAlwaysButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				trustAlwaysPressed();
			}
		});
		
		final JButton dontTrustButton = new JButton("Do not trust");
		dontTrustButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				dontTrustPressed();
			}
		});

		//jpButtons.add(trustButton);
		buttonsPanel.add(trustAlwaysButton);
		buttonsPanel.add(dontTrustButton);

		getContentPane().add(titlePanel, NORTH);
		getContentPane().add(certificatePanel, CENTER);
		getContentPane().add(buttonsPanel, SOUTH);

		setResizable(false);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				closeDialog();
			}
		});

		getRootPane().setDefaultButton(trustAlwaysButton);
		pack();
	}

	private JPanel createCertificateDetailsPanel(ParsedDistinguishedName subjectDN, ParsedDistinguishedName issuerDN) {
		/*
		 * Grid Bag Constraints templates for labels (column 1) and values
		 * (column 2) of certificate details
		 */
		GridBagConstraints gbc_labels = new GridBagConstraints();
		gbc_labels.gridx = 0;
		gbc_labels.ipadx = 20;
		gbc_labels.gridwidth = 1;
		gbc_labels.gridheight = 1;
		gbc_labels.insets = new Insets(2, 15, 2, 2);
		gbc_labels.anchor = LINE_START;

		GridBagConstraints gbc_values = new GridBagConstraints();
		gbc_values.gridx = 1;
		gbc_values.gridwidth = 1;
		gbc_values.gridheight = 1;
		gbc_values.insets = new Insets(2, 5, 2, 2);
		gbc_values.anchor = LINE_START;

		/*
		 * Netscape Certificate Type non-critical extension (if any) defines the
		 * intended uses of the certificate - to make it look like Firefox's
		 * view certificate dialog
		 * 
		 * From openssl's documentation: "The [above] extension is non standard,
		 * Netscape specific and largely obsolete. Their use in new applications
		 * is discouraged."
		 * 
		 * TODO replace with "basicConstraints, keyUsage and extended key usage
		 * extensions which are now used instead."
		 */
//		byte[] intendedUses = cert.getExtensionValue("2.16.840.1.113730.1.1"); // Netscape Certificate Type OID
//		JLabel intendedUsesLabel = null;
//		JTextField intendedUsesTextField = null;
//		JPanel intendedUsesPanel = null;
//		GridBagConstraints gbc_intendedUsesLabel = null;
//		if (intendedUses != null) {
//			intendedUsesLabel = new JLabel(
//					"This certificate has been approved for the following uses:");
//			intendedUsesLabel.setFont(new Font(null, Font.BOLD, 11));
//			intendedUsesLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
//
//			intendedUsesTextField = new JTextField(45);
//			intendedUsesTextField.setText(CMUtils.getIntendedCertificateUses(intendedUses));
//			intendedUsesTextField.setEditable(false);
//			intendedUsesTextField.setFont(new Font(null, Font.PLAIN, 11));
//
//			intendedUsesPanel = new JPanel(new BorderLayout());
//			intendedUsesPanel.add(intendedUsesLabel, BorderLayout.NORTH);
//			intendedUsesPanel.add(intendedUsesTextField, BorderLayout.CENTER);
//			JSeparator separator = new JSeparator(JSeparator.HORIZONTAL);
//			intendedUsesPanel.add(separator, BorderLayout.SOUTH);
//
//			gbc_intendedUsesLabel = (GridBagConstraints) gbc_labels.clone();
//			gbc_intendedUsesLabel.gridy = 0;
//			gbc_intendedUsesLabel.gridwidth = 2; // takes two columns
//			gbc_intendedUsesLabel.insets = new Insets(5, 5, 5, 5);// has slightly bigger insets
//		}

		// Issued To
		JLabel issuedToLabel = new JLabel("Issued To");
		issuedToLabel.setFont(new Font(null, BOLD, 11));
		GridBagConstraints gbc_issuedTo = (GridBagConstraints) gbc_labels
				.clone();
		gbc_issuedTo.gridy = 1;
		gbc_issuedTo.gridwidth = 2; // takes two columns
		gbc_issuedTo.insets = new Insets(5, 5, 5, 5);// has slightly bigger insets
		// Subject's Distinguished Name (DN)
		// Extract the CN, O, OU and EMAILADDRESS fields
		String subjectCN = subjectDN.getCN();
		String subjectOrg = subjectDN.getO();
		String subjectOU = subjectDN.getOU();
		// String sEMAILADDRESS = CMUtils.getEmilAddress();
		// Subject's Common Name (CN)
		JLabel subjectCNLabel = new JLabel("Common Name (CN)");
		subjectCNLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_subjectCNLabel = (GridBagConstraints) gbc_labels.clone();
		gbc_subjectCNLabel.gridy = 2;
		JLabel subjectCNValue = new JLabel(subjectCN);
		subjectCNValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_subjectCNValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_subjectCNValue.gridy = 2;
		// Subject's Organisation (O)
		JLabel subjectOrgLabel = new JLabel("Organisation (O)");
		subjectOrgLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_subjectOrgLabel = (GridBagConstraints) gbc_labels.clone();
		gbc_subjectOrgLabel.gridy = 3;
		JLabel subjectOrgValue = new JLabel(subjectOrg);
		subjectOrgValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_subjectOrgValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_subjectOrgValue.gridy = 3;
		// Subject's Organisation Unit (OU)
		JLabel subjectOULabel = new JLabel("Organisation Unit (OU)");
		subjectOULabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_subjectOULabel = (GridBagConstraints) gbc_labels.clone();
		gbc_subjectOULabel.gridy = 4;
		JLabel subjectOUValue = new JLabel(subjectOU);
		subjectOUValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_subjectOUValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_subjectOUValue.gridy = 4;
		// E-mail Address
		// JLabel jlEmail = new JLabel("E-mail Address");
		// jlEmail.setFont(new Font(null, Font.PLAIN, 11));
		// GridBagConstraints gbc_jlEmail = (GridBagConstraints)
		// gbcLabel.clone();
		// gbc_jlEmail.gridy = 5;
		// JLabel jlEmailValue = new JLabel(sEMAILADDRESS);
		// jlEmailValue.setFont(new Font(null, Font.PLAIN, 11));
		// GridBagConstraints gbc_jlEmailValue = (GridBagConstraints)
		// gbcValue.clone();
		// gbc_jlEmailValue.gridy = 5;
		// Serial Number
		JLabel snLabel = new JLabel("Serial Number");
		snLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_snLabel = (GridBagConstraints) gbc_labels.clone();
		gbc_snLabel.gridy = 6;
		JLabel snValue = new JLabel();
		// Get the hexadecimal serial number
		StringBuffer strBuff = new StringBuffer(new BigInteger(1, cert
				.getSerialNumber().toByteArray()).toString(16).toUpperCase());
		// Place colons at every two hexadecimal characters
		if (strBuff.length() > 2)
			for (int iCnt = 2; iCnt < strBuff.length(); iCnt += 3)
				strBuff.insert(iCnt, ':');
		snValue.setText(strBuff.toString());
		snValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_snValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_snValue.gridy = 6;
		// Certificate version number
		JLabel versionLabel = new JLabel("Version");
		versionLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_versionLabel = (GridBagConstraints) gbc_labels
				.clone();
		gbc_versionLabel.gridy = 7;
		JLabel versionValue = new JLabel(Integer.toString(cert.getVersion()));
		versionValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_versionValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_versionValue.gridy = 7;

		// Issued By
		JLabel issuedByLabel = new JLabel("Issued By");
		issuedByLabel.setFont(new Font(null, BOLD, 11));
		GridBagConstraints gbc_issuedByLabel = (GridBagConstraints) gbc_labels
				.clone();
		gbc_issuedByLabel.gridy = 8;
		gbc_issuedByLabel.gridwidth = 2; // takes two columns
		gbc_issuedByLabel.insets = new Insets(5, 5, 5, 5);// has slightly bigger insets
		// Issuer's Distinguished Name (DN)
		// Extract the CN, O and OU fields for the issuer
		String issuerCN = issuerDN.getCN();
		String issuerOrg = issuerDN.getO();
		String issuerOU = issuerDN.getOU();
		// Issuer's Common Name (CN)
		JLabel issuerCNLabel = new JLabel("Common Name (CN)");
		issuerCNLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_issuerCNLabel = (GridBagConstraints) gbc_labels.clone();
		gbc_issuerCNLabel.gridy = 9;
		JLabel issuerCNValue = new JLabel(issuerCN);
		issuerCNValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_issuerCNValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_issuerCNValue.gridy = 9;
		// Issuer's Organisation (O)
		JLabel issuerOrgLabel = new JLabel("Organisation (O)");
		issuerOrgLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_issuerOrgLabel = (GridBagConstraints) gbc_labels.clone();
		gbc_issuerOrgLabel.gridy = 10;
		JLabel issuerOrgValue = new JLabel(issuerOrg);
		issuerOrgValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_issuerOrgValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_issuerOrgValue.gridy = 10;
		// Issuer's Organisation Unit (OU)
		JLabel issuerOULabel = new JLabel("Organisation Unit (OU)");
		issuerOULabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_issuerOULabel = (GridBagConstraints) gbc_labels.clone();
		gbc_issuerOULabel.gridy = 11;
		JLabel issuerOUValue = new JLabel(issuerOU);
		issuerOUValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_issuerOUValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_issuerOUValue.gridy = 11;
		
		// Validity
		JLabel validityLabel = new JLabel("Validity");
		validityLabel.setFont(new Font(null, BOLD, 11));
		GridBagConstraints gbc_validityLabel = (GridBagConstraints) gbc_labels
				.clone();
		gbc_validityLabel.gridy = 12;
		gbc_validityLabel.gridwidth = 2; // takes two columns
		gbc_validityLabel.insets = new Insets(5, 5, 5, 5);// has slightly bigger insets
		// Issued On
		JLabel issuedOnLabel = new JLabel("Issued On");
		issuedOnLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_issuedOnLabel = (GridBagConstraints) gbc_labels
				.clone();
		gbc_issuedOnLabel.gridy = 13;
		JLabel issuedOnValue = new JLabel(cert.getNotBefore().toString());
		issuedOnValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_issuedOnValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_issuedOnValue.gridy = 13;
		// Expires On
		JLabel expiresOnLabel = new JLabel("Expires On");
		expiresOnLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_expiresOnLabel = (GridBagConstraints) gbc_labels
				.clone();
		gbc_expiresOnLabel.gridy = 14;
		JLabel expiresOnValue = new JLabel(cert.getNotAfter().toString());
		expiresOnValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_expiresOnValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_expiresOnValue.gridy = 14;

		// Fingerprints
		byte[] binaryCertificateEncoding = new byte[0];
		try {
			// each certificate has one binary encoding; for X.509 certs it is DER
			binaryCertificateEncoding = cert.getEncoded();
		} catch (CertificateEncodingException ex) {
			logger.error("Could not get the encoded form of the certificate.", ex);
		}
		JLabel fingerprintsLabel = new JLabel("Fingerprints");
		fingerprintsLabel.setFont(new Font(null, BOLD, 11));
		GridBagConstraints gbc_fingerprintsLabel = (GridBagConstraints) gbc_labels
				.clone();
		gbc_fingerprintsLabel.gridy = 15;
		gbc_fingerprintsLabel.gridwidth = 2; // takes two columns
		gbc_fingerprintsLabel.insets = new Insets(5, 5, 5, 5);// has slightly bigger insets
		// SHA-1 Fingerprint
		JLabel sha1FingerprintLabel = new JLabel("SHA1 Fingerprint");
		sha1FingerprintLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_sha1FingerprintLabel = (GridBagConstraints) gbc_labels
				.clone();
		gbc_sha1FingerprintLabel.gridy = 16;
		JLabel sha1FingerprintValue = new JLabel(
				dnParser.getMessageDigestAsFormattedString(
						binaryCertificateEncoding, "SHA1"));
		sha1FingerprintValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_sha1FingerprintValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_sha1FingerprintValue.gridy = 16;
		// MD5 Fingerprint
		JLabel md5FingerprintLabel = new JLabel("MD5 Fingerprint");
		md5FingerprintLabel.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_md5FingerprinLabel = (GridBagConstraints) gbc_labels
				.clone();
		gbc_md5FingerprinLabel.gridy = 17;
		JLabel md5FingerprintValue = new JLabel(
				dnParser.getMessageDigestAsFormattedString(
						binaryCertificateEncoding, "MD5"));
		md5FingerprintValue.setFont(new Font(null, PLAIN, 11));
		GridBagConstraints gbc_md5FingerprintValue = (GridBagConstraints) gbc_values
				.clone();
		gbc_md5FingerprintValue.gridy = 17;

		/*
		 * Empty label to add a bit space at the bottom of the panel to make it
		 * look like Firefox's view certificate dialog
		 */
		JLabel emptyLabel = new JLabel("");
		GridBagConstraints gbc_emptyLabel = (GridBagConstraints) gbc_labels.clone();
		gbc_emptyLabel.gridy = 18;
		gbc_emptyLabel.gridwidth = 2; // takes two columns
		gbc_emptyLabel.ipady = 40;

		JPanel certificatePanel = new JPanel(new GridBagLayout());
		certificatePanel.setBorder(new CompoundBorder(new EmptyBorder(15, 15, 15,
				15), new EtchedBorder()));

//		if (intendedUses != null)
//			certificatePanel.add(intendedUsesPanel, gbc_intendedUsesLabel);
		certificatePanel.add(issuedToLabel, gbc_issuedTo); // Issued To
		certificatePanel.add(subjectCNLabel, gbc_subjectCNLabel);
		certificatePanel.add(subjectCNValue, gbc_subjectCNValue);
		certificatePanel.add(subjectOrgLabel, gbc_subjectOrgLabel);
		certificatePanel.add(subjectOrgValue, gbc_subjectOrgValue);
		certificatePanel.add(subjectOULabel, gbc_subjectOULabel);
		certificatePanel.add(subjectOUValue, gbc_subjectOUValue);
		// jpCertificate.add(jlEmail, gbc_jlEmail);
		// jpCertificate.add(jlEmailValue, gbc_jlEmailValue);
		certificatePanel.add(snLabel, gbc_snLabel);
		certificatePanel.add(snValue, gbc_snValue);
		certificatePanel.add(versionLabel, gbc_versionLabel);
		certificatePanel.add(versionValue, gbc_versionValue);
		certificatePanel.add(issuedByLabel, gbc_issuedByLabel); // Issued By
		certificatePanel.add(issuerCNLabel, gbc_issuerCNLabel);
		certificatePanel.add(issuerCNValue, gbc_issuerCNValue);
		certificatePanel.add(issuerOrgLabel, gbc_issuerOrgLabel);
		certificatePanel.add(issuerOrgValue, gbc_issuerOrgValue);
		certificatePanel.add(issuerOULabel, gbc_issuerOULabel);
		certificatePanel.add(issuerOUValue, gbc_issuerOUValue);
		certificatePanel.add(validityLabel, gbc_validityLabel); // Validity
		certificatePanel.add(issuedOnLabel, gbc_issuedOnLabel);
		certificatePanel.add(issuedOnValue, gbc_issuedOnValue);
		certificatePanel.add(expiresOnLabel, gbc_expiresOnLabel);
		certificatePanel.add(expiresOnValue, gbc_expiresOnValue);
		certificatePanel.add(fingerprintsLabel, gbc_fingerprintsLabel); // Fingerprints
		certificatePanel.add(sha1FingerprintLabel, gbc_sha1FingerprintLabel);
		certificatePanel.add(sha1FingerprintValue, gbc_sha1FingerprintValue);
		certificatePanel.add(md5FingerprintLabel, gbc_md5FingerprinLabel);
		certificatePanel.add(md5FingerprintValue, gbc_md5FingerprintValue);
		// Empty label to get some vertical space on the frame
		certificatePanel.add(emptyLabel, gbc_emptyLabel);
		return certificatePanel;
	}

//	private void trustPressed() {
//		shouldTrust = true;
//		shouldSave = false;
//		closeDialog();
//	}

	private void trustAlwaysPressed() {
		shouldTrust = true;
		shouldSave = true;
		closeDialog();
	}

	private void dontTrustPressed() {
		shouldTrust = false;
		shouldSave = false;
		closeDialog();
	}

	public void closeDialog() {
		setVisible(false);
		dispose();
	}

	public boolean shouldTrust() {
		return shouldTrust;
	}

	public boolean shouldSave() {
		return shouldSave;
	}
}
