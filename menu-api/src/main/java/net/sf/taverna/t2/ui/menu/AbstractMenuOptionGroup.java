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
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#optionGroup}.
 * <p>
 * Subclass to create an SPI implementation for the {@link MenuManager} of an
 * option group. An option group is similar to a
 * {@link AbstractMenuSection section}, but enforces that only one of the
 * children are selected at any time.
 * </p>
 * <p>
 * Menu components are linked together using URIs, avoiding the need for compile
 * time dependencies between SPI implementations. To add actions or toggles to
 * an option group, use the {@link URI} identifying this section as their parent
 * id.
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
 * example <code>com.example.t2plugin.menu.MyMenu</code>) of the option group
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
public abstract class AbstractMenuOptionGroup extends AbstractMenuItem {

	/**
	 * Construct an option group.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The parent
	 *            should be of type
	 *            {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#menu}
	 *            or
	 *            {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#toolBar}.
	 * @param positionHint
	 *            The position hint to determine the position of this option
	 *            group among its siblings in the parent menu. For
	 *            extensibility, use BASIC style numbering such as 10, 20, etc.
	 *            (Note that position hints are local to each parent, so each
	 *            option group have their own position hint scheme for their
	 *            children.)
	 * @param id
	 *            The {@link URI} to identify this option group. Use this as the
	 *            parent ID for menu components to appear in this option group.
	 */
	public AbstractMenuOptionGroup(URI parentId, int positionHint, URI id) {
		super(MenuType.optionGroup, parentId, id);
		this.positionHint = positionHint;
	}

}
