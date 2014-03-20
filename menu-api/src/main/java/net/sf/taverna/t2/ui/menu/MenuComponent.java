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

import java.awt.Component;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.MenuElement;

/**
 * A menu component, including sub menus, toolbars, and menu items.
 * <p>
 * This is an {@link net.sf.taverna.t2.spi.SPIRegistry SPI}, and
 * implementations should list their fully qualified classnames in
 * META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent to be discovered by
 * the {@link MenuManager}.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * @author David Withers
 * 
 */
public interface MenuComponent {

	/**
	 * The {@link Action} describing this menu item, used for creating the UI
	 * representation of this item.
	 * <p>
	 * As a minimum the action should contain a name, and optionally an icon, a
	 * description and a keyboard shortcut. For {@link MenuType#action} and
	 * {@link MenuType#toggle} the Action's
	 * {@link ActionListener#actionPerformed(java.awt.event.ActionEvent)} method
	 * is called when the item is clicked/selected.
	 * 
	 * </p>
	 * <p>
	 * This action is ignored and should be <code>null</code> for items of type
	 * {@link MenuType#optionGroup} and {@link MenuType#custom}. The action is
	 * optional for {@link MenuType#toolBar} and {@link MenuType#section}, where
	 * the action's name would be used as a label.
	 * </p>
	 * 
	 * @return The {@link Action} describing this menu item, or
	 *         <code>null</code> if the {@link #getType()} is
	 *         {@link MenuType#section}, {@link MenuType#optionGroup} or
	 *         {@link MenuType#custom}.
	 */
	public Action getAction();

	/**
	 * Get a custom {@link Component} to be inserted into the parent
	 * menu/toolbar.
	 * <p>
	 * Used instead of creating menu elements from the {@link #getAction()} if
	 * the {@link #getType()} is {@link MenuType#custom}. This can be used to
	 * include dynamic menus.
	 * </p>
	 * <p>
	 * This value is ignored and should be <code>null</code> for all other
	 * types except {@link MenuType#custom}.
	 * </p>
	 * 
	 * @return A {@link Component} to be inserted into the parent menu/toolbar.
	 */
	public Component getCustomComponent();

	/**
	 * The {@link URI} to identify this menu item.
	 * <p>
	 * This identifier can be used with other menu item's {@link #getParentId()}
	 * if this item has a {@link #getType()} of {@link MenuType#menu},
	 * {@link MenuType#toolBar}, {@link MenuType#section} or
	 * {@link MenuType#optionGroup}.
	 * </p>
	 * <p>
	 * Leaf menu items of {@link #getType()} {@link MenuType#toggle},
	 * {@link MenuType#custom} and {@link MenuType#action} don't need an
	 * identifier as they can't have children, and may return <code>null</code>
	 * instead. However, a valid identifier might be used to look up the
	 * MenuItem with {@link MenuManager#getComponentByURI(URI)}
	 * </p>
	 * <p>
	 * <strong>Note:</strong>To avoid conflicts with other plugins, use a
	 * unique URI root that is related to the Java package name, for instance
	 * <code>http://cs.university.ac.uk/myplugin/2008/menu</code>, and use
	 * hash identifiers for each menu item, for instance
	 * <code>http://cs.university.ac.uk/myplugin/2008/menu#run</code> for a
	 * "Run" item. Use flat URI namespaces, don't base a child's URI on the
	 * parent's URI, as this might make it difficult to relocate the parent
	 * menu.
	 * </p>
	 * 
	 * @return The {@link URI} to identify this menu item.
	 */
	public URI getId();

	/**
	 * The {@link URI} of the parent menu item, as returned by the parent's
	 * {@link #getId()}.
	 * <p>
	 * If this is the {@link DefaultMenuBar#DEFAULT_MENU_BAR}, then this menu
	 * item will be one of the top level menus of the main application window,
	 * like "File" or "Edit", and must have {@link #getType()}
	 * {@link MenuType#menu}.
	 * </p>
	 * <p>
	 * This value should be <code>null</code> if this item is of
	 * {@link #getType()} {@link MenuType#toolBar}, and could be
	 * <code>null</code> if this is an independent root menu of type
	 * {@link MenuType#menu} (to be used outside the main window).
	 * </p>
	 * <strong>Note:</strong> To avoid compile time and runtime dependency on
	 * the parent menu item, always construct this URI directly using
	 * {@link URI#create(String)}.
	 * 
	 * @return The {@link URI} of the parent menu item.
	 */
	public URI getParentId();

	/**
	 * A hint on how to position this item below the parent.
	 * <p>
	 * Menu items within the same parent menu/group/toolBar are ordered
	 * according to this position hint. If several items have the same position
	 * hint, their internal order is undefined, although generally it will be
	 * the order in which they were loaded.
	 * </p>
	 * <p>
	 * <strong>Tip:</strong> Number the position hints in BASIC style, such as
	 * 10, 20, etc. so that plugins can use position hint such as 19 or 21 to be
	 * immediately before or after your item.
	 * 
	 * @return A position hint
	 */
	public int getPositionHint();

