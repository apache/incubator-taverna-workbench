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

package org.apache.taverna.workbench.views.graph.toolbar;

import static org.apache.taverna.workbench.views.graph.toolbar.GraphEditToolbarSection.GRAPH_EDIT_TOOLBAR_SECTION;

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.views.graph.actions.RenameWFInputOutputProcessorAction;

/**
 * @author Alex Nenadic
 */
public class RenameWFInputOutputProcessorToolbarAction extends
		AbstractMenuAction {
	private static final URI RENAME_WF_INPUT_OUTPUT_PROCESSOR_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphToolbarRenameWFInputOutputProcessor");

	private EditManager editManager;
	private SelectionManager selectionManager;

	public RenameWFInputOutputProcessorToolbarAction() {
		super(GRAPH_EDIT_TOOLBAR_SECTION, 30,
				RENAME_WF_INPUT_OUTPUT_PROCESSOR_URI);
	}

	@Override
	protected Action createAction() {
		return new RenameWFInputOutputProcessorAction(editManager,
				selectionManager);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}
}
