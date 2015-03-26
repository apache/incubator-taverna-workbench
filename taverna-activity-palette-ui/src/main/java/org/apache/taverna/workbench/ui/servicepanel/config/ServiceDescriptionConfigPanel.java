/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package org.apache.taverna.workbench.ui.servicepanel.config;

import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.apache.taverna.servicedescriptions.ConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;
import org.apache.taverna.servicedescriptions.ServiceDescriptionsConfiguration;
import org.apache.taverna.workbench.helper.Helper;

@SuppressWarnings("serial")
public class ServiceDescriptionConfigPanel extends JPanel {
	private static final String REMOVE_PERMANENTLY = "Allow permanent removal of default service providers";
	private static final String INCLUDE_DEFAULTS = "Include default service providers";

	private final ServiceDescriptionsConfiguration config;
	private JCheckBox includeDefaults;
	private JCheckBox removePermanently;
	private final ServiceDescriptionRegistry serviceDescRegistry;

	public ServiceDescriptionConfigPanel(ServiceDescriptionsConfiguration config,
			ServiceDescriptionRegistry serviceDescRegistry) {
		super(new GridBagLayout());
		this.config = config;
		this.serviceDescRegistry = serviceDescRegistry;
		initialize();
	}

	private void initialize() {
		removeAll();

		GridBagConstraints gbc = new GridBagConstraints();

		// Title describing what kind of settings we are configuring here
		JTextArea descriptionText = new JTextArea(
				"Configure behaviour of default service providers in Service Panel");
        descriptionText.setLineWrap(true);
        descriptionText.setWrapStyleWord(true);
        descriptionText.setEditable(false);
        descriptionText.setFocusable(false);
        descriptionText.setBorder(new EmptyBorder(10, 10, 10, 10));
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = WEST;
		gbc.fill = HORIZONTAL;
		add(descriptionText, gbc);

		includeDefaults = new JCheckBox(INCLUDE_DEFAULTS);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = WEST;
		gbc.fill = NONE;
        gbc.insets = new Insets(10, 0, 0, 0);
		add(includeDefaults, gbc);

		removePermanently = new JCheckBox(REMOVE_PERMANENTLY);
		gbc.gridx = 0;
		gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
		add(removePermanently, gbc);

		// Filler
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 0, 0, 0);
		add(createButtonPanel(), gbc);

		setFields(config);
	}

	/**
	 * Create the panel to contain the buttons
	 *
	 * @return
	 */
	private JPanel createButtonPanel() {
		final JPanel panel = new JPanel();

		/**
		 * The helpButton shows help about the current component
		 */
		JButton helpButton = new JButton(new AbstractAction("Help") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Helper.showHelp(panel);
			}
		});
		panel.add(helpButton);

		/**
		 * The resetButton changes the property values shown to those
		 * corresponding to the configuration currently applied.
		 */
		JButton resetButton = new JButton(new AbstractAction("Reset") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setFields(config);
			}
		});
		panel.add(resetButton);

		/**
		 * The applyButton applies the shown field values to the
		 * {@link HttpProxyConfiguration} and saves them for future.
		 */
		JButton applyButton = new JButton(new AbstractAction("Apply") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				applySettings();
				setFields(config);
			}
		});
		panel.add(applyButton);

		return panel;
	}

	protected void applySettings() {
		// Include default service providers
		config.setIncludeDefaults(includeDefaults.isSelected());
		for (ServiceDescriptionProvider provider : serviceDescRegistry
				.getDefaultServiceDescriptionProviders()) {
			if (! (provider instanceof ConfigurableServiceProvider))
				continue;
			if (config.isIncludeDefaults())
				serviceDescRegistry.addServiceDescriptionProvider(provider);
			else
				serviceDescRegistry.removeServiceDescriptionProvider(provider);
		}

		// Allow permanent removal of default service providers
		config.setRemovePermanently(removePermanently.isSelected());
	}

	/**
	 * Set the shown configuration field values to those currently in use
	 * (i.e. last saved configuration).
	 *
	 */
	private void setFields(ServiceDescriptionsConfiguration configurable) {
		includeDefaults.setSelected(configurable.isIncludeDefaults());
		removePermanently.setSelected(configurable.isRemovePermanently());
	}
}
