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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import uk.org.taverna.commons.plugin.Plugin;
import uk.org.taverna.commons.plugin.PluginException;
import uk.org.taverna.commons.plugin.PluginManager;
import uk.org.taverna.commons.plugin.xml.jaxb.PluginVersions;

/**
 * @author David Withers
 */
public class PluginManagerPanel extends JPanel {

	private static final Logger logger = Logger.getLogger(PluginManagerPanel.class);

	private PluginManager pluginManager;

	private JLabel message = new JLabel("");

	public PluginManagerPanel(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
		initialize();
	}

	public void initialize() {
		removeAll();
		setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy = 0;
		gbc.insets.left = 5;
		gbc.insets.right = 5;
		gbc.insets.top = 5;

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Available", createAvailablePluginsPanel());
		tabbedPane.addTab("Installed", createInstalledPluginsPanel());
		tabbedPane.addTab("Updates", createUpdatePluginsPanel());

		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridy = 1;
//		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		add(tabbedPane, gbc);

		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridy = 2;
		gbc.insets.bottom = 5;
		add(message, gbc);
	}

	public void checkForUpdates() {
		message.setText("Checking for updates");
		try {
			pluginManager.checkForUpdates();
		} catch (PluginException e) {
			logger.info("Error checking for plugin updates", e);
		} finally {
			message.setText("");
		}
	}

	private Component createAvailablePluginsPanel() {
		try {
			List<PluginVersions> availablePlugins = pluginManager.getAvailablePlugins();
			if (availablePlugins.size() == 0) {
				return new JLabel("No new plugins available", SwingConstants.CENTER);
			} else {
				JPanel avaialablePluginsPanel = new JPanel();
				avaialablePluginsPanel.setLayout(new ListLayout());
				for (PluginVersions plugin : availablePlugins) {
					avaialablePluginsPanel.add(new AvailablePluginPanel(plugin, pluginManager));
				}
				JScrollPane scrollPane = new JScrollPane(avaialablePluginsPanel);
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setBorder(null);
				return scrollPane;
			}
		} catch (PluginException e) {
			logger.info("Error looking for new plugins", e);
			return new JLabel("No new plugins available", SwingConstants.CENTER);
		}
	}

	private Component createInstalledPluginsPanel() {
		try {
			List<Plugin> installedPlugins = pluginManager.getInstalledPlugins();
			if (installedPlugins.size() == 0) {
				return new JLabel("No installed plugins", SwingConstants.CENTER);
			} else {
				JPanel installedPluginsPanel = new JPanel();
				installedPluginsPanel.setLayout(new ListLayout());
				for (Plugin plugin : installedPlugins) {
					installedPluginsPanel.add(new InstalledPluginPanel(plugin));
				}
				JScrollPane scrollPane = new JScrollPane(installedPluginsPanel);
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setBorder(null);
				return installedPluginsPanel;
			}
		} catch (PluginException e) {
			return new JLabel("No installed plugins", SwingConstants.CENTER);
		}
	}

	private Component createUpdatePluginsPanel() {
		try {
			List<PluginVersions> pluginUpdates = pluginManager.getPluginUpdates();
			if (pluginUpdates.size() == 0) {
				return new JLabel("All plugins are up to date", SwingConstants.CENTER);
			} else {
				JPanel updatePluginsPanel = new JPanel();
				updatePluginsPanel.setLayout(new ListLayout());
				for (PluginVersions plugin : pluginUpdates) {
					updatePluginsPanel.add(new UpdatePluginPanel(plugin, pluginManager));
				}
				JScrollPane scrollPane = new JScrollPane(updatePluginsPanel);
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				scrollPane.setBorder(null);
				return updatePluginsPanel;
			}
		} catch (PluginException e) {
			return new JLabel("All plugins are up to date", SwingConstants.CENTER);
		}
	}

	private final class ListLayout implements LayoutManager {

		@Override
		public void addLayoutComponent(String name, Component comp) {
		}

		@Override
		public void removeLayoutComponent(Component comp) {
		}

		@Override
		public Dimension preferredLayoutSize(Container parent) {
			Dimension preferredLayoutSize = new Dimension(0, 1);
			for (Component component : parent.getComponents()) {
				Dimension preferredSize = component.getPreferredSize();
				preferredLayoutSize.width = Math.max(preferredSize.width, preferredLayoutSize.width);
				preferredLayoutSize.height = preferredSize.height + preferredLayoutSize.height - 1;
			}
			return preferredLayoutSize;
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			Dimension minimumLayoutSize = new Dimension(0, 1);
			for (Component component : parent.getComponents()) {
				Dimension minimumSize = component.getMinimumSize();
				minimumLayoutSize.width = Math.max(minimumSize.width, minimumLayoutSize.width);
				minimumLayoutSize.height = minimumSize.height + minimumLayoutSize.height - 1;
			}
			return minimumLayoutSize;
		}

		@Override
		public void layoutContainer(Container parent) {
			int y = 0;
			for (Component component : parent.getComponents()) {
				component.setLocation(0, y);
				component.setSize(parent.getSize().width, component.getPreferredSize().height);
				y = y + component.getHeight() - 1;
			}
		}

	}

}
