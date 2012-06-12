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

import javax.swing.JFrame;

import uk.org.taverna.configuration.app.ApplicationConfiguration;

import net.sf.taverna.t2.security.credentialmanager.JavaTruststorePasswordProvider;
import net.sf.taverna.t2.security.credentialmanager.MasterPasswordProvider;
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
public class UIMasterPasswordProvider implements MasterPasswordProvider, JavaTruststorePasswordProvider {

	private ApplicationConfiguration applicationConfiguration;

	@Override
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

	@Override
	public void setJavaTruststorePassword(String password) {
	}

	@Override
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
		WarnUserAboutJCEPolicyDialog.warnUserAboutJCEPolicy(applicationConfiguration);

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

	@Override
	public void setMasterPassword(String password) {
	}

	@Override
	public int getProviderPriority() {
		return 100;
	}

	/**
	 * Sets the applicationConfiguration.
	 *
	 * @param applicationConfiguration the new value of applicationConfiguration
	 */
	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

}
