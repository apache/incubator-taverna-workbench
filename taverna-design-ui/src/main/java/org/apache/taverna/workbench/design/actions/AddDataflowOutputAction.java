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
import org.apache.taverna.workbench.design.ui.DataflowOutputPortPanel;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workflow.edits.AddWorkflowOutputPortEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;

/**
 * Action for adding an output port to the dataflow.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class AddDataflowOutputAction extends DataflowEditAction {
	private static final Logger logger = Logger
			.getLogger(AddDataflowOutputAction.class);

	public AddDataflowOutputAction(Workflow dataflow, Component component,
			EditManager editManager, SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		putValue(SMALL_ICON, WorkbenchIcons.outputIcon);
		putValue(NAME, "Workflow output port");
		putValue(SHORT_DESCRIPTION, "Add workflow output port");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			Set<String> usedOutputPorts = new HashSet<>();
			for (OutputWorkflowPort outputPort : dataflow.getOutputPorts())
				usedOutputPorts.add(outputPort.getName());

			DataflowOutputPortPanel inputPanel = new DataflowOutputPortPanel();

			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Add Workflow Output Port", inputPanel);
			vuid.addTextComponentValidation(inputPanel.getPortNameField(),
					"Set the workflow output port name.", usedOutputPorts,
					"Duplicate workflow output port name.",
					"[\\p{L}\\p{Digit}_.]+",
					"Invalid workflow output port name.");
			vuid.setSize(new Dimension(400, 200));

			if (vuid.show(component)) {
				String portName = inputPanel.getPortName();
				OutputWorkflowPort dataflowOutputPort = new OutputWorkflowPort();
				dataflowOutputPort.setName(portName);
				editManager.doDataflowEdit(dataflow.getParent(),
						new AddWorkflowOutputPortEdit(dataflow,
								dataflowOutputPort));
			}
		} catch (EditException e) {
			logger.debug("Create workflow output port failed", e);
		}
	}
}
