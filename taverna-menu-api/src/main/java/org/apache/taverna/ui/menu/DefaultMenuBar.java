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
 * The default {@link AbstractMenu menu bar} that appears in the main
 * application window, created using {@link MenuManager#createMenuBar()}.
 * Alternative menu bars can be created using
 * {@link MenuManager#createMenuBar(URI)} - referring to the URI of another
 * instance of {@link AbstractMenu}.
 * 
 * @author Stian Soiland-Reyes
 */
public class DefaultMenuBar extends AbstractMenu {
	/**
	 * The URI of a menu item representing the default menu bar. Menu items who
	 * has this URI as their {@link #getParentId()} will be shown in the top
	 * menu of the main application window.
	 */
	public static final URI DEFAULT_MENU_BAR = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#defaultMenuBar");

	/**
	 * Construct the default menu bar
	 * 
	 */
	public DefaultMenuBar() {
		super(DEFAULT_MENU_BAR);
	}
}
