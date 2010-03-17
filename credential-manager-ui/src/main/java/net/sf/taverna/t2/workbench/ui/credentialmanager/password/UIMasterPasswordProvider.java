/*******************************************************************************
 * Copyright (C) 2009-2010 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.credentialmanager.password;

import java.awt.GraphicsEnvironment;
import java.net.URI;
import java.security.cert.X509Certificate;

import javax.swing.JFrame;

import net.sf.taverna.t2.security.credentialmanager.CredentialProviderSPI;
import net.sf.taverna.t2.security.credentialmanager.TrustConfirmation;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.t2.workbench.ui.credentialmanager.GetMasterPasswordDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.SetMasterPasswordDialog;
import net.sf.taverna.t2.workbench.ui.credentialmanager.WarnUserAboutJCEPolicyDialog;

/**
 * A UI pop-up that asks user for a master password for Credential Manager.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class UIMasterPasswordProvider implements CredentialProviderSPI {

	public boolean canProvideMasterPassword() {
		return !GraphicsEnvironment.isHeadless();
	}

	public boolean canProvideJavaTruststorePassword() {
		return !GraphicsEnvironment.isHeadless();
	}

	public String getJavaTruststorePassword() {
		if (GraphicsEnvironment.isHeadless()) {
			return null;
		}

		GetMasterPasswordDialog getPasswordDialog = new GetMasterPasswordDialog(
				"Credential Manager needs to copy certificates from Java truststore. "
						+ "Please enter your password.");
		getPasswordDialog.setLocationRelativeTo(null);
		getPasswordDialog.setVisible(true);
		String javaTruststorePassword = getPasswordDialog.getPassword();
		return javaTruststorePassword;
	}

	public int getProviderPriority() {
		// TODO Auto-generated method stub
		return 100;
	}

	public String getMasterPassword(boolean firstTime) {

		// Check if this Taverna run is headless (i.e. Taverna Server or Taverna
		// from command line) - do not do anything here if it is as we do not
		// want
		// any windows popping up even if they could
		if (GraphicsEnvironment.isHeadless()) {
			return null;
		}

		// Pop up a warning about Java Cryptography Extension (JCE)
		// Unlimited Strength Jurisdiction Policy
		WarnUserAboutJCEPolicyDialog.warnUserAboutJCEPolicy();
		
		if (firstTime) {
			// Ask user to set the master password for Credential Manager (only
			// the first time)
			SetMasterPasswordDialog setPasswordDialog = new SetMasterPasswordDialog(
					(JFrame) null, "Set master password", true,
					"Set master password for Credential Manager");
			setPasswordDialog.setLocationRelativeTo(null);
			setPasswordDialog.setVisible(true);
			return setPasswordDialog.getPassword();
		} else {
			// Ask user to provide a master password for Credential Manager
			GetMasterPasswordDialog getPasswordDialog = new GetMasterPasswordDialog(
			"Enter master password for Credential Manager");
			getPasswordDialog.setLocationRelativeTo(null);
			getPasswordDialog.setVisible(true);
			return getPasswordDialog.getPassword();
		}
	}

	public boolean canHandleTrustConfirmation(X509Certificate[] chain) {
		return false;
	}

	public boolean canProvideUsernamePassword(URI serviceURI) {
		return false;
	}

	public UsernamePassword getUsernamePassword(URI serviceURI,
			String requestingPrompt) {
		return null;
	}

	public TrustConfirmation shouldTrust(X509Certificate[] chain) {
		return null;
	}
}
