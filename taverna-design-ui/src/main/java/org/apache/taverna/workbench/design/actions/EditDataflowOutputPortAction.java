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
import org.apache.taverna.workflow.edits.RenameEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;

/**
 * Action for editing a dataflow output port.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class EditDataflowOutputPortAction extends DataflowEditAction {
	private static final Logger logger = Logger
			.getLogger(EditDataflowOutputPortAction.class);

	private OutputWorkflowPort port;

	public EditDataflowOutputPortAction(Workflow dataflow,
			OutputWorkflowPort port, Component component,
			EditManager editManager, SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		this.port = port;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Edit workflow output port...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Set<String> usedOutputPorts = new HashSet<>();
		for (OutputWorkflowPort usedOutputPort : dataflow.getOutputPorts())
			if (!usedOutputPort.getName().equals(port.getName()))
				usedOutputPorts.add(usedOutputPort.getName());

		DataflowOutputPortPanel inputPanel = new DataflowOutputPortPanel();

		ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
				"Edit Workflow Output Port", inputPanel);
		vuid.addTextComponentValidation(inputPanel.getPortNameField(),
				"Set the workflow output port name.", usedOutputPorts,
				"Duplicate workflow output port name.",
				"[\\p{L}\\p{Digit}_.]+", "Invalid workflow output port name.");
		vuid.setSize(new Dimension(400, 200));

		inputPanel.setPortName(port.getName());

		try {
			if (vuid.show(component))
				changeOutputPort(inputPanel);
		} catch (EditException ex) {
			logger.debug("Rename workflow output port failed", ex);
		}
	}

	private void changeOutputPort(DataflowOutputPortPanel inputPanel)
			throws EditException {
		editManager.doDataflowEdit(dataflow.getParent(), new RenameEdit<>(port,
				inputPanel.getPortName()));
	}
}
