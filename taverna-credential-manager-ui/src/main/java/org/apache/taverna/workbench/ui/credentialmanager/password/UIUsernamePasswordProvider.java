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

package org.apache.taverna.workbench.ui.credentialmanager.password;

import static java.awt.GraphicsEnvironment.isHeadless;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.taverna.security.credentialmanager.DistinguishedNameParser;
import org.apache.taverna.security.credentialmanager.ServiceUsernameAndPasswordProvider;
import org.apache.taverna.security.credentialmanager.UsernamePassword;

import org.apache.log4j.Logger;

public class UIUsernamePasswordProvider implements
		ServiceUsernameAndPasswordProvider {
	private static final Logger logger = Logger
			.getLogger(UIUsernamePasswordProvider.class);

	private DistinguishedNameParser dnParser;

	public boolean canProvideUsernamePassword(URI serviceURI) {
		return !isHeadless();
	}

	@Override
	public UsernamePassword getServiceUsernameAndPassword(URI serviceURI,
			String requestingPrompt) {
		URI displayURI = serviceURI;

		try {
			displayURI = dnParser.setFragmentForURI(displayURI, null);
			displayURI = dnParser.setUserInfoForURI(displayURI, null);
		} catch (URISyntaxException e) {
			logger.warn("Could not strip fragment/userinfo from " + serviceURI,
					e);
		}

		StringBuilder message = new StringBuilder();
		message.append("<html><body>The Taverna Credential Manager could not find a ");
		message.append("username and password for the service at:");
		message.append("<br><br><code>");
		message.append(displayURI);
		message.append("</code>");
		if (requestingPrompt != null && !requestingPrompt.isEmpty()) {
			message.append("<p><i>");
			message.append(requestingPrompt);
			message.append("</i>");
		}
		message.append("<br><br>Please provide the username and password.</body></html>");

		GetPasswordDialog getPasswordDialog = new GetPasswordDialog(
				message.toString(), true);
		getPasswordDialog.setLocationRelativeTo(null);
		if (serviceURI.getRawUserInfo() != null
				&& serviceURI.getRawUserInfo().length() > 1) {
			String userInfo = serviceURI.getRawUserInfo();
			String[] userPassword = userInfo.split(":", 2);
			if (userPassword.length == 2) {
				getPasswordDialog.setUsername(userPassword[0]);
				getPasswordDialog.setPassword(userPassword[1]);
			}
		}
		getPasswordDialog.setVisible(true);

		String username = getPasswordDialog.getUsername(); // get username
		String password = getPasswordDialog.getPassword(); // get password
		boolean shouldSaveUsernameAndPassword = getPasswordDialog
				.shouldSaveUsernameAndPassword();
		if (username == null || password == null)
			// user cancelled - any of the above two variables is null
			return null;

		UsernamePassword credential = new UsernamePassword();
		credential.setUsername(username);
		credential.setPassword(password.toCharArray());
		credential.setShouldSave(shouldSaveUsernameAndPassword);
		return credential;
	}

	@Override
	public void setServiceUsernameAndPassword(URI serviceURI,
			UsernamePassword usernamePassword) {
	}

	/**
	 * @param dnParser
	 *            the dnParser to set
	 */
	public void setDistinguishedNameParser(DistinguishedNameParser dnParser) {
		this.dnParser = dnParser;
	}
}
