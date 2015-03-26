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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.taverna.lang.ui.ValidatingUserInputDialog;
import org.apache.taverna.workbench.design.ui.DataflowInputPortPanel;
import org.apache.taverna.workbench.edits.CompoundEdit;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.icons.WorkbenchIcons;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workflow.edits.ChangeDepthEdit;
import org.apache.taverna.workflow.edits.RenameEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;

/**
 * Action for editing a dataflow input port.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class EditDataflowInputPortAction extends DataflowEditAction {
	private static Logger logger = Logger
			.getLogger(EditDataflowInputPortAction.class);

	private InputWorkflowPort port;

	public EditDataflowInputPortAction(Workflow dataflow,
			InputWorkflowPort port, Component component,
			EditManager editManager, SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		this.port = port;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Edit workflow input port...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Set<String> usedInputPorts = new HashSet<>();
		for (InputWorkflowPort usedInputPort : dataflow.getInputPorts())
			if (!usedInputPort.getName().equals(port.getName()))
				usedInputPorts.add(usedInputPort.getName());

		DataflowInputPortPanel inputPanel = new DataflowInputPortPanel();

		ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
				"Edit Workflow Input Port", inputPanel);
		vuid.addTextComponentValidation(inputPanel.getPortNameField(),
				"Set the workflow input port name.", usedInputPorts,
				"Duplicate workflow input port name.", "[\\p{L}\\p{Digit}_.]+",
				"Invalid workflow input port name.");
		vuid.addMessageComponent(inputPanel.getSingleValueButton(),
				"Set the input port type.");
		vuid.addMessageComponent(inputPanel.getListValueButton(),
				"Set the input port list depth.");
		vuid.setSize(new Dimension(400, 250));

		inputPanel.setPortName(port.getName());
		inputPanel.setPortDepth(port.getDepth());

		try {
			if (vuid.show(component))
				changeInputPort(inputPanel);
		} catch (EditException e1) {
			logger.warn("Rename workflow input port failed", e1);
		}
	}

	private void changeInputPort(DataflowInputPortPanel inputPanel)
			throws EditException {
		List<Edit<?>> editList = new ArrayList<>();
		String portName = inputPanel.getPortName();
		if (!portName.equals(port.getName()))
			editList.add(new RenameEdit<>(port, portName));
		int portDepth = inputPanel.getPortDepth();
		if (portDepth != port.getDepth())
			editList.add(new ChangeDepthEdit<>(port, portDepth));
		if (editList.size() == 1)
			editManager.doDataflowEdit(dataflow.getParent(), editList.get(0));
		else if (editList.size() > 1)
			editManager.doDataflowEdit(dataflow.getParent(), new CompoundEdit(
					editList));
	}
}
