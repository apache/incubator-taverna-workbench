package net.sf.taverna.t2.servicedescriptions.impl;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

public class ServiceDescriptionsConfig extends AbstractConfigurable {

	private static final String INCLUDE_DEFAULTS = "includeDefaults";

	private static class Singleton {
		private static ServiceDescriptionsConfig instance = new ServiceDescriptionsConfig();
	}

	public static ServiceDescriptionsConfig getInstance() {
		return Singleton.instance;
	}

	private static final String SERVICE_PALETTE = "Service panel";
	private static final String CATEGORY = "Services";
	private static final String UUID = "f0d1ef24-9337-412f-b2c3-220a01e2efd0";
	private static final String REMOVE_PERMANENTLY = "removePermanently";

	public String getCategory() {
		return CATEGORY;
	}

	public Map<String, String> getDefaultPropertyMap() {
		Map<String, String> defaults = new HashMap<String, String>();
		defaults.put(INCLUDE_DEFAULTS, "true");
		defaults.put(REMOVE_PERMANENTLY, "true");
		return defaults;
	}

	public String getName() {
		return SERVICE_PALETTE;
	}

	public String getUUID() {
		return UUID;
	}

	public boolean isIncludeDefaults() {
		return Boolean.parseBoolean(getProperty(INCLUDE_DEFAULTS));
	}

	public void setIncludeDefaults(boolean includeDefaults) {
		setProperty(INCLUDE_DEFAULTS, Boolean.toString(includeDefaults));
	}

	public boolean isRemovePermanently() {
		return Boolean.parseBoolean(getProperty(REMOVE_PERMANENTLY));
	}

	public void setRemovePermanently(boolean removePermanently) {
		setProperty(REMOVE_PERMANENTLY, Boolean.toString(removePermanently));
	}

}
