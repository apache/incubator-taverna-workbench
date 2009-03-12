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
package net.sf.taverna.t2.partition;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * A singleton SPI registry class for extracting properties for target "Objects".
 * <p>
 * Through an SPI identified by classes defined in a file
 * </p>
 * <pre>
 * META-INF/services/net.sf.taverna.t2.partition.PropertyExtractorSPI
 * </pre>
 * PropertyExtractorSPIs are discovered, and then for each it then passes the target Object to retrieve
 * a property map for that Object.
 * <p>
 * If the PropertyExtractorSPI is not relevant to that Object type, the Extractor simply responds with an empty Map.
 * </p> 
 * @author Stuart Owen
 * @see PropertyExtractorSPI
 * 
 *
 */
public class PropertyExtractorSPIRegistry extends SPIRegistry<PropertyExtractorSPI> implements
		PropertyExtractorRegistry {
	
	private static PropertyExtractorSPIRegistry instance = new PropertyExtractorSPIRegistry();

	public static PropertyExtractorSPIRegistry getInstance() {
		return instance;
	}
	private PropertyExtractorSPIRegistry() {
		super(PropertyExtractorSPI.class);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.partition.PropertyExtractorRegistry#getAllPropertiesFor(java.lang.Object)
	 */
	public Map<String, Object> getAllPropertiesFor(Object target) {
		Map<String,Object> result = new HashMap<String, Object>();
		for (PropertyExtractorSPI extractor : getInstances()) {
			result.putAll(extractor.extractProperties(target));
		}
		return result;
	}

}
