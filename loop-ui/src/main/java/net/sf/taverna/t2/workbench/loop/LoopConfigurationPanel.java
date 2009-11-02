/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.workbench.loop;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellConfigView;
import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.helper.Helper;
import net.sf.taverna.t2.workbench.loop.comparisons.Comparison;
import net.sf.taverna.t2.workbench.ui.Utils;
import net.sf.taverna.t2.workbench.ui.impl.DataflowSelectionManager;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualViewComponent;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Loop;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.LoopConfiguration;

import org.apache.log4j.Logger;

/**
 * UI for {@link LoopConfiguration}
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class LoopConfigurationPanel extends JPanel {

	protected LoopConfiguration configuration;
	private EditManager editManager = EditManager.getInstance();

	private Edits edits = editManager.getEdits();

	protected final Processor processor;

	protected JPanel headerPanel = new JPanel();
	protected JPanel optionsPanel = new JPanel();
	protected JPanel configPanel = new JPanel();
	protected JPanel customPanel = new JPanel();

	protected Loop loopLayer;
	private Object Comparison;
	private Activity<?> originalCondition = null;

	public LoopConfigurationPanel(Processor processor, Loop loopLayer) {
		this.processor = processor;
		this.loopLayer = loopLayer;
		initialise();
		setConfiguration(loopLayer.getConfiguration());
	}

	public LoopConfiguration getConfiguration() {
		uiToConfig();
		return configuration.clone();
	}

	private static Logger logger = Logger
			.getLogger(LoopConfigurationPanel.class);

	protected void uiToConfig() {
		Properties properties = configuration.getProperties();
		if (properties.getProperty(ActivityGenerator.COMPARISON,
				ActivityGenerator.CUSTOM_COMPARISON).equals(
				ActivityGenerator.CUSTOM_COMPARISON)
				&& configuration.getCondition() != null) {
			// Ignore values
		} else {
			configuration.setRunFirst(true);
			if (portCombo.getSelectedItem() == null) {
				properties.remove(ActivityGenerator.COMPARE_PORT);
				configuration.setCondition(null);
				return;
			} else {
				properties.put(ActivityGenerator.COMPARE_PORT,
						((String) portCombo.getSelectedItem()));
			}

			Comparison comparison = (Comparison) comparisonCombo
					.getSelectedItem();
			if (comparison == null) {
				properties.remove(ActivityGenerator.COMPARISON);
				configuration.setCondition(null);
				return;
			} else {
				properties
						.put(ActivityGenerator.COMPARISON, comparison.getId());
			}
			properties.put(ActivityGenerator.COMPARE_VALUE, valueField
					.getText());

			properties.put(ActivityGenerator.IS_FEED_BACK, Boolean
					.toString(feedBackCheck.isSelected()));

			// Generate activity
			ActivityGenerator activityGenerator = new ActivityGenerator(
					properties, getFirstProcessorActivity());
			configuration.setCondition(activityGenerator.generateActivity());
		}
	}

	public class ResetAction extends AbstractAction {
		public ResetAction() {
			super("Clear");
		}

		public void actionPerformed(ActionEvent e) {
			configuration.setCondition(null);
			configToUi();
		}
	}

	private final class CustomizeAction extends AbstractAction {

		private final JButton customizeButton;

		public CustomizeAction(JButton customizeButton) {
			super("Customize");
			this.customizeButton = customizeButton;
		}

		private DataflowSelectionManager dataflowSelectionManager = DataflowSelectionManager
				.getInstance();

		private FileManager fileManager = FileManager.getInstance();

		public void actionPerformed(ActionEvent e) {
			uiToConfig();

			Activity<?> condition = configuration.getCondition();
			if (condition == null) {
				BeanshellActivity activity = new BeanshellActivity();
				try {
					activity
							.configure(new BeanshellActivityConfigurationBean());
				} catch (ActivityConfigurationException e1) {
					logger.warn("Can't configure new beanshell activity");
					return;
				}
				configuration.setCondition(activity);
			} else if (! (condition instanceof BeanshellActivity)) {
				logger.warn("Can't configure unknown conditional activity type " + condition.getClass());
				return;
			}

			final BeanshellActivity beanshellActivity = (BeanshellActivity) configuration.getCondition();

			Frame owner = Utils
					.getParentFrame(LoopConfigurationPanel.this);

			final BeanshellConfigView beanshellConfigView = new BeanshellConfigView(
					beanshellActivity);
			final JDialog dialog = new JDialog(owner, true);
			dialog.setLayout(new BorderLayout());
			Helper.setKeyCatcher(dialog);
			dialog.add(beanshellConfigView, BorderLayout.NORTH);
			dialog.setSize(600, 600);
			JPanel buttonPanel = new JPanel();

			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

			JButton applyButton = new JButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					if (beanshellConfigView.isConfigurationChanged()) {
						try {
							beanshellConfigView.noteConfiguration();
							beanshellActivity.configure(beanshellConfigView
									.getConfiguration());
							configuration.setCondition(beanshellActivity);
							configuration.getProperties().put(
									ActivityGenerator.COMPARISON,
									ActivityGenerator.CUSTOM_COMPARISON);
						} catch (ActivityConfigurationException e1) {
							logger
									.warn("Can't configure conditional beanshell",
											e1);
						}
					}
					dialog.setVisible(false);
					configToUi();					
				}

			});
			applyButton.setText("Apply");

			buttonPanel.add(applyButton);
			JButton closeButton = new JButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
				}
			});
			closeButton.setText("Cancel");
			buttonPanel.add(closeButton);
			dialog.add(buttonPanel, BorderLayout.SOUTH);
			dialog.setLocationRelativeTo(customizeButton);
			dialog.setVisible(true);

		}
	}

	public void setConfiguration(LoopConfiguration configuration) {
		this.configuration = configuration.clone();
		configToUi();
	}

	protected void configToUi() {
		Properties properties = configuration.getProperties();

		String comparisonId = properties.getProperty(
				ActivityGenerator.COMPARISON,
				ActivityGenerator.CUSTOM_COMPARISON);

		if (comparisonId.equals(ActivityGenerator.CUSTOM_COMPARISON)
				&& configuration.getCondition() != null) {
			configPanel.setVisible(false);
			customPanel.setVisible(true);
		} else {
			configPanel.setVisible(true);
			customPanel.setVisible(false);
		}

		portCombo.setSelectedItem(properties
				.getProperty(ActivityGenerator.COMPARE_PORT));
		if (portCombo.getSelectedIndex() == -1
				&& portCombo.getModel().getSize() > 0) {
			portCombo.setSelectedIndex(0);
		}

		Comparison comparison = ActivityGenerator
				.getComparisonById(comparisonId);
		comparisonCombo.setSelectedItem(comparison);

		valueField.setText(properties.getProperty(
				ActivityGenerator.COMPARE_VALUE, ""));
		feedBackCheck.setSelected(Boolean.parseBoolean(properties
				.getProperty(ActivityGenerator.IS_FEED_BACK)));
	}

	private void initialise() {
		removeAll();
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.gridx = 0;
		gbc.weightx = 0.1;

		makeHeader();
		add(headerPanel, gbc);

		makeConfigPanel();
		gbc.weighty = 0.1;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.BOTH;
		add(configPanel, gbc);

		makeCustomPanel();
		add(customPanel, gbc);

		makeOptions();
		add(optionsPanel, gbc);
	}

	protected void makeCustomPanel() {
		customPanel.removeAll();
		customPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;

		JLabel helpLabel = new JLabel(
				"<html><em>"
						+ "The processor's activity will be invoked repeatedly as<br>"
						+ "long as the conditional activity returns a string equal<br>"
						+ "to 'true' on it's output port <code>loop</code>."
						+ "<br><br>"
						+ "Any conditional input ports will be populated with values from<br>"
						+ "the matching output ports of the processor's activity, <br>"
						+ "and outputs from the conditional will replace the original<br>"
						+ "inputs to the processor's activity.</em></html>");
		customPanel.add(helpLabel, gbc);

		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		JButton customizeButton = new JButton();
		customizeButton.setAction(new CustomizeAction(customizeButton));
		customPanel.add(customizeButton, gbc);

		gbc.gridx++;
		customPanel.add(new JButton(new ResetAction()), gbc);

	}

	protected JLabel valueTypeLabel = new JLabel("the string");

	protected JTextField valueField = new JTextField("", 15);
	private JComboBox portCombo;
	private JComboBox comparisonCombo;

	protected void makeConfigPanel() {
		configPanel.removeAll();
		configPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		// gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;

		JLabel invokedRepeatedlyLabel = new JLabel(
				"The activity will be invoked repeatedly until its output");
		configPanel.add(invokedRepeatedlyLabel, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 1;

		List<String> activityOutputPorts = getActivityOutputPorts();

		portCombo = new JComboBox(activityOutputPorts.toArray());
		configPanel.add(portCombo, gbc);
		gbc.gridx++;

		comparisonCombo = new JComboBox(ActivityGenerator.comparisons.toArray());
		configPanel.add(comparisonCombo, gbc);
		gbc.gridy++;
		gbc.gridx = 0;

		configPanel.add(valueTypeLabel, gbc);
		gbc.gridx++;
		configPanel.add(valueField, gbc);
		gbc.gridy++;
		gbc.gridx = 0;

		if (activityOutputPorts.isEmpty()) {
			JLabel warningLabel = new JLabel(
					"<html><strong>Warning:</strong><br>"
							+ "<i>No activity output ports detected,<br>"
							+ "can't use built-in comparisons.</i></html>");
			gbc.gridwidth = 3;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy++;
			configPanel.add(warningLabel, gbc);
			invokedRepeatedlyLabel.setVisible(false);
			portCombo.setVisible(false);
			comparisonCombo.setVisible(false);
			valueTypeLabel.setVisible(false);
			valueField.setVisible(false);
			gbc.gridwidth = 1;
		}

		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LAST_LINE_END;
		JButton customizeButton = new JButton();
		customizeButton.setAction(new CustomizeAction(customizeButton));
		configPanel.add(customizeButton, gbc);

		// filler
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 3;
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		configPanel.add(Box.createGlue(), gbc);

	}

	private Activity<?> getFirstProcessorActivity() {
		List<? extends Activity<?>> activityList = processor.getActivityList();
		if (activityList.isEmpty()) {
			return null;
		}
		return activityList.get(0);
	}

	private List<String> getActivityOutputPorts() {
		// TODO: Support multiple activities
		Activity<?> activity = getFirstProcessorActivity();
		if (activity == null) {
			return new ArrayList<String>();
		}
		List<String> ports = new ArrayList<String>();
		for (OutputPort outPort : activity.getOutputPorts()) {
			ports.add(outPort.getName());
		}
		Collections.sort(ports);
		return ports;
	}

	protected JCheckBox feedBackCheck = new JCheckBox(
			"Feed back matching ports");

	protected void makeOptions() {
		optionsPanel.removeAll();
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		optionsPanel.add(feedBackCheck, gbc);

		gbc.gridy = 1;
		String help = "<html><small>"
				+ "If an output port's name "
				+ "matches that of an input port,<br> "
				+ "on the next looped invocation the input value will be replaced <br>"
				+ "with the previous output value of the matching output port."
				+ "</small></html>";
		optionsPanel.add(new JLabel(help), gbc);
	}

	protected void makeHeader() {
		headerPanel.removeAll();
		headerPanel.setLayout(new BorderLayout());
		headerPanel.add(new ShadedLabel("Looping for "
				+ processor.getLocalName(), ShadedLabel.ORANGE));
	}
}
