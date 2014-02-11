/**
 * 
 */
package net.sf.taverna.t2.workbench.loop;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workbench.helper.Helper;
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

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public class LoopConfigureAction extends AbstractAction {
	
	private static Logger logger = Logger.getLogger(LoopConfigureAction.class);
	
	private EditManager editManager = EditManager.getInstance();

	private FileManager fileManager = FileManager.getInstance();

	private Edits edits = EditManager.getInstance().getEdits();

	
		private final Frame owner;
		private final Loop loopLayer;
		private final LoopContextualView contextualView;
		private final Processor processor;
		
		private static AbstractAction helpAction = new AbstractAction("Help"){

			@Override
			public void actionPerformed(ActionEvent e) {
				Helper.showID(LoopConfigureAction.class.getCanonicalName());
			}};

		protected LoopConfigureAction(Frame owner, LoopContextualView contextualView, Loop loopLayer) {
			super("Configure");
			this.owner = owner;
			this.contextualView = contextualView;
			this.loopLayer = loopLayer;
			this.processor = loopLayer.getProcessor();
		}

		public void actionPerformed(ActionEvent e) {
			String title = "Looping for service " + processor.getLocalName();
			final JDialog dialog = new HelpEnabledDialog(owner, title, true);
			LoopConfigurationPanel loopConfigurationPanel = new LoopConfigurationPanel(
					processor, loopLayer);
			dialog.add(loopConfigurationPanel, BorderLayout.CENTER);

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());
			
			JButton helpButton = new JButton(helpAction);
			buttonPanel.add(helpButton);

			JButton okButton = new JButton(new OKAction(dialog,
					loopConfigurationPanel));
			buttonPanel.add(okButton);

			JButton resetButton = new JButton(new ResetAction(
					loopConfigurationPanel));
			buttonPanel.add(resetButton);

			JButton cancelButton = new JButton(new CancelAction(dialog));
			buttonPanel.add(cancelButton);

			dialog.add(buttonPanel, BorderLayout.SOUTH);
			dialog.pack();
			dialog.setSize(650, 430);
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
				if (contextualView != null) {
				contextualView.refreshView();
				}
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
					if (contextualView != null) {
					contextualView.refreshView();
					}
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
				if (contextualView != null) {
				contextualView.refreshView();
				}
				loopConfigurationPanel.setConfiguration(loopLayer
						.getConfiguration());
			}

		}

}
