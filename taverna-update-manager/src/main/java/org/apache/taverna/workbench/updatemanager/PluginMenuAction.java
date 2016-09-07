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

package org.apache.taverna.workbench.updatemanager;

import static java.awt.event.KeyEvent.VK_F12;
import static javax.swing.KeyStroke.getKeyStroke;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;

public class PluginMenuAction extends AbstractMenuAction {
	private static final String UPDATES_AND_PLUGINS = "Updates and plugins";

	@SuppressWarnings("serial")
	protected class SoftwareUpdates extends AbstractAction {
		public SoftwareUpdates() {
			super(UPDATES_AND_PLUGINS, null/*UpdatesAvailableIcon.updateRecommendedIcon*/);
			putValue(ACCELERATOR_KEY, getKeyStroke(VK_F12, 0));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			@SuppressWarnings("unused")
			Component parent = null;
			if (e.getSource() instanceof Component) {
				parent = (Component) e.getSource();
			}
			//FIXME this does nothing!
			//final PluginManagerFrame pluginManagerUI = new PluginManagerFrame(
			//		PluginManager.getInstance());
			//if (parent != null) {
			//	pluginManagerUI.setLocationRelativeTo(parent);
			//}
			//pluginManagerUI.setVisible(true);
		}
	}

	public PluginMenuAction() {
		super(URI.create("http://taverna.sf.net/2008/t2workbench/menu#advanced"),
				100);
	}

	@Override
	protected Action createAction() {
		//return new SoftwareUpdates();
		return null;
	}
}
