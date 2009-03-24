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
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;

/**
 * Action for adding an output port to the dataflow.
 *
 * @author David Withers
 */
public class AddDataflowOutputAction extends DataflowEditAction {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AddDataflowOutputAction.class);

	public AddDataflowOutputAction(Dataflow dataflow, Component component) {
		super(dataflow, component);
		putValue(SMALL_ICON, WorkbenchIcons.outputIcon);
		putValue(NAME, "Add workflow output port");	
		putValue(SHORT_DESCRIPTION, "Add workflow output port");		
	}

	public void actionPerformed(ActionEvent event) {
		try {
			Set<String> usedOutputPorts = new HashSet<String>();
			for (DataflowOutputPort outputPort : dataflow.getOutputPorts()) {
				usedOutputPorts.add(outputPort.getName());
			}

			DataflowOutputPortPanel inputPanel = new DataflowOutputPortPanel();
			
			ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
					"Add Workflow Output Port", inputPanel);
			vuid.addTextComponentValidation(inputPanel.getPortNameField(),
					"Set the workflow output port name.", usedOutputPorts,
					"Duplicate workflow output port name.", "[\\p{L}\\p{Digit}_.]+",
					"Invalid workflow output port name.");
			vuid.setSize(new Dimension(400, 200));

			if (vuid.show(component)) {
				String portName = inputPanel.getPortName();
				DataflowOutputPort dataflowOutputPort = edits.createDataflowOutputPort(portName, dataflow);
				editManager.doDataflowEdit(dataflow, edits.getAddDataflowOutputPortEdit(dataflow, dataflowOutputPort));
			}
		} catch (EditException e) {
			logger.debug("Create dataflow output port failed", e);
		}

	}

}
