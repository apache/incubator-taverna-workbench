/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
package net.sf.taverna.t2.ui.menu;

import java.net.URI;

/**
 * The default contextual menu, created using
 * {@link MenuManager#createContextMenu(Object, Object, java.awt.Component)()}.
 * <p>
 * Items that are part of a contextual menu should also implement
 * {@link ContextualMenuComponent}, the menu manager will then be able to tell
 * the items what is the current selection for the contextual menu, so that the
 * items can update their {@link MenuComponent#isEnabled()} (only visible for
 * some selections) and {@link MenuComponent#getAction()} (the action needs the
 * selected object).
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class DefaultContextualMenu extends AbstractMenu {

	/**
	 * The URI of a menu item representing the default menu bar. Menu items who
	 * has this URI as their {@link #getParentId()} will be shown in the top
	 * menu of the main application window.
	 */
	public static final URI DEFAULT_CONTEXT_MENU = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#defaultContextMenu");

	/**
	 * Construct the default menu bar
	 * 
	 */
	public DefaultContextualMenu() {
		super(DEFAULT_CONTEXT_MENU);
	}

}
