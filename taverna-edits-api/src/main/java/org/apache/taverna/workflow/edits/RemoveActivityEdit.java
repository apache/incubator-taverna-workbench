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

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * Remove an Activity from a Processor.
 *
 * @author alanrw
 */
public class RemoveActivityEdit extends AbstractEdit<Processor> {
	private Activity activityToRemove;
	private ProcessorBinding removedProcessorBinding;

	public RemoveActivityEdit(Processor processor, Activity activity) {
		super(processor);
		this.activityToRemove = activity;
	}

	@Override
	protected void doEditAction(Processor processor) {
		for (ProcessorBinding binding : scufl2Tools
				.processorBindingsToActivity(activityToRemove))
			if (binding.getBoundProcessor().equals(processor)) {
				removedProcessorBinding = binding;
				removedProcessorBinding.setParent(null);
			}
	}

	@Override
	protected void undoEditAction(Processor processor) {
		removedProcessorBinding.setParent(activityToRemove.getParent());
	}
}
