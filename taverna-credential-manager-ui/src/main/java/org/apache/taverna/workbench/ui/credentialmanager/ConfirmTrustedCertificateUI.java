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

import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Frame;
import java.security.cert.X509Certificate;

import org.apache.taverna.security.credentialmanager.DistinguishedNameParser;
import org.apache.taverna.security.credentialmanager.TrustConfirmationProvider;

import org.apache.log4j.Logger;

/**
 * @author Stian Soiland-Reyes
 */
public class ConfirmTrustedCertificateUI implements TrustConfirmationProvider {
	private static Logger logger = Logger
			.getLogger(ConfirmTrustedCertificateUI.class);

	private DistinguishedNameParser dnParser;

	@Override
	public Boolean shouldTrustCertificate(X509Certificate[] chain) {
		boolean trustConfirm = false;
		logger.info("Asking the user if they want to trust a certificate.");
		// Ask user if they want to trust this service
		ConfirmTrustedCertificateDialog confirmCertTrustDialog = new ConfirmTrustedCertificateDialog(
				(Frame) null, "Untrusted HTTPS connection", true,
				(X509Certificate) chain[0], dnParser);
		confirmCertTrustDialog.setLocationRelativeTo(null);
		confirmCertTrustDialog.setVisible(true);
		trustConfirm = confirmCertTrustDialog.shouldTrust();
//		trustConfirm.setShouldSave(confirmCertTrustDialog.shouldSave());
		if (!confirmCertTrustDialog.shouldTrust())
			showMessageDialog(
					null,
					"As you refused to trust this host, you will not be able to use its services from a workflow.",
					"Untrusted HTTPS connection", INFORMATION_MESSAGE);

		return trustConfirm;
	}

	/**
	 * @param dnParser
	 *            the dnParser to set
	 */
	public void setDistinguishedNameParser(DistinguishedNameParser dnParser) {
		this.dnParser = dnParser;
	}
}
