package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import static java.awt.BorderLayout.SOUTH;
import static java.awt.Cursor.DEFAULT_CURSOR;
import static java.awt.Cursor.WAIT_CURSOR;
import static java.awt.Cursor.getPredefinedCursor;
import static java.lang.Math.max;
import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static net.sf.taverna.t2.workbench.MainWindow.getMainWindow;
import static net.sf.taverna.t2.workbench.helper.Helper.showHelp;
import static net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction.clearDialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.DeselectingButton;
import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowRedoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.DataFlowUndoEvent;
import net.sf.taverna.t2.workbench.edits.EditManager.EditManagerEvent;
import net.sf.taverna.t2.workbench.helper.HelpEnabledDialog;
import net.sf.taverna.t2.workflow.edits.AddChildEdit;
import net.sf.taverna.t2.workflow.edits.AddProcessorInputPortEdit;
import net.sf.taverna.t2.workflow.edits.AddProcessorOutputPortEdit;
import net.sf.taverna.t2.workflow.edits.ChangeDepthEdit;
import net.sf.taverna.t2.workflow.edits.ChangeGranularDepthEdit;
import net.sf.taverna.t2.workflow.edits.ChangeJsonEdit;
import net.sf.taverna.t2.workflow.edits.RemoveChildEdit;
import net.sf.taverna.t2.workflow.edits.RemoveProcessorInputPortEdit;
import net.sf.taverna.t2.workflow.edits.RemoveProcessorOutputPortEdit;
import net.sf.taverna.t2.workflow.edits.RenameEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.ActivityPort;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputActivityPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorOutputPortBinding;
import uk.org.taverna.scufl2.api.profiles.Profile;

import com.fasterxml.jackson.databind.node.ObjectNode;

@SuppressWarnings("serial")
public class ActivityConfigurationDialog extends HelpEnabledDialog {
	private enum PortType {
		INPUT, OUTPUT
	}
	
	protected static Logger logger = Logger.getLogger(ActivityConfigurationDialog.class);
	private static final Scufl2Tools scufl2Tools = new Scufl2Tools();

	private final EditManager editManager;

	private Activity activity;
	private ActivityConfigurationPanel panel;
	protected WorkflowBundle owningWorkflowBundle;
	protected Processor owningProcessor;
	private Observer<EditManagerEvent> observer;
	Dimension minimalSize = null;
	Dimension buttonPanelSize = null;
	JPanel buttonPanel;
	protected JButton applyButton;

