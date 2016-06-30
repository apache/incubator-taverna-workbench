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

package org.apache.taverna.workbench.ui.impl.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;

import org.apache.log4j.Logger;
import org.apache.taverna.configuration.AbstractConfigurable;
import org.apache.taverna.configuration.ConfigurationManager;
import org.apache.taverna.configuration.app.ApplicationConfiguration;

/**
 * An implementation of Configurable for general Workbench configuration
 * properties
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 */
public class WorkbenchConfigurationImpl extends AbstractConfigurable implements
		WorkbenchConfiguration {
	private static Logger logger = Logger
			.getLogger(WorkbenchConfiguration.class);
	private static final int DEFAULT_MAX_MENU_ITEMS = 20;
	public static final String TAVERNA_DOTLOCATION = "taverna.dotlocation";
	public static final String MAX_MENU_ITEMS = "taverna.maxmenuitems";
	public static final String WARN_INTERNAL_ERRORS = "taverna.warninternal";
	public static final String CAPTURE_CONSOLE = "taverna.captureconsole";
	private static final String BIN = "bin";
	private static final String BUNDLE_CONTENTS = "Contents";
	private static final String BUNDLE_MAC_OS = "MacOS";
	private static final String DOT_EXE = "dot.exe";
	private static final String DOT_FALLBACK = "dot";
	public static String uuid = "c14856f0-5967-11dd-ae16-0800200c9a66";
	private static final String MAC_OS_X = "Mac OS X";
	private static final String WIN32I386 = "win32i386";
	private static final String WINDOWS = "Windows";

	private ApplicationConfiguration applicationConfiguration;

	/**
	 * Constructs a new <code>WorkbenchConfigurationImpl</code>.
	 * 
	 * @param configurationManager
	 */
	public WorkbenchConfigurationImpl(ConfigurationManager configurationManager) {
		super(configurationManager);
	}

	Map<String, String> defaultWorkbenchProperties = null;
	Map<String, String> workbenchProperties = new HashMap<String, String>();

	@Override
	public String getCategory() {
		return "general";
	}

	@Override
	public Map<String, String> getDefaultPropertyMap() {
		if (defaultWorkbenchProperties == null) {
			defaultWorkbenchProperties = new HashMap<>();
			String dotLocation = System.getProperty(TAVERNA_DOTLOCATION) != null ? System
					.getProperty(TAVERNA_DOTLOCATION) : getDefaultDotLocation();
			if (dotLocation != null)
				defaultWorkbenchProperties
						.put(TAVERNA_DOTLOCATION, dotLocation);
			defaultWorkbenchProperties.put(MAX_MENU_ITEMS,
					Integer.toString(DEFAULT_MAX_MENU_ITEMS));
			defaultWorkbenchProperties.put(WARN_INTERNAL_ERRORS,
					Boolean.FALSE.toString());
			defaultWorkbenchProperties.put(CAPTURE_CONSOLE,
					Boolean.TRUE.toString());
		}
		return defaultWorkbenchProperties;
	}

	@Override
	public String getDisplayName() {
		return "Workbench";
	}

	@Override
	public String getFilePrefix() {
		return "Workbench";
	}

	@Override
	public String getUUID() {
		return uuid;
	}

	@Override
	public boolean getWarnInternalErrors() {
		String property = getProperty(WARN_INTERNAL_ERRORS);
		return Boolean.parseBoolean(property);
	}

	@Override
	public boolean getCaptureConsole() {
		String property = getProperty(CAPTURE_CONSOLE);
		return Boolean.parseBoolean(property);
	}

	@Override
	public void setWarnInternalErrors(boolean warnInternalErrors) {
		setProperty(WARN_INTERNAL_ERRORS, Boolean.toString(warnInternalErrors));
	}

	@Override
	public void setCaptureConsole(boolean captureConsole) {
		setProperty(CAPTURE_CONSOLE, Boolean.toString(captureConsole));
	}

	@Override
	public void setMaxMenuItems(int maxMenuItems) {
		if (maxMenuItems < 2)
			throw new IllegalArgumentException(
					"Maximum menu items must be at least 2");
		setProperty(MAX_MENU_ITEMS, Integer.toString(maxMenuItems));
	}

	@Override
	public int getMaxMenuItems() {
		String property = getProperty(MAX_MENU_ITEMS);
		try {
			int maxMenuItems = Integer.parseInt(property);
			if (maxMenuItems >= 2)
				return maxMenuItems;
			logger.warn(MAX_MENU_ITEMS + " can't be less than 2");
		} catch (NumberFormatException ex) {
			logger.warn("Invalid number for " + MAX_MENU_ITEMS + ": "
					+ property);
		}
		// We'll return the default instead
		return DEFAULT_MAX_MENU_ITEMS;
	}

	@Override
	public String getDotLocation() {
		return getProperty(TAVERNA_DOTLOCATION);
	}

	@Override
	public void setDotLocation(String dotLocation) {
		setProperty(TAVERNA_DOTLOCATION, dotLocation);
	}

	private String getDefaultDotLocation() {
		if (applicationConfiguration == null)
			return null;
		File startupDir = applicationConfiguration.getStartupDir().toFile();
		if (startupDir == null)
			return DOT_FALLBACK;

		String os = System.getProperty("os.name");
		if (os.equals(MAC_OS_X))
			if (startupDir.getParentFile() != null) {
				File contentsDir = startupDir.getParentFile().getParentFile();
				if (contentsDir != null
						&& contentsDir.getName().equalsIgnoreCase(
								BUNDLE_CONTENTS)) {
					File dot = new File(new File(contentsDir, BUNDLE_MAC_OS),
							DOT_FALLBACK);
					if (dot.exists())
						return dot.getAbsolutePath();
				}
			} else if (os.startsWith(WINDOWS)) {
				File binWin386Dir = new File(new File(startupDir, BIN),
						WIN32I386);
				File dot = new File(binWin386Dir, DOT_EXE);
				if (dot.exists())
					return dot.getAbsolutePath();
			}
		return DOT_FALLBACK;
	}

	/**
	 * Sets the applicationConfiguration.
	 * 
	 * @param applicationConfiguration
	 *            the new value of applicationConfiguration
	 */
	public void setApplicationConfiguration(
			ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
		defaultWorkbenchProperties = null;
	}
}
