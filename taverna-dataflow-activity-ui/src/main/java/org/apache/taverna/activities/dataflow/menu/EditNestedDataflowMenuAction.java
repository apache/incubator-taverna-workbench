package org.apache.taverna.activities.dataflow.menu;
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

import javax.swing.Action;

import org.apache.taverna.activities.dataflow.actions.EditNestedDataflowAction;
import org.apache.taverna.activities.dataflow.servicedescriptions.DataflowTemplateService;
import org.apache.taverna.workbench.activitytools.AbstractConfigureActivityMenuAction;
import org.apache.taverna.workbench.selection.SelectionManager;

public class EditNestedDataflowMenuAction extends AbstractConfigureActivityMenuAction {

	private SelectionManager selectionManager;

	public EditNestedDataflowMenuAction() {
		super(DataflowTemplateService.ACTIVITY_TYPE);
	}

	@Override
	protected Action createAction() {
		EditNestedDataflowAction configAction = new EditNestedDataflowAction(findActivity(), selectionManager);
		return configAction;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

}
