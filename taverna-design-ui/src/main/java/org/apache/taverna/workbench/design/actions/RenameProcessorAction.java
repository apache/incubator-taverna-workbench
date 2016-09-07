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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.taverna.lang.ui.ValidatingUserInputDialog;
import org.apache.taverna.workbench.design.ui.ProcessorPanel;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workflow.edits.RenameEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * Action for renaming a processor.
 * 
 * @author David Withers
 */
public class RenameProcessorAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(RenameProcessorAction.class);

	private Processor processor;

	public RenameProcessorAction(Workflow dataflow, Processor processor,
			Component component, EditManager editManager,
			SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		this.processor = processor;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Rename service...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Set<String> usedProcessors = new HashSet<>();
		for (Processor usedProcessor : dataflow.getProcessors())
			if (!usedProcessor.getName().equals(processor.getName()))
				usedProcessors.add(usedProcessor.getName());

		ProcessorPanel inputPanel = new ProcessorPanel();

		ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
				"Rename service", inputPanel);
		vuid.addTextComponentValidation(inputPanel.getProcessorNameField(),
				"Set the service name.", usedProcessors, "Duplicate service.",
				"[\\p{L}\\p{Digit}_.]+", "Invalid service name.");
		vuid.setSize(new Dimension(400, 200));

		inputPanel.setProcessorName(processor.getName());

		try {
			if (vuid.show(component))
				changeProcessorName(inputPanel);
		} catch (EditException e1) {
			logger.debug("Rename service (processor) failed", e1);
		}
	}

	private void changeProcessorName(ProcessorPanel inputPanel)
			throws EditException {
		String processorName = inputPanel.getProcessorName();
		editManager.doDataflowEdit(dataflow.getParent(), new RenameEdit<>(
				processor, processorName));
	}
}
