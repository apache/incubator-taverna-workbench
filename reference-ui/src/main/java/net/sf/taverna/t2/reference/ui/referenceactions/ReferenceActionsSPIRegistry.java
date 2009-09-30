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
package net.sf.taverna.t2.reference.ui.referenceactions;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * An SPI registry for discovering ReferenceActionSPIs
 * 
 * @author Alan R Williams
 *  
 */
@SuppressWarnings("unchecked")
public class ReferenceActionsSPIRegistry extends SPIRegistry<ReferenceActionSPI>{

	protected ReferenceActionsSPIRegistry() {
		super(ReferenceActionSPI.class);
	}

	/**
	 * Get the singleton instance of this registry.
	 */
	public static synchronized ReferenceActionsSPIRegistry getInstance() {
		return Singleton.instance;
	}
	
	private static class Singleton {
		private static final ReferenceActionsSPIRegistry instance = new ReferenceActionsSPIRegistry();
	}
	
	/**
	 * Returns a list containing all instances of the ReferenceActionSPI.
	 */
	public List<ReferenceActionSPI> getLoadInputsActions() {
		List<ReferenceActionSPI> result = new ArrayList<ReferenceActionSPI>();
		for (ReferenceActionSPI spi : getInstances()) {
			result.add(spi);
		}
		return result;
	}
}
