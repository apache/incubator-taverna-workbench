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
package org.apache.taverna.raven.plugins.ui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.plugin.PluginException;
import org.apache.taverna.plugin.PluginManager;
import org.apache.taverna.workbench.StartupSPI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Startup hook for checking if there are available updates for Taverna plugins.
 * 
 */
public class CheckForUpdatesStartupHook implements StartupSPI, EventHandler {

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

	public static final String UPDATES = "updates";
	public static final String LAST_UPDATE_CHECK = "last_update_check";

	private PluginManager pluginManager;
	private ApplicationConfiguration applicationConfiguration;
	private Logger logger = Logger.getLogger(CheckForUpdatesStartupHook.class);

	public int positionHint() {
		return 90;
	}

	public boolean startup() {
		Path lastUpdateCheckFile = lastUpdateCheckFile();
		if (Files.exists(lastUpdateCheckFile)) {
			FileTime lastChecked;
			try {
				lastChecked = Files.getLastModifiedTime(lastUpdateCheckFile);
			} catch (IOException e) {
				// Should be able to check time of an existing file, some kind
				// of disk error?
				logger.error("Can't check file " + lastUpdateCheckFile, e);
				return false;
			}
			Instant twoWeeksAgo = Instant.now().minus(2, ChronoUnit.WEEKS);
			if (lastChecked.toInstant().isAfter(twoWeeksAgo)) {
				// No need to check yet
				return true;
			}
		}
		
		// last-check-file didn't exist, or it's more than two weeks ago
		
		try {
			pluginManager.checkForUpdates();
			// Content of file doesn't matter.. but we'll write
			// today's date even if we don't check the content of the
			// file later
			String message = Instant.now().toString();			
			Files.write(lastUpdateCheckFile, Arrays.asList(message), StandardCharsets.UTF_8);
		} catch (PluginException e) {
			logger.error("Can't check for updates", e);
			return false;
		} catch (IOException e) {
			logger.error("Can't write to file " + lastUpdateCheckFile, e);
			return false;
		}
		return true;
	}

	private Path lastUpdateCheckFile() {
		Path dir = applicationConfiguration.getApplicationHomeDir().resolve(UPDATES);
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			logger.error("Can't create directories" + dir, e);
			// We can't recover from this here, but this would cause another
			// error in the calling methods. It's still OK to return 
			// the non-existing path below:
		}
		return dir.resolve(LAST_UPDATE_CHECK);
	}


	@Override
	public void handleEvent(Event event) {
		// TODO: Handle Plug
		if (event.getTopic().equals(PluginManager.UPDATES_AVAILABLE)) {
			CheckForUpdatesDialog dialog = new CheckForUpdatesDialog(lastUpdateCheckFile());
			dialog.setVisible(true);
		}
	}
}
