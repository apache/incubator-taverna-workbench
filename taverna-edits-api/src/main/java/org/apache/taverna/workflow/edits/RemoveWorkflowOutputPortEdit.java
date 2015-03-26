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

package org.apache.taverna.workflow.edits;

import static org.apache.taverna.scufl2.api.common.Scufl2Tools.NESTED_WORKFLOW;

import java.util.List;

import org.apache.taverna.workbench.edits.CompoundEdit;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.workbench.edits.EditException;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.OutputActivityPort;
import org.apache.taverna.scufl2.api.port.OutputProcessorPort;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.ProcessorOutputPortBinding;
import org.apache.taverna.scufl2.api.profiles.Profile;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Removes an output port from a workflow.
 *
 * @author David Withers
 */
public class RemoveWorkflowOutputPortEdit extends AbstractEdit<Workflow> {
	private final OutputWorkflowPort port;
	private final CompoundEdit nestedPortEdit = new CompoundEdit();

	public RemoveWorkflowOutputPortEdit(Workflow workflow,
			OutputWorkflowPort port) {
		super(workflow);
		this.port = port;
		WorkflowBundle workflowBundle = workflow.getParent();
		if (workflowBundle != null)
			for (Profile profile : workflowBundle.getProfiles())
				for (Activity activity : profile.getActivities())
					if (activity.getType().equals(NESTED_WORKFLOW))
						for (Configuration c : scufl2Tools.configurationsFor(
								activity, profile))
							defineEditsForConfiguration(workflow, port,
									workflowBundle, activity, c);
	}

	private void defineEditsForConfiguration(Workflow workflow,
			OutputWorkflowPort port, WorkflowBundle workflowBundle,
			Activity activity, Configuration c) {
		List<Edit<?>> edits = nestedPortEdit.getChildEdits();
		JsonNode nested = c.getJson().get("nestedWorkflow");
		Workflow nestedWorkflow = workflowBundle.getWorkflows().getByName(
				nested.asText());
		if (nestedWorkflow != workflow)
			return;

		OutputActivityPort activityPort = activity.getOutputPorts().getByName(
				port.getName());
		edits.add(new RemoveChildEdit<>(activity, activityPort));
		for (ProcessorBinding processorBinding : scufl2Tools
				.processorBindingsToActivity(activity))
			for (ProcessorOutputPortBinding portBinding : processorBinding
					.getOutputPortBindings())
				if (portBinding.getBoundActivityPort() == activityPort) {
					OutputProcessorPort processorPort = portBinding
							.getBoundProcessorPort();
					edits.add(new RemoveProcessorOutputPortEdit(
							processorBinding.getBoundProcessor(), processorPort));
					edits.add(new RemoveChildEdit<>(processorBinding,
							portBinding));
				}
	}

	@Override
	protected void doEditAction(Workflow workflow) throws EditException {
		port.setParent(null);
		nestedPortEdit.doEdit();
	}

	@Override
	protected void undoEditAction(Workflow workflow) {
		port.setParent(workflow);
		nestedPortEdit.undo();
	}
}