	public ActivityConfigurationDialog(Activity a, ActivityConfigurationPanel p,
			EditManager editManager) {
		super(getMainWindow(), "Configuring " + a.getClass().getSimpleName(),
				false, null);
		this.activity = a;
		this.panel = p;
		this.editManager = editManager;

		owningWorkflowBundle = activity.getParent().getParent();
		owningProcessor = findProcessor(a);

		setTitle(getRelativeName(owningWorkflowBundle, activity));
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setLayout(new BorderLayout());

		add(panel, BorderLayout.CENTER);

		buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBorder(new EmptyBorder(5, 20, 5, 5));

		JButton helpButton = new DeselectingButton("Help", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showHelp(panel);
			}
		});
		buttonPanel.add(helpButton);

		applyButton = new DeselectingButton("Apply", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * For the moment it always does an apply as what should be
				 * happening is that the apply button only becomes available
				 * when the configuration has changed. However, many
				 * configuration panels are not set up to detected changes
				 */
				// if (panel.isConfigurationChanged()) {
				if (checkPanelValues())
					applyConfiguration();
				// } else {
				// logger.info("Ignoring apply");
				// }
			}
		});
		buttonPanel.add(applyButton);

		JButton closeButton = new DeselectingButton("Close", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
		});
		buttonPanel.add(closeButton);

		add(buttonPanel, SOUTH);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				requestFocusInWindow();
				panel.whenOpened();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				closeDialog();
			}
		});
		pack();
		minimalSize = getSize();
		setLocationRelativeTo(null);
		setResizable(true);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int newWidth = max(getWidth(), minimalSize.width);
				int newHeight = max(getHeight(), minimalSize.height);
				setSize(new Dimension(newWidth, newHeight));
			}
		});

		observer = new Observer<EditManagerEvent>() {
			@Override
			public void notify(Observable<EditManagerEvent> sender, EditManagerEvent message)
					throws Exception {
				logger.info("sender is a " + sender.getClass().getCanonicalName());
				logger.info("message is a " + message.getClass().getCanonicalName());
				Edit<?> edit = message.getEdit();
				logger.info(edit.getClass().getCanonicalName());
				considerEdit(message, edit);
			}
		};
		editManager.addObserver(observer);
	}

	private boolean checkPanelValues() {
		boolean result = false;
		try {
			setCursor(getPredefinedCursor(WAIT_CURSOR));
			result = panel.checkValues();
		} finally {
			setCursor(getPredefinedCursor(DEFAULT_CURSOR));
		}
		return result;
	}

	private void considerEdit(EditManagerEvent message, Edit<?> edit) {
		// boolean result = false;
		if (edit instanceof CompoundEdit) {
			for (Edit<?> subEdit : ((CompoundEdit) edit).getChildEdits())
				considerEdit(message, subEdit);
			return;
		}

		Object subject = edit.getSubject();
		if (subject == owningProcessor) {
			// panel.reevaluate();
			setTitle(getRelativeName(owningWorkflowBundle, activity));
		} else if (subject == owningWorkflowBundle) {
			for (Workflow workflow : owningWorkflowBundle.getWorkflows())
				if (!workflow.getProcessors().contains(owningProcessor))
					clearDialog(activity);
		} else if (subject == activity) {
			if (message instanceof DataFlowUndoEvent) {
				logger.info("undo of activity edit found");
				panel.refreshConfiguration();
			} else if (message instanceof DataFlowRedoEvent) {
				logger.info("redo of activity edit found");
				panel.refreshConfiguration();
			}
		}
	}

	protected void configureActivity(ObjectNode json, List<ActivityPortConfiguration> inputPorts,
			List<ActivityPortConfiguration> outputPorts) {
		configureActivity(owningWorkflowBundle, activity, json, inputPorts, outputPorts);
	}

	public void configureActivity(WorkflowBundle workflowBundle, Activity activity,
			ObjectNode json, List<ActivityPortConfiguration> inputPorts,
			List<ActivityPortConfiguration> outputPorts) {
		try {
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			Profile profile = activity.getParent();
			List<ProcessorBinding> processorBindings = scufl2Tools
					.processorBindingsToActivity(activity);
			Configuration configuration = scufl2Tools.configurationFor(activity, profile);
			editList.add(new ChangeJsonEdit(configuration, json));

			configurePorts(activity, editList, processorBindings, inputPorts, PortType.INPUT);
			configurePorts(activity, editList, processorBindings, outputPorts, PortType.OUTPUT);
			editManager.doDataflowEdit(workflowBundle, new CompoundEdit(editList));
		} catch (IllegalStateException | EditException e) {
			logger.error(e);
		}
	}

	private void configurePorts(Activity activity, List<Edit<?>> editList,
			List<ProcessorBinding> processorBindings,
			List<ActivityPortConfiguration> portDefinitions, PortType portType) {
		Set<ActivityPort> ports = new HashSet<>();
		for (ActivityPort activityPort : portType == PortType.INPUT ? activity
				.getInputPorts() : activity.getOutputPorts())
			ports.add(activityPort);
		for (ActivityPortConfiguration portDefinition : portDefinitions) {
			String portName = portDefinition.getName();
			int portDepth = portDefinition.getDepth();
			int granularPortDepth = portDefinition.getGranularDepth();
			ActivityPort activityPort = portDefinition.getActivityPort();
			if (activityPort == null) {
				// no activity port so add a new one
				if (portType == PortType.INPUT)
					createInputPort(activity, editList, processorBindings, portDefinition);
				else
					createOutputPort(activity, editList, processorBindings, portDefinition);
			} else {
				ports.remove(activityPort);
				// check if port has changed
				for (ProcessorBinding processorBinding : processorBindings)
					if (portType == PortType.INPUT)
						for (ProcessorInputPortBinding portBinding : processorBinding
								.getInputPortBindings()) {
							if (!portBinding.getBoundActivityPort().equals(
									activityPort))
								continue;
							InputProcessorPort processorPort = portBinding
									.getBoundProcessorPort();
							if (!activityPort.getName().equals(portName))
								// port name changed
								if (processorPort.getName().equals(activityPort.getName()))
									// default mapping so change processor port
									editList.add(new RenameEdit<>(processorPort, portName));
							if (!processorPort.getDepth().equals(portDepth))
								// port depth changed
								editList.add(new ChangeDepthEdit<>(
										processorPort, portDepth));
						}
					else
						for (ProcessorOutputPortBinding portBinding : processorBinding
								.getOutputPortBindings()) {
							if (!portBinding.getBoundActivityPort().equals(
									activityPort))
								continue;
							OutputProcessorPort processorPort = portBinding
									.getBoundProcessorPort();
							if (!activityPort.getName().equals(portName))
								// port name changed
								if (processorPort.getName().equals(
										activityPort.getName()))
									// default mapping so change processor port
									editList.add(new RenameEdit<>(
											processorPort, portName));
							if (!processorPort.getDepth().equals(portDepth))
								// port depth changed
								editList.add(new ChangeDepthEdit<>(
										processorPort, portDepth));
							if (!processorPort.getGranularDepth().equals(
									granularPortDepth))
								// port granular depth changed
								editList.add(new ChangeGranularDepthEdit<>(
										processorPort, granularPortDepth));
						}
				if (!activityPort.getName().equals(portName))
					// port name changed
					editList.add(new RenameEdit<>(activityPort, portName));
				if (!activityPort.getDepth().equals(portDepth))
					// port depth changed
					editList.add(new ChangeDepthEdit<>(activityPort, portDepth));
				if (activityPort instanceof OutputActivityPort) {
					OutputActivityPort outputActivityPort = (OutputActivityPort) activityPort;
					Integer granularDepth = outputActivityPort
							.getGranularDepth();
					if (granularDepth == null
							|| !granularDepth.equals(granularPortDepth))
						// granular port depth changed
						editList.add(new ChangeGranularDepthEdit<>(
								outputActivityPort, granularPortDepth));
				}
			}
		}

		// remove any unconfigured ports
		for (ActivityPort activityPort : ports) {
			// remove processor ports and bindings
			for (ProcessorBinding processorBinding : processorBindings)
				if (portType.equals(PortType.INPUT))
					for (ProcessorInputPortBinding portBinding : processorBinding
							.getInputPortBindings()) {
						if (portBinding.getBoundActivityPort().equals(activityPort)) {
							editList.add(new RemoveProcessorInputPortEdit(processorBinding
									.getBoundProcessor(), portBinding.getBoundProcessorPort()));
							editList.add(new RemoveChildEdit<>(processorBinding,
									portBinding));
						}
					}
				else
					for (ProcessorOutputPortBinding portBinding : processorBinding
							.getOutputPortBindings())
						if (portBinding.getBoundActivityPort().equals(activityPort)) {
							editList.add(new RemoveProcessorOutputPortEdit(processorBinding
									.getBoundProcessor(), portBinding.getBoundProcessorPort()));
							editList.add(new RemoveChildEdit<>(processorBinding,
									portBinding));
						}
			// remove activity port
			editList.add(new RemoveChildEdit<Activity>(activity, activityPort));
		}
	}

	private void createInputPort(Activity activity, List<Edit<?>> editList,
			List<ProcessorBinding> processorBindings,
			ActivityPortConfiguration portDefinition) {
		InputActivityPort actPort = new InputActivityPort(null,
				portDefinition.getName());
		actPort.setDepth(portDefinition.getDepth());
		// add port to activity
		editList.add(new AddChildEdit<>(activity, actPort));
		for (ProcessorBinding processorBinding : processorBindings) {
			Processor processor = processorBinding.getBoundProcessor();
			// add a new processor port
			InputProcessorPort procPort = new InputProcessorPort();
			procPort.setName(portDefinition.getName());
			procPort.setDepth(portDefinition.getDepth());
			editList.add(new AddProcessorInputPortEdit(processor, procPort));
			// add a new port binding
			ProcessorInputPortBinding binding = new ProcessorInputPortBinding();
			binding.setBoundProcessorPort(procPort);
			binding.setBoundActivityPort(actPort);
			editList.add(new AddChildEdit<>(processorBinding, binding));
		}
	}

	private void createOutputPort(Activity activity, List<Edit<?>> editList,
			List<ProcessorBinding> processorBindings,
			ActivityPortConfiguration portDefinition) {
		OutputActivityPort actPort = new OutputActivityPort(null,
				portDefinition.getName());
		actPort.setDepth(portDefinition.getDepth());
		actPort.setGranularDepth(portDefinition.getGranularDepth());
		// add port to activity
		editList.add(new AddChildEdit<Activity>(activity, actPort));
		for (ProcessorBinding processorBinding : processorBindings) {
			Processor processor = processorBinding.getBoundProcessor();
			// add a new processor port
			OutputProcessorPort procPort = new OutputProcessorPort();
			procPort.setName(portDefinition.getName());
			procPort.setDepth(portDefinition.getDepth());
			procPort.setGranularDepth(portDefinition.getGranularDepth());
			editList.add(new AddProcessorOutputPortEdit(processor, procPort));
			// add a new port binding
			ProcessorOutputPortBinding binding = new ProcessorOutputPortBinding();
			binding.setBoundProcessorPort(procPort);
			binding.setBoundActivityPort(actPort);
			editList.add(new AddChildEdit<>(processorBinding, binding));
		}
	}

	protected static Processor findProcessor(Activity activity) {
		for (ProcessorBinding processorBinding : scufl2Tools
				.processorBindingsToActivity(activity))
			return processorBinding.getBoundProcessor();
		return null;
	}

	public static String getRelativeName(WorkflowBundle workflowBundle, Activity activity) {
		StringBuilder relativeName = new StringBuilder("");
		if (workflowBundle != null) {
			Workflow workflow = workflowBundle.getMainWorkflow();
			if (workflow != null) {
				relativeName.append(workflow.getName());
				relativeName.append(":");
			}
		}
		Processor processor = findProcessor(activity);
		if (processor != null)
			relativeName.append(processor.getName());
		return relativeName.toString();
	}

	public boolean closeDialog() {
		if (panel.isConfigurationChanged()) {
			String relativeName = getRelativeName(owningWorkflowBundle, activity);
			if (checkPanelValues()) {
				int answer = showConfirmDialog(this,
						"Do you want to save the configuration of " + relativeName + "?",
						relativeName, YES_NO_CANCEL_OPTION);
				if (answer == YES_OPTION) {
					applyConfiguration();
				} else if (answer == CANCEL_OPTION) {
					return false;
				}
			} else if (showConfirmDialog(
					this,
					"New configuration could not be saved. Do you still want to close?",
					relativeName, YES_NO_OPTION) == NO_OPTION)
				return false;
		}
		panel.whenClosed();
		clearDialog(activity);
		return true;
	}

	private void applyConfiguration() {
		panel.noteConfiguration();
		configureActivity(panel.getJson(), panel.getInputPorts(),
				panel.getOutputPorts());
		panel.refreshConfiguration();
	}

	@Override
	public void dispose() {
		super.dispose();
		editManager.removeObserver(observer);
	}
}
