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
package org.apache.taverna.workbench.loop;

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

import net.sf.taverna.t2.activities.beanshell.views.BeanshellConfigurationPanel;
import org.apache.taverna.workbench.helper.HelpEnabledDialog;
import org.apache.taverna.workbench.loop.comparisons.Comparison;
import org.apache.taverna.workbench.ui.Utils;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.profiles.Profile;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * UI for {@link LoopConfiguration}
 *
 * @author Stian Soiland-Reyes
 *
 */
@SuppressWarnings("serial")
public class LoopConfigurationPanel extends JPanel {

	private static final String CONDITION_ACTIVITY = "conditionActivity";
    private static final String DEFAULT_DELAY_S = "0.5";
	protected ObjectNode configuration;

	private static final Scufl2Tools scufl2tools = new Scufl2Tools();
	private ApplicationConfiguration applicationConfig;

	
	protected final Processor processor;

	protected JPanel headerPanel = new JPanel();
	protected JPanel optionsPanel = new JPanel();
	protected JPanel configPanel = new JPanel();
	protected JPanel customPanel = new JPanel();

	protected JLabel valueTypeLabel = new JLabel("the string");

	protected JTextField valueField = new JTextField("", 15);

	protected JLabel delayLabel = new JLabel("adding a delay of ");
	protected JTextField delayField = new JTextField(
			Double.toString(ActivityGenerator.DEFAULT_DELAY_S), 4);
	protected JLabel secondsLabel = new JLabel(" seconds between the loops.");

	private JComboBox<String> portCombo;
	private JComboBox<Comparison> comparisonCombo;
	private JButton customizeButton;

	protected ObjectNode loopLayer;
	private Object Comparison;
	private Activity originalCondition = null;
    private Profile profile;

    public LoopConfigurationPanel(Processor processor, ObjectNode loopLayer,
            Profile profile, ApplicationConfiguration applicationConfig) {
		this.processor = processor;
		this.loopLayer = loopLayer;
        this.profile = profile;
        this.applicationConfig = applicationConfig;
		this.setBorder(new EmptyBorder(10,10,10,10));
		initialise();
		setConfiguration(loopLayer);
	}

	public ObjectNode getConfiguration() {
		uiToConfig();
		return loopLayer.deepCopy();
	}

	private static Logger logger = Logger
			.getLogger(LoopConfigurationPanel.class);

	protected void uiToConfig() {
	    String comparisonStr = configuration.path(ActivityGenerator.COMPARISON).asText();
	    if (comparisonStr.isEmpty()) {
	        comparisonStr = ActivityGenerator.CUSTOM_COMPARISON;
	    }
		if (comparisonStr.equals(ActivityGenerator.CUSTOM_COMPARISON)
				&& ! configuration.path(CONDITION_ACTIVITY).asText().isEmpty()) {
			// Ignore values
		} else {
		    configuration.put("runFirst", true);
			if (portCombo.getSelectedItem() == null) {
			    // unconfigured port
				configuration.remove(ActivityGenerator.COMPARE_PORT);
				configuration.putNull(CONDITION_ACTIVITY);
				return;
			} else {
				configuration.put(ActivityGenerator.COMPARE_PORT,
						((String) portCombo.getSelectedItem()));
			}

			Comparison comparison = (Comparison) comparisonCombo
					.getSelectedItem();
			if (comparison == null) {
				configuration.remove(ActivityGenerator.COMPARISON);
				configuration.putNull(CONDITION_ACTIVITY);
				return;
			} else {
				configuration
						.put(ActivityGenerator.COMPARISON, comparison.getId());
			}
			configuration.put(ActivityGenerator.COMPARE_VALUE, valueField
					.getText());
			configuration.put(ActivityGenerator.DELAY, Double.parseDouble(delayField.getText()));
			configuration.put(ActivityGenerator.IS_FEED_BACK, feedBackCheck.isSelected());

			// Generate activity
			ActivityGenerator activityGenerator = new ActivityGenerator(
					configuration, processor);
			configuration.put(CONDITION_ACTIVITY, activityGenerator.generateActivity().getName());
		}
	}

