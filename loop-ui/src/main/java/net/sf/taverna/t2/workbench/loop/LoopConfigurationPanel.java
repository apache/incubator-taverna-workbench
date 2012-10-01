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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.activities.beanshell.views.BeanshellConfigView;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.loop.comparisons.Comparison;
import net.sf.taverna.t2.workbench.ui.Utils;
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
@SuppressWarnings("serial")
public class LoopConfigurationPanel extends JPanel {

	private static final String DEFAULT_DELAY_S = "0.5";
	protected LoopConfiguration configuration;

	protected final Processor processor;

	protected JPanel headerPanel = new JPanel();
	protected JPanel optionsPanel = new JPanel();
	protected JPanel configPanel = new JPanel();
	protected JPanel customPanel = new JPanel();

	protected JLabel valueTypeLabel = new JLabel("the string");

	protected JTextField valueField = new JTextField("", 15);

	protected JLabel delayLabel = new JLabel("adding a delay of ");
	protected JTextField delayField = new JTextField(
			ActivityGenerator.DEFAULT_DELAY_S, 4);
	protected JLabel secondsLabel = new JLabel(" seconds between the loops.");

	private JComboBox portCombo;
	private JComboBox comparisonCombo;
	private JButton customizeButton;

	protected Loop loopLayer;
	private Object Comparison;
	private Activity<?> originalCondition = null;

	public LoopConfigurationPanel(Processor processor, Loop loopLayer) {
		this.processor = processor;
		this.loopLayer = loopLayer;
		this.setBorder(new EmptyBorder(10,10,10,10));
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
			properties.put(ActivityGenerator.DELAY, delayField.getText());

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

	private final class CustomizeAction implements ActionListener {

//		public CustomizeAction() {
//			super();
//			//putValue(NAME, "Customise loop condition");
//		}

		public void actionPerformed(ActionEvent e) {
			uiToConfig();

			Activity<?> condition = configuration.getCondition();
			if (condition == null) {
				BeanshellActivity activity = new BeanshellActivity(null);
				try {
					activity
							.configure(new BeanshellActivityConfigurationBean());
				} catch (ActivityConfigurationException e1) {
					logger.warn("Can't configure new beanshell activity");
					return;
				}
				configuration.setCondition(activity);
			} else if (!(condition instanceof BeanshellActivity)) {
				logger.warn("Can't configure unsupported loop condition of service type "
						+ condition.getClass());
				return;
			}

			final BeanshellActivity beanshellActivity = (BeanshellActivity) configuration
					.getCondition();

			Frame owner = Utils.getParentFrame(LoopConfigurationPanel.this);

			final BeanshellConfigView beanshellConfigView = new BeanshellConfigView(
					beanshellActivity);

			final JDialog dialog = new HelpEnabledDialog(owner, "Customize looping", true);
			dialog.setLayout(new BorderLayout());
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
							logger.warn("Can't configure condition beanshell",
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
		if (comparisonCombo.getSelectedIndex() == -1
				&& comparisonCombo.getModel().getSize() > 0) {
			comparisonCombo.setSelectedIndex(0);
		}

		valueField.setText(properties.getProperty(
				ActivityGenerator.COMPARE_VALUE, ""));

		delayField.setText(properties.getProperty(ActivityGenerator.DELAY,
				DEFAULT_DELAY_S));

		feedBackCheck.setSelected(Boolean.parseBoolean(properties
				.getProperty(ActivityGenerator.IS_FEED_BACK)));
		updateFeedbackHelp();
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
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		JLabel helpLabel = new JLabel(
				"<html><body>"
						+ "The service <strong>" + processor.getLocalName() +  "</strong> will be "
						+ "invoked repeatedly as "
						+ "long as the <em>customized loop condition service</em> returns a string equal "
						+ "to <strong>\"true\"</strong> on its output port <code>loop</code>."
//						+ "<br><br>"
//						+ "Input ports of the condition service will be populated with values from "
//						+ "the <em>corresponding output ports</em> of the main service invocation "
//						+ "(as long as they are also "
//						+ "<strong>connected</strong> in the containing workflow)."
//						+ "<br><br> "
//
//						+ "Any <em>matching "
//						+ "output ports</em> from the condition service will provide the corresponding "
//						+ "<em>inputs</em> to the main service while looping. You will need to connect "
//						+ "the <em>initial inputs</em> in the containing workflow."
						+ "</body></html>");
		customPanel.add(helpLabel, gbc);

		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		JPanel customiseButtonPanel = new JPanel(new FlowLayout());
		customiseButtonPanel.setBorder(new EmptyBorder(10,0,0,0));
		customizeButton = new JButton("Customize loop condition");
		customizeButton.addActionListener(new CustomizeAction());
		customiseButtonPanel.add(customizeButton);
		customiseButtonPanel.add(new JButton(new ResetAction()));
		customPanel.add(customiseButtonPanel, gbc);

	}

	protected void makeConfigPanel() {
		configPanel.removeAll();
		configPanel.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 4;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel invokedRepeatedlyLabel = new JLabel(

				"<html><body>The service <strong>" + processor.getLocalName() +  "</strong> " +
						"will be invoked repeatedly <em>until</em> its output port</body></html>");
		invokedRepeatedlyLabel.setBorder(new EmptyBorder(10,0,10,0)); // give some top and bottom border to the label
		configPanel.add(invokedRepeatedlyLabel, gbc);
		gbc.ipadx = 4;
		gbc.ipady = 4;

		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		List<String> activityOutputPorts = getActivityOutputPorts();
		portCombo = new JComboBox(activityOutputPorts.toArray());
		configPanel.add(portCombo, gbc);

		comparisonCombo = new JComboBox(ActivityGenerator.comparisons.toArray());
		comparisonCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Comparison selectedComparison = (Comparison) comparisonCombo
						.getSelectedItem();
				if (selectedComparison != null) {
					valueTypeLabel.setText("the "
							+ selectedComparison.getValueType());
				}
			}
		});
		if (comparisonCombo.getSelectedIndex() == -1) {
			comparisonCombo.setSelectedIndex(0);
		}
		gbc.gridx = 1;
		gbc.gridy = 1;
		configPanel.add(comparisonCombo, gbc);

		gbc.gridx = 2;
		gbc.gridy = 1;
		valueTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		configPanel.add(valueTypeLabel, gbc);

		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.weightx = 0.5; // request all extra space
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(valueField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0.0;
		configPanel.add(delayLabel, gbc);

		gbc.gridx = 1;
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 0.0;
		delayField.setHorizontalAlignment(JTextField.RIGHT);
		configPanel.add(delayField, gbc);

		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.weightx = 0.5; // request all extra space
		gbc.fill = GridBagConstraints.HORIZONTAL;
		configPanel.add(secondsLabel, gbc);

		if (activityOutputPorts.isEmpty()) {
			JLabel warningLabel = new JLabel(
					"<html><body><strong>Warning:</strong><br>"
							+ "<i>No single value output ports detected on the main service, "
							+ "cannot use built-in comparisons. You may still add a customized " +
									"looping script</i></body></html>");
			gbc.gridx = 0;
			gbc.gridy++;
			gbc.gridwidth = 4;
			gbc.weightx = 0.1;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy++;
			configPanel.add(warningLabel, gbc);
			invokedRepeatedlyLabel.setVisible(false);
			portCombo.setVisible(false);
			comparisonCombo.setVisible(false);
			portWarning.setVisible(false);
			valueTypeLabel.setVisible(false);
			valueField.setVisible(false);
			delayField.setVisible(false);
			delayLabel.setVisible(false);
			secondsLabel.setVisible(false);
		}

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0.1;
		gbc.gridwidth = 4;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(10, 0, 10, 0);
		configPanel.add(portWarning, gbc);

		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 4;
		gbc.anchor = GridBagConstraints.LAST_LINE_END;
		JPanel customiseButtonPanel = new JPanel(new FlowLayout());
		customizeButton = new JButton("Customize loop condition");
		customizeButton.addActionListener(new CustomizeAction());
		customiseButtonPanel.add(customizeButton);
		configPanel.add(customiseButtonPanel, gbc);

		// filler
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 4;
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		gbc.gridwidth = 4;
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
			if (outPort.getDepth() == 0) {
				ports.add(outPort.getName());
			}
		}
		Collections.sort(ports);
		return ports;
	}

