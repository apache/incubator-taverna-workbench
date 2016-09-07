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

package org.apache.taverna.workbench.ui.impl;

import static org.apache.taverna.scufl2.api.container.WorkflowBundle.generateIdentifier;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.edits.EditManager.AbstractDataflowEditEvent;
import org.apache.taverna.workbench.edits.EditManager.DataFlowRedoEvent;
import org.apache.taverna.workbench.edits.EditManager.DataFlowUndoEvent;
import org.apache.taverna.workbench.edits.EditManager.DataflowEditEvent;
import org.apache.taverna.workbench.edits.EditManager.EditManagerEvent;
import org.apache.taverna.workflow.edits.UpdateDataflowInternalIdentifierEdit;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Listens out for any edits on a dataflow and changes its internal id (or back
 * to the old one in the case of redo/undo). Is first created when the workbench
 * is initialised.
 * 
 * @author Ian Dunlop
 */
public class DataflowEditsListener implements Observer<EditManagerEvent> {
	private static Logger logger = Logger
			.getLogger(DataflowEditsListener.class);

	private Map<Edit<?>, URI> dataflowEditMap;

	public DataflowEditsListener() {
		super();
		dataflowEditMap = new HashMap<>();
	}

	/**
	 * Receives {@link EditManagerEvent}s from the {@link EditManager} and
	 * changes the id of the {@link Dataflow} to a new one or back to its old
	 * one depending on whether it is a do/undo/redo event. Stores the actual
	 * edit and the pre-edit dataflow id in a Map and changes the id when it
	 * gets further actions against this same edit
	 */
	@Override
	public void notify(Observable<EditManagerEvent> observable,
			EditManagerEvent event) throws Exception {
		Edit<?> edit = event.getEdit();
		WorkflowBundle dataFlow = ((AbstractDataflowEditEvent) event)
				.getDataFlow();

		if (event instanceof DataflowEditEvent) {
			/*
			 * the dataflow has been edited in some way so change its internal
			 * id and store the old one against the edit that is changing
			 * 'something'
			 */
			URI internalIdentifier = dataFlow.getGlobalBaseURI();
			dataflowEditMap.put(edit, internalIdentifier);
			URI newIdentifier = generateIdentifier();
			new UpdateDataflowInternalIdentifierEdit(dataFlow, newIdentifier)
					.doEdit();
			logger.debug("Workflow edit, id changed from: "
					+ internalIdentifier + " to " + newIdentifier);
		} else if (event instanceof DataFlowRedoEvent) {
			/*
			 * change the id back to the old one and store the new one in case
			 * we want to change it back
			 */
			URI newId = dataFlow.getGlobalBaseURI();
			URI oldId = dataflowEditMap.get(edit);
			dataflowEditMap.put(edit, newId);
			new UpdateDataflowInternalIdentifierEdit(dataFlow, oldId).doEdit();
			logger.debug("Workflow edit, id changed from: " + newId + " to "
					+ oldId);
		} else if (event instanceof DataFlowUndoEvent) {
			/*
			 * change the id back to the old one and store the new one in case
			 * we want to change it back
			 */
			URI newId = dataFlow.getGlobalBaseURI();
			URI oldId = dataflowEditMap.get(edit);
			dataflowEditMap.put(edit, newId);
			new UpdateDataflowInternalIdentifierEdit(dataFlow, oldId).doEdit();
			logger.debug("Workflow edit, id changed from: " + newId + " to "
					+ oldId);
		}
	}
}
