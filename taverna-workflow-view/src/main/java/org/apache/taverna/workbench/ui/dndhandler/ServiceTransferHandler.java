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
package org.apache.taverna.workbench.ui.dndhandler;

import static java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType;
import static org.apache.taverna.workbench.ui.workflowview.WorkflowView.copyProcessor;
import static org.apache.taverna.workbench.ui.workflowview.WorkflowView.cutProcessor;
import static org.apache.taverna.workbench.ui.workflowview.WorkflowView.pasteTransferable;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;

import org.apache.log4j.Logger;

import org.apache.taverna.services.ServiceRegistry;

/**
 * TransferHandler for accepting ActivityAndBeanWrapper object dropped on the
 * GraphView. On a successful drop a Processor is created from the Activity and
 * configured with the ConfigurationBean.
 *
 * @author David Withers
 * @author Alan R Williams
 */
@SuppressWarnings("serial")
public class ServiceTransferHandler extends TransferHandler {
	private static Logger logger = Logger.getLogger(ServiceTransferHandler.class);

	private DataFlavor serviceDescriptionDataFlavor;

	private final EditManager editManager;
	private final MenuManager menuManager;
	private final SelectionManager selectionManager;
	private final ServiceRegistry serviceRegistry;

	public ServiceTransferHandler(EditManager editManager,
			MenuManager menuManager, SelectionManager selectionManager,
			ServiceRegistry serviceRegistry) {
		this.editManager = editManager;
		this.menuManager = menuManager;
		this.selectionManager = selectionManager;
		this.serviceRegistry = serviceRegistry;

		try {
			serviceDescriptionDataFlavor = new DataFlavor(
					javaJVMLocalObjectMimeType + ";class="
							+ ServiceDescription.class.getCanonicalName(),
					"ServiceDescription", getClass().getClassLoader());
		} catch (ClassNotFoundException e) {
			logger.warn("Could not find the class "
					+ ServiceDescription.class);
		}
	}

	@Override
	public boolean canImport(JComponent component, DataFlavor[] dataFlavors) {
		logger.debug("Trying to import something");
		for (DataFlavor dataFlavor : dataFlavors)
			if (dataFlavor.equals(serviceDescriptionDataFlavor))
				return true;
		return false;
	}

	@Override
	public boolean importData(JComponent component, Transferable transferable) {
		logger.info("Importing a transferable");
		logger.debug(component.getClass().getCanonicalName());
		pasteTransferable(transferable, editManager, menuManager,
				selectionManager, serviceRegistry);
		return true;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		return null;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action)
			throws IllegalStateException {
		super.exportToClipboard(comp, clip, action);
		if (action == COPY)
			copyProcessor(selectionManager);
		else if (action == MOVE)
			cutProcessor(editManager, selectionManager);
	}
}
