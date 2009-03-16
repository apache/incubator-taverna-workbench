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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
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
import net.sf.taverna.t2.workflowmodel.impl.ConnectDatalinkEdit;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public class ConnectInputPortMenuActions extends AbstractMenuCustom implements
		ContextualMenuComponent {

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

	@Override
	protected Component createCustomComponent() {
		Dataflow dataflow = (Dataflow) getContextualSelection().getParent();
		ActivityInputPort inputPort = (ActivityInputPort) getContextualSelection()
				.getSelection();
		Component component = getContextualSelection().getRelativeToComponent();

		final Map<Processor, List<OutputPort>> ports = findOutputPorts(
				dataflow, inputPort);
		if (ports.isEmpty()) {
			return null;
		}
		JMenu conditionMenu = new JMenu("Connect with output from…");
		conditionMenu.setIcon(WorkbenchIcons.datalinkIcon);

		conditionMenu
				.add(new ShadedLabel("Workflow inputs", ShadedLabel.ORANGE));

		for (DataflowInputPort dataflowInput : dataflow.getInputPorts()) {
			
			ConnectPortsAction connectPortsAction = new ConnectPortsAction(
					dataflow, inputPort, dataflowInput.getInternalOutputPort());
			connectPortsAction.putValue(Action.NAME, dataflowInput.getName());
			connectPortsAction.putValue(Action.SMALL_ICON, WorkbenchIcons.inputIcon);
			
			conditionMenu.add(new JMenuItem(connectPortsAction));
		}
		conditionMenu.add(new JMenuItem("<html><i>New…</i></html>", WorkbenchIcons.inputIcon));
		

		conditionMenu.add(new ShadedLabel("Processors", ShadedLabel.ORANGE));
		conditionMenu.addSeparator();

		List<Processor> processors = new ArrayList<Processor>(ports.keySet());
		Collections.sort(processors, new Comparator<Processor>() {
			public int compare(Processor o1, Processor o2) {
				return o1.getLocalName().compareTo(o2.getLocalName());
			}
		});

		for (Processor processor : processors) {
			Icon icon = null;
			if (!processor.getActivityList().isEmpty()) {
				icon = activityIconManager.iconForActivity(processor
						.getActivityList().get(0));
			}
			JMenu processorMenu = new JMenu(new AbstractAction(processor
					.getLocalName(), icon) {
				public void actionPerformed(ActionEvent e) {
				}
			});
			conditionMenu.add(processorMenu);

			for (OutputPort outputPort : ports.get(processor)) {
				ConnectPortsAction connectPortsAction = new ConnectPortsAction(
						dataflow, inputPort, outputPort);
				connectPortsAction.putValue(Action.NAME, outputPort.getName());
				processorMenu.add(new JMenuItem(connectPortsAction));

			}

		}
		return conditionMenu;
	}

	protected Map<Processor, List<OutputPort>> findOutputPorts(
			Dataflow dataflow, ActivityInputPort targetPort) {

		HashMap<Processor, List<OutputPort>> allOutPorts = new HashMap<Processor, List<OutputPort>>();
		Collection<Processor> processorsWithActivityInputPort = Tools
				.getProcessorsWithActivityInputPort(dataflow, targetPort);
		for (Processor processor : dataflow.getProcessors()) {
			if (processorsWithActivityInputPort.contains(processor)) {
				// Don't link to ourself
				continue;
			}
			List<OutputPort> outputPorts = new ArrayList<OutputPort>();

			for (ProcessorOutputPort procOutPort : processor.getOutputPorts()) {
				// The processor ports not yet connected added very first
				if (procOutPort.getOutgoingLinks().isEmpty()) {
					outputPorts.add(procOutPort);
				}
			}

			for (Activity<?> activity : processor.getActivityList()) {
				Set<OutputPort> activityOuts = activity
						.getOutputPorts();
				for (OutputPort actOutPort : activityOuts) {
					if (activity.getOutputPortMapping().containsKey(
							actOutPort.getName())) {
						// Should be added from processor ports
						continue;
					}
					outputPorts.add(actOutPort);
				}
			}
			for (ProcessorOutputPort procOutPort : processor.getOutputPorts()) {
				// Probably already mapped, so probably also already connected,
				// so we'll list these last.

				// We'll skip the empty ones as they were added earlier
				if (!procOutPort.getOutgoingLinks().isEmpty()) {
					outputPorts.add(procOutPort);
				}
			}
			if (!outputPorts.isEmpty()) {
				allOutPorts.put(processor, outputPorts);
			}

		}
		return allOutPorts;
	}
}
