/**
 *
 */
package org.apache.taverna.activities.dataflow.actions;
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

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.taverna.activities.dataflow.servicedescriptions.DataflowTemplateService;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.Workflow;

@SuppressWarnings("serial")
public class EditNestedDataflowAction extends AbstractAction {

	private final Activity activity;
	private final SelectionManager selectionManager;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public EditNestedDataflowAction(Activity activity, SelectionManager selectionManager) {
		super("Edit nested workflow");
		this.activity = activity;
		this.selectionManager = selectionManager;
	}

	public void actionPerformed(ActionEvent e) {
		if (activity.getType().equals(DataflowTemplateService.ACTIVITY_TYPE)) {
			for (Configuration configuration : scufl2Tools.configurationsFor(activity, selectionManager.getSelectedProfile())) {
				JsonNode nested = configuration.getJson().get("nestedWorkflow");
				Workflow nestedWorkflow = selectionManager.getSelectedWorkflowBundle().getWorkflows().getByName(nested.asText());
				if (nestedWorkflow != null) {
					selectionManager.setSelectedWorkflow(nestedWorkflow);
					break;
				}
			}
		}
	}

}