	/**
	 * The {@link MenuType type} of menu item.
	 * <p>
	 * In the simple case of a "File -> New" menu structure, the "File" menu
	 * item has a type of {@link MenuType#menu}, while the "New" has a type of
	 * {@link MenuType#action}.
	 * </p>
	 * <p>
	 * The menu item can only have children (ie. items with
	 * {@link #getParentId()} equalling to this item's {@link #getId()}) if the
	 * type is not a leaf type, ie. not {@link MenuType#toggle} or
	 * {@link MenuType#action}.
	 * </p>
	 * 
	 * @return A {@link MenuType} to specify the role of this menu item.
	 */
	public MenuType getType();

	/**
	 * True if this menu component is to be included in the menu/toolbar.
	 * 
	 * @return True is this menu component is to be included
	 */
	public boolean isEnabled();

	/**
	 * The type of menu item, such as {@link #action}, {@link #menu} or
	 * {@link #toolBar}.
	 * <p>
	 * Some types are {@link #isParentType() parent types} - that means URIs to
	 * menu components of that type can be used as a
	 * {@link MenuComponent#getParentId() parent id}.
	 * 
	 * @author Stian Soiland-Reyes
	 * 
	 */
	public static enum MenuType {
		/**
		 * A normal {@link Action} as part of a {@link #menu}, {@link #toolBar},
		 * {@link #section} or {@link #optionGroup}. Such menu items are leaf
		 * nodes, which no {@link MenuComponent}s can have this as it's
		 * {@link MenuComponent#getParentId()}. The action's
		 * {@link ActionListener#actionPerformed(java.awt.event.ActionEvent)}
		 * will be called when choosing/clicking the menu item from the menu or
		 * toolBar.
		 */
		action,
		/**
		 * Provide a customised {@link MenuElement} from
		 * {@link MenuComponent#getCustomComponent()} that is to be used instead
		 * of creating an element from {@link MenuComponent#getAction()}.
		 */
		custom,
		/**
		 * A group containing mutually exclusive choices (as {@link #action}s),
		 * to be grouped in a {@link ButtonGroup}, separated using
		 * {@link JMenu#addSeparator()} or {@link JToolBar#addSeparator()} when
		 * needed. The {@link MenuComponent#getAction()} is ignored and should
		 * be <code>null</code>.
		 */
		optionGroup,
		/**
		 * A section of menu items within {@link #menu} or {@link #toolBar}.
		 * Sections are separated using {@link JMenu#addSeparator()} or
		 * {@link JToolBar#addSeparator()} when needed. The
		 * {@link MenuComponent#getAction()} is ignored and should be
		 * <code>null</code>.
		 */
		section,
		/**
		 * A (sub)menu that contain other menu items, including deeper
		 * {@link #menu}s. The {@link Action} from
		 * {@link MenuComponent#getAction()} is used to find the name, icon etc.
		 * for the sub-menu, while its
		 * {@link ActionListener#actionPerformed(java.awt.event.ActionEvent)}
		 * method is ignored. The {@link DefaultMenuBar} is the default top
		 * level menu, although others can be created with <code>null</code>
		 * as their parent.
		 */
		menu,
		/**
		 * A boolean toggle action, the action will be shown as a
		 * {@link JCheckBox} on a menu or toolBar. Such menu items are leaf
		 * nodes, which no {@link MenuComponent}s can have this as it's
		 * {@link MenuComponent#getParentId()}. The action's
		 * {@link ActionListener#actionPerformed(java.awt.event.ActionEvent)}
		 * will be called when toggling the action.
		 */
		toggle,
		/**
		 * A toolBar containing {@link #optionGroup}s, {@link #toggle}s or
		 * {@link #action}s. The toolBar can be shown as a {@link JToolBar}.
		 * The {@link MenuComponent#getAction()} and
		 * {@link MenuComponent#getParentId()} are ignored and should be
		 * <code>null</code>.
		 */
		toolBar;

		private Set<MenuType> parentTypes = defineParentTypes();

		/**
		 * True if the menu type is a parent type such as {@link #optionGroup},
		 * {@link #section}, {@link #menu} or {@link #toolBar}. If the type of
		 * a menu component is a a parent type it can (should) have children,
		 * ie. the children has a {@link MenuComponent#getParentId()} that
		 * equals the parent's {@link MenuComponent#getId()}.
		 * 
		 * @return True if the menu type is a parent type.
		 */
		public boolean isParentType() {
			return parentTypes.contains(this);
		}

		/**
		 * Create the set of {@link MenuType}s that {@link #isParentType()}
		 * would return <code>true</code> for.
		 * 
		 * @return A {@link Set} of {@link MenuType}s.
		 */
		private Set<MenuType> defineParentTypes() {
			HashSet<MenuType> types = new HashSet<MenuType>();
			types.add(optionGroup);
			types.add(section);
			types.add(menu);
			types.add(toolBar);
			return types;
		}

	}
}
