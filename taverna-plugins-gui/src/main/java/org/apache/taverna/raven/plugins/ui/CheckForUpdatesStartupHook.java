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

import uk.org.taverna.commons.plugin.PluginManager;
import uk.org.taverna.configuration.app.ApplicationConfiguration;

import org.apache.taverna.workbench.StartupSPI;

/**
 * Startup hook for checking if there are available updates for Taverna plugins.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class CheckForUpdatesStartupHook implements StartupSPI {

	public static final String CHECK_FOR_UPDATES_DIRECTORY_NAME = "updates";
	public static final String LAST_UPDATE_CHECK_FILE_NAME = "last_update_check";

	private PluginManager pluginManager;
	private ApplicationConfiguration applicationConfiguration;

	public static File checkForUpdatesDirectory = getCheckForUpdatesDirectory();
	public static File lastUpdateCheckFile = new File(checkForUpdatesDirectory,
			LAST_UPDATE_CHECK_FILE_NAME);

	public int positionHint() {
		return 90;
	}

	public boolean startup() {

		// Check if more than 2 weeks passed since we checked for updates.
		if (lastUpdateCheckFile.exists()) {
			long lastModified = lastUpdateCheckFile.lastModified();
			long now = new Date().getTime();

			if (now - lastModified < 14 * 24 * 3600 * 1000) { // 2 weeks have not passed since we
																// last asked
				return true;
			} else { // Check again for updates
				if (pluginManager.checkForUpdates()) {
					CheckForUpdatesDialog dialog = new CheckForUpdatesDialog();
					dialog.setVisible(true);
				}
				return true;
			}
		} else {
			// If we are here - then this is the first time to check for updates
			if (pluginManager.checkForUpdates()) {
				CheckForUpdatesDialog dialog = new CheckForUpdatesDialog();
				dialog.setVisible(true);
			}
			return true;
		}
	}

	/**
	 * Gets the registration directory where info about registration will be saved to.
	 */
	public File getCheckForUpdatesDirectory() {

		File home = applicationConfiguration.getApplicationHomeDir();

		File registrationDirectory = new File(home, CHECK_FOR_UPDATES_DIRECTORY_NAME);
		if (!registrationDirectory.exists()) {
			registrationDirectory.mkdir();
		}
		return registrationDirectory;
	}
}
