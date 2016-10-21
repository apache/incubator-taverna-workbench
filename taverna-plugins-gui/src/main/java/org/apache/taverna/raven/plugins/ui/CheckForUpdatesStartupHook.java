package org.apache.taverna.raven.plugins.ui;
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

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.plugin.PluginException;
import org.apache.taverna.plugin.PluginManager;
import org.apache.taverna.workbench.StartupSPI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

/**
 * Startup hook for checking if there are available updates for Taverna plugins.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class CheckForUpdatesStartupHook implements StartupSPI, EventHandler {

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	public static final String CHECK_FOR_UPDATES_DIRECTORY_NAME = "updates";
	public static final String LAST_UPDATE_CHECK_FILE_NAME = "last_update_check";

	private EventAdmin eventAdmin;
	private PluginManager pluginManager;
	private ApplicationConfiguration applicationConfiguration;
	private Logger logger = Logger.getLogger(CheckForUpdatesStartupHook.class);

	public int positionHint() {
		return 90;
	}

	public boolean startup() {
		File lastUpdateCheckFile = new File(getCheckForUpdatesDirectory(),
				LAST_UPDATE_CHECK_FILE_NAME);
		// Check if more than 2 weeks passed since we checked for updates.
		if (lastUpdateCheckFile.exists()) {
			long lastModified = lastUpdateCheckFile.lastModified();
			long now = new Date().getTime();

			if (now - lastModified < 14 * 24 * 3600 * 1000) { // 2 weeks have not passed since we
																// last asked
				// No need to check for updates yet
				return true;
			}
		}
		try {
			pluginManager.checkForUpdates();
		} catch (PluginException e) {
			logger.error("Can't check for updates", e);
			return false;
		}
		return true;
	}


	/**
	 * Gets the registration directory where info about registration will be saved to.
	 */
	public File getCheckForUpdatesDirectory() {

		File home = applicationConfiguration.getApplicationHomeDir().toFile();

		File registrationDirectory = new File(home, CHECK_FOR_UPDATES_DIRECTORY_NAME);
		if (!registrationDirectory.exists()) {
			registrationDirectory.mkdir();
		}
		return registrationDirectory;
	}

	@Override
	public void handleEvent(Event event) {
		// TODO: Handle Plug
		if (event.getTopic().equals(PluginManager.UPDATES_AVAILABLE) {
			CheckForUpdatesDialog dialog = new CheckForUpdatesDialog();
			dialog.setVisible(true);
		}
	}
}
