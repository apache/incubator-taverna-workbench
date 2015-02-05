package net.sf.taverna.t2.ui.menu.items.activityport;

import java.awt.Color;
import java.awt.Component;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.NamedSet;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputPort;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.port.Port;
import uk.org.taverna.scufl2.api.port.ProcessorPort;
import uk.org.taverna.scufl2.api.port.ReceiverPort;
import uk.org.taverna.scufl2.api.port.SenderPort;
import uk.org.taverna.scufl2.api.port.WorkflowPort;
import uk.org.taverna.scufl2.api.profiles.Profile;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.ui.menu.MenuManager.ComponentFactory;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

public abstract class AbstractConnectPortMenuActions extends AbstractMenuCustom
		implements ContextualMenuComponent {

	protected ActivityIconManager activityIconManager;
	protected ContextualSelection contextualSelection;
	protected MenuManager menuManager;
	protected WorkbenchConfiguration workbenchConfiguration;
	protected ColourManager colourManager;
	private EditManager editManager;

	public static final String CONNECT_AS_INPUT_TO = "Connect as input to...";
	public static final String CONNECT_WITH_OUTPUT_FROM = "Connect with output from...";

	public static final String SERVICE_INPUT_PORTS = "Service input ports";
	public static final String SERVICE_OUTPUT_PORTS = "Service output ports";

	public static final String NEW_WORKFLOW_INPUT_PORT = "New workflow input port...";
	public static final String NEW_WORKFLOW_OUTPUT_PORT = "New workflow output port...";

	public static final String WORKFLOW_INPUT_PORTS = "Workflow input ports";
	public static final String WORKFLOW_OUTPUT_PORTS = "Workflow output ports";

	public static final String SERVICES = "Services";

	private Scufl2Tools scufl2Tools  = new Scufl2Tools();

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
		Workflow workflow = (Workflow) getContextualSelection().getParent();
		Profile profile = workflow.getParent().getMainProfile();
		Port port = getSelectedPort();
		// Component component =
		// getContextualSelection().getRelativeToComponent();

		String label;
		if (port instanceof ReceiverPort) {
			label = CONNECT_WITH_OUTPUT_FROM;
		} else {
			label = CONNECT_AS_INPUT_TO;
		}
		JMenu connectMenu = new JMenu(new DummyAction(label,
				WorkbenchIcons.datalinkIcon));
		addPortMenuItems(workflow, port, connectMenu);
		addProcessorMenuItems(workflow, profile, port, connectMenu);
		return connectMenu;
	}

	private Port getSelectedPort() {
		Port port = (Port) getContextualSelection().getSelection();
		return port;
	}

	protected void addPortMenuItems(Workflow workflow, Port port, JMenu connectMenu) {
		Color workflowPortColour = colourManager.getPreferredColour(WorkflowPort.class.getCanonicalName());

		boolean addedPorts = false;
		if (port instanceof SenderPort) {
			connectMenu.add(new ShadedLabel(WORKFLOW_OUTPUT_PORTS, workflowPortColour));
			for (OutputWorkflowPort outputWorkflowPort : workflow.getOutputPorts()) {
				ConnectPortsAction connectPortsAction =
						new ConnectPortsAction(workflow, (SenderPort) port, outputWorkflowPort, editManager);
				connectPortsAction.putValue(Action.SMALL_ICON, WorkbenchIcons.outputIcon);
				connectPortsAction.putValue(Action.NAME, outputWorkflowPort.getName());
				connectMenu.add(new JMenuItem(connectPortsAction));
				addedPorts = true;
			}
		} else if (port instanceof ReceiverPort) {
			connectMenu.add(new ShadedLabel(WORKFLOW_INPUT_PORTS, workflowPortColour));
			for (InputWorkflowPort inputWorkflowPort : workflow.getInputPorts()) {
				ConnectPortsAction connectPortsAction =
						new ConnectPortsAction(workflow, inputWorkflowPort, (ReceiverPort) port, editManager);
				connectPortsAction.putValue(Action.SMALL_ICON, WorkbenchIcons.inputIcon);
				connectPortsAction.putValue(Action.NAME, inputWorkflowPort.getName());
				connectMenu.add(new JMenuItem(connectPortsAction));
				addedPorts = true;
			}
		}
		if (addedPorts) {
			connectMenu.addSeparator();
		}
		CreateAndConnectDataflowPortAction newDataflowPortAction = new CreateAndConnectDataflowPortAction(
				workflow, port, getSuggestedName(port), contextualSelection.getRelativeToComponent(), editManager);

		if (port instanceof ReceiverPort) {
			newDataflowPortAction.putValue(Action.NAME, NEW_WORKFLOW_INPUT_PORT);
		} else if (port instanceof SenderPort) {
			newDataflowPortAction.putValue(Action.NAME, NEW_WORKFLOW_OUTPUT_PORT);
		}
		newDataflowPortAction.putValue(Action.SMALL_ICON, WorkbenchIcons.newIcon);
		connectMenu.add(new JMenuItem(newDataflowPortAction));
	}

	/**
	 * @param port
	 * @return
	 */
	private String getSuggestedName(Port port) {
		String suggestedName;
		if (port instanceof ProcessorPort) {
			suggestedName = ((ProcessorPort) port).getParent().getName() + "_" + port.getName();
		} else {
			suggestedName = port.getName();
		}
		return suggestedName;
	}

	protected void addProcessorMenuItems(Workflow dataflow, Profile profile,
			final Port targetPort, JMenu connectMenu) {
		final Set<Processor> processors = findProcessors(dataflow, targetPort);
		if (processors.isEmpty()) {
			return;
		}
		connectMenu.add(new ShadedLabel(SERVICES, colourManager.getPreferredColour(Processor.class.getCanonicalName())));

		List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
		for (Processor processor : processors) {
			Activity activity = scufl2Tools.processorBindingForProcessor(processor, profile).getBoundActivity();
			Icon icon = activityIconManager.iconForActivity(activity);
			final Color processorPortColour = colourManager.getPreferredColour(ProcessorPort.class.getCanonicalName());

			JMenu processorMenu = new JMenu(new DummyAction(processor.getName(), icon));
			List<JMenuItem> processorMenuItems = new ArrayList<JMenuItem>();
			if (targetPort instanceof ReceiverPort) {
				processorMenu.add(new ShadedLabel(SERVICE_OUTPUT_PORTS,
						processorPortColour));
				menuItems.add(processorMenu);
				for (OutputProcessorPort outputProcessorPort : processor.getOutputPorts()) {
					ConnectPortsAction connectPortsAction = new ConnectPortsAction(dataflow,
							outputProcessorPort, (ReceiverPort) targetPort, editManager);
					connectPortsAction.putValue(Action.SMALL_ICON,
							WorkbenchIcons.outputPortIcon);
					connectPortsAction.putValue(Action.NAME, outputProcessorPort.getName());
					processorMenuItems.add(new JMenuItem(connectPortsAction));
				}
			} else if (targetPort instanceof SenderPort) {
				processorMenu.add(new ShadedLabel(SERVICE_INPUT_PORTS,
						processorPortColour));
				menuItems.add(processorMenu);
				for (InputProcessorPort inputProcessorPort : processor.getInputPorts()) {
					ConnectPortsAction connectPortsAction = new ConnectPortsAction(dataflow,
							(SenderPort) targetPort, inputProcessorPort, editManager);
					connectPortsAction.putValue(Action.SMALL_ICON,
							WorkbenchIcons.inputPortIcon);
					connectPortsAction.putValue(Action.NAME, inputProcessorPort.getName());
					processorMenuItems.add(new JMenuItem(connectPortsAction));
				}
			}

			menuManager.addMenuItemsWithExpansion(processorMenuItems,
					processorMenu, workbenchConfiguration.getMaxMenuItems(),
					new ComponentFactory() {
						public Component makeComponent() {
							if (targetPort instanceof InputPort) {
								return new ShadedLabel(SERVICE_OUTPUT_PORTS, processorPortColour);
							} else {
								return new ShadedLabel(SERVICE_INPUT_PORTS, processorPortColour);
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

	protected Set<Processor> findProcessors(Workflow dataflow, Port targetPort) {
		Set<Processor> possibleProcessors = new HashSet<Processor>();
		if (targetPort instanceof InputProcessorPort) {
			InputProcessorPort inputProcessorPort = (InputProcessorPort) targetPort;
			possibleProcessors = scufl2Tools.possibleUpStreamProcessors(dataflow, inputProcessorPort.getParent());
		} else if (targetPort instanceof OutputProcessorPort) {
			OutputProcessorPort outputProcessorPort = (OutputProcessorPort) targetPort;
			possibleProcessors = scufl2Tools.possibleDownStreamProcessors(dataflow, outputProcessorPort.getParent());
		} else {
			// Probably a dataflow port, everything is allowed
			possibleProcessors = dataflow.getProcessors();
		}
		return possibleProcessors;
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

}