	protected JCheckBox feedBackCheck = new JCheckBox(
			"Enable output port to input port feedback");
	private JLabel portWarning = new JLabel(
			"<html><body><small>Note that for Taverna to be able to execute this loop, "
					+ "the output port <strong>must</strong> be connected to an input of another service "
					+ "or a workflow output port.</small></body></html>");

	protected void makeOptions() {
		optionsPanel.removeAll();
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		feedBackCheck.setBorder(new EmptyBorder(0,0,10,0));
		optionsPanel.add(feedBackCheck, gbc);
		feedBackCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateFeedbackHelp();
			}
		});
		updateFeedbackHelp();

		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		optionsPanel.add(feedbackHelp, gbc);
	}

	protected void updateFeedbackHelp() {
		feedbackHelp.setEnabled(feedBackCheck.isSelected());
		Color color;
		if (feedBackCheck.isSelected()) {
			color = valueTypeLabel.getForeground();
		} else {
			// Work around
			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4303706
			// and assume gray is the 'disabled' colour in our Look n Feel
			color = Color.gray;
		}
		feedbackHelp.setForeground(color);

	}

	JLabel feedbackHelp = new JLabel(
			"<html><small>"
					+ "<p>When feedback is enabled, the value of the output port is used as input " +
							"the next time the loop in invoked. The input and output ports used for feedback "
					+ "<strong>must</strong> have the same <strong>name</strong> and <strong>depth</strong>."
					+ "</p><br>"

					+ "<p>Feedback can be useful for looping over a nested workflow, "
					+ "where the nested workflow's output determines its next input value.</p><br>"

					+ "<p>In order to use feedback looping, you must provide an initial value to the input port by "
					+ "connecting it to the output of a previous service or workflow input port."
					+ "The output port used as feedback also has to be connected to a downstream service " +
							"or a workflow output port.</p>"

					+ "</small></html>");

	protected void makeHeader() {
		headerPanel.removeAll();
		headerPanel.setLayout(new BorderLayout());
		//headerPanel.add(new ShadedLabel("Looping for service"
		//		+ processor.getLocalName(), ShadedLabel.ORANGE));
	}
}
