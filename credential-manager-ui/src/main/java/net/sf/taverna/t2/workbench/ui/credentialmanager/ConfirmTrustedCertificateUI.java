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

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.security.cert.X509Certificate;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI;
import net.sf.taverna.t2.security.credentialmanager.TrustConfirmation;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;

import org.apache.log4j.Logger;

public class ConfirmTrustedCertificateUI implements CredentialProviderSPI {

	private static Logger logger = Logger
			.getLogger(ConfirmTrustedCertificateUI.class);

	public boolean canHandleTrustConfirmation(X509Certificate[] chain) {
		return !GraphicsEnvironment.isHeadless();
	}

	public int getProviderPriority() {
		return 100;
	}

	public TrustConfirmation shouldTrust(X509Certificate[] chain) {
		TrustConfirmation trustConfirm = new TrustConfirmation();
		logger.info("Asking the user if they want to trust a certificate.");
		// Ask user if they want to trust this service
		ConfirmTrustedCertificateDialog confirmCertTrustDialog = new ConfirmTrustedCertificateDialog(
				(Frame) null, "Untrusted HTTPS connection", true,
				(X509Certificate) chain[0]);
		confirmCertTrustDialog.setLocationRelativeTo(null);
		confirmCertTrustDialog.setVisible(true);
		trustConfirm.setShouldTrust(confirmCertTrustDialog.shouldTrust());
		trustConfirm.setShouldSave(confirmCertTrustDialog.shouldSave());
		if (!confirmCertTrustDialog.shouldTrust()) {
			JOptionPane
					.showMessageDialog(
							null,
							"As you refused to trust this host, you will not be able to use its services from a workflow.",
							"Untrusted HTTPS connection",
							JOptionPane.INFORMATION_MESSAGE);
		}

		return trustConfirm;
	}

	public boolean canProvideUsernamePassword(URI serviceURI) {
		return false;
	}

	public boolean canProvideMasterPassword() {
		return false;
	}

	public boolean canProvideJavaTruststorePassword() {
		return false;
	}

	public String getJavaTruststorePassword() {
		return null;
	}

	public String getMasterPassword(boolean firstTime) {
		return null;
	}

	public UsernamePassword getUsernamePassword(URI serviceURI,
			String requestingPrompt) {
		return null;
	}

}
