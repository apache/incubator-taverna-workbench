/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.taverna.ui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * A {@link MenuComponent} of the type {@link MenuType#toggle}.
 * <p>
 * Subclass to create an SPI implementation for the {@link MenuManager} of an
 * toggle action. A toggle is a menu item that can be turned on/off and are
 * typically represented with a check box when they are enabled.
 * <p>
 * This action can have as an parent a {@linkplain AbstractMenu menu} or
 * {@linkplain AbstractToolBar toolbar}, or grouped within a
 * {@linkplain AbstractMenuSection section} or
 * {@linkplain AbstractMenuOptionGroup option group}.
 * <p>
 * To define the {@link Action}, implement {@link #createAction()}. The action
 * should provide both the label/icon (representation) and
 * {@link ActionListener#actionPerformed(ActionEvent)}.
 * <p>
 * You need to list the {@linkplain Class#getName() fully qualified class name}
 * (for example <code>com.example.t2plugin.menu.MyMenuAction</code>) of the menu
 * action implementation in the SPI description resource file
 * <code>/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent</code> so
 * that it can be discovered by the {@link MenuManager}. This requirement also
 * applies to parent menu components (except {@link DefaultToolBar} and
 * {@link DefaultMenuBar}, but ensure they are only listed once.
 * 
 * @author Stian Soiland-Reyes
 */
public abstract class AbstractMenuToggle extends AbstractMenuItem {
	/**
	 * Construct a toggle action to appear within the specified menu component.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The component
	 *            should be a {@linkplain MenuType#isParentType() parent type}
	 *            and must have been registered separately as an SPI.
	 * @param positionHint
	 *            The position hint to determine the position of this toggle
	 *            action among its siblings in the parent menu, section or
	 *            toolbar. For extensibility, use BASIC style numbering such as
	 *            10, 20, etc. (Note that position hints are local to each
	 *            parent, so each {@linkplain AbstractMenuSection section} have
	 *            their own position hint scheme.)
	 */
	public AbstractMenuToggle(URI parentId, int positionHint) {
		this(parentId, null, positionHint);
	}

	/**
	 * Construct a toggle action to appear within the specified menu component.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu component. The component
	 *            should be a {@link MenuType#isParentType() parent type} and
	 *            must have been registered separately as an SPI.
	 * @param id
	 *            The {@link URI} to identify this toggle action. Although no
	 *            components can have an action as their parent, this URI can be
	 *            used to retrieve the realisation of this component using
	 *            {@link MenuManager#getComponentByURI(URI)}. This ID might also
	 *            be registered as a help identifier with the help system.
	 * @param positionHint
	 *            The position hint to determine the position of this action
	 *            among its siblings in the parent menu, section or toolbar. For
	 *            extensibility, use BASIC style numbering such as 10, 20, etc.
	 *            (Note that position hints are local to each parent, so each
	 *            {@linkplain AbstractMenuSection section} have their own
	 *            position hint scheme.)
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
	 *      {@inheritDoc}
	 */
	@Override
	public synchronized Action getAction() {
		if (action == null)
			action = createAction();
		return action;
	}

	/**
	 * Create the {@link Action} that labels this toggle action, in addition to
	 * performing the desired action on
	 * {@link ActionListener#actionPerformed(ActionEvent)}.
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
