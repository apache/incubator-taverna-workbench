package org.apache.taverna.ui.menu.items.controllink;
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

import java.awt.Component;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.design.actions.RemoveConditionAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.core.ControlLink;
import org.apache.taverna.scufl2.api.core.Workflow;

public class RemoveConditionMenuAction extends AbstractContextualMenuAction {

	private EditManager editManager;
	private SelectionManager selectionManager;

	public RemoveConditionMenuAction() {
		super(ConditionSection.conditionSection, 10);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof ControlLink
				&& getContextualSelection().getParent() instanceof Workflow;
	}

	@Override
	protected Action createAction() {
		Workflow dataflow = (Workflow) getContextualSelection().getParent();
		ControlLink controlLink = (ControlLink) getContextualSelection()
				.getSelection();
		Component component = getContextualSelection().getRelativeToComponent();
		return new RemoveConditionAction(dataflow, controlLink, component, editManager, selectionManager);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
