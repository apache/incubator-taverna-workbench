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

import javax.swing.AbstractAction;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.DataflowSelectionModel;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * Abstract superclass of dataflow edit actions.
 * 
 * @author David Withers
 */
public abstract class DataflowEditAction extends AbstractAction {
	private static final long serialVersionUID = -1155192575675025091L;

	protected final SelectionManager selectionManager;
	protected EditManager editManager;
	protected DataflowSelectionModel dataflowSelectionModel;
	protected Workflow dataflow;
	protected Component component;
	protected Scufl2Tools scufl2Tools = new Scufl2Tools();

	public DataflowEditAction(Workflow dataflow, Component component,
			EditManager editManager, SelectionManager selectionManager) {
		this.dataflow = dataflow;
		this.component = component;
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		dataflowSelectionModel = selectionManager
				.getDataflowSelectionModel(dataflow.getParent());
	}
}
