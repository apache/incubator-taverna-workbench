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
