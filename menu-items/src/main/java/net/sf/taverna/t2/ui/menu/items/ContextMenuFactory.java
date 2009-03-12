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
package net.sf.taverna.t2.ui.menu.items;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import net.sf.taverna.t2.lang.ui.ShadedLabel;
import net.sf.taverna.t2.workbench.design.actions.AddConditionAction;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowInputAction;
import net.sf.taverna.t2.workbench.design.actions.AddDataflowOutputAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveConditionAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveDatalinkAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveMergeAction;
import net.sf.taverna.t2.workbench.design.actions.RemoveProcessorAction;
import net.sf.taverna.t2.workbench.design.actions.RenameProcessorAction;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.Processor;

public class ContextMenuFactory {

	public static JPopupMenu getContextMenu(Dataflow dataflow, Object dataflowObject, Component component) {
		JPopupMenu popupMenu = new JPopupMenu();

		if (dataflowObject instanceof Dataflow) {
			popupMenu.add(new ShadedLabel("Workflow Inputs", ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new AddDataflowInputAction(dataflow, component)));
			popupMenu.addSeparator();
			popupMenu.add(new ShadedLabel("Workflow Outputs", ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new AddDataflowOutputAction(dataflow, component)));
		} else if (dataflowObject instanceof Processor) {
			Processor processor = (Processor) dataflowObject;
			popupMenu.add(new ShadedLabel("Processor: " + processor.getLocalName(), ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RenameProcessorAction(dataflow, processor, component)));
			List<AddConditionAction> conditions = getAddConditionActions(dataflow, processor, component);
			if (!conditions.isEmpty()) {
				JMenu conditionMenu = new JMenu("Coordinate from");
				conditionMenu.setIcon(WorkbenchIcons.controlLinkIcon);
				conditionMenu.add(new ShadedLabel("Processors:", ShadedLabel.ORANGE));
				conditionMenu.addSeparator();
				for (AddConditionAction addConditionAction : conditions) {
					conditionMenu.add(new JMenuItem(addConditionAction));
				}
				popupMenu.add(conditionMenu);
			}
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RemoveProcessorAction(dataflow, processor, component)));
		} else if (dataflowObject instanceof Merge) {
			Merge merge = (Merge) dataflowObject;
			popupMenu.add(new ShadedLabel("Merge", ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RemoveMergeAction(dataflow, merge, component)));
		} else if (dataflowObject instanceof DataflowInputPort) {
			DataflowInputPort dataflowInputPort = (DataflowInputPort) dataflowObject;
			popupMenu.add(new ShadedLabel("Workflow Input : " + dataflowInputPort.getName(), ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new EditDataflowInputPortAction(dataflow, dataflowInputPort, component)));		
			popupMenu.add(new JMenuItem(new RemoveDataflowInputPortAction(dataflow, dataflowInputPort, component)));		
		} else if (dataflowObject instanceof DataflowOutputPort) {
			DataflowOutputPort dataflowOutputPort = (DataflowOutputPort) dataflowObject;
			popupMenu.add(new ShadedLabel("Workflow Output : " + dataflowOutputPort.getName(), ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new EditDataflowOutputPortAction(dataflow, dataflowOutputPort, component)));		
			popupMenu.add(new JMenuItem(new RemoveDataflowOutputPortAction(dataflow, dataflowOutputPort, component)));		
//		} else if (dataflowObject instanceof ActivityInputPort) {
//			ActivityInputPort inputPort = (ActivityInputPort) dataflowObject;
//			popupMenu.add(new ShadedLabel("Input port : " + inputPort.getName(), ShadedLabel.GREEN));
//			popupMenu.addSeparator();
//			popupMenu.add(new JMenuItem(new AddInputPortDefaultValueAction(dataflow, inputPort, component)));	
		} else if (dataflowObject instanceof Datalink) {
			popupMenu.add(new ShadedLabel("Link",         ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RemoveDatalinkAction(dataflow, (Datalink) dataflowObject, component)));		
		} else if (dataflowObject instanceof Condition) {
			popupMenu.add(new ShadedLabel("Coordination", ShadedLabel.GREEN));
			popupMenu.addSeparator();
			popupMenu.add(new JMenuItem(new RemoveConditionAction(dataflow, (Condition) dataflowObject, component)));		
		}
		return popupMenu;
	}

	private static List<AddConditionAction> getAddConditionActions(Dataflow dataflow, Processor targetProcessor, Component component) {
		List<AddConditionAction> actions = new ArrayList<AddConditionAction>();
		Set<Processor> invalidControlProcessors = new HashSet<Processor>();
		invalidControlProcessors.add(targetProcessor);
		for (Condition condition : targetProcessor.getPreconditionList()) {
			invalidControlProcessors.add(condition.getControl());
		}
		for (Processor processor : dataflow.getProcessors()) {
			if (!invalidControlProcessors.contains(processor)) {
				actions.add(new AddConditionAction(dataflow, processor, targetProcessor, component));
			}
		}
		return actions;
	}

}
