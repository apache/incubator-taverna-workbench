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

/**
 * @author Alan R Williams
 */
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;

/**
 * An SPI registry for discovering ActivityViewFactories for a given object,
 * like an {@link net.sf.taverna.t2.workflowmodel.processor.activity.Activity}.
 * <p>
 * For {@link ContextualViewFactory factories} to be found, its full qualified
 * name needs to be defined as a resource file
 * <code>/META-INF/services/net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualViewFactory</code>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * @author Stian Soiland-Reyes
 * 
 * @see ContextualViewFactory
 */
public class ContextualViewFactoryRegistryImpl implements
		ContextualViewFactoryRegistry {
	private List<ContextualViewFactory<?>> contextualViewFactories;

	/**
	 * Discover and return the ContextualViewFactory associated to the provided
	 * object. This is accomplished by returning the discovered
	 * {@link ContextualViewFactory#canHandle(Object)} that returns true for
	 * that Object.
	 * 
	 * @param object
	 * @return
	 * @see ContextualViewFactory#canHandle(Object)
	 */
	@Override
	public List<ContextualViewFactory<?>> getViewFactoriesForObject(
			Object object) {
		List<ContextualViewFactory<?>> result = new ArrayList<>();
		for (ContextualViewFactory<?> factory : contextualViewFactories)
			if (factory.canHandle(object))
				result.add(factory);
		return result;
	}

	public void setContextualViewFactories(
			List<ContextualViewFactory<?>> contextualViewFactories) {
		this.contextualViewFactories = contextualViewFactories;
	}
}
