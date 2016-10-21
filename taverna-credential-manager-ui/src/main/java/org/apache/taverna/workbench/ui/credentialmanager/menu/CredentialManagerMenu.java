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
package org.apache.taverna.workbench.ui.credentialmanager.menu;

import java.net.URI;

import javax.swing.Action;

//import org.apache.log4j.Logger;

import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.security.credentialmanager.DistinguishedNameParser;
import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.ui.credentialmanager.action.CredentialManagerAction;

public class CredentialManagerMenu extends AbstractMenuAction {
	private static final String MENU_URI = "http://taverna.sf.net/2008/t2workbench/menu#advanced";

	private CredentialManager credentialManager;
	private DistinguishedNameParser dnParser;

	// private static Logger logger = Logger.getLogger(CredentialManagerMenu.class);

	public CredentialManagerMenu() {
		super(URI.create(MENU_URI), 60);
		/* This is now done in the initialise SSL startup hook - no need to do it here.
		// Force initialisation at startup
		try {
			CredentialManager.getInstance();
		} catch (CMException e) {
			logger.error("Could not initialise SSL properties for SSL connections from Taverna.", e);
		}
		*/
	}

	@Override
	protected Action createAction() {
		return new CredentialManagerAction(credentialManager, dnParser);
	}

	public void setCredentialManager(CredentialManager credentialManager) {
		this.credentialManager = credentialManager;
	}

	/**
	 * @param dnParser
	 *            the dnParser to set
	 */
	public void setDistinguishedNameParser(DistinguishedNameParser dnParser) {
		this.dnParser = dnParser;
	}
}
