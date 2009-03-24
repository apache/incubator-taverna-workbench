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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.taverna.raven.appconfig.ApplicationConfig;
import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * An implementation of Configurable for general Workbench configuration
 * properties
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 * 
 */
public class WorkbenchConfiguration extends AbstractConfigurable {

	private static Logger logger = Logger.getLogger(WorkbenchConfiguration.class);
	
	private static final int DEFAULT_MAX_MENU_ITEMS = 20;
	public static final String TAVERNA_DOTLOCATION = "taverna.dotlocation";
	public static final String MAX_MENU_ITEMS = "taverna.maxmenuitems";
	
	private static final String BIN = "bin";
	private static final String BUNDLE_CONTENTS = "Contents";
	private static final String BUNDLE_MAC_OS = "MacOS";
	private static final String DOT_EXE = "dot.exe";
	private static final String DOT_FALLBACK = "dot";

	public static String uuid = "c14856f0-5967-11dd-ae16-0800200c9a66";

	private static final String MAC_OS_X = "Mac OS X";
	private static final String WIN32I386 = "win32i386";
	private static final String WINDOWS = "Windows";

	private static class Singleton {
		public static WorkbenchConfiguration instance = new WorkbenchConfiguration();
	}

	public static WorkbenchConfiguration getInstance() {
		return Singleton.instance;
	}

	private static ApplicationConfig appConfig = ApplicationConfig
			.getInstance();

	Map<String, String> defaultWorkbenchProperties = null;
	Map<String, String> workbenchProperties = new HashMap<String, String>();

	public String getCategory() {
		return "general";
	}

	public Map<String, String> getDefaultPropertyMap() {
		if (defaultWorkbenchProperties == null) {
			defaultWorkbenchProperties = new HashMap<String, String>();
			String dotLocation = System.getProperty(TAVERNA_DOTLOCATION) != null ? System
					.getProperty(TAVERNA_DOTLOCATION)
					: getDefaultDotLocation();
			defaultWorkbenchProperties.put(TAVERNA_DOTLOCATION, dotLocation);
			defaultWorkbenchProperties.put(MAX_MENU_ITEMS, Integer.toString(DEFAULT_MAX_MENU_ITEMS));
		}
		return defaultWorkbenchProperties;
	}

	public String getName() {
		return "Workbench";
	}

	public String getUUID() {
		return uuid;
	}
	
	public int getMaxMenuItems() {
		String property = getProperty(MAX_MENU_ITEMS);
		try {
			int maxMenuItems = Integer.parseInt(property);
			if (maxMenuItems >= 2) {
				return maxMenuItems;
			} else {
				logger.warn(MAX_MENU_ITEMS + " can't be less than 2");
			}
		} catch (NumberFormatException ex) {
			logger.warn("Invalid number for " + MAX_MENU_ITEMS +": " + property);
		}
		// We'll return the default instead
		return DEFAULT_MAX_MENU_ITEMS;
	}

	private String getDefaultDotLocation() {
		File startupDir;
		try {
			startupDir = appConfig.getStartupDir();
		} catch (IOException e) {
			return DOT_FALLBACK;
		}
		String os = System.getProperty("os.name");
		if (os.equals(MAC_OS_X)) {
			if (startupDir.getParentFile() != null) {
				File contentsDir = startupDir.getParentFile().getParentFile();
				if (contentsDir != null
						&& contentsDir.getName().equalsIgnoreCase(
								BUNDLE_CONTENTS)) {
					File dot = new File(new File(contentsDir, BUNDLE_MAC_OS),
							DOT_FALLBACK);
					if (dot.exists()) {
						return dot.getAbsolutePath();
					}
				}
			}
		} else if (os.startsWith(WINDOWS)) {
			File binWin386Dir = new File(new File(startupDir, BIN), WIN32I386);
			File dot = new File(binWin386Dir, DOT_EXE);
			if (dot.exists()) {
				return dot.getAbsolutePath();
			}
		}
		return DOT_FALLBACK;
	}

}
