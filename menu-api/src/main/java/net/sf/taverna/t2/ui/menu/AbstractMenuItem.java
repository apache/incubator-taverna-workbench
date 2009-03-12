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
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

/**
 * An abstract implementation of {@link MenuComponent} that can be used by
 * convenience to create menu component SPIs for the {@link MenuManager}.
 * <p>
 * Abstract subclasses of this class are specialised by their
 * {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType}. To create a menu,
 * toolbar, section, action etc, create an SPI implementation by subclassing
 * depending on the required type:
 * </p>
 * <dl>
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#menu} </dt>
 * <dd> Subclass {@link AbstractMenu} </dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#toolBar} </dt>
 * <dd> Subclass {@link AbstractToolBar} </dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#section} </dt>
 * <dd> Subclass {@link AbstractMenuSection} </dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#action} </dt>
 * <dd> Subclass {@link AbstractMenuAction} </dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#toggle} </dt>
 * <dd> Subclass {@link AbstractMenuToggle} </dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#custom} </dt>
 * <dd> Subclass {@link AbstractMenuCustom} </dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#optionGroup}
 * </dt>
 * <dd> Subclass {@link AbstractMenuOptionGroup} </dd>
 * 
 * </dl>
 * <p>
 * Note that you are not required to subclass any of these as long as your SPI
 * implementations implement the {@link MenuComponent} interface. In all cases
 * you are still required to list all your implementations, including
 * intermediate menus and sections, in the SPI description resource file
 * <code>/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent</code>
 * </p>
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractMenuItem implements MenuComponent {

	/**
	 * An {@link Action} that does not perform any action, but only contains a
	 * name and icon. Used by {@link AbstractMenu} and others.
	 * 
	 * @author Stian Soiland-Reyes
	 * 
	 */
	public static class DummyAction extends AbstractAction {
		public DummyAction(String name) {
			super(name);
		}

		public DummyAction(String name, Icon icon) {
			super(name, icon);
		}

		public void actionPerformed(ActionEvent e) {
		}
	}

	public AbstractMenuItem(MenuType type, URI parentId, URI id) {
		this.type = type;
		this.parentId = parentId;
		this.id = id;
	}

	private final MenuType type;
	private final URI parentId;
	private final URI id;
	protected int positionHint = 100;
	protected Action action;
	protected Component customComponent;

	/**
	 * {@inheritDoc}
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * {@inheritDoc}
	 */
	public Component getCustomComponent() {
		return customComponent;
	}

	/**
	 * {@inheritDoc}
	 */
	public URI getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public URI getParentId() {
		return parentId;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPositionHint() {
		return positionHint;
	}

	/**
	 * {@inheritDoc}
	 */
	public MenuType getType() {
		return type;
	}
	
	public boolean isEnabled() {
		return true;
	}

}
