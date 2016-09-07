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

package org.apache.taverna.workbench.ui.credentialmanager.startup;

import org.apache.log4j.Logger;

import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.workbench.StartupSPI;

/**
 * 
 * Startup hook to initialise SSL socket factory used by Taverna for creating
 * HTTPS connections.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 */
public class InitialiseSSLStartupHook implements StartupSPI {
	private static final Logger logger = Logger
			.getLogger(InitialiseSSLStartupHook.class);

	private CredentialManager credManager;

	@Override
	public int positionHint() {
		return 25;
	}

	@Override
	public boolean startup() {
		logger.info("Initialising SSL socket factory for SSL connections from Taverna.");
		try {
			credManager.initializeSSL();
		} catch (CMException e) {
			logger.error(
					"Could not initialise the SSL socket factory (for creating SSL connections)"
							+ " using Taverna's keystores.", e);
		}
		return true;
	}

	public void setCredentialManager(CredentialManager credManager) {
		this.credManager = credManager;
	}
}
