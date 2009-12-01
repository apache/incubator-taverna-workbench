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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.loop.comparisons.Comparison;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Loop;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.LoopConfiguration;

import org.apache.log4j.Logger;

/**
 * View of a processor, including it's iteration stack, activities, etc.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class LoopContextualView extends ContextualView {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(LoopContextualView.class);

	private EditManager editManager = EditManager.getInstance();

	private FileManager fileManager = FileManager.getInstance();

	private Edits edits = EditManager.getInstance().getEdits();

	private Loop loopLayer;

	private JPanel panel;

	private Processor processor;

	public LoopContextualView(Loop loopLayer) {
		super();
		this.loopLayer = loopLayer;
		processor = loopLayer.getProcessor();
		initialise();
		initView();
	}

	@Override
	public Action getConfigureAction(Frame owner) {
		return new ConfigureAction(owner);
	}

	@Override
	public void refreshView() {
		initialise();
	}

	private void initialise() {
		if (panel == null) {
			panel = new JPanel();
		} else {
			panel.removeAll();
		}
		panel.setLayout(new GridBagLayout());
		updateUIByConfig();
	}

	@Override
	public JComponent getMainFrame() {
		return panel;
	}

	@Override
	public String getViewTitle() {
		return "Loop of " + processor.getLocalName();
	}

	protected void updateUIByConfig() {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0.1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		StringBuilder description = new StringBuilder("<html><body>");
		Properties properties = loopLayer.getConfiguration().getProperties();
		if (properties.getProperty(ActivityGenerator.COMPARISON,
				ActivityGenerator.CUSTOM_COMPARISON).equals(
				ActivityGenerator.CUSTOM_COMPARISON)) {
			Activity<?> condition = loopLayer.getConfiguration().getCondition();
			if (condition != null) {
				description.append("Looping using custom conditional ");
				if (condition instanceof BeanshellActivity) {
					String script = ((BeanshellActivity)condition).getConfiguration().getScript();
					if (script != null) {
						if (script.length() <= 100) {
							description.append("<pre>\n");
							description.append(script);
							description.append("</pre>\n");
						}
					}
				}
			} else {
				description.append("<i>Unconfigured, will not loop</i>");
			}
		} else {
			description.append("The service will be invoked repeatedly ");
			description.append("until<br> its output <strong>");
			description.append(properties
					.getProperty(ActivityGenerator.COMPARE_PORT));
			description.append("</strong> ");

			Comparison comparison = ActivityGenerator
					.getComparisonById(properties
							.getProperty(ActivityGenerator.COMPARISON));
			description.append(comparison.getName());
			
			description.append(" the " + comparison.getValueType() + ": <pre>");
			description.append(properties
					.getProperty(ActivityGenerator.COMPARE_VALUE));
			description.append("</pre>");
			
			String delay = properties.getProperty(ActivityGenerator.DELAY, "");
			try {
				if (Double.parseDouble(delay) > 0) {
					description.append("adding a delay of " + delay
							+ " seconds between loops.");
				}
			} catch (NumberFormatException ex) {
			}
		}
		description.append("</body></html>");

		panel.add(new JLabel(description.toString()), gbc);
		gbc.gridy++;

		revalidate();
	}

	protected class ConfigureAction extends AbstractAction {
		private final Frame owner;

		protected ConfigureAction(Frame owner) {
			super("Configure");
			this.owner = owner;
		}

		public void actionPerformed(ActionEvent e) {
			String title = "Looping for " + processor.getLocalName();
			final JDialog dialog = new JDialog(owner, title, true);
			LoopConfigurationPanel loopConfigurationPanel = new LoopConfigurationPanel(
					processor, loopLayer);
			dialog.add(loopConfigurationPanel, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());

			JButton okButton = new JButton(new OKAction(dialog,
					loopConfigurationPanel));
			buttonPanel.add(okButton);

			JButton resetButton = new JButton(new ResetAction(
					loopConfigurationPanel));
			buttonPanel.add(resetButton);

			JButton cancelButton = new JButton(new CancelAction(dialog));
			buttonPanel.add(cancelButton);

			dialog.add(buttonPanel, BorderLayout.SOUTH);
			dialog.setSize(450, 450);
			dialog.setLocationRelativeTo(null);
			dialog.setVisible(true);
		}

		protected class CancelAction extends AbstractAction {
			private final JDialog dialog;

			protected CancelAction(JDialog dialog) {
				super("Cancel");
				this.dialog = dialog;
			}

			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				refreshView();
			}

		}

		protected class OKAction extends AbstractAction {
			private final JDialog dialog;
			private final LoopConfigurationPanel loopConfigurationPanel;

			protected OKAction(JDialog dialog,
					LoopConfigurationPanel loopConfigurationPanel) {
				super("OK");
				this.dialog = dialog;
				this.loopConfigurationPanel = loopConfigurationPanel;
			}

			public void actionPerformed(ActionEvent e) {
				try {

					List<Edit<?>> compoundEdit = new ArrayList<Edit<?>>();
					LoopConfiguration configuration = loopConfigurationPanel
							.getConfiguration();
					compoundEdit.add(edits.getConfigureEdit(loopLayer,
							configuration));
					compoundEdit.addAll(checkPortMappings(configuration
							.getCondition()));

					editManager.doDataflowEdit(
							fileManager.getCurrentDataflow(), new CompoundEdit(
									compoundEdit));
					dialog.setVisible(false);
					refreshView();
				} catch (RuntimeException ex) {
					logger.warn("Could not configure looping", ex);
					JOptionPane.showMessageDialog(owner,
							"Could not configure looping",
							"An error occured when configuring looping: "
									+ ex.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				} catch (EditException ex) {
					logger.warn("Could not configure looping", ex);
					JOptionPane.showMessageDialog(owner,
							"Could not configure looping",
							"An error occured when configuring looping: "
									+ ex.getMessage(),
							JOptionPane.ERROR_MESSAGE);
				}
			}

			protected List<Edit<?>> checkPortMappings(
					Activity<?> conditionActivity) {

				List<Edit<?>> compoundEdit = new ArrayList<Edit<?>>();
				if (processor.getActivityList().isEmpty()) {
					return compoundEdit;
				}
				Set<String> newInputs = new HashSet<String>();
				Set<String> newOutputs = new HashSet<String>();

				Activity<?> firstProcessorActivity;
				firstProcessorActivity = processor.getActivityList().get(0);
				if (conditionActivity != null) {
					for (OutputPort condOutPort : conditionActivity
							.getOutputPorts()) {
						String portName = condOutPort.getName();
						Map<String, String> mapping = firstProcessorActivity
								.getInputPortMapping();
						if (!mapping.containsKey(portName)) {
							if (mapping.containsKey(portName)) {
								logger.warn("Can't re-map input for "
										+ "conditional output " + portName);
							}
							for (InputPort inputPort : firstProcessorActivity
									.getInputPorts()) {
								if (inputPort.equals(portName)) {
									Edit<Activity<?>> edit = edits
											.getAddActivityInputPortMappingEdit(
													firstProcessorActivity,
													portName, portName);
									compoundEdit.add(edit);
									newInputs.add(portName);
								}
							}
						}
					}
					for (InputPort condInPort : conditionActivity
							.getInputPorts()) {
						String portName = condInPort.getName();
						Map<String, String> mapping = firstProcessorActivity
								.getOutputPortMapping();
						if (!mapping.containsValue(portName)) {
							for (OutputPort outputPort : firstProcessorActivity
									.getOutputPorts()) {
								if (outputPort.equals(portName)) {
									if (mapping.containsKey(portName)) {
										logger.warn("Can't re-map output for "
												+ "conditional input "
												+ portName);
									}
									Edit<Activity<?>> edit = edits
											.getAddActivityOutputPortMappingEdit(
													firstProcessorActivity,
													portName, portName);
									logger
											.info("Mapping for conditional non-outgoing activity port binding "
													+ portName);
									compoundEdit.add(edit);
									newOutputs.add(portName);
								}
							}
						}
					}
				}
				// Remove any stale bindings that no longer match neither
				// conditional activity or the processor output ports
				for (String processorIn : firstProcessorActivity
						.getInputPortMapping().keySet()) {
					if (newInputs.contains(processorIn)) {
						continue;
					}
					boolean foundMatch = false;
					for (InputPort processorPort : processor.getInputPorts()) {
						if (processorPort.getName().equals(processorIn)) {
							foundMatch = true;
							break;
						}
					}
					if (!foundMatch) {
						Edit<Activity<?>> edit = edits
								.getRemoveActivityInputPortMappingEdit(
										firstProcessorActivity, processorIn);
						logger.info("Removing stale input port binding "
								+ processorIn);
						compoundEdit.add(edit);
					}
				}
				for (String processorOut : firstProcessorActivity
						.getOutputPortMapping().keySet()) {
					if (newInputs.contains(processorOut)) {
						continue;
					}
					boolean foundMatch = false;
					for (OutputPort processorPort : processor.getOutputPorts()) {
						if (processorPort.getName().equals(processorOut)) {
							foundMatch = true;
							break;
						}
					}
					if (!foundMatch) {
						Edit<Activity<?>> edit = edits
								.getRemoveActivityOutputPortMappingEdit(
										firstProcessorActivity, processorOut);
						logger.info("Removing stale output port binding "
								+ processorOut);
						compoundEdit.add(edit);
					}
				}

				return compoundEdit;
			}
		}

		protected class ResetAction extends AbstractAction {
			private LoopConfigurationPanel loopConfigurationPanel;

			protected ResetAction(LoopConfigurationPanel loopConfigurationPanel) {
				super("Reset");
				this.loopConfigurationPanel = loopConfigurationPanel;
			}

			public void actionPerformed(ActionEvent e) {
				refreshView();
				loopConfigurationPanel.setConfiguration(loopLayer
						.getConfiguration());
			}

		}

	}

	@Override
	public int getPreferredPosition() {
		return 400;
	}

}
