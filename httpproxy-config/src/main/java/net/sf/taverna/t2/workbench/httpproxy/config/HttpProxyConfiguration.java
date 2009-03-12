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
package net.sf.taverna.t2.workbench.httpproxy.config;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.axis.AxisProperties;
import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;
import net.sf.taverna.raven.launcher.LauncherHttpProxyConfiguration;

/**
 * Configuration for the HTTP Proxy.
 * 
 * @author Alan R Williams
 */
public class HttpProxyConfiguration extends AbstractConfigurable {

	private static Logger logger = Logger
			.getLogger(HttpProxyConfiguration.class);

	/**
	 * Only present to please Configurable
	 */
	private Map<String, String> defaultPropertyMap;

	/**
	 * The instance of the HttpProxyConfiguration. In theory Taverna could be
	 * extended to use multiple instances.
	 */
	private static HttpProxyConfiguration instance;

	/**
	 * String constants inherited from LauncherHttpProxyConfiguration. This just
	 * simplified the code somewhat.
	 */
	public static String USE_SYSTEM_PROPERTIES_OPTION = LauncherHttpProxyConfiguration.USE_SYSTEM_PROPERTIES_OPTION;
	public static String USE_NO_PROXY_OPTION = LauncherHttpProxyConfiguration.USE_NO_PROXY_OPTION;
	public static String USE_SPECIFIED_VALUES_OPTION = LauncherHttpProxyConfiguration.USE_SPECIFIED_VALUES_OPTION;

	public static String PROXY_USE_OPTION = LauncherHttpProxyConfiguration.PROXY_USE_OPTION;

	public static String TAVERNA_PROXY_HOST = LauncherHttpProxyConfiguration.TAVERNA_PROXY_HOST;
	public static String TAVERNA_PROXY_PORT = LauncherHttpProxyConfiguration.TAVERNA_PROXY_PORT;
	public static String TAVERNA_PROXY_USER = LauncherHttpProxyConfiguration.TAVERNA_PROXY_USER;
	public static String TAVERNA_PROXY_PASSWORD = LauncherHttpProxyConfiguration.TAVERNA_PROXY_PASSWORD;
	public static String TAVERNA_NON_PROXY_HOSTS = LauncherHttpProxyConfiguration.TAVERNA_NON_PROXY_HOSTS;

	public static String SYSTEM_PROXY_HOST = LauncherHttpProxyConfiguration.SYSTEM_PROXY_HOST;
	public static String SYSTEM_PROXY_PORT = LauncherHttpProxyConfiguration.SYSTEM_PROXY_PORT;
	public static String SYSTEM_PROXY_USER = LauncherHttpProxyConfiguration.SYSTEM_PROXY_USER;
	public static String SYSTEM_PROXY_PASSWORD = LauncherHttpProxyConfiguration.SYSTEM_PROXY_PASSWORD;
	public static String SYSTEM_NON_PROXY_HOSTS = LauncherHttpProxyConfiguration.SYSTEM_NON_PROXY_HOSTS;

	public static String PROXY_HOST = LauncherHttpProxyConfiguration.PROXY_HOST;
	public static String PROXY_PORT = LauncherHttpProxyConfiguration.PROXY_PORT;
	public static String PROXY_USER = LauncherHttpProxyConfiguration.PROXY_USER;
	public static String PROXY_PASSWORD = LauncherHttpProxyConfiguration.PROXY_PASSWORD;
	public static String NON_PROXY_HOSTS = LauncherHttpProxyConfiguration.NON_PROXY_HOSTS;

	/**
	 * Returns the singleton instance of HttpProxyConfiguration
	 * 
	 * @return
	 */
	public static HttpProxyConfiguration getInstance() {
		if (instance == null) {
			instance = new HttpProxyConfiguration();
		}
		return instance;
	}

	/**
	 * Construct a new HttpProxyConfiguration and copy the settings from the
	 * LauncherHttpProxyConfiguration
	 */
	private HttpProxyConfiguration() {
		super();
		LauncherHttpProxyConfiguration c = LauncherHttpProxyConfiguration
				.getInstance();
		setProperty(SYSTEM_PROXY_HOST, c.getOriginalSystemSetting(PROXY_HOST));
		setProperty(SYSTEM_PROXY_PORT, c.getOriginalSystemSetting(PROXY_PORT));
		setProperty(SYSTEM_PROXY_USER, c.getOriginalSystemSetting(PROXY_USER));
		setProperty(SYSTEM_PROXY_PASSWORD, c
				.getOriginalSystemSetting(PROXY_PASSWORD));
		setProperty(SYSTEM_NON_PROXY_HOSTS, c
				.getOriginalSystemSetting(NON_PROXY_HOSTS));

		/*
		 * Default to using the System properties if nothing else has been
		 * specified
		 */
		if (getProperty(PROXY_USE_OPTION) == null) {
			setProperty(PROXY_USE_OPTION, USE_SYSTEM_PROPERTIES_OPTION);
		}
		/*
		 * Change the proxy setting according to what the configuration file
		 * (read in by LauncherHttpProxyConfiguration has specified
		 */
		changeProxySettings();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getCategory()
	 */
	public String getCategory() {
		return "general";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.sf.taverna.t2.workbench.configuration.Configurable#getDefaultPropertyMap
	 * ()
	 */
	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<String, String>();
		}
		return defaultPropertyMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getName()
	 */
	public String getName() {
		return LauncherHttpProxyConfiguration.getName();
	}

	/**
	 * Change the java System property specified by the key to the given value.
	 * If the value is null then clear the property. Also sets the corresponding
	 * AxisPropert.
	 * 
	 * @param key
	 * @param value
	 */
	private void changeSystemProperty(String key, String value) {
		if ((value == null) || value.equals("")) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
		AxisProperties.setProperty(key, (value == null ? "" : value));
	}

	/**
	 * Change the HTTP proxy settings via the LauncherHttpProxyConfiguration.
	 */
	public void changeProxySettings() {
		Properties props = new Properties();
		for (String k : this.getKeys()) {
			props.setProperty(k, this.getProperty(k));
		}
		LauncherHttpProxyConfiguration.getInstance().changeProxySettings(props);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getUUID()
	 */
	public String getUUID() {
		return LauncherHttpProxyConfiguration.getUUID();
	}
}
