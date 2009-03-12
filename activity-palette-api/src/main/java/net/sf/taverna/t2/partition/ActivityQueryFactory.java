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

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.configuration.Configurable;

import org.apache.log4j.Logger;

/**
 * A query factory specialised for use with Activities. 
 * <p>
 * The getPropertyKey and createQuery need to be implemented to make this class concrete.
 * <br>
 * When discovered as an SPI, factories of this type will have the Configurable set on it, and this configurable
 * will provide a list of property values to relate to the propertyKey.
 * <br>
 * For each property defined in the configuration a Query will be requested to be constructed with a call to createQuery.
 * </p>
 * 
 * @author Stuart Owen
 * 
 * @see QueryFactory
 * @see QueryFactoryRegistry
 * @see Query
 * @see ActivityQuery
 *
 */
public abstract class ActivityQueryFactory implements QueryFactory {
	
	private static Logger logger = Logger.getLogger(ActivityQueryFactory.class);
	
	public List<Query<?>> getQueries() {
		List<Query<?>> result = new ArrayList<Query<?>>();
		if (getPropertyKey()==null) { //no property required
			result.add(createQuery(null));
		}
		else {
			List<String> properties = config.getPropertyStringList(getPropertyKey());
			
			if (properties!=null) {
				for (String property : properties) {
					result.add(createQuery(property));
				}
			}
		}
		return result;
	}

	protected Configurable config;

	/**
	 * The implementation of this method will return the key for the property values held in the configuration object passed to setConfigurable.
	 * <br>
	 * If the implementation doesnot require a property to do a query, then this method should return null
	 * @return
	 * @see Configurable 
	 */
	protected abstract String getPropertyKey();
	
	/**
	 * The implementation of this method should construct an ActivityQuery using the property provided.
	 * 
	 * @param property
	 * @return
	 * @see ActivityQuery
	 */
	protected abstract ActivityQuery createQuery(String property);
 
	public boolean hasAddQueryActionHandler() {
		return false;
	}
	
	public AddQueryActionHandler getAddQueryActionHandler() {
		if (hasAddQueryActionHandler()) {
			logger.warn("ActivityQueryFactory.getAddQueryActionHandler needs to return an action handler if hasAddQueryActionHandler indicates one is supported.");
		}
		return null;
	}
	/**
	 * @param config - the Configurable object that holds the values for the propertyKey
	 */
	public void setConfigurable(Configurable config) {
		this.config=config;
	}
	
	Configurable getConfigurable() {
		return this.config;
	}
	
	

}
