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
package net.sf.taverna.t2.workbench.views.results.saveactions;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

/**
 * An SPI registry for discovering SaveResultActionSPIs.
 * <p>
 * For {@link SaveIndividualResultSPI} to be found, its full qualified
 * name needs to be defined as a resource file
 * <code>/META-INF/services/net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI</code>
 * </p>
 * 
 * @author Alex Nenadic
 *  
 */
@SuppressWarnings("unchecked")
public class SaveIndividualResultSPIRegistry extends SPIRegistry<SaveIndividualResultSPI>{

	protected SaveIndividualResultSPIRegistry() {
		super(SaveIndividualResultSPI.class);
	}

	/**
	 * Get the singleton instance of this registry.
	 * 
	 * @return The SaveIndividualResultSPIRegistry singleton
	 */
	public static synchronized SaveIndividualResultSPIRegistry getInstance() {
		return Singleton.instance;
	}
	
	private static class Singleton {
		private static final SaveIndividualResultSPIRegistry instance = new SaveIndividualResultSPIRegistry();
	}
	
	/**
	 * Returns a list containing all instances of the SaveIndividualResultSPI.
	 */
	public List<SaveIndividualResultSPI> getSaveResultActions() {
		List<SaveIndividualResultSPI> result = new ArrayList<SaveIndividualResultSPI>();
		for (SaveIndividualResultSPI spi : getInstances()) {
			result.add(spi);
		}
		return result;
	}
}
