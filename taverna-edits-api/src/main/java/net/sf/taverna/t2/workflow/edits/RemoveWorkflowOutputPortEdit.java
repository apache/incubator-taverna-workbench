/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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

import java.util.List;

import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
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
