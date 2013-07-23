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

import java.util.List;

import net.sf.taverna.t2.workbench.edits.CompoundEdit;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.configurations.Configuration;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputActivityPort;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;
import uk.org.taverna.scufl2.api.profiles.ProcessorInputPortBinding;
import uk.org.taverna.scufl2.api.profiles.Profile;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Removes an input port from a workflow.
 *
 * @author David Withers
 */
public class RemoveWorkflowInputPortEdit extends AbstractEdit<Workflow> {

	private final InputWorkflowPort port;

	private CompoundEdit nestedPortEdit = new CompoundEdit();

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public RemoveWorkflowInputPortEdit(Workflow workflow, InputWorkflowPort port) {
		super(workflow);
		this.port = port;
		WorkflowBundle workflowBundle = workflow.getParent();
		if (workflowBundle != null) {
			List<Edit<?>> edits = nestedPortEdit.getChildEdits();
			for (Profile profile : workflowBundle.getProfiles()) {
				for (Activity activity : profile.getActivities()) {
					if (activity.getType().equals(Scufl2Tools.NESTED_WORKFLOW)) {
						for (Configuration c : scufl2Tools.configurationsFor(activity, profile)) {
							JsonNode nested = c.getJson().get("nestedWorkflow");
							Workflow nestedWorkflow = workflowBundle.getWorkflows().getByName(nested.asText());
							if (nestedWorkflow == workflow) {
								InputActivityPort activityPort = activity.getInputPorts().getByName(port.getName());
								edits.add(new RemoveChildEdit<Activity>(activity, activityPort));
								for (ProcessorBinding binding : scufl2Tools.processorBindingsToActivity(activity)) {
									Processor processor = binding.getBoundProcessor();
									for (ProcessorInputPortBinding portBinding : binding.getInputPortBindings()) {
										if (portBinding.getBoundActivityPort() == activityPort) {
											InputProcessorPort processorPort = portBinding.getBoundProcessorPort();
											edits.add(new RemoveProcessorInputPortEdit(processor, processorPort));
											edits.add(new RemoveChildEdit<ProcessorBinding>(binding, portBinding));
										}
									}
								}
							}
						}
					}
				}
			}
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
