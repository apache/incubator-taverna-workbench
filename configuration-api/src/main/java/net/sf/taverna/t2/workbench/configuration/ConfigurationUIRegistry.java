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
package net.sf.taverna.t2.workbench.configuration;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

public class ConfigurationUIRegistry extends SPIRegistry<ConfigurationUIFactory>{

	private static ConfigurationUIRegistry instance = new ConfigurationUIRegistry();

	public static ConfigurationUIRegistry getInstance() {
		return instance;
	}
	
	private ConfigurationUIRegistry() {
		super(ConfigurationUIFactory.class);
	}
	
	public List<ConfigurationUIFactory> getConfigurationUIFactoriesForConfigurable(Configurable configurable) {
		List<ConfigurationUIFactory> result = new ArrayList<ConfigurationUIFactory>();
		for (ConfigurationUIFactory factory : getConfigurationUIFactories()) {
			if (factory.canHandle(configurable.getUUID())) {
				result.add(factory);
			}
		}
		return result;
	}
	
	public List<ConfigurationUIFactory> getConfigurationUIFactories() {
		return getInstances();
	}
}
