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

	private final class CustomizeAction extends AbstractAction {

		private final JButton customizeButton;

		public CustomizeAction(JButton customizeButton) {
			super("Customize condition service");
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
			} else if (!(condition instanceof BeanshellActivity)) {
				logger.warn("Can't configure unknown condition service type "
						+ condition.getClass());
				return;
			}

			final BeanshellActivity beanshellActivity = (BeanshellActivity) configuration
					.getCondition();

			Frame owner = Utils.getParentFrame(LoopConfigurationPanel.this);

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

		valueField.setText(properties.getProperty(
				ActivityGenerator.COMPARE_VALUE, ""));

		delayField.setText(properties.getProperty(ActivityGenerator.DELAY,
				"0.0"));

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
						+ "The main service will be invoked repeatedly as "
						+ "long as the <em>customized condition service</em> returns a string equal "
						+ "to <strong>\"true\"</strong> on its output port <code>loop</code>."
						+ "<br><br>"
						+ "Input ports of the condition service will be populated with values from "
						+ "the <em>corresponding output ports</em> of the main service invocation "
						+ "(as long as they are also "
						+ "<strong>connected</strong> in the containing workflow)."
						+ "<br><br> "

						+ "Any <em>matching "
						+ "output ports</em> from the condition service will provide the corresponding "
						+ "<em>inputs</em> to the main service while looping. You will need to provide "
						+ "the <em>initial inputs</em> from the containing workflow."
						+ "</body></html>");
		customPanel.add(helpLabel, gbc);

		gbc.weightx = 0;
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

	protected JLabel delayLabel = new JLabel("adding a delay of ");
	protected JTextField delayField = new JTextField(
			ActivityGenerator.DEFAULT_DELAY_S, 4);
	protected JLabel secondsLabel = new JLabel(" seconds between loops.");

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
		gbc.gridwidth = 3;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		JLabel invokedRepeatedlyLabel = new JLabel(
				"<html><body>The main service will be invoked repeatedly until its output</body></html>");
		configPanel.add(invokedRepeatedlyLabel, gbc);
		gbc.gridy++;
		gbc.ipadx = 4;
		gbc.ipady = 4;

		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		List<String> activityOutputPorts = getActivityOutputPorts();
		portCombo = new JComboBox(activityOutputPorts.toArray());
		configPanel.add(portCombo, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx++;

		configPanel.add(new JPanel(), gbc);
		gbc.gridx++;

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
		configPanel.add(comparisonCombo, gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;

		valueTypeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

		configPanel.add(valueTypeLabel, gbc);
		gbc.gridx = 2;
		gbc.gridwidth = 2;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		configPanel.add(valueField, gbc);
		gbc.weightx = 0.0;

		delayLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		configPanel.add(delayLabel, gbc);
		// gbc.gridy++;
		// gbc.gridwidth = 2;

		gbc.gridx = 1;
		delayField.setHorizontalAlignment(JTextField.RIGHT);
		configPanel.add(delayField, gbc);
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		configPanel.add(secondsLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy++;

		if (activityOutputPorts.isEmpty()) {
			JLabel warningLabel = new JLabel(
					"<html><body><strong>Warning:</strong><br>"
							+ "<i>No output ports detected on the main service, "
							+ "cannot use built-in comparisons.</i></body></html>");
			gbc.gridx = 0;
			gbc.gridwidth = 3;
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

			gbc.gridwidth = 1;
		}

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.weightx = 0.1;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		configPanel.add(portWarning, gbc);

		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.LAST_LINE_END;
		JButton customizeButton = new JButton();
		customizeButton.setAction(new CustomizeAction(customizeButton));
		configPanel.add(customizeButton, gbc);

		// filler
		gbc.gridy++;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 4;
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
	private JLabel portWarning = new JLabel(
			"<html><body><small>Note that for the looping to be able to check this output, "
					+ "the <strong>selected service output</strong> must also be <strong>connected</strong> to "
					+ "another service or workflow output port in the containing workflow.</small></body></html>");

	protected void makeOptions() {
		optionsPanel.removeAll();
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
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
					+ "When looping, any service <em>input ports</em> which <em>names</em> "
					+ "match those of service <em>output ports</em> will get their inputs "
					+ "from the matching outputs of the <em>previous invocation</em>. <br>"

					+ "This can be useful if the main service is a <em>nested workflow</em> "
					+ "which is able to calculate its next input parameters.<br>"

					+ "You will need to provide the <em>initial</em> inputs by "
					+ "connecting the input ports in the containing workflow. You will also "
					+ "need to <strong>connect all service output ports</strong> "
					+ "in the containing workflow.<br>"

					+ "</small></html>");

	protected void makeHeader() {
		headerPanel.removeAll();
		headerPanel.setLayout(new BorderLayout());
		headerPanel.add(new ShadedLabel("Looping for "
				+ processor.getLocalName(), ShadedLabel.ORANGE));
	}
}
