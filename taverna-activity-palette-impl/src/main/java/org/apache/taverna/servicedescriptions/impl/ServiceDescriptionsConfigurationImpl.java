/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.servicedescriptions.impl;

import java.util.HashMap;
import java.util.Map;
import org.apache.taverna.configuration.AbstractConfigurable;
import org.apache.taverna.configuration.ConfigurationManager;

import org.apache.taverna.servicedescriptions.ServiceDescriptionsConfiguration;

public class ServiceDescriptionsConfigurationImpl extends AbstractConfigurable
		implements ServiceDescriptionsConfiguration {
	private static final String INCLUDE_DEFAULTS = "includeDefaults";
	private static final String SERVICE_PALETTE = "Service providers";
	private static final String SERVICE_PALETTE_PREFIX = "ServiceProviders";
	private static final String CATEGORY = "Services";
	private static final String UUID = "f0d1ef24-9337-412f-b2c3-220a01e2efd0";
	private static final String REMOVE_PERMANENTLY = "removePermanently";

	public ServiceDescriptionsConfigurationImpl(
			ConfigurationManager configurationManager) {
		super(configurationManager);
	}

	@Override
	public String getCategory() {
		return CATEGORY;
	}

	@Override
	public Map<String, String> getDefaultPropertyMap() {
		Map<String, String> defaults = new HashMap<String, String>();
		defaults.put(INCLUDE_DEFAULTS, "true");
		defaults.put(REMOVE_PERMANENTLY, "true");
		return defaults;
	}

	@Override
	public String getDisplayName() {
		return SERVICE_PALETTE;
	}

	@Override
	public String getFilePrefix() {
		return SERVICE_PALETTE_PREFIX;
	}

	@Override
	public String getUUID() {
		return UUID;
	}

	@Override
	public boolean isIncludeDefaults() {
		return Boolean.parseBoolean(getProperty(INCLUDE_DEFAULTS));
	}

	@Override
	public void setIncludeDefaults(boolean includeDefaults) {
		setProperty(INCLUDE_DEFAULTS, Boolean.toString(includeDefaults));
	}

	@Override
	public boolean isRemovePermanently() {
		return Boolean.parseBoolean(getProperty(REMOVE_PERMANENTLY));
	}

	@Override
	public void setRemovePermanently(boolean removePermanently) {
		setProperty(REMOVE_PERMANENTLY, Boolean.toString(removePermanently));
	}
}
