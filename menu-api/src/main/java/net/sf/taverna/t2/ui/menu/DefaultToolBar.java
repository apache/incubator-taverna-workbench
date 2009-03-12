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
package net.sf.taverna.t2.ui.menu;

import java.net.URI;

/**
 * The default tool bar that will be shown by the main application window. Use
 * {@link #DEFAULT_TOOL_BAR} as the {@link #getParentId()} for items that should
 * appear in this toolbar. This toolbar can be created using
 * {@link MenuManager#createToolBar()}
 * <p>
 * Separate toolbars can be made by subclassing {@link AbstractToolBar} and
 * created by using {@link MenuManager#createToolBar(URI)}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class DefaultToolBar extends AbstractToolBar {

	/**
	 * The URI of a tool bar item representing the default tool bar. Items who
	 * has this URI as their {@link #getParentId()} will be shown in the default
	 * toolbar of the main application window.
	 */
	public static final URI DEFAULT_TOOL_BAR = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#defaultToolBar");

	/**
	 * Construct the default toolbar.
	 * 
	 */
	public DefaultToolBar() {
		super(DEFAULT_TOOL_BAR);
	}

}