	public class ResetAction extends AbstractAction {
		public ResetAction() {
			super("Clear");
		}

		public void actionPerformed(ActionEvent e) {
			configuration.putNull(CONDITION_ACTIVITY);
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

			String conditionName = configuration.path(CONDITION_ACTIVITY).asText();
			
			Activity condition = profile.getActivities().getByName(conditionName);
			if (condition == null) {
			    condition = new Activity();
			    profile.getActivities().add(condition);
			    configuration.put(CONDITION_ACTIVITY, condition.getName());
			    condition.setType(ActivityGenerator.BEANSHELL_ACTIVITY);
			    Configuration config = scufl2tools.createConfigurationFor(condition, ActivityGenerator.BEANSHELL_CONFIG);
			} else if (!(condition.getType().equals(ActivityGenerator.BEANSHELL_ACTIVITY))) {
				logger.warn("Can't configure unsupported loop condition of service type "
						+ condition.getType());
				return;
			}

			Frame owner = Utils.getParentFrame(LoopConfigurationPanel.this);
			
			
            final BeanshellConfigurationPanel beanshellConfigView = new BeanshellConfigurationPanel(
                    condition, applicationConfig);
			
			final JDialog dialog = new HelpEnabledDialog(owner, "Customize looping", true);
			dialog.setLayout(new BorderLayout());
			dialog.add(beanshellConfigView, BorderLayout.NORTH);
			dialog.setSize(600, 600);
			JPanel buttonPanel = new JPanel();

			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

			JButton applyButton = new JButton(new AbstractAction() {

				public void actionPerformed(ActionEvent e) {
					if (beanshellConfigView.isConfigurationChanged()) {
						beanshellConfigView.noteConfiguration();
//							beanshellActivity.configure(beanshellConfigView
//									.getConfiguration());
//							configuration.setCondition(beanshellActivity);
						Configuration config = beanshellConfigView.getConfiguration();
						// TODO: Do we need to store this somehow?
						configuration.put(
								ActivityGenerator.COMPARISON,
								ActivityGenerator.CUSTOM_COMPARISON);
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

	public void setConfiguration(ObjectNode configuration) {
		this.configuration = configuration.deepCopy();
		configToUi();
	}

	protected void configToUi() {
		

		String comparisonId;
		
		if (configuration.has(ActivityGenerator.COMPARISON)) {
            comparisonId = configuration.get(ActivityGenerator.COMPARISON)
                    .asText();
		} else {
            comparisonId = ActivityGenerator.CUSTOM_COMPARISON;
		}

		if (comparisonId.equals(ActivityGenerator.CUSTOM_COMPARISON)
				&& configuration.has("conditionalActivity")) {
			configPanel.setVisible(false);
			customPanel.setVisible(true);
		} else {
			configPanel.setVisible(true);
			customPanel.setVisible(false);
		}

		portCombo.setSelectedItem(configuration.get(ActivityGenerator.COMPARE_PORT).asText());
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

		valueField.setText(configuration.get(ActivityGenerator.COMPARE_VALUE).asText());

		if (configuration.has(ActivityGenerator.DELAY)) {
		    delayField.setText(configuration.get(ActivityGenerator.DELAY).asText());
		} else {
		    delayField.setText(DEFAULT_DELAY_S);
		}

		feedBackCheck.setSelected(configuration.get(ActivityGenerator.IS_FEED_BACK).asBoolean());
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
						+ "The service <strong>" + processor.getName() +  "</strong> will be "
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

				"<html><body>The service <strong>" + processor.getName() +  "</strong> " +
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

	private List<String> getActivityOutputPorts() {
	    // Should already be sorted
	    return new ArrayList<>(processor.getOutputPorts().getNames());
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
