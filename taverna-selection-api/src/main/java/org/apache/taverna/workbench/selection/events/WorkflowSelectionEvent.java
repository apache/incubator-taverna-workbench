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

package org.apache.taverna.workbench.selection.events;

import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * {@link SelectionManagerEvent} for changes to the selected {@link Workflow}.
 * 
 * @author David Withers
 */
public class WorkflowSelectionEvent implements SelectionManagerEvent {
	private Workflow previouslySelectedWorkflow;
	private Workflow selectedWorkflow;

	public WorkflowSelectionEvent(Workflow previouslySelectedWorkflow,
			Workflow selectedWorkflow) {
		this.previouslySelectedWorkflow = previouslySelectedWorkflow;
		this.selectedWorkflow = selectedWorkflow;
	}

	/**
	 * Returns the previously selected Workflow.
	 * 
	 * @return the previously selected Workflow
	 */
	public Workflow getPreviouslySelectedWorkflow() {
		return previouslySelectedWorkflow;
	}

	/**
	 * Returns the currently selected Workflow.
	 * 
	 * @return the currently selected Workflow
	 */
	public Workflow getSelectedWorkflow() {
		return selectedWorkflow;
	}
}
