/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.design.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import org.apache.taverna.lang.ui.ValidatingUserInputDialog;
import org.apache.taverna.workbench.design.ui.DataflowInputPortPanel;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workflow.edits.AddWorkflowInputPortEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;

/**
 * Action for adding an input port to the dataflow.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class AddDataflowInputAction extends DataflowEditAction {
	private static final Logger logger = Logger
			.getLogger(AddDataflowInputAction.class);

	public AddDataflowInputAction(Workflow dataflow, Component component,
			EditManager editManager, SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		putValue(SMALL_ICON, WorkbenchIcons.inputIcon);
		putValue(NAME, "Workflow input port");
		putValue(SHORT_DESCRIPTION, "Add workflow input port");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			Set<String> usedInputPorts = new HashSet<>();
			for (InputWorkflowPort inputPort : dataflow.getInputPorts())
				usedInputPorts.add(inputPort.getName());

			DataflowInputPortPanel inputPanel = new DataflowInputPortPanel();

			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Add Workflow Input Port", inputPanel);
			vuid.addTextComponentValidation(inputPanel.getPortNameField(),
					"Set the workflow input port name.", usedInputPorts,
					"Duplicate workflow input port name.",
					"[\\p{L}\\p{Digit}_.]+",
					"Invalid workflow input port name.");
			vuid.addMessageComponent(inputPanel.getSingleValueButton(),
					"Set the input port type.");
			vuid.addMessageComponent(inputPanel.getListValueButton(),
					"Set the input port list depth.");
			vuid.setSize(new Dimension(400, 250));

			inputPanel.setPortDepth(0);

			if (vuid.show(component)) {
				InputWorkflowPort dataflowInputPort = new InputWorkflowPort();
				dataflowInputPort.setName(inputPanel.getPortName());
				dataflowInputPort.setDepth(inputPanel.getPortDepth());
				editManager.doDataflowEdit(dataflow.getParent(),
						new AddWorkflowInputPortEdit(dataflow,
								dataflowInputPort));
			}
		} catch (EditException e) {
			logger.warn("Adding a new workflow input port failed");
		}
	}
}
