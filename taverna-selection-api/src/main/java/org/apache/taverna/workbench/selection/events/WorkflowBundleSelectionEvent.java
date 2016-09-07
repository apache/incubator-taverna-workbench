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

import org.apache.taverna.scufl2.api.container.WorkflowBundle;

/**
 * {@link SelectionManagerEvent} for changes to the selected
 * {@link WorkflowBundle}.
 * 
 * @author David Withers
 */
public class WorkflowBundleSelectionEvent implements SelectionManagerEvent {
	private WorkflowBundle previouslySelectedWorkflowBundle;
	private WorkflowBundle selectedWorkflowBundle;

	public WorkflowBundleSelectionEvent(
			WorkflowBundle previouslySelectedWorkflowBundle,
			WorkflowBundle selectedWorkflowBundle) {
		this.previouslySelectedWorkflowBundle = previouslySelectedWorkflowBundle;
		this.selectedWorkflowBundle = selectedWorkflowBundle;
	}

	/**
	 * Returns the previously selected WorkflowBundle.
	 * 
	 * @return the previously selected WorkflowBundle
	 */
	public WorkflowBundle getPreviouslySelectedWorkflowBundle() {
		return previouslySelectedWorkflowBundle;
	}

	/**
	 * Returns the currently selected WorkflowBundle.
	 * 
	 * @return the currently selected WorkflowBundle
	 */
	public WorkflowBundle getSelectedWorkflowBundle() {
		return selectedWorkflowBundle;
	}
}
