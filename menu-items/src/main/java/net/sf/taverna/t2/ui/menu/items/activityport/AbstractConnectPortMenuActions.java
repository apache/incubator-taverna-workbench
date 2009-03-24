package net.sf.taverna.t2.ui.menu.items.activityport;

import java.awt.Color;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.ui.menu.AbstractMenuCustom;
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.ContextualSelection;
import net.sf.taverna.t2.ui.menu.MenuManager;
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
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityPort;
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

	protected void addPortMenuItems(Dataflow dataflow,
			ActivityPort activityPort, JMenu connectMenu) {
		Color workflowPortColour = colourManager
				.getPreferredColour(DataflowPort.class.getCanonicalName());
		List<DataflowPort> ports;
		if (activityPort instanceof ActivityOutputPort) {
			connectMenu.add(new ShadedLabel(WORKFLOW_INPUT_PORTS,
					workflowPortColour));
			ports = new ArrayList<DataflowPort>(dataflow.getInputPorts());
		} else {
			connectMenu.add(new ShadedLabel(WORKFLOW_OUTPUT_PORTS,
					workflowPortColour));
			ports = new ArrayList<DataflowPort>(dataflow.getOutputPorts());
		}

		Collections.sort(ports, portComparator);

		boolean addedPorts = false;
		for (DataflowPort dataflowInput : ports) {
			ConnectPortsAction connectPortsAction;
			if (dataflowInput instanceof DataflowInputPort) {
				EventForwardingOutputPort internalOutputPort = ((DataflowInputPort) dataflowInput)
						.getInternalOutputPort();
				connectPortsAction = new ConnectPortsAction(dataflow,
						internalOutputPort, (ActivityInputPort) activityPort);
				connectPortsAction.putValue(Action.SMALL_ICON,
						WorkbenchIcons.inputIcon);
			} else if (dataflowInput instanceof DataflowOutputPort) {
				EventHandlingInputPort internalInputPort = ((DataflowOutputPort) dataflowInput)
						.getInternalInputPort();
				connectPortsAction = new ConnectPortsAction(dataflow,
						(ActivityOutputPort) activityPort, internalInputPort);
				connectPortsAction.putValue(Action.SMALL_ICON,
						WorkbenchIcons.outputIcon);
				if (internalInputPort.getIncomingLink() != null) {
					// Can't connect to an output port that already has a
					// link (although a merge would be inserted it can't currently
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
		Collection<Processor> processorsWithActivityPort;
		if (activityPort instanceof ActivityInputPort) {
			processorsWithActivityPort = Tools
					.getProcessorsWithActivityInputPort(dataflow,
							(ActivityInputPort) activityPort);
		} else if (activityPort instanceof ActivityOutputPort) {
			processorsWithActivityPort = Tools
					.getProcessorsWithActivityOutputPort(dataflow,
							(ActivityOutputPort) activityPort);
		} else {
			throw new IllegalArgumentException(
					"Port must be instance of ActivityInputPort or ActivityOutputPort");
		}

		String suggestedName;
		if (processorsWithActivityPort.isEmpty()) {
			suggestedName = activityPort.getName();
		} else {
			suggestedName = processorsWithActivityPort.iterator().next()
					.getLocalName()
					+ "_" + activityPort.getName();
		}

		
		CreateAndConnectDataflowPortAction newDataflowPortAction = new CreateAndConnectDataflowPortAction(
				dataflow, activityPort, suggestedName, contextualSelection
						.getRelativeToComponent());
		if (activityPort instanceof ActivityInputPort) {
			newDataflowPortAction.putValue(Action.NAME, NEW_WORKFLOW_OUTPUT_PORT);
		} else if (activityPort instanceof ActivityOutputPort) {
			newDataflowPortAction.putValue(Action.NAME, NEW_WORKFLOW_INPUT_PORT);
		} else {
			throw new IllegalArgumentException(
					"Port must be instance of ActivityInputPort or ActivityOutputPort");
		}
		newDataflowPortAction.putValue(Action.SMALL_ICON,
				WorkbenchIcons.newIcon);
		connectMenu.add(new JMenuItem(newDataflowPortAction));
	}

}