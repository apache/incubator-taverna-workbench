/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.plugin.impl;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import uk.org.taverna.commons.plugin.PluginException;
import uk.org.taverna.commons.plugin.PluginManager;
import uk.org.taverna.commons.plugin.xml.jaxb.PluginVersions;

/**
 *
 *
 * @author David Withers
 */
public class AvailablePluginPanel extends PluginPanel {

	private final PluginManager pluginManager;

	private final PluginVersions pluginVersions;

	public AvailablePluginPanel(PluginVersions pluginVersions, PluginManager pluginManager) {
		super(pluginVersions.getName(), pluginVersions.getOrganization(), pluginVersions.getLatestVersion().getVersion(), pluginVersions.getDescription());
		this.pluginVersions = pluginVersions;
		this.pluginManager = pluginManager;
	}

	@Override
	public Action getPluginAction() {
		return new PluginAction();
	}


	@SuppressWarnings("serial")
	class PluginAction extends AbstractAction {

		public PluginAction() {
			putValue(Action.NAME, "Install");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			setEnabled(false);
			putValue(Action.NAME, "Installing");
			try {
				pluginManager.installPlugin(pluginVersions.getPluginSiteUrl(), pluginVersions.getLatestVersion().getFile()).start();
			} catch (PluginException ex) {
				ex.printStackTrace();
			}
			putValue(Action.NAME, "Installed");
		}
	}

}
