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
 * The default tool bar that will be shown by the main application window. Use
 * {@link #DEFAULT_TOOL_BAR} as the {@link #getParentId()} for items that should
 * appear in this toolbar. This toolbar can be created using
 * {@link MenuManager#createToolBar()}
 * <p>
 * Separate toolbars can be made by subclassing {@link AbstractToolBar} and
 * created by using {@link MenuManager#createToolBar(URI)}.
 * 
 * @author Stian Soiland-Reyes
 */
public class DefaultToolBar extends AbstractToolBar {
	/**
	 * The URI of a tool bar item representing the default tool bar. Items who
	 * has this URI as their {@link #getParentId()} will be shown in the default
	 * toolbar of the main application window.
	 */
	public static final URI DEFAULT_TOOL_BAR = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#defaultToolBar");

	/**
	 * Construct the default toolbar.
	 */
	public DefaultToolBar() {
		super(DEFAULT_TOOL_BAR);
	}
}
