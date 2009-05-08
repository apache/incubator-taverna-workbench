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
package net.sf.taverna.t2.workbench.provenance;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

public class ProvenanceConfiguration extends AbstractConfigurable {
	
	public static String uuid = "879A9816-ADA8-4FDD-919A-6E8913DF37C1";
	Map<String, String> defaultProvenanceProperties = null;
	Map<String, String> provenanceProperties = new HashMap<String, String>();
	
	public static ProvenanceConfiguration getInstance() {
		return Singleton.instance;
	}

	private static class Singleton {
		public static ProvenanceConfiguration instance = new ProvenanceConfiguration();
	}

	public String getCategory() {
		return "provenance";
	}

	public Map<String, String> getDefaultPropertyMap() {
		if (defaultProvenanceProperties == null) {
			defaultProvenanceProperties = new HashMap<String,String>();
		}
		defaultProvenanceProperties.put("enabled", "yes");
		defaultProvenanceProperties.put("storage", "local");
		defaultProvenanceProperties.put("database", "derby");
		defaultProvenanceProperties.put("create", "true");
		defaultProvenanceProperties.put("delete", "false");
		defaultProvenanceProperties.put("connector", "Derby DB Connector");
		return defaultProvenanceProperties;
	}

	public String getName() {
		return "Provenance";
	}

	public String getUUID() {
		return uuid;
	}

}
