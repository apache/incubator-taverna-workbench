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

import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.ProcessorInputPortBinding;

public class AddActivityInputPortMappingEdit extends AbstractEdit<Activity> {
	private final InputProcessorPort inputProcessorPort;
	private final InputActivityPort inputActivityPort;
	private List<ProcessorInputPortBinding> portBindings;

	public AddActivityInputPortMappingEdit(Activity activity,
			InputProcessorPort inputProcessorPort,
			InputActivityPort inputActivityPort) {
		super(activity);
		this.inputProcessorPort = inputProcessorPort;
		this.inputActivityPort = inputActivityPort;
	}

	@Override
	protected void doEditAction(Activity activity) {
		portBindings = new ArrayList<>();
		for (ProcessorBinding binding : scufl2Tools
				.processorBindingsToActivity(activity))
			portBindings.add(new ProcessorInputPortBinding(binding,
					inputProcessorPort, inputActivityPort));
	}

	@Override
	protected void undoEditAction(Activity activity) {
		for (ProcessorInputPortBinding binding : portBindings)
			binding.setParent(null);
	}
}
