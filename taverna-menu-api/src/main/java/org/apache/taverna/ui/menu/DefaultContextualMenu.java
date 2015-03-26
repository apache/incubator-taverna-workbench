/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.ui.menu;

import java.net.URI;

/**
 * The default contextual menu, created using
 * {@link MenuManager#createContextMenu(Object, Object, java.awt.Component)()}.
 * <p>
 * Items that are part of a contextual menu should also implement
 * {@link ContextualMenuComponent}, the menu manager will then be able to tell
 * the items what is the current selection for the contextual menu, so that the
 * items can update their {@link MenuComponent#isEnabled()} (only visible for
 * some selections) and {@link MenuComponent#getAction()} (the action needs the
 * selected object).
 * 
 * @author Stian Soiland-Reyes
 */
public class DefaultContextualMenu extends AbstractMenu {
	/**
	 * The URI of a menu item representing the default menu bar. Menu items who
	 * has this URI as their {@link #getParentId()} will be shown in the top
	 * menu of the main application window.
	 */
	public static final URI DEFAULT_CONTEXT_MENU = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#defaultContextMenu");

	/**
	 * Construct the default menu bar
	 */
	public DefaultContextualMenu() {
		super(DEFAULT_CONTEXT_MENU);
	}
}
