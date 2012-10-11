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

import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflow.edits.AddDataLinkEdit;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.ReceiverPort;
import uk.org.taverna.scufl2.api.port.SenderPort;

@SuppressWarnings("serial")
public class ConnectPortsAction extends AbstractAction {
	private static Logger logger = Logger.getLogger(ConnectPortsAction.class);
	private final Workflow workflow;
	private final ReceiverPort receiverPort;
	private final SenderPort senderPort;
	private final EditManager editManager;

	public ConnectPortsAction(Workflow workflow,
			SenderPort senderPort, ReceiverPort receiverPort, EditManager editManager) {
		super("Connect " + senderPort.getName() + " to " + receiverPort.getName());
		this.workflow = workflow;
		this.receiverPort = receiverPort;
		this.senderPort = senderPort;
		this.editManager = editManager;
	}

	public void actionPerformed(ActionEvent e) {
		DataLink dataLink = new DataLink();
		dataLink.setReceivesFrom(senderPort);
		dataLink.setSendsTo(receiverPort);
		Edit<Workflow> edit = new AddDataLinkEdit(workflow, dataLink);
		try {
			editManager.doDataflowEdit(workflow.getParent(), edit);
		} catch (EditException ex) {
			logger.warn("Can't create connection between " + senderPort
					+ " and " + receiverPort, ex);
		}
	}
}
