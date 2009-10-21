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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import net.sf.taverna.t2.lang.ui.DialogTextArea;
import net.sf.taverna.t2.workbench.configuration.ConfigurationManager;
import net.sf.taverna.t2.workbench.helper.Helper;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 *         The HttpProxyConfigurationPanel provides the user interface to a
 *         {@link HttpProxyConfiguration} to determine how HTTP Connections are
 *         made by Taverna.
 * 
 */
/**
 * @author alanrw
 * 
 */
public class HttpProxyConfigurationPanel extends JPanel {

	/**
	 * 
	 */
	static final long serialVersionUID = 3668473431971125038L;

	private static Logger logger = Logger
			.getLogger(HttpProxyConfigurationUIFactory.class);

	/**
	 * RadioButtons that are in a common ButtonGroup. Selecting one of them
	 * indicates whether the system http proxy settings, the ad hoc specified
	 * values or no proxy settings at all should be used.
	 */
	private JRadioButton useSystemProperties;
	private JRadioButton useSpecifiedValues;
	private JRadioButton useNoProxy;

	/**
	 * JTextFields and one DialogTextArea to hold the settings for the HTTP proxy
	 * properties. The values are only editable if the user picks
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
	private String shownOption = HttpProxyConfiguration.USE_SYSTEM_PROPERTIES_OPTION;

	/**
	 * The size of the field for the JTextFields.
	 */
	private static int TEXTFIELD_SIZE = 25;

	/**
	 * The HttpProxyConfigurationPanel consists of two sub-panels. The
	 * properties panel where the configuration values for HTTP can be specified
	 * and the buttons panel where the more general apply, help etc. appear.
	 */
	public HttpProxyConfigurationPanel() {

		setLayout(new BorderLayout());

		add(createPropertiesPanel(HttpProxyConfiguration.getInstance()),
				BorderLayout.CENTER);

		add(createButtonPanel(), BorderLayout.SOUTH);
	}

	/**
	 * Create a JPanel containing the label followed by the component.
	 * 
	 * @param label
	 * @param entry
	 * @return
	 */
	private JPanel createLabeledComponentPanel(String label, JComponent entry) {

		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
		JLabel l = new JLabel(label);
		l.setLabelFor(entry);
		result.add(l);
		result.add(entry);
		result.setAlignmentX(LEFT_ALIGNMENT);
		return result;
	}

