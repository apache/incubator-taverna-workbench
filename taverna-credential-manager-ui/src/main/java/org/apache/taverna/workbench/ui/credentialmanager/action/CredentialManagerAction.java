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

package org.apache.taverna.workbench.ui.credentialmanager.action;

import static javax.swing.SwingUtilities.invokeLater;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.security.credentialmanager.DistinguishedNameParser;
import org.apache.taverna.workbench.ui.credentialmanager.CredentialManagerUI;

//import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class CredentialManagerAction extends AbstractAction {
	private static ImageIcon ICON = new ImageIcon(
			CredentialManagerAction.class
					.getResource("/images/cred_manager16x16.png"));

	private CredentialManagerUI cmUI;
	private final CredentialManager credentialManager;
	private final DistinguishedNameParser dnParser;

	public CredentialManagerAction(CredentialManager credentialManager,
			DistinguishedNameParser dnParser) {
		super("Credential Manager", ICON);
		this.credentialManager = credentialManager;
		this.dnParser = dnParser;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (cmUI != null) {
			cmUI.setVisible(true);
			return;
		}

		invokeLater(new Runnable() {
			@Override
			public void run() {
				cmUI = new CredentialManagerUI(credentialManager, dnParser);
				cmUI.setVisible(true);
			}
		});
	}
}
