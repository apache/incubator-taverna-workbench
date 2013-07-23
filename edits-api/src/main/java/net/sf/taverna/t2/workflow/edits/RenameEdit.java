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

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Named;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
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
		if (named instanceof WorkflowPort) {
			checkNestedPortNames((WorkflowPort) named, oldName, newName);
		}
	}

	@Override
	protected void undoEditAction(T named) {
		named.setName(oldName);
		if (named instanceof WorkflowPort) {
			checkNestedPortNames((WorkflowPort) named, newName, oldName);
		}
	}

	private void checkNestedPortNames(WorkflowPort workflowPort, String oldName, String newName) {
		Workflow workflow = workflowPort.getParent();
		if (workflow != null) {
			WorkflowBundle workflowBundle = workflow.getParent();
			if (workflowBundle != null) {
				for (Profile profile : workflowBundle.getProfiles()) {
					for (Activity activity : profile.getActivities()) {
						if (activity.getType().equals(Scufl2Tools.NESTED_WORKFLOW)) {
							for (Configuration c : scufl2Tools.configurationsFor(activity, profile)) {
								JsonNode nested = c.getJson().get("nestedWorkflow");
								Workflow nestedWorkflow = workflowBundle.getWorkflows().getByName(
										nested.asText());
								if (nestedWorkflow == workflow) {
									ActivityPort activityPort;
									if (workflowPort instanceof InputPort) {
										activityPort = activity.getInputPorts().getByName(oldName);
									} else {
										activityPort = activity.getOutputPorts().getByName(oldName);
									}
									activityPort.setName(newName);
									for (ProcessorBinding binding : scufl2Tools.processorBindingsToActivity(activity)) {
										if (activityPort instanceof InputPort) {
											for (ProcessorInputPortBinding portBinding : binding.getInputPortBindings()) {
												if (portBinding.getBoundActivityPort() == activityPort) {
													ProcessorPort processorPort = portBinding.getBoundProcessorPort();
													if (processorPort.getName().equals(oldName)) {
														processorPort.setName(newName);
													}
												}
											}
										} else {
											for (ProcessorOutputPortBinding portBinding : binding.getOutputPortBindings()) {
												if (portBinding.getBoundActivityPort() == activityPort) {
													ProcessorPort processorPort = portBinding.getBoundProcessorPort();
													if (processorPort.getName().equals(oldName)) {
														processorPort.setName(newName);
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
			}
		}
	}

}
