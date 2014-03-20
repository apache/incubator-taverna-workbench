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
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * A {@link MenuComponent} of the type
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#custom}.
 * <p>
 * Subclass to create an SPI implementation for the {@link MenuManager} of a
 * custom menu or toolbar {@link Component}, for instance a {@link JMenu},
 * {@link JMenuItem} or {@link JButton}.
 * <p>
 * This type of component can be useful for adding third party components that
 * are built using other menu systems, or to provide dynamic menus such as a
 * list of open files. This is the recommended way to customise the menus,
 * although it is also possible to modify the components returned using
 * {@link MenuManager#getComponentByURI(URI)}, but as the components built by
 * the menu manager might be refreshed by various actions forcing an update to
 * the SPI registry, such as installing a plugin. By using a custom menu
 * component it is possible to avoid these problems and to provide the
 * {@link Component} to be inserted into the menu/toolbar as built by the
 * {@link MenuManager}.
 * </p>
 * <p>
 * This component can have as an parent any menu component that
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#isParentType() is a parent type}. Note that although you can
 * specify an {@link URI} to identify the custom component (to be used with
 * {@link MenuManager#getComponentByURI(URI)} a custom component can't have
 * children. Such children would have to be created manually and added to the
 * component.
 * </p>
 * <p>
 * You need to list the {@link Class#getName() fully qualified class name} (for
 * example <code>com.example.t2plugin.menu.MyMenuAction</code>) of the menu
 * action implementation in the SPI description resource file
 * <code>/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent</code> so
 * that it can be discovered by the {@link MenuManager}. This requirement also
 * applies to parent menu components (except {@link DefaultToolBar} and
 * {@link DefaultMenuBar}, but ensure they are only listed once.
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractMenuCustom extends AbstractMenuItem {
	/**
	 * Construct a menu action to appear within the specified menu component.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The component
	 *            should be a {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#isParentType() parent type} and
	 *            must have been registered separately as an SPI.
	 * @param positionHint
	 *            The position hint to determine the position of this action
	 *            among its siblings in the parent menu, section or toolbar. For
	 *            extensibility, use BASIC style numbering such as 10, 20, etc.
	 *            (Note that position hints are local to each parent, so each
	 *            {@link AbstractMenuSection section} have their own position
	 *            hint scheme.)
	 */
	public AbstractMenuCustom(URI parentId, int positionHint) {
		this(parentId, positionHint, null);
	}

	/**
	 * Construct a menu action to appear within the specified menu component.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The component
	 *            should be a {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#isParentType() parent type} and
	 *            must have been registered separately as an SPI.
	 * @param positionHint
	 *            The position hint to determine the position of this action
	 *            among its siblings in the parent menu, section or toolbar. For
	 *            extensibility, use BASIC style numbering such as 10, 20, etc.
	 *            (Note that position hints are local to each parent, so each
	 *            {@link AbstractMenuSection section} have their own position
	 *            hint scheme.)
	 * @param id
	 *            The {@link URI} to identify this action. Although no
	 *            components can have an action as their parent, this URI can be
	 *            used to retrieve the realisation of this component using
	 *            {@link MenuManager#getComponentByURI(URI)}. This ID might
	 *            also be registered as a help identifier with the help system.
	 */
	public AbstractMenuCustom(URI parentId, int positionHint, URI id) {
		super(MenuType.custom, parentId, id);
		this.positionHint = positionHint;
	}

	/**
	 * Create the {@link Component} that is to be added to the parent.
	 * <p>
	 * The component must be compatible with the parent realisation from the
	 * {@link MenuManager}, for instance you can't add {@link JMenuItem}s to a
	 * toolbar.
	 * </p>
	 * <p>
	 * Note that the component might get assigned new parents if the
	 * menues/toolbars are rebuilt by the {@link MenuManager} is refreshed,
	 * although the menu manager will try to avoid a second call to
	 * {@link #createCustomComponent()}.
	 * </p>
	 * 
	 * @return A custom {@link Component} such as {@link JMenu},
	 *         {@link JMenuItem} or {@link JButton} to be added to the parent
	 *         menu component.
	 */
	protected abstract Component createCustomComponent();

	/**
	 * Return the custom component created using
	 * {@link #createCustomComponent()} on first call, return cached instance on
	 * later calls.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public final synchronized Component getCustomComponent() {
		if (customComponent == null) {
			customComponent = createCustomComponent();
		}
		return customComponent;
	}

}
