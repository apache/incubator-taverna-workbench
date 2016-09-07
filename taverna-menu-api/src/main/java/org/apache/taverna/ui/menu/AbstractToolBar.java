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

import java.net.URI;

/**
 * A {@link MenuComponent} of the type {@link MenuType#toolBar}.
 * <p>
 * Subclass to create an SPI implementation for the {@link MenuManager} of a
 * toolbar. A toolbar can contain {@linkplain AbstractMenuAction actions},
 * {@linkplain AbstractMenuToggle toggles} or {@linkplain AbstractMenuCustom
 * custom components}, or any of the above grouped in a
 * {@linkplain AbstractMenuSection section} or an
 * {@linkplain AbstractMenuOptionGroup option group}.
 * <p>
 * The {@link DefaultToolBar default toolbar} can be used as a parent for items
 * that are to be returned in the toolbar {@link MenuManager#createToolBar()},
 * while toolbars from other instances of AbstractToolBar can be created using
 * {@link MenuManager#createToolBar(URI)} specifying the URI of the toolbar's
 * identifier.
 * <p>
 * Menu components are linked together using URIs, avoiding the need for compile
 * time dependencies between SPI implementations. To add components to a
 * toolbar, use the {@link URI} identifying this toolbar as their parent id.
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
 * (for example <code>com.example.t2plugin.menu.MyMenu</code>) of the toolbar
 * implementation in the SPI description resource file
 * <code>/META-INF/services/net.sf.taverna.t2.ui.menu.MenuComponent</code> so
 * that it can be discovered by the {@link MenuManager}. This requirement also
 * applies to parent menu components (except {@link DefaultToolBar} and
 * {@link DefaultMenuBar}, but ensure they are only listed once.
 * 
 * @author Stian Soiland-Reyes
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
