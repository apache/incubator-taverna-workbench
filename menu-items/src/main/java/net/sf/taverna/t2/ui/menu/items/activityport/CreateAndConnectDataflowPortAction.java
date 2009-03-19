/**********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
 **********************************************************************/
package net.sf.taverna.t2.ui.menu.items.activityport;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.lang.ui.ValidatingUserInputDialog;
import net.sf.taverna.t2.workbench.design.ui.DataflowInputPortPanel;
import net.sf.taverna.t2.workbench.design.ui.DataflowOutputPortPanel;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

/**
 * Action to create a dataflow input/output port and connect it to the specified
 * processor/activity output/input port.
 * <p>
 * The created dataflow port name will be taken from the name of the provided
 * port.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
@SuppressWarnings("serial")
public class CreateAndConnectDataflowPortAction extends AbstractAction {

	private static Logger logger = Logger
			.getLogger(CreateAndConnectDataflowPortAction.class);
	private final Dataflow dataflow;
	private EditManager editManager = EditManager.getInstance();

	private Edits edits = editManager.getEdits();
	private final Port port;
	private final String suggestedName;
	private final Component parentComponent;

	/**
	 * Action for creating a dataflow input/output port and linking it to the
	 * specified port.
	 * <p>
	 * If the provided port is an InputPort (in a Processor or Activity) then a
	 * dataflow OutputPort will be created and linked. Vice versa, if the
	 * provided port is an OutputPort, a dataflow InputPort will be created.
	 * 
	 * @param dataflow
	 *            Dataflow where to create the dataflow input/output port
	 * @param port
	 *            Existing Processor or Activity port to connect to
	 * @param suggestedName
	 *            suggested port name
	 * @param parentComponent
	 *            Component to be parent of any pop-ups
	 */
	public CreateAndConnectDataflowPortAction(Dataflow dataflow, Port port,
			String suggestedName, Component parentComponent) {
		super("Connect to new dataflow port");
		this.dataflow = dataflow;
		this.port = port;
		this.suggestedName = suggestedName;
		this.parentComponent = parentComponent;
		if (!(port instanceof InputPort || port instanceof OutputPort)) {
			throw new IllegalArgumentException("Port " + port
					+ " must be either an InputPort or OutputPort");
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (port instanceof InputPort) {
			String portName = Tools.uniquePortName(suggestedName, dataflow
					.getInputPorts());
			DataflowInputPort dataflowInputPort = edits
					.createDataflowInputPort(portName, port.getDepth(), port
							.getDepth(), dataflow);
			showDialogue(dataflowInputPort);

		} else if (port instanceof OutputPort) {
			String portName = Tools.uniquePortName(suggestedName, dataflow
					.getOutputPorts());
			DataflowOutputPort dataflowOutputPort = edits
					.createDataflowOutputPort(portName, dataflow);
			showDialogue(dataflowOutputPort);
		} else {
			throw new IllegalStateException("Port " + port
					+ " must be either an InputPort or OutputPort");
		}

	}

	protected void showDialogue(DataflowInputPort portTemplate) {
		Set<String> usedInputPorts = new HashSet<String>();
		for (DataflowInputPort usedInputPort : dataflow.getInputPorts()) {
			usedInputPorts.add(usedInputPort.getName());
		}
		DataflowInputPortPanel inputPanel = new DataflowInputPortPanel();

		ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
				"Create workflow input port", inputPanel);
		vuid.addTextComponentValidation(inputPanel.getPortNameField(),
				"Set the workflow input port name.", usedInputPorts,
				"Duplicate workflow input port name.", "[\\p{L}\\p{Digit}_.]+",
				"Invalid workflow input port name.");
		vuid.addMessageComponent(inputPanel.getSingleValueButton(),
				"Set the input port type.");
		vuid.addMessageComponent(inputPanel.getListValueButton(),
				"Set the input port list depth.");
		vuid.setSize(new Dimension(400, 250));

		inputPanel.setPortName(portTemplate.getName());
		inputPanel.setPortDepth(portTemplate.getDepth());

		if (vuid.show(parentComponent)) {
			DataflowInputPort dataflowInputPort = edits
					.createDataflowInputPort(inputPanel.getPortName(),
							inputPanel.getPortDepth(), inputPanel
									.getPortDepth(), dataflow);
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			editList.add(edits.getAddDataflowInputPortEdit(dataflow,
					dataflowInputPort));
			editList.add(Tools
					.getCreateAndConnectDatalinkEdit(dataflow,
							dataflowInputPort.getInternalOutputPort(),
							(InputPort) port));
			try {
				CompoundEdit compoundEdit = new CompoundEdit(editList);
				editManager.doDataflowEdit(dataflow, compoundEdit);
			} catch (EditException ex) {
				logger.warn("Can't create or connect new input port", ex);
			}

		}
	}

	protected void showDialogue(DataflowOutputPort portTemplate) {
		Set<String> usedOutputPorts = new HashSet<String>();
		for (DataflowOutputPort usedInputPort : dataflow.getOutputPorts()) {
			usedOutputPorts.add(usedInputPort.getName());
		}
		DataflowOutputPortPanel outputPanel = new DataflowOutputPortPanel();

		ValidatingUserInputDialog vuid = new ValidatingUserInputDialog(
				"Create workflow output port", outputPanel);
		vuid.addTextComponentValidation(outputPanel.getPortNameField(),
				"Set the workflow output port name.", usedOutputPorts,
				"Duplicate workflow output port name.",
				"[\\p{L}\\p{Digit}_.]+", "Invalid workflow output port name.");
		vuid.setSize(new Dimension(400, 200));
		outputPanel.setPortName(portTemplate.getName());

		if (vuid.show(parentComponent)) {
			List<Edit<?>> editList = new ArrayList<Edit<?>>();
			DataflowOutputPort dataflowOutputPort = edits
					.createDataflowOutputPort(outputPanel.getPortName(),
							dataflow);
			editList.add(edits.getAddDataflowOutputPortEdit(dataflow,
					dataflowOutputPort));
			editList.add(Tools.getCreateAndConnectDatalinkEdit(dataflow,
					(OutputPort) port, dataflowOutputPort
							.getInternalInputPort()));
			try {
				CompoundEdit compoundEdit = new CompoundEdit(editList);
				editManager.doDataflowEdit(dataflow, compoundEdit);
			} catch (EditException ex) {
				logger.warn("Can't create or connect new output port", ex);
			}
		}
	}

}
