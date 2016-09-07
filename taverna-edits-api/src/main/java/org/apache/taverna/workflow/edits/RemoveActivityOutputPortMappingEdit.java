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

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.port.OutputActivityPort;
import org.apache.taverna.scufl2.api.profiles.ProcessorBinding;
import org.apache.taverna.scufl2.api.profiles.ProcessorOutputPortBinding;

public class RemoveActivityOutputPortMappingEdit extends AbstractEdit<Activity> {
	private final OutputActivityPort outputActivityPort;
	private ProcessorOutputPortBinding removedPortBinding;
	private ProcessorBinding processorBinding;

	public RemoveActivityOutputPortMappingEdit(Activity activity,
			OutputActivityPort outputActivityPort) {
		super(activity);
		this.outputActivityPort = outputActivityPort;
	}

	@Override
	protected void doEditAction(Activity activity) {
		removedPortBinding = scufl2Tools.processorPortBindingForPort(
				outputActivityPort, activity.getParent());
		processorBinding = removedPortBinding.getParent();
		removedPortBinding.setParent(null);
	}

	@Override
	protected void undoEditAction(Activity activity) {
		removedPortBinding.setParent(processorBinding);
	}
}
