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

import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.DataflowSelectionManager;
import net.sf.taverna.t2.workflow.edits.RemoveControlLinkEdit;
import net.sf.taverna.t2.workflow.edits.RemoveDataLinkEdit;
import net.sf.taverna.t2.workflow.edits.RemoveProcessorEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.common.NamedSet;
import uk.org.taverna.scufl2.api.core.BlockingControlLink;
import uk.org.taverna.scufl2.api.core.ControlLink;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;

/**
 * Action for removing a processor from the dataflow.
 *
 * @author David Withers
 */
public class RemoveProcessorAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RemoveProcessorAction.class);

	private Processor processor;

	public RemoveProcessorAction(Workflow dataflow, Processor processor, Component component, EditManager editManager, DataflowSelectionManager dataflowSelectionManager) {
		super(dataflow, component, editManager, dataflowSelectionManager);
		this.processor = processor;
		putValue(SMALL_ICON, WorkbenchIcons.deleteIcon);
		putValue(NAME, "Delete service");
	}

	public void actionPerformed(ActionEvent e) {
		try {
			NamedSet<InputProcessorPort> inputPorts = processor.getInputPorts();
			NamedSet<OutputProcessorPort> outputPorts = processor.getOutputPorts();
			List<BlockingControlLink> controlLinksBlocking = scufl2Tools.controlLinksBlocking(processor);
			List<BlockingControlLink> controlLinksWaitingFor = scufl2Tools.controlLinksWaitingFor(processor);
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			for (InputProcessorPort inputPort : inputPorts) {
				for (DataLink datalink : scufl2Tools.datalinksTo(inputPort)) {
					editList.add(new RemoveDataLinkEdit(dataflow, datalink));
				}
			}
			for (OutputProcessorPort outputPort : outputPorts) {
				for (DataLink datalink : scufl2Tools.datalinksFrom(outputPort)) {
					editList.add(new RemoveDataLinkEdit(dataflow, datalink));
				}
			}
			for (ControlLink controlLink : controlLinksBlocking) {
				editList.add(new RemoveControlLinkEdit(dataflow, controlLink));
			}
			for (ControlLink controlLink : controlLinksWaitingFor) {
				editList.add(new RemoveControlLinkEdit(dataflow, controlLink));
			}

			if (editList.isEmpty()) {
				editManager.doDataflowEdit(dataflow.getParent(), new RemoveProcessorEdit(dataflow, processor));
			} else {
				editList.add(new RemoveProcessorEdit(dataflow, processor));
				editManager.doDataflowEdit(dataflow.getParent(), new CompoundEdit(editList));
			}
			dataflowSelectionModel.removeSelection(processor);
		} catch (EditException e1) {
			logger.error("Delete processor failed", e1);
		}
	}

}
