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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.design.ui.DataflowOutputPortPanel;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.RenameEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;

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
