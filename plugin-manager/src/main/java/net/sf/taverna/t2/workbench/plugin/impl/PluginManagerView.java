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

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.workbench.MainWindow;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import uk.org.taverna.commons.plugin.PluginException;
import uk.org.taverna.commons.plugin.PluginManager;

/**
 *
 *
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
			dialog = new JDialog(MainWindow.getMainWindow(), "Plugin Manager");
			dialog.add(getPluginManagerPanel());
			dialog.setSize(700, 500);
			dialog.setLocationRelativeTo(dialog.getOwner());
			dialog.setVisible(true);
		}
		return dialog;
	}

	private PluginManagerPanel getPluginManagerPanel() {
		if (pluginManagerPanel == null) {
			pluginManagerPanel = new PluginManagerPanel(pluginManager);
		}
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
