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

import static org.apache.taverna.ui.menu.MenuComponent.MenuType.menu;

import java.net.URI;

import javax.swing.Action;

/**
 * A {@link MenuComponent} of the type {@link MenuType#menu menu}.
 * <p>
 * Subclass to create an SPI implementation for the {@link MenuManager} of a
 * menu. The definition of "menu" includes both the menu bar and sub menus. A
 * menu can contain {@linkplain AbstractMenuAction actions},
 * {@linkplain AbstractMenuToggle toggles} or {@linkplain AbstractMenuCustom
 * custom components}, or any of the above grouped in a
 * {@linkplain AbstractMenuSection section},
 * {@linkplain AbstractMenuOptionGroup option group} or a
 * {@linkplain AbstractMenu submenu}.
 * <p>
 * Menu components are linked together using URIs, avoiding the need for compile
 * time dependencies between SPI implementations. To add components to a menu,
 * use the {@link URI} identifying this menu as their parent id.
 * <p>
 * <strong>Note:</strong> To avoid conflicts with other plugins, use a unique
 * URI root that is related to the Java package name, for instance
 * <code>http://cs.university.ac.uk/myplugin/2008/menu</code>, and use hash
 * identifiers for each menu item, for instance
 * <code>http://cs.university.ac.uk/myplugin/2008/menu#run</code> for a "Run"
 * item. Use flat URI namespaces, don't base a child's URI on the parent's URI,
 * as this might make it difficult to relocate the parent menu.
 * <p>
 * You need to list the {@linkplain Class#getName() fully qualified class name}
 * (for example <code>com.example.t2plugin.menu.MyMenu</code>) of the menu
 * implementation in the SPI description resource file
 * <code>/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent</code> so
 * that it can be discovered by the {@link MenuManager}. This requirement also
 * applies to parent menu components (except {@link DefaultToolBar} and
 * {@link DefaultMenuBar}, but ensure they are only listed once.
 * 
 * @author Stian Soiland-Reyes
 */
public abstract class AbstractMenu extends AbstractMenuItem {
	/**
	 * Construct a menu bar (does not have a parent). This menu bar can be built
	 * and used through {@link MenuManager#createMenuBar(URI)}. There is a
	 * default menu bar implementation in {@link DefaultMenuBar} that can be
	 * built using {@link MenuManager#createMenuBar()}, but in case you need
	 * several menu bars for different windows or modes, use this constructor.
	 * 
	 * @param id
	 *            The {@link URI} to identify this menu bar. Use this as the
	 *            parent ID for menu components to appear in this menu.
	 */
	public AbstractMenu(URI id) {
		super(menu, (URI) null, id);
	}

	/**
	 * Construct a submenu.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu. The parent should be of
	 *            type
	 *            {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#menu}.
	 * @param positionHint
	 *            The position hint to determine the position of this submenu
	 *            among its siblings in the parent menu. For extensibility, use
	 *            BASIC style numbering such as 10, 20, etc.
	 * @param id
	 *            The {@link URI} to identify this menu bar. Use this as the
	 *            parent ID for menu components to appear in this submenu.
	 * @param label
	 *            The label for presenting this sub-menu in the parent menu.
	 */
	public AbstractMenu(URI parentId, int positionHint, URI id, String label) {
		this(parentId, positionHint, id, new DummyAction(label));
	}

	/**
	 * Construct a submenu.
	 * 
	 * @param parentId
	 *            The {@link URI} of the parent menu. The parent should be of
	 *            type
	 *            {@link net.sf.taverna.t2.ui.menu.MenuComponent.MenuType#menu}.
	 * @param positionHint
	 *            The position hint to determine the position of this submenu
	 *            among its siblings in the parent menu. For extensibility, use
	 *            BASIC style numbering such as 10, 20, etc.
	 * @param id
	 *            The {@link URI} to identify this menu bar. Use this as the
	 *            parent ID for menu components to appear in this submenu.
	 * @param action
	 *            The action containing a label and icon for presenting this
	 *            sub-menu in the parent menu.
	 */
	public AbstractMenu(URI parentId, int positionHint, URI id, Action action) {
		super(menu, parentId, id);
		this.action = action;
		this.positionHint = positionHint;
	}
}
