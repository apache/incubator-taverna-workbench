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

import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.deleteIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.RemoveChildEdit;
import net.sf.taverna.t2.workflow.edits.RemoveDataLinkEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.NamedSet;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.BlockingControlLink;
import org.apache.taverna.scufl2.api.core.ControlLink;
import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.OutputProcessorPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * Action for removing a processor from the dataflow.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class RemoveProcessorAction extends DataflowEditAction {
	private static final Logger logger = Logger
			.getLogger(RemoveProcessorAction.class);

	private Processor processor;

	public RemoveProcessorAction(Workflow dataflow, Processor processor,
			Component component, EditManager editManager,
			SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		this.processor = processor;
		putValue(SMALL_ICON, deleteIcon);
		putValue(NAME, "Delete service");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			dataflowSelectionModel.removeSelection(processor);

			NamedSet<InputProcessorPort> inputPorts = processor.getInputPorts();
			NamedSet<OutputProcessorPort> outputPorts = processor
					.getOutputPorts();
			List<BlockingControlLink> controlLinksBlocking = scufl2Tools
					.controlLinksBlocking(processor);
			List<BlockingControlLink> controlLinksWaitingFor = scufl2Tools
					.controlLinksWaitingFor(processor);
			List<Edit<?>> editList = new ArrayList<>();
			for (InputProcessorPort inputPort : inputPorts)
				for (DataLink datalink : scufl2Tools.datalinksTo(inputPort))
					editList.add(new RemoveDataLinkEdit(dataflow, datalink));
			for (OutputProcessorPort outputPort : outputPorts)
				for (DataLink datalink : scufl2Tools.datalinksFrom(outputPort))
					editList.add(new RemoveDataLinkEdit(dataflow, datalink));
			for (ControlLink controlLink : controlLinksBlocking)
				editList.add(new RemoveChildEdit<>(dataflow, controlLink));
			for (ControlLink controlLink : controlLinksWaitingFor)
				editList.add(new RemoveChildEdit<>(dataflow, controlLink));

			for (Profile profile : dataflow.getParent().getProfiles()) {
				List<ProcessorBinding> processorBindings = scufl2Tools
						.processorBindingsForProcessor(processor, profile);
				for (ProcessorBinding processorBinding : processorBindings) {
					Activity boundActivity = processorBinding
							.getBoundActivity();
					List<ProcessorBinding> processorBindingsToActivity = scufl2Tools
							.processorBindingsToActivity(boundActivity);
					if (processorBindingsToActivity.size() == 1) {
						editList.add(new RemoveChildEdit<>(profile,
								boundActivity));
						for (Configuration configuration : scufl2Tools
								.configurationsFor(boundActivity, profile))
							editList.add(new RemoveChildEdit<Profile>(profile,
									configuration));
					}
					editList.add(new RemoveChildEdit<Profile>(profile,
							processorBinding));
				}
			}
			for (Profile profile : dataflow.getParent().getProfiles()) {
				List<Configuration> configurations = scufl2Tools
						.configurationsFor(processor, profile);
				for (Configuration configuration : configurations)
					editList.add(new RemoveChildEdit<>(profile, configuration));
			}
			if (editList.isEmpty())
				editManager.doDataflowEdit(dataflow.getParent(),
						new RemoveChildEdit<>(dataflow, processor));
			else {
				editList.add(new RemoveChildEdit<>(dataflow, processor));
				editManager.doDataflowEdit(dataflow.getParent(),
						new CompoundEdit(editList));
			}
		} catch (EditException e1) {
			logger.error("Delete processor failed", e1);
		}
	}
}
