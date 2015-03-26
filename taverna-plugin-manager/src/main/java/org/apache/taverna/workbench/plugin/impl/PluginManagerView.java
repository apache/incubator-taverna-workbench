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

package org.apache.taverna.workbench.plugin.impl;

import static org.apache.taverna.workbench.MainWindow.getMainWindow;

import javax.swing.JDialog;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import uk.org.taverna.commons.plugin.PluginManager;

/**
 * @author David Withers
 */
public class PluginManagerView implements EventHandler {
	private JDialog dialog;
	private PluginManagerPanel pluginManagerPanel;
	private PluginManager pluginManager;

	public void showDialog() {
		getDialog().setVisible(true);
		getPluginManagerPanel().checkForUpdates();
	}

	private JDialog getDialog() {
		if (dialog == null) {
			dialog = new JDialog(getMainWindow(), "Plugin Manager");
			dialog.add(getPluginManagerPanel());
			dialog.setSize(700, 500);
			dialog.setLocationRelativeTo(dialog.getOwner());
			dialog.setVisible(true);
		}
		return dialog;
	}

	private PluginManagerPanel getPluginManagerPanel() {
		if (pluginManagerPanel == null)
			pluginManagerPanel = new PluginManagerPanel(pluginManager);
		return pluginManagerPanel;
	}

	@Override
	public void handleEvent(Event event) {
		pluginManagerPanel.initialize();
		pluginManagerPanel.revalidate();
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}
}
