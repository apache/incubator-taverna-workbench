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

package org.apache.taverna.workflow.edits;

import static org.apache.taverna.scufl2.api.common.Scufl2Tools.NESTED_WORKFLOW;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Named;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.ActivityPort;
import org.apache.taverna.scufl2.api.port.InputPort;
import org.apache.taverna.scufl2.api.port.ProcessorPort;
import org.apache.taverna.scufl2.api.port.WorkflowPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import org.apache.taverna.scufl2.api.profiles.ProcessorOutputPortBinding;
import org.apache.taverna.scufl2.api.profiles.Profile;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Renames a Named WorkflowBean.
 *
 * @author David Withers
 */
public class RenameEdit<T extends Named> extends AbstractEdit<T> {
	private String oldName, newName;

	public RenameEdit(T named, String newName) {
		super(named);
		this.newName = newName;
		oldName = named.getName();
	}

	@Override
	protected void doEditAction(T named) {
		named.setName(newName);
		if (named instanceof WorkflowPort)
			checkNestedPortNames((WorkflowPort) named, oldName, newName);
	}

	@Override
	protected void undoEditAction(T named) {
		named.setName(oldName);
		if (named instanceof WorkflowPort)
			checkNestedPortNames((WorkflowPort) named, newName, oldName);
	}

	private void checkNestedPortNames(WorkflowPort workflowPort, String oldName, String newName) {
		Workflow workflow = workflowPort.getParent();
		if (workflow == null)
			return;
		WorkflowBundle workflowBundle = workflow.getParent();
		if (workflowBundle == null)
			return;
		for (Profile profile : workflowBundle.getProfiles())
			for (Activity activity : profile.getActivities())
				if (activity.getType().equals(NESTED_WORKFLOW))
					for (Configuration c : scufl2Tools.configurationsFor(activity, profile))
						changeActivityPortName(workflowPort, oldName,
								newName, workflow, workflowBundle, activity, c);
	}

	private void changeActivityPortName(WorkflowPort workflowPort,
			String oldName, String newName, Workflow workflow,
			WorkflowBundle workflowBundle, Activity activity, Configuration c) {
		JsonNode nested = c.getJson().get("nestedWorkflow");
		Workflow nestedWorkflow = workflowBundle.getWorkflows().getByName(
				nested.asText());
		if (nestedWorkflow != workflow)
			return;

		ActivityPort activityPort;
		if (workflowPort instanceof InputPort) {
			activityPort = activity.getInputPorts().getByName(oldName);
			changeProcessorInputPortName(oldName, newName, activity,
					activityPort);
		} else {
			activityPort = activity.getOutputPorts().getByName(oldName);
			changeProcessorOutputPortName(oldName, newName, activity,
					activityPort);
		}
		activityPort.setName(newName);
	}

	private void changeProcessorInputPortName(String oldName, String newName,
			Activity activity, ActivityPort activityPort) {
		bindings: for (ProcessorBinding binding : scufl2Tools
				.processorBindingsToActivity(activity))
			for (ProcessorInputPortBinding portBinding : binding
					.getInputPortBindings())
				if (portBinding.getBoundActivityPort() == activityPort) {
					ProcessorPort processorPort = portBinding
							.getBoundProcessorPort();
					if (processorPort.getName().equals(oldName)) {
						processorPort.setName(newName);
						continue bindings;
					}
				}
	}

	private void changeProcessorOutputPortName(String oldName, String newName,
			Activity activity, ActivityPort activityPort) {
		bindings: for (ProcessorBinding binding : scufl2Tools
				.processorBindingsToActivity(activity))
			for (ProcessorOutputPortBinding portBinding : binding
					.getOutputPortBindings())
				if (portBinding.getBoundActivityPort() == activityPort) {
					ProcessorPort processorPort = portBinding
							.getBoundProcessorPort();
					if (processorPort.getName().equals(oldName)) {
						processorPort.setName(newName);
						continue bindings;
					}
				}
	}
}
