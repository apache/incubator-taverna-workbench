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
package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.util.List;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;

/**
 * Defines a factory class that when associated with a selected object creates a
 * {@link ContextualView} for that selection.
 * <p>
 * This factory acts as an SPI to find {@link ContextualView}s for a given
 * Activity and other workflow components.
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * @author Stian Soiland-Reyes
 * 
 * 
 * @param <SelectionType>
 *            - the selection type this factory is associated with
 * 
 * @see ContextualView
 * @see ContextualViewFactoryRegistry
 */
public interface ContextualViewFactory<SelectionType> {

	/**
	 * @param selection
	 *            - the object for which ContextualViews needs to be generated
	 * @return instance of {@link ContextualView}
	 */
	public List<ContextualView> getViews(SelectionType selection);

	/**
	 * Used by the SPI system to find the correct factory that can handle the
	 * given object type. 
	 * 
	 * @param selection
	 * @return true if this factory relates to the given selection type
	 * @see ContextualViewFactoryRegistry
	 */
	public boolean canHandle(Object selection);

}
