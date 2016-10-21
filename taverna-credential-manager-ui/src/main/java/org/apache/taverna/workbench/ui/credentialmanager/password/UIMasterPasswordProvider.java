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
package org.apache.taverna.workbench.ui.credentialmanager.password;

import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;
import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.security.credentialmanager.DistinguishedNameParser;

import org.apache.taverna.security.credentialmanager.JavaTruststorePasswordProvider;
import org.apache.taverna.security.credentialmanager.MasterPasswordProvider;
import org.apache.taverna.workbench.ui.credentialmanager.GetMasterPasswordDialog;
import org.apache.taverna.workbench.ui.credentialmanager.SetMasterPasswordDialog;
import org.apache.taverna.workbench.ui.credentialmanager.WarnUserAboutJCEPolicyDialog;

/**
 * A UI pop-up that asks user for a master password for Credential Manager.
 *
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 *
 */
public class UIMasterPasswordProvider implements MasterPasswordProvider, JavaTruststorePasswordProvider {

	private ApplicationConfiguration applicationConfiguration;

        private DistinguishedNameParser dnParser;
        
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
		WarnUserAboutJCEPolicyDialog.warnUserAboutJCEPolicy(applicationConfiguration, dnParser);

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
        
        /**
	 * @param dnParser the dnParser to set
	 */
	public void setDistinguishedNameParser(DistinguishedNameParser dnParser) {
		this.dnParser = dnParser;
	}
}
