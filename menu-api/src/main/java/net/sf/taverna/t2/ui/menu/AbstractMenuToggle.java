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

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * A {@link MenuComponent} of the type
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#toggle}.
 * <p>
 * Subclass to create an SPI implementation for the {@link MenuManager} of an
 * toggle action. A toggle is a menu item that can be turned on/off and are
 * typically represented with a check box when they are enabled.
 * </p>
 * <p>
 * This action can have as an parent a {@link AbstractMenu menu} or
 * {@link AbstractToolBar toolbar}, or grouped within an
 * {@link AbstractMenuSection section} or
 * {@link AbstractMenuOptionGroup option group}.
 * </p>
 * <p>
 * To define the {@link Action}, implement {@link #createAction()}. The action
 * should provide both the label/icon (representation) and
 * {@link java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)}.
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
public abstract class AbstractMenuToggle extends AbstractMenuItem {

	/**
	 * Construct a toggle action to appear within the specified menu component.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The component
	 *            should be a {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#isParentType() parent type} and
	 *            must have been registered separately as an SPI.
	 * @param positionHint
	 *            The position hint to determine the position of this toggle
	 *            action among its siblings in the parent menu, section or
	 *            toolbar. For extensibility, use BASIC style numbering such as
	 *            10, 20, etc. (Note that position hints are local to each
	 *            parent, so each {@link AbstractMenuSection section} have their
	 *            own position hint scheme.)
	 */
	public AbstractMenuToggle(URI parentId, int positionHint) {
		this(parentId, null, positionHint);
	}

	/**
	 * Construct a toggle action to appear within the specified menu component.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The component
	 *            should be a {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#isParentType() parent type} and
	 *            must have been registered separately as an SPI.
	 * @param id
	 *            The {@link URI} to identify this toggle action. Although no
	 *            components can have an action as their parent, this URI can be
	 *            used to retrieve the realisation of this component using
	 *            {@link MenuManager#getComponentByURI(URI)}. This ID might
	 *            also be registered as a help identifier with the help system.
	 * @param positionHint
	 *            The position hint to determine the position of this action
	 *            among its siblings in the parent menu, section or toolbar. For
	 *            extensibility, use BASIC style numbering such as 10, 20, etc.
	 *            (Note that position hints are local to each parent, so each
	 *            {@link AbstractMenuSection section} have their own position
	 *            hint scheme.)
	 */
	public AbstractMenuToggle(URI parentId, URI id, int positionHint) {
		super(MenuType.toggle, parentId, id);
		this.positionHint = positionHint;
	}

	/**
	 * Call {@link #createAction()} on first call, after that return cached
	 * action.
	 * 
	 * @see #createAction()
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Action getAction() {
		if (action == null) {
			action = createAction();
		}
		return action;
	}

	/**
	 * Create the {@link Action} that labels this toggle action, in addition to
	 * performing the desired action on
	 * {@link java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)}.
	 * <p>
	 * Implementations might use {@link AbstractAction} as a superclass for menu
	 * actions. It is recommended to make the action a top level class so that
	 * it can be used both within an {@link AbstractMenuAction} of a menu bar
	 * and within an {@link AbstractMenuAction} of a tool bar.
	 * </p>
	 * 
	 * @return A configured {@link Action} that should at least have a label or
	 *         icon.
	 */
	protected abstract Action createAction();

}
