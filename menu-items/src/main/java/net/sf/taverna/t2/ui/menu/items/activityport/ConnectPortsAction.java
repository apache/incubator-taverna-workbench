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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ConnectPortsAction extends AbstractAction {
	private static Logger logger = Logger.getLogger(ConnectPortsAction.class);
	private final Dataflow dataflow;
	private EditManager editManager = EditManager.getInstance();
	private final InputPort inputPort;
	private final OutputPort outputPort;

	public ConnectPortsAction(Dataflow dataflow, 
			OutputPort outputPort, InputPort inputPort) {
		super("Connect " + inputPort.getName() + " to " + outputPort.getName());
		this.dataflow = dataflow;
		this.inputPort = inputPort;
		this.outputPort = outputPort;
	}

	public void actionPerformed(ActionEvent e) {
		Edit<?> edit = Tools.getCreateAndConnectDatalinkEdit(dataflow,
				outputPort, inputPort);
		try {
			editManager.doDataflowEdit(dataflow, edit);
		} catch (EditException ex) {
			logger.warn("Can't create connection between " + inputPort
					+ " and " + outputPort, ex);
		}
	}
}
