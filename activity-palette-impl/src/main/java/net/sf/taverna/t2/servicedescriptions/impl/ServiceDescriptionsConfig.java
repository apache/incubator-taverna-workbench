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

	private static final String SERVICE_PALETTE = "Service providers";
	private static final String SERVICE_PALETTE_PREFIX = "ServiceProviders";
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

	public String getDisplayName() {
		return SERVICE_PALETTE;
	}
	
	public String getFilePrefix() {
		return SERVICE_PALETTE_PREFIX;
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
