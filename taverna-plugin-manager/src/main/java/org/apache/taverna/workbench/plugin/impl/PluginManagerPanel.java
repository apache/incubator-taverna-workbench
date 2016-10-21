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
package org.apache.taverna.workbench.plugin.impl;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import static java.lang.Math.max;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.SwingConstants.CENTER;

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

import org.apache.log4j.Logger;
import org.apache.taverna.plugin.Plugin;
import org.apache.taverna.plugin.PluginException;
import org.apache.taverna.plugin.PluginManager;
import org.apache.taverna.plugin.xml.jaxb.PluginVersions;

//import uk.org.taverna.commons.plugin.Plugin;
//import uk.org.taverna.commons.plugin.PluginException;
//import uk.org.taverna.commons.plugin.PluginManager;
//import uk.org.taverna.commons.plugin.xml.jaxb.PluginVersions;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public class PluginManagerPanel extends JPanel {
	private static final Logger logger = Logger
			.getLogger(PluginManagerPanel.class);

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
		gbc.anchor = WEST;
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
		gbc.fill = BOTH;
		add(tabbedPane, gbc);

		gbc.anchor = EAST;
		gbc.fill = NONE;
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

	private static Component scrolled(Component view) {
		JScrollPane scrollPane = new JScrollPane(view);
		scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		return scrollPane;
	}

	private Component createAvailablePluginsPanel() {
		try {
			List<PluginVersions> availablePlugins = pluginManager
					.getAvailablePlugins();
			if (availablePlugins.size() == 0)
				return new JLabel("No new plugins available", CENTER);

			JPanel panel = new JPanel(new ListLayout());
			for (PluginVersions plugin : availablePlugins)
				panel.add(new AvailablePluginPanel(plugin, pluginManager));
			return scrolled(panel);
		} catch (PluginException e) {
			logger.info("Error looking for new plugins", e);
			return new JLabel("No new plugins available", CENTER);
		}
	}

	private Component createInstalledPluginsPanel() {
		try {
			List<Plugin> installedPlugins = pluginManager.getInstalledPlugins();
			if (installedPlugins.size() == 0)
				return new JLabel("No installed plugins", CENTER);

			JPanel panel = new JPanel(new ListLayout());
			for (Plugin plugin : installedPlugins)
				panel.add(new InstalledPluginPanel(plugin));
			return scrolled(panel);
		} catch (PluginException e) {
			return new JLabel("No installed plugins", CENTER);
		}
	}

	private Component createUpdatePluginsPanel() {
		try {
			List<PluginVersions> pluginUpdates = pluginManager
					.getPluginUpdates();
			if (pluginUpdates.size() == 0)
				return new JLabel("All plugins are up to date", CENTER);

			JPanel panel = new JPanel(new ListLayout());
			for (PluginVersions plugin : pluginUpdates)
				panel.add(new UpdatePluginPanel(plugin, pluginManager));
			return scrolled(panel);
		} catch (PluginException e) {
			return new JLabel("All plugins are up to date", CENTER);
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
				preferredLayoutSize.width = max(preferredSize.width,
						preferredLayoutSize.width);
				preferredLayoutSize.height = preferredSize.height
						+ preferredLayoutSize.height - 1;
			}
			return preferredLayoutSize;
		}

		@Override
		public Dimension minimumLayoutSize(Container parent) {
			Dimension minimumLayoutSize = new Dimension(0, 1);
			for (Component component : parent.getComponents()) {
				Dimension minimumSize = component.getMinimumSize();
				minimumLayoutSize.width = max(minimumSize.width,
						minimumLayoutSize.width);
				minimumLayoutSize.height = minimumSize.height
						+ minimumLayoutSize.height - 1;
			}
			return minimumLayoutSize;
		}

		@Override
		public void layoutContainer(Container parent) {
			int y = 0;
			for (Component component : parent.getComponents()) {
				component.setLocation(0, y);
				component.setSize(parent.getSize().width,
						component.getPreferredSize().height);
				y += component.getHeight() - 1;
			}
		}
	}
}
