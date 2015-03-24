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
package net.sf.taverna.t2.workbench.httpproxy.config;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static java.awt.GridBagConstraints.WEST;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static net.sf.taverna.t2.workbench.helper.Helper.showHelp;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.PROXY_USE_OPTION;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.SYSTEM_NON_PROXY_HOSTS;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.SYSTEM_PROXY_HOST;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.SYSTEM_PROXY_PASSWORD;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.SYSTEM_PROXY_PORT;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.SYSTEM_PROXY_USER;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.TAVERNA_NON_PROXY_HOSTS;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.TAVERNA_PROXY_HOST;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.TAVERNA_PROXY_PASSWORD;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.TAVERNA_PROXY_PORT;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.TAVERNA_PROXY_USER;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.USE_NO_PROXY_OPTION;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.USE_SPECIFIED_VALUES_OPTION;
import static uk.org.taverna.configuration.proxy.HttpProxyConfiguration.USE_SYSTEM_PROPERTIES_OPTION;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import org..taverna.configuration.proxy.HttpProxyConfiguration;

/**
 * The HttpProxyConfigurationPanel provides the user interface to a
 * {@link HttpProxyConfiguration} to determine how HTTP Connections are made by
 * Taverna.
 * 
 * @author alanrw
 * @author David Withers
 */
public class HttpProxyConfigurationPanel extends JPanel {
	static final long serialVersionUID = 3668473431971125038L;
	/**
	 * The size of the field for the JTextFields.
	 */
	private static int TEXTFIELD_SIZE = 25;

	private final HttpProxyConfiguration httpProxyConfiguration;
	/**
	 * RadioButtons that are in a common ButtonGroup. Selecting one of them
	 * indicates whether the system http proxy settings, the ad hoc specified
	 * values or no proxy settings at all should be used.
	 */
	private JRadioButton useSystemProperties;
	private JRadioButton useSpecifiedValues;
	private JRadioButton useNoProxy;
	/**
	 * JTextFields and one DialogTextArea to hold the settings for the HTTP
	 * proxy properties. The values are only editable if the user picks
	 * useSpecifiedValues.
	 */
	private JTextField proxyHostField;
	private JTextField proxyPortField;
	private JTextField proxyUserField;
	private JTextField proxyPasswordField;
	private DialogTextArea nonProxyHostsArea;
	private JScrollPane nonProxyScrollPane;
	/**
	 * A string that indicates which HTTP setting option the user has currently
	 * picked. This does not necesarily match that which has been applied.
	 */
	private String shownOption = USE_SYSTEM_PROPERTIES_OPTION;

	/**
	 * The HttpProxyConfigurationPanel consists of a set of properties where the
	 * configuration values for HTTP can be specified and a set of buttons where
	 * the more general apply, help etc. appear.
	 */
	public HttpProxyConfigurationPanel(
			HttpProxyConfiguration httpProxyConfiguration) {
		this.httpProxyConfiguration = httpProxyConfiguration;
		initComponents();
	}

