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

package org.apache.taverna.workbench.plugin.impl;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.taverna.plugin.PluginException;
import org.apache.taverna.plugin.PluginManager;
import org.apache.taverna.plugin.xml.jaxb.PluginVersions;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public class UpdatePluginPanel extends PluginPanel {
	private final PluginVersions pluginVersions;
	private final PluginManager pluginManager;

	public UpdatePluginPanel(PluginVersions pluginVersions,
			PluginManager pluginManager) {
		super(pluginVersions.getName(), pluginVersions.getOrganization(),
				pluginVersions.getLatestVersion().getVersion(), pluginVersions
						.getDescription());
		this.pluginVersions = pluginVersions;
		this.pluginManager = pluginManager;
	}

	@Override
	public Action getPluginAction() {
		return new AbstractAction("Update") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(false);
				putValue(NAME, "Updating");
				boolean succeeded = doUpdate();
				putValue(NAME, succeeded ? "Updated" : "Failed to update");
			}
		};
	}

	private boolean doUpdate() {
		try {
			pluginManager.updatePlugin(pluginVersions);
			return true;
		} catch (PluginException e) {
			// FIXME Log exception properly
			e.printStackTrace();
			return false;
		}
	}
}
