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
 * A {@link MenuComponent} of the type
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#toolBar}.
 * <p>
 * Subclass to create an SPI implementation for the {@link MenuManager} of a
 * toolbar. A toolbar can contain {@link AbstractMenuAction actions},
 * {@link AbstractMenuToggle toggles} or
 * {@link AbstractMenuCustom custom components}, or any of the above grouped in
 * a {@link AbstractMenuSection section} or an
 * {@link AbstractMenuOptionGroup option group}.
 * </p>
 * <p>
 * The {@link DefaultToolBar default toolbar} can be used as a parent for items
 * that are to be returned in the toolbar {@link MenuManager#createToolBar()},
 * while toolbars from other instances of AbstractToolBar can be created using
 * {@link MenuManager#createToolBar(URI)} specifying the URI of the toolbar's
 * identifier.
 * </p>
 * <p>
 * Menu components are linked together using URIs, avoiding the need for compile
 * time dependencies between SPI implementations. To add components to a
 * toolbar, use the {@link URI} identifying this toolbar as their parent id.
 * </p>
 * <p>
 * <strong>Note:</strong>To avoid conflicts with other plugins, use a unique
 * URI root that is related to the Java package name, for instance
 * <code>http://cs.university.ac.uk/myplugin/2008/menu</code>, and use hash
 * identifiers for each menu item, for instance
 * <code>http://cs.university.ac.uk/myplugin/2008/menu#run</code> for a "Run"
 * item. Use flat URI namespaces, don't base a child's URI on the parent's URI,
 * as this might make it difficult to relocate the parent menu.
 * </p>
 * <p>
 * You need to list the {@link Class#getName() fully qualified class name} (for
 * example <code>com.example.t2plugin.menu.MyMenu</code>) of the toolbar
 * implementation in the SPI description resource file
 * <code>/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent</code> so
 * that it can be discovered by the {@link MenuManager}. This requirement also
 * applies to parent menu components (except {@link DefaultToolBar} and
 * {@link DefaultMenuBar}, but ensure they are only listed once.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractToolBar extends AbstractMenuItem {

	/**
	 * Construct a toolbar with the given {@link URI} as identifier.
	 * 
	 * @param id
	 *            The {@link URI} to identify this toolbar. Use this as the
	 *            parent ID for menu components to appear in this toolbar.
	 */
	public AbstractToolBar(URI id) {
		super(MenuType.toolBar, null, id);
	}

}
