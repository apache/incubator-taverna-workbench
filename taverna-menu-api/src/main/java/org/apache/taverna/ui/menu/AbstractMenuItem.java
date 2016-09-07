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
/*

package org.apache.taverna.ui.menu;

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
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#menu}</dt>
 * <dd>Subclass {@link AbstractMenu}</dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#toolBar}</dt>
 * <dd>Subclass {@link AbstractToolBar}</dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#section}</dt>
 * <dd>Subclass {@link AbstractMenuSection}</dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#action}</dt>
 * <dd>Subclass {@link AbstractMenuAction}</dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#toggle}</dt>
 * <dd>Subclass {@link AbstractMenuToggle}</dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#custom}</dt>
 * <dd>Subclass {@link AbstractMenuCustom}</dd>
 * 
 * <dt> {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#optionGroup}</dt>
 * <dd>Subclass {@link AbstractMenuOptionGroup}</dd>
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
	@SuppressWarnings("serial")
	public static class DummyAction extends AbstractAction {
		public DummyAction(String name) {
			super(name);
		}

		public DummyAction(String name, Icon icon) {
			super(name, icon);
		}

		@Override
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

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public Component getCustomComponent() {
		return customComponent;
	}

	@Override
	public URI getId() {
		return id;
	}

	@Override
	public URI getParentId() {
		return parentId;
	}

	@Override
	public int getPositionHint() {
		return positionHint;
	}

	@Override
	public MenuType getType() {
		return type;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
