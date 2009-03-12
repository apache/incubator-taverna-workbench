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
package net.sf.taverna.t2.workbench.ui.zaria;

import javax.swing.ImageIcon;

/**
 * Implementations can construct UI components to be inserted into containers
 * within the workbench. These components may or may not bind to any given model
 * object, the previous approach of forcing all UI components to be model
 * listeners wasn't really very bright.
 * <p>
 * This class is intended to allow minimal information for building menus and
 * the like without having to construct potentially heavy swing objects every
 * time.
 * 
 * @author Tom Oinn
 */
public interface UIComponentFactorySPI {

	/**
	 * Get the preferred name of this component, for titles in windows etc.
	 */
	public String getName();

	/**
	 * Get an icon to be used in window decorations for this component.
	 */
	public ImageIcon getIcon();

	/**
	 * Construct a JComponent from this factory, cast as a UIComponent but must
	 * also implement JComponent (if anyone knows how to define this sensibly
	 * I'm all ears...)
	 */
	public UIComponentSPI getComponent();

}
