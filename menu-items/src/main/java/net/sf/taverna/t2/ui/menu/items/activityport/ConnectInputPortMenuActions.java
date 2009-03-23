/**********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
 **********************************************************************/
package net.sf.taverna.t2.ui.menu.items.activityport;

import java.awt.Color;
import java.awt.Component;
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
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.utils.NamedWorkflowEntityComparator;
import net.sf.taverna.t2.workflowmodel.utils.PortComparator;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public class ConnectInputPortMenuActions extends AbstractMenuCustom implements
		ContextualMenuComponent {

	private static final int MAX_PROCESSORS_IN_MENU = 19;

	public static final Color PURPLISH = new Color(0x8070ff);

	private NamedWorkflowEntityComparator processorComparator = new NamedWorkflowEntityComparator();

	private PortComparator portComparator = new PortComparator();

	private ActivityIconManager activityIconManager = ActivityIconManager
			.getInstance();

	private ContextualSelection contextualSelection;

	public ConnectInputPortMenuActions() {
		super(ActivityInputPortSection.activityInputPortSection, 20);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof ActivityInputPort
				&& getContextualSelection().getParent() instanceof Dataflow;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.customComponent = null;
	}

	private void addPortMenuItems(Dataflow dataflow,
			ActivityInputPort inputPort, JMenu connectMenu) {
		connectMenu.add(new ShadedLabel("Workflow input ports",
				ShadedLabel.ORANGE));

		List<DataflowInputPort> inputPorts = new ArrayList<DataflowInputPort>(
				dataflow.getInputPorts());
		Collections.sort(inputPorts, portComparator);
		boolean addedPorts = false;
		for (DataflowInputPort dataflowInput : inputPorts) {
			ConnectPortsAction connectPortsAction = new ConnectPortsAction(
					dataflow, dataflowInput.getInternalOutputPort(), inputPort);
			connectPortsAction.putValue(Action.NAME, dataflowInput.getName());
			connectPortsAction.putValue(Action.SMALL_ICON,
					WorkbenchIcons.inputIcon);
			connectMenu.add(new JMenuItem(connectPortsAction));
			addedPorts = true;
		}
		if (addedPorts) {
			connectMenu.addSeparator();
		}

		Collection<Processor> processorsWithActivityInPort = Tools
				.getProcessorsWithActivityInputPort(dataflow, inputPort);
		String suggestedName;
		if (processorsWithActivityInPort.isEmpty()) {
			suggestedName = inputPort.getName();
		} else {
			suggestedName = processorsWithActivityInPort.iterator().next()
					.getLocalName()
					+ "_" + inputPort.getName();
		}

		CreateAndConnectDataflowPortAction newDataflowPortAction = new CreateAndConnectDataflowPortAction(
				dataflow, inputPort, suggestedName, contextualSelection
						.getRelativeToComponent());
		newDataflowPortAction.putValue(Action.NAME, "New workflow input port...");
		newDataflowPortAction.putValue(Action.SMALL_ICON,
				WorkbenchIcons.newIcon);
		connectMenu.add(new JMenuItem(newDataflowPortAction));
	}

	private void addProcessorMenuItems(Dataflow dataflow,
			ActivityInputPort inputPort, JMenu connectMenu) {
		final Map<Processor, List<OutputPort>> ports = findOutputPorts(
				dataflow, inputPort);
		if (ports.isEmpty()) {
			return;
		}
		connectMenu.add(new ShadedLabel("Services", ShadedLabel.GREEN));
		// connectMenu.addSeparator();

		List<Processor> processors = new ArrayList<Processor>(ports.keySet());
		Collections.sort(processors, processorComparator);
		
		// TAV-172
		JMenu expansionMenu;
		boolean manyProcessors;
		if (processors.size() > MAX_PROCESSORS_IN_MENU) {
			manyProcessors = true;
			expansionMenu = new JMenu();
			expansionMenu.add(new ShadedLabel("Services", ShadedLabel.GREEN));
			connectMenu.add(expansionMenu);
		} else {
			manyProcessors = false;
			expansionMenu = connectMenu;
		}
		
		for (Processor processor : processors) {
			if (manyProcessors && expansionMenu.getItemCount() >= MAX_PROCESSORS_IN_MENU) {
				labelExpansionMenu(expansionMenu);
				// Create new blank one for the rest
				expansionMenu = new JMenu();
				expansionMenu.add(new ShadedLabel("Services", ShadedLabel.GREEN));
				connectMenu.add(expansionMenu);
			}
			
			Icon icon = null;
			if (!processor.getActivityList().isEmpty()) {
				// Pick the icon of the first activity
				icon = activityIconManager.iconForActivity(processor
						.getActivityList().get(0));
			}
			JMenu processorMenu = new JMenu(new DummyAction(processor
					.getLocalName(), icon));
			processorMenu.add(new ShadedLabel("Service output ports", PURPLISH));
			expansionMenu.add(processorMenu);

			List<OutputPort> outputPorts = ports.get(processor);
			Collections.sort(outputPorts, portComparator);
			for (OutputPort outputPort : outputPorts) {
				ConnectPortsAction connectPortsAction = new ConnectPortsAction(
						dataflow, outputPort, inputPort);
				connectPortsAction.putValue(Action.NAME, outputPort.getName());
				connectPortsAction.putValue(Action.SMALL_ICON,
						WorkbenchIcons.outputPortIcon);
				processorMenu.add(new JMenuItem(connectPortsAction));
			}
		}
		if (manyProcessors) {
			labelExpansionMenu(expansionMenu);
		}
	}

	private void labelExpansionMenu(JMenu subMenu) {
		JMenuItem firstItem = subMenu.getItem(1);
		JMenuItem lastItem = subMenu.getItem(subMenu.getItemCount()-1);
		subMenu.setText(firstItem.getText() + " ... " + lastItem.getText());
	}

	@Override
	protected Component createCustomComponent() {
		Dataflow dataflow = (Dataflow) getContextualSelection().getParent();
		ActivityInputPort inputPort = (ActivityInputPort) getContextualSelection()
				.getSelection();
		// Component component =
		// getContextualSelection().getRelativeToComponent();

		JMenu connectMenu = new JMenu(new DummyAction(
				"Connect with output from...", WorkbenchIcons.datalinkIcon));

		addPortMenuItems(dataflow, inputPort, connectMenu);
		addProcessorMenuItems(dataflow, inputPort, connectMenu);
		return connectMenu;
	}

	protected Map<Processor, List<OutputPort>> findOutputPorts(
			Dataflow dataflow, ActivityInputPort targetPort) {

		HashMap<Processor, List<OutputPort>> allOutPorts = new HashMap<Processor, List<OutputPort>>();
		Processor ourProcessor = Tools.getFirstProcessorWithActivityInputPort(
				dataflow, targetPort);
		for (Processor processor : Tools.possibleUpStreamProcessors(dataflow,
				ourProcessor)) {
			List<OutputPort> outputPorts = new ArrayList<OutputPort>();

			for (ProcessorOutputPort procOutPort : processor.getOutputPorts()) {
				outputPorts.add(procOutPort);
			}

			for (Activity<?> activity : processor.getActivityList()) {
				Set<OutputPort> activityOuts = activity.getOutputPorts();
				for (OutputPort actOutPort : activityOuts) {
					if (activity.getOutputPortMapping().containsKey(
							actOutPort.getName())) {
						// Should be added from processor ports
						continue;
					}
					outputPorts.add(actOutPort);
				}
			}
			if (!outputPorts.isEmpty()) {
				allOutPorts.put(processor, outputPorts);
			}
		}
		return allOutPorts;
	}

}
