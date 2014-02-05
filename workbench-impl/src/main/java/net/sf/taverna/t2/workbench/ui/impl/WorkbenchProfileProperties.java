/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public final class WorkbenchProfileProperties extends Properties {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7222929323275707972L;

	private static final String WORKBENCH_PROFILE_PROPERTIES = "workbench-profile.properties";
	
	private static Logger logger = Logger.getLogger(WorkbenchProfileProperties.class);


	
	private static WorkbenchProfileProperties INSTANCE = null;

	public static synchronized WorkbenchProfileProperties getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new WorkbenchProfileProperties();
		}
		return INSTANCE;
	}
	
	private WorkbenchProfileProperties() {
		super();
		
		InputStream inStream = null;
		try {
			String startup = System.getProperty("taverna.startup");
		File startupDir = new File(startup);
		File confDir = new File(startupDir, "conf");
		File workbenchProfilePropertiesFile = new File(confDir, WORKBENCH_PROFILE_PROPERTIES);
		inStream = new FileInputStream(workbenchProfilePropertiesFile);
		
		this.load(inStream);
		}
		catch (IOException e) {
			logger.error("Unable to read workbench profile properties file", e);
		}
		catch (NullPointerException e) {
			logger.error("Unable to read workbench profile properties file", e);
		}
		finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					logger.error("Unable to close workbench profile properties file",e);
				}
			}
		}
	}
	
	public static String getWorkbenchProfileProperty(String key,
			String fallback) {
		String result = getInstance().getProperty(key, fallback);
		return result;
	}
	
	public static int getWorkbenchProfileIntegerProperty(String key, int fallback) {
		try {
			return Integer.parseInt(getWorkbenchProfileProperty(key, Integer.toString(fallback)));
		} catch (NumberFormatException e) {
			logger.error(e);
			return fallback;
		}
	}

}
