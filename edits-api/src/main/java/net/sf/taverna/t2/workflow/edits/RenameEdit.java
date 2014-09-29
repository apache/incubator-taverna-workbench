/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workflow.edits;

import static uk.org.taverna.scufl2.api.common.Scufl2Tools.NESTED_WORKFLOW;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Named;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.ActivityPort;
import uk.org.taverna.scufl2.api.port.InputPort;
import uk.org.taverna.scufl2.api.port.ProcessorPort;
import uk.org.taverna.scufl2.api.port.WorkflowPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorOutputPortBinding;
import uk.org.taverna.scufl2.api.profiles.Profile;

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
