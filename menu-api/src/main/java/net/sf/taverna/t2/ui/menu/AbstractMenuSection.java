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

import java.awt.Color;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * A {@link MenuComponent} of the type
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#section}.
 * <p>
 * Subclass to create an SPI implementation for the {@link MenuManager} of a
 * section. A section is a part of an {@link AbstractMenu menu} or
 * {@link AbstractToolBar toolbar} that group together
 * {@link AbstractMenuAction actions} or {@link AbstractMenuToggle toggles}, and
 * separates them from siblings using separators if needed.
 * </p>
 * <p>
 * Menu components are linked together using URIs, avoiding the need for compile
 * time dependencies between SPI implementations. To add actions to a section,
 * use the {@link URI} identifying this section as their parent id.
 * </p>
 * <p>
 * <strong>Note:</strong>To avoid conflicts with other plugins, use a unique URI
 * root that is related to the Java package name, for instance
 * <code>http://cs.university.ac.uk/myplugin/2008/menu</code>, and use hash
 * identifiers for each menu item, for instance
 * <code>http://cs.university.ac.uk/myplugin/2008/menu#run</code> for a "Run"
 * item. Use flat URI namespaces, don't base a child's URI on the parent's URI,
 * as this might make it difficult to relocate the parent menu.
 * </p>
 * <p>
 * You need to list the {@link Class#getName() fully qualified class name} (for
 * example <code>com.example.t2plugin.menu.MyMenu</code>) of the section
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
public abstract class AbstractMenuSection extends AbstractMenuItem {

	public static final String SECTION_COLOR = "sectionColor";
	
	/**
	 * Construct a menu section.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The parent
	 *            should be of type
	 *            {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#menu}
	 *            or
	 *            {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#toolBar}
	 *            .
	 * @param positionHint
	 *            The position hint to determine the position of this section
	 *            among its siblings in the parent menu. For extensibility, use
	 *            BASIC style numbering such as 10, 20, etc. (Note that position
	 *            hints are local to each parent, so each section have their own
	 *            position hint scheme for their children.)
	 * @param id
	 *            The {@link URI} to identify this menu section. Use this as the
	 *            parent ID for menu components to appear in this section.
	 */
	public AbstractMenuSection(URI parentId, int positionHint, URI id) {
		super(MenuType.section, parentId, id);
		this.positionHint = positionHint;
	}

	@Override
	public synchronized Action getAction() {
		if (action == null) {
			action = createAction();
		}
		return action;
	}

	/**
	 * (Optionally) create the {@link Action} that labels this section.
	 * <p>
	 * The actual action will be ignored, but the label and/or icon will be used
	 * as a section header in the menu.
	 * If the property {@link #SECTION_COLOR} has been defined in the action, that
	 * {@link Color} will be used to make the section background.
	 * <p>
	 * The default implementation of this method returns <code>null</code>,
	 * meaning that no section header will be created - instead a simple line
	 * will separate this section from the items above (if needed).
	 * </p>
	 * <p>
	 * Implementations might use {@link AbstractAction} as a superclass for menu
	 * actions.
	 * </p>
	 * 
	 * @return A configured {@link Action} that should at least have a label or
	 *         icon.
	 */
	protected Action createAction() {
		return null;
	}

}
