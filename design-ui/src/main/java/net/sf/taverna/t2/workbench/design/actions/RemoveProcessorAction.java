/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
 ******************************************************************************/
package net.sf.taverna.t2.workbench.design.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.utils.Tools;
import net.sf.taverna.t2.workbench.ui.actions.activity.ActivityConfigurationAction;

import org.apache.log4j.Logger;

/**
 * Action for removing a processor from the dataflow.
 *
 * @author David Withers
 */
public class RemoveProcessorAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RemoveProcessorAction.class);

	private Processor processor;

	public RemoveProcessorAction(Dataflow dataflow, Processor processor, Component component) {
		super(dataflow, component);
		this.processor = processor;
		putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
		putValue(NAME, "Delete service");		
	}

	public void actionPerformed(ActionEvent e) {
		try {
			List<? extends ProcessorInputPort> inputPorts = processor.getInputPorts();
			List<? extends ProcessorOutputPort> outputPorts = processor.getOutputPorts();
			List<? extends Condition> controlledPreconditions = processor.getControlledPreconditionList();
			List<? extends Condition> preconditions = processor.getPreconditionList();
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			for (ProcessorInputPort inputPort : inputPorts) {
				Datalink datalink = inputPort.getIncomingLink();
				if (datalink != null) {
					editList.add(Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
				}
			}
			for (ProcessorOutputPort outputPort : outputPorts) {
				for (Datalink datalink : outputPort.getOutgoingLinks()) {
					editList.add(Tools.getDisconnectDatalinkAndRemovePortsEdit(datalink));
				}
			}
			for (Condition condition : controlledPreconditions) {
				editList.add(edits.getRemoveConditionEdit(condition.getControl(), condition.getTarget()));
			}
			for (Condition condition : preconditions) {
				editList.add(edits.getRemoveConditionEdit(condition.getControl(), condition.getTarget()));
			}

			if (editList.isEmpty()) {
				editManager.doDataflowEdit(dataflow, edits.getRemoveProcessorEdit(dataflow, processor));
			} else {
				editList.add(edits.getRemoveProcessorEdit(dataflow, processor));
				editManager.doDataflowEdit(dataflow, new CompoundEdit(editList));
			}
			dataflowSelectionModel.removeSelection(processor);
		} catch (EditException e1) {
			logger.error("Delete processor failed", e1);
		}
	}

}