	/**
	 * Creates a JPanel containing a representation of the current HTTP proxy
	 * settings for the specified {@link HttpProxyConfiguration} and also the
	 * capability to alter them
	 * 
	 * @param configurable
	 * @return
	 */
	private JPanel createPropertiesPanel(
			final HttpProxyConfiguration configurable) {

		shownOption = configurable
				.getProperty(HttpProxyConfiguration.PROXY_USE_OPTION);

		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));

		/**
		 * Generate the three radio buttons and put them in a group. Eacgh
		 * button is bound to an action that alters the shownOption and
		 * re-populates the shown HTTP property fields.
		 */
		useNoProxy = new JRadioButton("Do not use a proxy");
		useNoProxy.setAlignmentX(LEFT_ALIGNMENT);
		result.add(useNoProxy);
		ActionListener useNoProxyListener = new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				shownOption = HttpProxyConfiguration.USE_NO_PROXY_OPTION;
				populateFields(configurable);
			}
		};
		useNoProxy.addActionListener(useNoProxyListener);

		useSystemProperties = new JRadioButton("Use System Properties");
		useSystemProperties.setAlignmentX(LEFT_ALIGNMENT);
		result.add(useSystemProperties);
		ActionListener systemPropertiesListener = new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				shownOption = HttpProxyConfiguration.USE_SYSTEM_PROPERTIES_OPTION;
				populateFields(configurable);
			}
		};
		useSystemProperties.addActionListener(systemPropertiesListener);

		useSpecifiedValues = new JRadioButton("Use Specified Values");
		useSpecifiedValues.setAlignmentX(LEFT_ALIGNMENT);
		result.add(useSpecifiedValues);
		ActionListener specifiedValuesListener = new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				shownOption = HttpProxyConfiguration.USE_SPECIFIED_VALUES_OPTION;
				populateFields(configurable);
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
		result.add(createLabeledComponentPanel("Proxy Host", proxyHostField));

		proxyPortField = new JTextField(TEXTFIELD_SIZE);
		result.add(createLabeledComponentPanel("Proxy Port", proxyPortField));

		proxyUserField = new JTextField(TEXTFIELD_SIZE);
		result.add(createLabeledComponentPanel("Proxy User", proxyUserField));

		proxyPasswordField = new JTextField(TEXTFIELD_SIZE);
		result.add(createLabeledComponentPanel("Proxy Password",
				proxyPasswordField));

		nonProxyHostsArea = new DialogTextArea(10, 40);
		nonProxyScrollPane = new JScrollPane(nonProxyHostsArea);
		nonProxyScrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		nonProxyScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		nonProxyScrollPane.setPreferredSize(new Dimension(300, 500));
		result.add(new JLabel("Non-proxy Hosts"));
		result.add(nonProxyScrollPane);

		resetFields(configurable);
		return result;
	}

	/**
	 * Populate the fields in the property panel according to which option is
	 * being shown and the stored values within the
	 * {@link HttpProxyConfiguration}.
	 * 
	 * @param configurable
	 */
	private void populateFields(HttpProxyConfiguration configurable) {

		/**
		 * Editing of the property fields is only available when the option is
		 * to use the specified values.
		 */
		boolean editingEnabled = shownOption
				.equals(HttpProxyConfiguration.USE_SPECIFIED_VALUES_OPTION);

		if (shownOption
				.equals(HttpProxyConfiguration.USE_SYSTEM_PROPERTIES_OPTION)) {
			proxyHostField.setText(configurable
					.getProperty(HttpProxyConfiguration.SYSTEM_PROXY_HOST));
			proxyPortField.setText(configurable
					.getProperty(HttpProxyConfiguration.SYSTEM_PROXY_PORT));
			proxyUserField.setText(configurable
					.getProperty(HttpProxyConfiguration.SYSTEM_PROXY_USER));
			proxyPasswordField.setText(configurable
					.getProperty(HttpProxyConfiguration.SYSTEM_PROXY_PASSWORD));
			nonProxyHostsArea
					.setText(configurable
							.getProperty(HttpProxyConfiguration.SYSTEM_NON_PROXY_HOSTS));
		} else if (shownOption
				.equals(HttpProxyConfiguration.USE_SPECIFIED_VALUES_OPTION)) {
			proxyHostField.setText(configurable
					.getProperty(HttpProxyConfiguration.TAVERNA_PROXY_HOST));
			proxyPortField.setText(configurable
					.getProperty(HttpProxyConfiguration.TAVERNA_PROXY_PORT));
			proxyUserField.setText(configurable
					.getProperty(HttpProxyConfiguration.TAVERNA_PROXY_USER));
			proxyPasswordField
					.setText(configurable
							.getProperty(HttpProxyConfiguration.TAVERNA_PROXY_PASSWORD));
			nonProxyHostsArea
					.setText(configurable
							.getProperty(HttpProxyConfiguration.TAVERNA_NON_PROXY_HOSTS));
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
			public void actionPerformed(ActionEvent arg0) {
				resetFields(HttpProxyConfiguration.getInstance());
			}
		});
		panel.add(resetButton);

		/**
		 * The applyButton applies the shown field values to the
		 * {@link HttpProxyConfiguration} and saves them for future.
		 */
		JButton applyButton = new JButton(new AbstractAction("Apply") {
			public void actionPerformed(ActionEvent arg0) {
				applySettings();
				resetFields(HttpProxyConfiguration.getInstance());
			}
		});
		panel.add(applyButton);

		return panel;
	}

	/**
	 * saveSettings checks that the specified values for the HTTP properties are
	 * a valid combination and, if so, saves them for future use. It does not
	 * apply them to the currently executing Taverna.
	 */
	private void saveSettings() {
		HttpProxyConfiguration conf = HttpProxyConfiguration.getInstance();
		if (useSystemProperties.isSelected()) {
			conf.setProperty(HttpProxyConfiguration.PROXY_USE_OPTION,
					HttpProxyConfiguration.USE_SYSTEM_PROPERTIES_OPTION);
		} else if (useNoProxy.isSelected()) {
			conf.setProperty(HttpProxyConfiguration.PROXY_USE_OPTION,
					HttpProxyConfiguration.USE_NO_PROXY_OPTION);
		} else {
			if (validateFields()) {
				conf.setProperty(HttpProxyConfiguration.PROXY_USE_OPTION,
						HttpProxyConfiguration.USE_SPECIFIED_VALUES_OPTION);
				conf.setProperty(HttpProxyConfiguration.TAVERNA_PROXY_HOST,
						proxyHostField.getText());
				conf.setProperty(HttpProxyConfiguration.TAVERNA_PROXY_PORT,
						proxyPortField.getText());
				conf.setProperty(HttpProxyConfiguration.TAVERNA_PROXY_USER,
						proxyUserField.getText());
				conf.setProperty(HttpProxyConfiguration.TAVERNA_PROXY_PASSWORD,
						proxyPasswordField.getText());
				conf.setProperty(
						HttpProxyConfiguration.TAVERNA_NON_PROXY_HOSTS,
						nonProxyHostsArea.getText());
			}
		}
		try {
			ConfigurationManager.getInstance().store(conf);
		} catch (Exception e) {
			logger.error("Error storing updated configuration");
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
	 * host has also been specified.
	 * 
	 * Formats the non-proxy hosts string so that if the user has entered the
	 * hosts on separate lines, then the stored values are separated by bars.
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
					&& dependsUpon("non-proxy host", "host", proxyHostField
							.getText());
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
		if ((value != null) && (!value.equals(""))) {
			result = result
					&& dependsUpon("password", "user", proxyHostField.getText());
		}
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
		if ((value != null) && (!value.equals(""))) {
			result = result
					&& dependsUpon("user", "host", proxyHostField.getText());
		}
		return result;
	}

	/**
	 * Checks that if a port has been specified then a host has also been
	 * specified. Checks that the port number is a non-negative integer.
	 * 
	 * If the port has not been specified, then if a host has been specified,
	 * the default value 80 is used.
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
					JOptionPane.showMessageDialog(this,
							"The port must be non-negative");
					result = false;
				}
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this,
						"The port must be an integer");
				result = false;
			}
		} else {
			String hostField = proxyHostField.getText();
			if ((hostField != null) && !hostField.equals("")) {
				proxyPortField.setText("80");
			}
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
			JOptionPane.showMessageDialog(this, "A " + dependent
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
		String value = proxyHostField.getText();
		return result;
	}

	/**
	 * Saves the currently shown field values (if valid) to the
	 * {@link HttpProxyConfiguration}. Also applies those values to the
	 * currently running Taverna.
	 */
	private void applySettings() {
		HttpProxyConfiguration conf = HttpProxyConfiguration.getInstance();
		if (validateFields()) {
			saveSettings();
			conf.changeProxySettings();
		}
	}

	/**
	 * Resets the shown field values to those currently in use.
	 * 
	 * @param configurable
	 */
	private void resetFields(HttpProxyConfiguration configurable) {
		shownOption = configurable
				.getProperty(HttpProxyConfiguration.PROXY_USE_OPTION);
		useSystemProperties.setSelected(shownOption
				.equals(HttpProxyConfiguration.USE_SYSTEM_PROPERTIES_OPTION));
		useSpecifiedValues.setSelected(shownOption
				.equals(HttpProxyConfiguration.USE_SPECIFIED_VALUES_OPTION));
		useNoProxy.setSelected(shownOption
				.equals(HttpProxyConfiguration.USE_NO_PROXY_OPTION));
		populateFields(configurable);
	}
}
