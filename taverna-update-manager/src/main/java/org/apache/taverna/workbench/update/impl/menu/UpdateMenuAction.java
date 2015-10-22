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

package org.apache.taverna.workbench.update.impl.menu;

import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;

import org.apache.log4j.Logger;
import org.apache.taverna.update.UpdateException;
import org.apache.taverna.update.UpdateManager;

public class UpdateMenuAction extends AbstractMenuAction {
	private static final Logger logger = Logger.getLogger(UpdateMenuAction.class);
	private static final URI ADVANCED_MENU_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#advanced");

	private UpdateManager updateManager;

	public UpdateMenuAction() {
		super(ADVANCED_MENU_URI, 1000);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Check for updates") {
			@Override
			public void actionPerformed(ActionEvent e) {
				findUpdates();
			}
		};
	}

	public void setUpdateManager(UpdateManager updateManager) {
		this.updateManager = updateManager;
	}

	private void findUpdates() {
		Component parent = null;
		try {
			if (!areUpdatesAvailable()) {
				showMessageDialog(null, "No update available");
				return;
			}
			if (showConfirmDialog(parent, "Update available. Update Now?") != YES_OPTION)
				return;
			applyUpdates();
			showMessageDialog(parent,
					"Update complete. Restart Taverna to apply update.");
		} catch (UpdateException ex) {
			showMessageDialog(parent, "Update failed: " + ex.getMessage());
			logger.warn("Update failed", ex);
		}
	}

	protected boolean areUpdatesAvailable() throws UpdateException {
		return updateManager.checkForUpdates();
	}

	protected void applyUpdates() throws UpdateException {
		updateManager.update();
	}
}
