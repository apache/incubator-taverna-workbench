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

package org.apache.taverna.workbench.file.events;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Abstract FileManagerEvent that relates to a {@link WorkflowBundle}
 * 
 * @see AbstractDataflowEvent
 * @see ClosedDataflowEvent
 * @see OpenedDataflowEvent
 * @see SavedDataflowEvent
 * @see SetCurrentDataflowEvent
 * @author Stian Soiland-Reyes
 */
public abstract class AbstractDataflowEvent extends FileManagerEvent {
	private final WorkflowBundle workflowBundle;

	public AbstractDataflowEvent(WorkflowBundle workflowBundle) {
		this.workflowBundle = workflowBundle;
	}

	public WorkflowBundle getDataflow() {
		return workflowBundle;
	}
}
