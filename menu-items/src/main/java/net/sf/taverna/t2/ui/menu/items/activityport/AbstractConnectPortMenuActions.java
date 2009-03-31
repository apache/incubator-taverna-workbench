package net.sf.taverna.t2.ui.menu.items.activityport;

import java.awt.Color;
import java.awt.Component;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.ui.menu.MenuManager.ComponentFactory;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.impl.configuration.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowPort;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.NamedWorkflowEntityComparator;
import net.sf.taverna.t2.workflowmodel.utils.PortComparator;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public abstract class AbstractConnectPortMenuActions extends AbstractMenuCustom
		implements ContextualMenuComponent {

	protected NamedWorkflowEntityComparator processorComparator = new NamedWorkflowEntityComparator();
	protected PortComparator portComparator = new PortComparator();
	protected ActivityIconManager activityIconManager = ActivityIconManager
			.getInstance();
	protected ContextualSelection contextualSelection;
	protected MenuManager menuManager = MenuManager.getInstance();
	protected WorkbenchConfiguration workbenchConfiguration = WorkbenchConfiguration
			.getInstance();
	protected ColourManager colourManager = ColourManager.getInstance();

	public static final String CONNECT_AS_INPUT_TO = "Connect as input to...";
	public static final String CONNECT_WITH_OUTPUT_FROM = "Connect with output from...";

	public static final String SERVICE_INPUT_PORTS = "Service input ports";
	public static final String SERVICE_OUTPUT_PORTS = "Service output ports";

	public static final String NEW_WORKFLOW_INPUT_PORT = "New workflow input port...";
	public static final String NEW_WORKFLOW_OUTPUT_PORT = "New workflow output port...";

	public static final String WORKFLOW_INPUT_PORTS = "Workflow input ports";
	public static final String WORKFLOW_OUTPUT_PORTS = "Workflow output ports";

	public static final String SERVICES = "Services";

	public AbstractConnectPortMenuActions(URI parentId, int positionHint) {
		super(parentId, positionHint);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.customComponent = null;
	}

	@Override
	protected Component createCustomComponent() {
		Dataflow dataflow = (Dataflow) getContextualSelection().getParent();
		Port port = getSelectedPort();
		// Component component =
		// getContextualSelection().getRelativeToComponent();

		String label;
		if (port instanceof InputPort) {
			label = CONNECT_WITH_OUTPUT_FROM;
		} else {
			label = CONNECT_AS_INPUT_TO;
		}
		JMenu connectMenu = new JMenu(new DummyAction(label,
				WorkbenchIcons.datalinkIcon));
		addPortMenuItems(dataflow, port, connectMenu);
		addProcessorMenuItems(dataflow, port, connectMenu);
		return connectMenu;
	}

	private Port getSelectedPort() {
		Port port = (Port) getContextualSelection().getSelection();
		if (port instanceof DataflowInputPort) {
			return ((DataflowInputPort)port).getInternalOutputPort();
		} else if (port instanceof DataflowOutputPort) {
			return ((DataflowOutputPort)port).getInternalInputPort();
		}
		return port;
	}

	protected void addPortMenuItems(Dataflow dataflow, Port port,
			JMenu connectMenu) {
		Color workflowPortColour = colourManager
				.getPreferredColour(DataflowPort.class.getCanonicalName());
		List<DataflowPort> ports;
		if (port instanceof OutputPort) {
			connectMenu.add(new ShadedLabel(WORKFLOW_OUTPUT_PORTS,
					workflowPortColour));
			ports = new ArrayList<DataflowPort>(dataflow.getOutputPorts());
		} else {
			connectMenu.add(new ShadedLabel(WORKFLOW_INPUT_PORTS,
					workflowPortColour));
			ports = new ArrayList<DataflowPort>(dataflow.getInputPorts());
		}

		Collections.sort(ports, portComparator);

		boolean addedPorts = false;
		for (DataflowPort dataflowInput : ports) {
			ConnectPortsAction connectPortsAction;
			if (dataflowInput instanceof DataflowInputPort) {
				EventForwardingOutputPort internalOutputPort = ((DataflowInputPort) dataflowInput)
						.getInternalOutputPort();
				connectPortsAction = new ConnectPortsAction(dataflow,
						internalOutputPort, (InputPort) port);
				connectPortsAction.putValue(Action.SMALL_ICON,
						WorkbenchIcons.inputIcon);
			} else if (dataflowInput instanceof DataflowOutputPort) {
				EventHandlingInputPort internalInputPort = ((DataflowOutputPort) dataflowInput)
						.getInternalInputPort();
				connectPortsAction = new ConnectPortsAction(dataflow,
						(OutputPort) port, internalInputPort);
				connectPortsAction.putValue(Action.SMALL_ICON,
						WorkbenchIcons.outputIcon);
				if (internalInputPort.getIncomingLink() != null) {
					// Can't connect to an output port that already has a
					// link (although a merge would be inserted it can't
					// currently
					// be serialised)
					connectPortsAction.setEnabled(false);
				}
			} else {
				throw new IllegalStateException(
						"getDataflowPorts() must return instances of DataflowInputPort or DataflowOutputPort");
			}
			connectPortsAction.putValue(Action.NAME, dataflowInput.getName());
			connectMenu.add(new JMenuItem(connectPortsAction));
			addedPorts = true;
		}
		if (addedPorts) {
			connectMenu.addSeparator();
		}
		Collection<Processor> processorsWithActivityPort = Collections
				.emptyList();
		if (port instanceof ActivityInputPort) {
			processorsWithActivityPort = Tools
					.getProcessorsWithActivityInputPort(dataflow,
							(ActivityInputPort) port);
		} else if (port instanceof ActivityOutputPort) {
			processorsWithActivityPort = Tools
					.getProcessorsWithActivityOutputPort(dataflow,
							(ActivityOutputPort) port);
		}

		String suggestedName;
		if (processorsWithActivityPort.isEmpty()) {
			suggestedName = port.getName();
		} else {
			suggestedName = processorsWithActivityPort.iterator().next()
					.getLocalName()
					+ "_" + port.getName();
		}

		CreateAndConnectDataflowPortAction newDataflowPortAction = new CreateAndConnectDataflowPortAction(
				dataflow, port, suggestedName, contextualSelection
						.getRelativeToComponent());
		if (port instanceof InputPort) {
			newDataflowPortAction
			.putValue(Action.NAME, NEW_WORKFLOW_INPUT_PORT);
		} else if (port instanceof OutputPort) {
			newDataflowPortAction.putValue(Action.NAME,
					NEW_WORKFLOW_OUTPUT_PORT);
		} else {
			throw new IllegalArgumentException(
					"Port must be instance of InputPort or OutputPort");
		}
		newDataflowPortAction.putValue(Action.SMALL_ICON,
				WorkbenchIcons.newIcon);
		connectMenu.add(new JMenuItem(newDataflowPortAction));
	}

	protected void addProcessorMenuItems(Dataflow dataflow,
			final Port targetPort, JMenu connectMenu) {
		final Map<Processor, List<Port>> ports = findPorts(dataflow, targetPort);
		if (ports.isEmpty()) {
			return;
		}
		connectMenu.add(new ShadedLabel(SERVICES, colourManager
				.getPreferredColour(Processor.class.getCanonicalName())));
		List<Processor> processors = new ArrayList<Processor>(ports.keySet());
		Collections.sort(processors, processorComparator);

		// TAV-172

		List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
		for (Processor processor : processors) {
			Icon icon = null;
			if (!processor.getActivityList().isEmpty()) {
				// Pick the icon of the first activity
				icon = activityIconManager.iconForActivity(processor
						.getActivityList().get(0));
			}
			JMenu processorMenu = new JMenu(new DummyAction(processor
					.getLocalName(), icon));
			final Color processorPortColour = colourManager
					.getPreferredColour(ProcessorPort.class.getCanonicalName());
			if (targetPort instanceof InputPort) {
				processorMenu.add(new ShadedLabel(SERVICE_OUTPUT_PORTS,
						processorPortColour));
			} else {
				processorMenu.add(new ShadedLabel(SERVICE_INPUT_PORTS,
						processorPortColour));
			}
			menuItems.add(processorMenu);

			List<Port> processorPorts = ports.get(processor);
			Collections.sort(processorPorts, portComparator);
			List<JMenuItem> processorMenuItems = new ArrayList<JMenuItem>();
			for (Port outputPort : processorPorts) {
				ConnectPortsAction connectPortsAction;
				if (outputPort instanceof OutputPort) {
					connectPortsAction = new ConnectPortsAction(dataflow,
							(OutputPort) outputPort, (InputPort) targetPort);
					connectPortsAction.putValue(Action.SMALL_ICON,
							WorkbenchIcons.outputPortIcon);
				} else {
					connectPortsAction = new ConnectPortsAction(dataflow,
							(OutputPort) targetPort, (InputPort) outputPort);
					connectPortsAction.putValue(Action.SMALL_ICON,
							WorkbenchIcons.inputPortIcon);
				}
				connectPortsAction.putValue(Action.NAME, outputPort.getName());
				processorMenuItems.add(new JMenuItem(connectPortsAction));
			}

			menuManager.addMenuItemsWithExpansion(processorMenuItems,
					processorMenu, workbenchConfiguration.getMaxMenuItems(),
					new ComponentFactory() {
						public Component makeComponent() {
							if (targetPort instanceof InputPort) {
								return new ShadedLabel(SERVICE_OUTPUT_PORTS,
										processorPortColour);
							} else {
								return new ShadedLabel(SERVICE_INPUT_PORTS,
										processorPortColour);
							}
						}
					});
		}
		menuManager.addMenuItemsWithExpansion(menuItems, connectMenu,
				workbenchConfiguration.getMaxMenuItems(),
				new ComponentFactory() {
					public Component makeComponent() {
						return new ShadedLabel(SERVICES, colourManager
								.getPreferredColour(Processor.class
										.getCanonicalName()));
					}
				});
	}

	protected Map<Processor, List<Port>> findPorts(Dataflow dataflow,
			Port targetPort) {
		HashMap<Processor, List<Port>> allPorts = new HashMap<Processor, List<Port>>();

		Processor ourProcessor = null;
		if (targetPort instanceof ActivityInputPort) {
			ourProcessor = Tools.getFirstProcessorWithActivityInputPort(
					dataflow, (ActivityInputPort) targetPort);
		} else if (targetPort instanceof ActivityOutputPort) {
			ourProcessor = Tools.getFirstProcessorWithActivityOutputPort(
					dataflow, (ActivityOutputPort) targetPort);
		}

		Collection<? extends Processor> possibleProcessors;
		if (targetPort instanceof ActivityInputPort) {
			possibleProcessors = Tools.possibleUpStreamProcessors(dataflow,
					ourProcessor);
		} else if (targetPort instanceof ActivityOutputPort) {
			possibleProcessors = Tools.possibleDownStreamProcessors(dataflow,
					ourProcessor);
		} else {
			// Probably a dataflow port, everything is allowed
			possibleProcessors = dataflow.getProcessors();
		}
		for (Processor processor : possibleProcessors) {
			List<Port> ports = new ArrayList<Port>();

			List<? extends ProcessorPort> processorPorts;
			if (targetPort instanceof InputPort) {
				processorPorts = processor.getOutputPorts();
			} else {
				processorPorts = processor.getInputPorts();
			}

			for (ProcessorPort procOutPort : processorPorts) {
				ports.add(procOutPort);
			}

			for (Activity<?> activity : processor.getActivityList()) {
				Set<? extends Port> activityOuts;
				if (targetPort instanceof InputPort) {
					activityOuts = activity.getOutputPorts();
				} else {
					activityOuts = activity.getInputPorts();
				}
				for (Port actPort : activityOuts) {
					if (actPort instanceof OutputPort
							&& activity.getOutputPortMapping().containsKey(
									actPort.getName())) {
						// Should be added from processor ports
						continue;
					}
					if (actPort instanceof InputPort
							&& activity.getInputPortMapping().containsValue(
									actPort.getName())) {
						// Should be added from processor ports
						continue;
					}

					ports.add(actPort);
				}
			}
			if (!ports.isEmpty()) {
				allPorts.put(processor, ports);
			}
		}
		return allPorts;
	}

}