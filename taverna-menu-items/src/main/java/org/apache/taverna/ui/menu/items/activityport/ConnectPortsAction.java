package org.apache.taverna.ui.menu.items.activityport;
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workflow.edits.AddDataLinkEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.DataLink;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.ReceiverPort;
import org.apache.taverna.scufl2.api.port.SenderPort;

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
