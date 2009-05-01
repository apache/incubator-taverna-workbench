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
package net.sf.taverna.t2.workbench.reference.config;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * Configuration for the reference service.
 * 
 * @author David Withers
 */
public class ReferenceConfiguration extends AbstractConfigurable {

	public static final String REFERENCE_SERVICE_CONTEXT = "referenceService.context";

	public static final String IN_MEMORY_CONTEXT = "inMemoryReferenceServiceContext.xml";

	public static final String HIBERNATE_CONTEXT = "hibernateReferenceServiceContext.xml";

	public static final String HIBERNATE_CACHE_CONTEXT = "hibernateCacheReferenceServiceContext.xml";

	private Map<String, String> defaultPropertyMap;

	private static ReferenceConfiguration instance;
	
	public static ReferenceConfiguration getInstance() {
		if (instance == null) {
			instance = new ReferenceConfiguration();
		}
		return instance;
	}

	private ReferenceConfiguration() {
	}

	public String getCategory() {
		return "general";
	}

	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<String, String>();
			defaultPropertyMap.put(REFERENCE_SERVICE_CONTEXT,
					HIBERNATE_CACHE_CONTEXT);
		}
		return defaultPropertyMap;
	}

	public String getName() {
		return "Data Storage";
	}

	public String getUUID() {
		return "6BD3F5C1-C68D-4893-8D9B-2F46FA1DDB19";
	}

}
