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

import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.ui.activitypalette.ActivityPaletteConfiguration;

public class QueryFactoryRegistry extends SPIRegistry<QueryFactory> {
	
	private static QueryFactoryRegistry instance = new QueryFactoryRegistry();

	public static QueryFactoryRegistry getInstance() {
		return instance;
	}
	
	private QueryFactoryRegistry() {
		super(QueryFactory.class);
	}
	
	@Override
	public List<QueryFactory> getInstances() {
		List<QueryFactory> instances = super.getInstances();
		for (QueryFactory fac : instances) {
			if (fac instanceof ActivityQueryFactory) {
				((ActivityQueryFactory)fac).setConfigurable(getConfigurable());
			}
		}
		return instances;
	}

	public List<Query<?>> getQueries() {
		List<QueryFactory> instances = getInstances();
		List<Query<?>> result = new ArrayList<Query<?>>();
		for (QueryFactory fac : instances) {
			result.addAll(fac.getQueries());
		}
		return result;
	}
	
	protected Configurable getConfigurable() {
		return ActivityPaletteConfiguration.getInstance();
	}
}
