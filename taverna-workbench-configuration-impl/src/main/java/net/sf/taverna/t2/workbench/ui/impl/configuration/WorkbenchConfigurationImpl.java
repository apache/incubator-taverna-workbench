/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.ui.impl.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.AbstractConfigurable;
import uk.org.taverna.configuration.ConfigurationManager;
import uk.org.taverna.configuration.app.ApplicationConfiguration;

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
		File startupDir = applicationConfiguration.getStartupDir();
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
