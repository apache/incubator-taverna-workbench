/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

package org.apache.taverna.workbench.design.actions;

import static org.apache.taverna.workbench.icons.WorkbenchIcons.deleteIcon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.workbench.edits.CompoundEdit;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workflow.edits.RemoveChildEdit;
import org.apache.taverna.workflow.edits.RemoveDataLinkEdit;

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
