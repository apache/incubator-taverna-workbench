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
import net.sf.taverna.t2.ui.menu.MenuManager.ComponentFactory;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

public class ConnectOutputPortMenuActions extends AbstractConnectPortMenuActions  {

	public ConnectOutputPortMenuActions() {
		super(ActivityOutputPortSection.activityOutputPortSection, 20);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof ActivityOutputPort
				&& getContextualSelection().getParent() instanceof Dataflow;
	}


	protected void addProcessorMenuItems(Dataflow dataflow,
			ActivityOutputPort outputPort, JMenu connectMenu) {
		final Map<Processor, List<InputPort>> ports = findInputPorts(dataflow,
				outputPort);
		if (ports.isEmpty()) {
			return;
		}
		connectMenu.add(new ShadedLabel(SERVICES, colourManager
				.getPreferredColour(Processor.class.getCanonicalName())));
		connectMenu.addSeparator();

		List<Processor> processors = new ArrayList<Processor>(ports.keySet());
		Collections.sort(processors, processorComparator);

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
			processorMenu.add(new ShadedLabel(SERVICE_INPUT_PORTS,
					processorPortColour));
			menuItems.add(processorMenu);

			List<InputPort> inputPorts = ports.get(processor);
			Collections.sort(inputPorts, portComparator);
			List<JMenuItem> processorMenuItems = new ArrayList<JMenuItem>();
			for (InputPort inputPort : inputPorts) {
				ConnectPortsAction connectPortsAction = new ConnectPortsAction(
						dataflow, outputPort, inputPort);
				connectPortsAction.putValue(Action.NAME, inputPort.getName());
				connectPortsAction.putValue(Action.SMALL_ICON,
						WorkbenchIcons.inputPortIcon);
				processorMenuItems.add(new JMenuItem(connectPortsAction));
			}
			menuManager.addMenuItemsWithExpansion(processorMenuItems,
					processorMenu, workbenchConfiguration.getMaxMenuItems(),
					new ComponentFactory() {
						public Component makeComponent() {
							return new ShadedLabel(SERVICE_INPUT_PORTS,
									processorPortColour);
						}
					});
		}
		menuManager.addMenuItemsWithExpansion(menuItems, connectMenu,
				(workbenchConfiguration.getMaxMenuItems()),
				new ComponentFactory() {
					public Component makeComponent() {
						return new ShadedLabel(SERVICES, ShadedLabel.GREEN);
					}
				});
	}

	@Override
	protected Component createCustomComponent() {
		Dataflow dataflow = (Dataflow) getContextualSelection().getParent();
		ActivityOutputPort outputPort = (ActivityOutputPort) getContextualSelection()
				.getSelection();
		// Component component =
		// getContextualSelection().getRelativeToComponent();

		JMenu connectMenu = new JMenu(new DummyAction(CONNECT_AS_INPUT_TO,
				WorkbenchIcons.datalinkIcon));
		addPortMenuItems(dataflow, outputPort, connectMenu);
		addProcessorMenuItems(dataflow, outputPort, connectMenu);
		return connectMenu;
	}

	protected Map<Processor, List<InputPort>> findInputPorts(Dataflow dataflow,
			ActivityOutputPort sourcePort) {
		HashMap<Processor, List<InputPort>> allInPorts = new HashMap<Processor, List<InputPort>>();
		Processor ourProcessor = Tools.getFirstProcessorWithActivityOutputPort(
				dataflow, sourcePort);
		for (Processor processor : Tools.possibleDownStreamProcessors(dataflow,
				ourProcessor)) {
			List<InputPort> inputPorts = new ArrayList<InputPort>();
			for (ProcessorInputPort procInPort : processor.getInputPorts()) {
				inputPorts.add(procInPort);
			}

			for (Activity<?> activity : processor.getActivityList()) {
				Set<ActivityInputPort> activityIns = activity.getInputPorts();
				for (InputPort actInPort : activityIns) {
					if (activity.getInputPortMapping().containsValue(
							actInPort.getName())) {
						// Should be added from processor ports
						continue;
					}
					inputPorts.add(actInPort);
				}
			}
			if (!inputPorts.isEmpty()) {
				allInPorts.put(processor, inputPorts);
			}
		}
		return allInPorts;
	}

}
