package org.apache.taverna.ui.menu.items.ports;
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

import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.items.activityport.AbstractConnectPortMenuActions;

public class ConnectDataflowOutputPortMenuActions extends
		AbstractConnectPortMenuActions implements ContextualMenuComponent {

	public ConnectDataflowOutputPortMenuActions() {
		super(WorkflowOutputPortSection.outputPort, 20);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof OutputWorkflowPort
				&& getContextualSelection().getParent() instanceof Workflow;
	}

}
