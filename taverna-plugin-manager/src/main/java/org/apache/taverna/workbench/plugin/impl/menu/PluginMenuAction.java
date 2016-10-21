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
package org.apache.taverna.workbench.plugin.impl.menu;

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.plugin.impl.PluginManagerView;

public class PluginMenuAction extends AbstractMenuAction {
	private static final URI ADVANCED_MENU_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#advanced");

	private PluginManagerView pluginManagerView;

	public PluginMenuAction() {
		super(ADVANCED_MENU_URI, 1100);
	}

	@Override
	@SuppressWarnings("serial")
	protected Action createAction() {
		return new AbstractAction("Plugin Manager") {
			@Override
			public void actionPerformed(ActionEvent e) {
				pluginManagerView.showDialog();
			}
		};
	}

	public void setPluginManagerView(PluginManagerView pluginManagerView) {
		this.pluginManagerView = pluginManagerView;
	}
}
