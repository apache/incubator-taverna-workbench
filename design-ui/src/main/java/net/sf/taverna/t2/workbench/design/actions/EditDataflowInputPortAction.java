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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.design.ui.DataflowInputPortPanel;
import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workflow.edits.ChangeDepthEdit;
import net.sf.taverna.t2.workflow.edits.RenameEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.DepthPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.Port;

/**
 * Action for editing a dataflow input port.
 *
 * @author David Withers
 */
public class EditDataflowInputPortAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger
			.getLogger(EditDataflowInputPortAction.class);

	private InputWorkflowPort port;

	public EditDataflowInputPortAction(Workflow dataflow,
			InputWorkflowPort port, Component component, EditManager editManager, SelectionManager selectionManager) {
		super(dataflow, component, editManager, selectionManager);
		this.port = port;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Edit workflow input port...");
	}

	public void actionPerformed(ActionEvent e) {
		try {
			Set<String> usedInputPorts = new HashSet<String>();
			for (InputWorkflowPort usedInputPort : dataflow.getInputPorts()) {
				if (!usedInputPort.getName().equals(port.getName())) {
					usedInputPorts.add(usedInputPort.getName());
				}
			}

			DataflowInputPortPanel inputPanel = new DataflowInputPortPanel();

			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Edit Workflow Input Port", inputPanel);
			vuid.addTextComponentValidation(inputPanel.getPortNameField(),
					"Set the workflow input port name.", usedInputPorts,
					"Duplicate workflow input port name.", "[\\p{L}\\p{Digit}_.]+",
					"Invalid workflow input port name.");
			vuid.addMessageComponent(inputPanel.getSingleValueButton(), "Set the input port type.");
			vuid.addMessageComponent(inputPanel.getListValueButton(), "Set the input port list depth.");
			vuid.setSize(new Dimension(400, 250));

			inputPanel.setPortName(port.getName());
			inputPanel.setPortDepth(port.getDepth());

			if (vuid.show(component)) {
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				String portName = inputPanel.getPortName();
				if (!portName.equals(port.getName())) {
					editList.add(new RenameEdit<Port>(port, portName));
				}
				int portDepth = inputPanel.getPortDepth();
				if (portDepth != port.getDepth()) {
					editList.add(new ChangeDepthEdit<DepthPort>(port, portDepth));
				}
				if (editList.size() == 1) {
					editManager.doDataflowEdit(dataflow.getParent(), editList.get(0));
				} else if (editList.size() > 1) {
					editManager.doDataflowEdit(dataflow.getParent(), new CompoundEdit(editList));
				}
			}
		} catch (EditException e1) {
			logger.warn("Rename workflow input port failed", e1);
		}
	}

}
