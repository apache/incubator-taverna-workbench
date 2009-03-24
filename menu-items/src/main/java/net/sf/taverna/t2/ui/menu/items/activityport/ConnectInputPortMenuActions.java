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
import net.sf.taverna.t2.ui.menu.ContextualMenuComponent;
import net.sf.taverna.t2.ui.menu.MenuManager.ComponentFactory;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public class ConnectInputPortMenuActions extends AbstractConnectPortMenuActions
		implements ContextualMenuComponent {

	public ConnectInputPortMenuActions() {
		super(ActivityInputPortSection.activityInputPortSection, 20);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof ActivityInputPort
				&& getContextualSelection().getParent() instanceof Dataflow;
	}

	private void addProcessorMenuItems(Dataflow dataflow,
			ActivityInputPort inputPort, JMenu connectMenu) {
		final Map<Processor, List<OutputPort>> ports = findOutputPorts(
				dataflow, inputPort);
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
			processorMenu.add(new ShadedLabel(SERVICE_OUTPUT_PORTS,
					processorPortColour));
			menuItems.add(processorMenu);

			List<OutputPort> outputPorts = ports.get(processor);
			Collections.sort(outputPorts, portComparator);
			List<JMenuItem> processorMenuItems = new ArrayList<JMenuItem>();
			for (OutputPort outputPort : outputPorts) {
				ConnectPortsAction connectPortsAction = new ConnectPortsAction(
						dataflow, outputPort, inputPort);
				connectPortsAction.putValue(Action.NAME, outputPort.getName());
				connectPortsAction.putValue(Action.SMALL_ICON,
						WorkbenchIcons.outputPortIcon);
				processorMenuItems.add(new JMenuItem(connectPortsAction));
			}

			menuManager.addMenuItemsWithExpansion(processorMenuItems,
					processorMenu, workbenchConfiguration.getMaxMenuItems(),
					new ComponentFactory() {
						public Component makeComponent() {
							return new ShadedLabel(SERVICE_OUTPUT_PORTS,
									processorPortColour);
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

	@Override
	protected Component createCustomComponent() {
		Dataflow dataflow = (Dataflow) getContextualSelection().getParent();
		ActivityInputPort inputPort = (ActivityInputPort) getContextualSelection()
				.getSelection();
		// Component component =
		// getContextualSelection().getRelativeToComponent();

		JMenu connectMenu = new JMenu(new DummyAction(CONNECT_WITH_OUTPUT_FROM,
				WorkbenchIcons.datalinkIcon));

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
