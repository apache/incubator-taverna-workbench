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
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.ActivityPort;
import org.apache.taverna.scufl2.api.port.DepthPort;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import org.apache.taverna.scufl2.api.profiles.Profile;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Changes the depth of a port.
 *
 * @author David Withers
 */
public class ChangeDepthEdit<T extends DepthPort> extends AbstractEdit<T> {
	private Integer newDepth, oldDepth;

	public ChangeDepthEdit(T depthPort, Integer newDepth) {
		super(depthPort);
		this.newDepth = newDepth;
		oldDepth = depthPort.getDepth();
	}

	@Override
	protected void doEditAction(T depthPort) {
		depthPort.setDepth(newDepth);
		if (depthPort instanceof InputWorkflowPort)
			checkNestedPortDepths((InputWorkflowPort) depthPort, newDepth);
	}

	@Override
	protected void undoEditAction(T depthPort) {
		depthPort.setDepth(oldDepth);
		if (depthPort instanceof InputWorkflowPort)
			checkNestedPortDepths((InputWorkflowPort) depthPort, oldDepth);
	}

	private void checkNestedPortDepths(InputWorkflowPort workflowPort,
			Integer depth) {
		Workflow workflow = workflowPort.getParent();
		if (workflow != null) {
			WorkflowBundle workflowBundle = workflow.getParent();
			if (workflowBundle != null)
				for (Profile profile : workflowBundle.getProfiles())
					for (Activity activity : profile.getActivities())
						if (activity.getType().equals(NESTED_WORKFLOW))
							for (Configuration c : scufl2Tools
									.configurationsFor(activity, profile))
								checkOneConfiguration(workflowPort, depth,
										workflow, workflowBundle, activity, c);
		}
	}

	private void checkOneConfiguration(InputWorkflowPort workflowPort,
			Integer depth, Workflow workflow, WorkflowBundle workflowBundle,
			Activity activity, Configuration c) {
		JsonNode nested = c.getJson().get("nestedWorkflow");
		Workflow nestedWorkflow = workflowBundle.getWorkflows().getByName(
				nested.asText());
		if (nestedWorkflow != workflow)
			return;

		ActivityPort activityPort = activity.getInputPorts().getByName(
				workflowPort.getName());
		activityPort.setDepth(depth);
		for (ProcessorBinding binding : scufl2Tools
				.processorBindingsToActivity(activity))
			for (ProcessorInputPortBinding portBinding : binding
					.getInputPortBindings())
				if (portBinding.getBoundActivityPort() == activityPort) {
					InputProcessorPort processorPort = portBinding
							.getBoundProcessorPort();
					processorPort.setDepth(depth);
				}
	}
}