	/**
	 * Populates the panel with a representation of the current HTTP proxy
	 * settings for the specified {@link HttpProxyConfiguration} and also the
	 * capability to alter them.
	 */
	private void initComponents() {
		shownOption = httpProxyConfiguration.getProperty(PROXY_USE_OPTION);

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();

		// Title describing what kind of settings we are configuring here
		JTextArea descriptionText = new JTextArea("HTTP proxy configuration");
		descriptionText.setLineWrap(true);
		descriptionText.setWrapStyleWord(true);
		descriptionText.setEditable(false);
		descriptionText.setFocusable(false);
		descriptionText.setBorder(new EmptyBorder(10, 10, 10, 10));
		gbc.anchor = WEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.fill = HORIZONTAL;
		this.add(descriptionText, gbc);

		/**
		 * Generate the three radio buttons and put them in a group. Each button
		 * is bound to an action that alters the shownOption and re-populates
		 * the shown HTTP property fields.
		 */
		useNoProxy = new JRadioButton("Do not use a proxy");
		useNoProxy.setAlignmentX(LEFT_ALIGNMENT);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.fill = NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		this.add(useNoProxy, gbc);
		ActionListener useNoProxyListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				shownOption = USE_NO_PROXY_OPTION;
				populateFields();
			}
		};
		useNoProxy.addActionListener(useNoProxyListener);

		useSystemProperties = new JRadioButton("Use system properties");
		useSystemProperties.setAlignmentX(LEFT_ALIGNMENT);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		this.add(useSystemProperties, gbc);
		ActionListener systemPropertiesListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				shownOption = USE_SYSTEM_PROPERTIES_OPTION;
				populateFields();
			}
		};
		useSystemProperties.addActionListener(systemPropertiesListener);

		useSpecifiedValues = new JRadioButton("Use specified values");
		useSpecifiedValues.setAlignmentX(LEFT_ALIGNMENT);
		gbc.gridx = 0;
		gbc.gridy = 3;
		this.add(useSpecifiedValues, gbc);
		ActionListener specifiedValuesListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				shownOption = USE_SPECIFIED_VALUES_OPTION;
				populateFields();
			}
		};
		useSpecifiedValues.addActionListener(specifiedValuesListener);

		ButtonGroup bg = new ButtonGroup();
		bg.add(useSystemProperties);
		bg.add(useSpecifiedValues);
		bg.add(useNoProxy);

		/**
		 * Create the fields to show the HTTP proxy property values. These
		 * become editable if the shown option is to use specified values.
		 */
		proxyHostField = new JTextField(TEXTFIELD_SIZE);
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.fill = NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		this.add(new JLabel("Proxy host"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.fill = HORIZONTAL;
		this.add(proxyHostField, gbc);

		proxyPortField = new JTextField(TEXTFIELD_SIZE);
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.fill = NONE;
		gbc.insets = new Insets(0, 0, 0, 0);
		this.add(new JLabel("Proxy port"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.fill = HORIZONTAL;
		this.add(proxyPortField, gbc);

		proxyUserField = new JTextField(TEXTFIELD_SIZE);
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.fill = NONE;
		this.add(new JLabel("Proxy user"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.fill = HORIZONTAL;
		this.add(proxyUserField, gbc);

		proxyPasswordField = new JTextField(TEXTFIELD_SIZE);
		gbc.gridx = 0;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.fill = NONE;
		this.add(new JLabel("Proxy password"), gbc);
		gbc.gridx = 1;
		gbc.gridy = 7;
		gbc.gridwidth = 1;
		gbc.fill = HORIZONTAL;
		this.add(proxyPasswordField, gbc);

		nonProxyHostsArea = new DialogTextArea(10, 40);
		nonProxyScrollPane = new JScrollPane(nonProxyHostsArea);
		nonProxyScrollPane
				.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		nonProxyScrollPane
				.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
		// nonProxyScrollPane.setPreferredSize(new Dimension(300, 500));
		gbc.gridx = 0;
		gbc.gridy = 8;
		gbc.gridwidth = 2;
		gbc.fill = NONE;
		gbc.insets = new Insets(10, 0, 0, 0);
		this.add(new JLabel("Non-proxy hosts"), gbc);
		gbc.gridx = 0;
		gbc.gridy = 9;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = BOTH;
		this.add(nonProxyScrollPane, gbc);

		// Add buttons panel
		gbc.gridx = 0;
		gbc.gridy = 10;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		gbc.gridwidth = 2;
		gbc.fill = HORIZONTAL;
		gbc.anchor = CENTER;
		gbc.insets = new Insets(10, 0, 0, 0);
		this.add(createButtonPanel(), gbc);

		setFields();
	}

	/**
	 * Populate the fields in the property panel according to which option is
	 * being shown and the stored values within the
	 * {@link HttpProxyConfiguration}.
	 */
	private void populateFields() {
		/**
		 * Editing of the property fields is only available when the option is
		 * to use the specified values.
		 */
		boolean editingEnabled = shownOption
				.equals(USE_SPECIFIED_VALUES_OPTION);

		if (shownOption.equals(USE_SYSTEM_PROPERTIES_OPTION)) {
			proxyHostField.setText(httpProxyConfiguration
					.getProperty(SYSTEM_PROXY_HOST));
			proxyPortField.setText(httpProxyConfiguration
					.getProperty(SYSTEM_PROXY_PORT));
			proxyUserField.setText(httpProxyConfiguration
					.getProperty(SYSTEM_PROXY_USER));
			proxyPasswordField.setText(httpProxyConfiguration
					.getProperty(SYSTEM_PROXY_PASSWORD));
			nonProxyHostsArea.setText(httpProxyConfiguration
					.getProperty(SYSTEM_NON_PROXY_HOSTS));
		} else if (shownOption.equals(USE_SPECIFIED_VALUES_OPTION)) {
			proxyHostField.setText(httpProxyConfiguration
					.getProperty(TAVERNA_PROXY_HOST));
			proxyPortField.setText(httpProxyConfiguration
					.getProperty(TAVERNA_PROXY_PORT));
			proxyUserField.setText(httpProxyConfiguration
					.getProperty(TAVERNA_PROXY_USER));
			proxyPasswordField.setText(httpProxyConfiguration
					.getProperty(TAVERNA_PROXY_PASSWORD));
			nonProxyHostsArea.setText(httpProxyConfiguration
					.getProperty(TAVERNA_NON_PROXY_HOSTS));
		} else {
			proxyHostField.setText(null);
			proxyPortField.setText(null);
			proxyUserField.setText(null);
			proxyPasswordField.setText(null);
			nonProxyHostsArea.setText(null);
		}

		proxyHostField.setEnabled(editingEnabled);
		proxyPortField.setEnabled(editingEnabled);
		proxyUserField.setEnabled(editingEnabled);
		proxyPasswordField.setEnabled(editingEnabled);
		nonProxyHostsArea.setEnabled(editingEnabled);
		nonProxyHostsArea.setEditable(editingEnabled);
		nonProxyScrollPane.setEnabled(editingEnabled);
	}

	/**
	 * Create the panel to contain the buttons
	 * 
	 * @return
	 */
	@SuppressWarnings("serial")
	private JPanel createButtonPanel() {
		final JPanel panel = new JPanel();

		/**
		 * The helpButton shows help about the current component
		 */
		JButton helpButton = new JButton(new AbstractAction("Help") {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showHelp(panel);
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
				setFields();
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
				setFields();
			}
		});
		panel.add(applyButton);

		return panel;
	}

	/**
	 * Checks that the specified values for the HTTP properties are a valid
	 * combination and, if so, saves them for future use. It does not apply them
	 * to the currently executing Taverna.
	 */
	private void saveSettings() {
		if (useSystemProperties.isSelected()) {
			httpProxyConfiguration.setProperty(PROXY_USE_OPTION,
					USE_SYSTEM_PROPERTIES_OPTION);
		} else if (useNoProxy.isSelected()) {
			httpProxyConfiguration.setProperty(PROXY_USE_OPTION,
					USE_NO_PROXY_OPTION);
		} else {
			if (validateFields()) {
				httpProxyConfiguration.setProperty(PROXY_USE_OPTION,
						USE_SPECIFIED_VALUES_OPTION);
				httpProxyConfiguration.setProperty(TAVERNA_PROXY_HOST,
						proxyHostField.getText());
				httpProxyConfiguration.setProperty(TAVERNA_PROXY_PORT,
						proxyPortField.getText());
				httpProxyConfiguration.setProperty(TAVERNA_PROXY_USER,
						proxyUserField.getText());
				httpProxyConfiguration.setProperty(TAVERNA_PROXY_PASSWORD,
						proxyPasswordField.getText());
				httpProxyConfiguration.setProperty(TAVERNA_NON_PROXY_HOSTS,
						nonProxyHostsArea.getText());
			}
		}
	}

	/**
	 * Validates and, where appropriate formats, the properties values specified
	 * for HTTP Proxy configuration.
	 * 
	 * @return
	 */
	private boolean validateFields() {
		boolean result = true;
		result = result && validateHostField();
		result = result && validatePortField();
		result = result && validateUserField();
		result = result && validatePasswordField();
		result = result && validateNonProxyHostsArea();
		return result;
	}

	/**
	 * Checks that, if a value is specified for non-proxy hosts then a proxy
	 * host has also been specified. Formats the non-proxy hosts string so that
	 * if the user has entered the hosts on separate lines, then the stored
	 * values are separated by bars.
	 * 
	 * @return
	 */
	private boolean validateNonProxyHostsArea() {
		boolean result = true;
		String value = nonProxyHostsArea.getText();
		if ((value != null) && (!value.equals(""))) {
			value = value.replaceAll("\\n", "|");
			nonProxyHostsArea.setText(value);
			result = result
					&& dependsUpon("non-proxy host", "host",
							proxyHostField.getText());
		}
		return result;
	}

	/**
	 * Checks that, if a password has been specified, then a user has also been
	 * specified.
	 * 
	 * @return
	 */
	private boolean validatePasswordField() {
		boolean result = true;
		String value = proxyPasswordField.getText();
		if ((value != null) && !value.isEmpty())
			result = result
					&& dependsUpon("password", "user", proxyHostField.getText());
		return result;
	}

	/**
	 * Checks that if a user has been specified, then a host has also been
	 * specified.
	 * 
	 * @return
	 */
	private boolean validateUserField() {
		boolean result = true;
		String value = proxyUserField.getText();
		if ((value != null) && !value.isEmpty())
			result = result
					&& dependsUpon("user", "host", proxyHostField.getText());
		return result;
	}

	/**
	 * Checks that if a port has been specified then a host has also been
	 * specified. Checks that the port number is a non-negative integer. If the
	 * port has not been specified, then if a host has been specified, the
	 * default value 80 is used.
	 * 
	 * @return
	 */
	private boolean validatePortField() {
		boolean result = true;
		String value = proxyPortField.getText();
		if ((value != null) && (!value.equals(""))) {
			result = result
					&& dependsUpon("port", "host", proxyHostField.getText());
			try {
				int parsedNumber = Integer.parseInt(value);
				if (parsedNumber <= 0) {
					showMessageDialog(this, "The port must be non-negative");
					result = false;
				}
			} catch (NumberFormatException e) {
				showMessageDialog(this, "The port must be an integer");
				result = false;
			}
		} else {
			String hostField = proxyHostField.getText();
			if ((hostField != null) && !hostField.isEmpty())
				proxyPortField.setText("80");
		}
		return result;
	}

	/**
	 * Checks if the targetValue has been specified. If not then a message is
	 * displayed indicating that the dependent cannot be specified with the
	 * target.
	 * 
	 * @param dependent
	 * @param target
	 * @param targetValue
	 * @return
	 */
	private boolean dependsUpon(String dependent, String target,
			String targetValue) {
		boolean result = true;
		if ((targetValue == null) || target.equals("")) {
			showMessageDialog(this, "A " + dependent
					+ " cannot be specified without a " + target);
			result = false;
		}
		return result;
	}

	/**
	 * Could validate the host field e.g. by establishing a connection.
	 * Currently no validation is done.
	 * 
	 * @return
	 */
	private boolean validateHostField() {
		boolean result = true;
		// String value = proxyHostField.getText();
		return result;
	}

	/**
	 * Save the currently set field values (if valid) to the
	 * {@link HttpProxyConfiguration}. Also applies those values to the
	 * currently running Taverna.
	 */
	private void applySettings() {
		if (validateFields()) {
			saveSettings();
			httpProxyConfiguration.changeProxySettings();
		}
	}

	/**
	 * Set the shown field values to those currently in use (i.e. last saved
	 * configuration).
	 */
	private void setFields() {
		shownOption = httpProxyConfiguration.getProperty(PROXY_USE_OPTION);
		useSystemProperties.setSelected(shownOption
				.equals(USE_SYSTEM_PROPERTIES_OPTION));
		useSpecifiedValues.setSelected(shownOption
				.equals(USE_SPECIFIED_VALUES_OPTION));
		useNoProxy.setSelected(shownOption.equals(USE_NO_PROXY_OPTION));
		populateFields();
	}
}
