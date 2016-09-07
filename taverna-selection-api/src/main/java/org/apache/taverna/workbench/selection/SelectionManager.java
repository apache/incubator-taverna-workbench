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

package org.apache.taverna.workbench.selection;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.profiles.Profile;

/**
 * Manages workflowBundles, workflows, profiles and perspectives selected on the
 * Workbench.
 * 
 * @author David Withers
 */
public interface SelectionManager extends Observable<SelectionManagerEvent> {
	/**
	 * Returns the <code>DataflowSelectionModel</code> for the WorkflowBundle.
	 * 
	 * @param workflowBundle
	 *            the WorkflowBundle to return the current selection model for
	 * @return the <code>DataflowSelectionModel</code> for the WorkflowBundle
	 */
	DataflowSelectionModel getDataflowSelectionModel(
			WorkflowBundle workflowBundle);

	/**
	 * Returns the currently selected WorkflowBundle.
	 * 
	 * @return the currently selected WorkflowBundle
	 */
	WorkflowBundle getSelectedWorkflowBundle();

	/**
	 * Sets the currently selected WorkflowBundle.
	 * 
	 * @param workflowBundle
	 *            the WorkflowBundle to set as currently selected
	 */
	void setSelectedWorkflowBundle(WorkflowBundle workflowBundle);

	/**
	 * Returns the currently selected Workflow.
	 * 
	 * @return the currently selected Workflow
	 */
	Workflow getSelectedWorkflow();

	/**
	 * Sets the currently selected Workflow.
	 * 
	 * @param workflow
	 *            the Workflow to set as currently selected
	 */
	void setSelectedWorkflow(Workflow workflow);

	/**
	 * Returns the currently selected Profile.
	 * 
	 * @return the currently selected Profile
	 */
	Profile getSelectedProfile();

	/**
	 * Sets the currently selected Profile.
	 * 
	 * @param profile
	 *            the Profile to set as currently selected
	 */
	void setSelectedProfile(Profile profile);

	/**
	 * Returns the currently selected workflow run.
	 * 
	 * @return the currently selected workflow run. If there are no workflow
	 *         runs <code>null</code> is returned.
	 */
	String getSelectedWorkflowRun();

	/**
	 * Sets the currently selected workflow run.
	 * 
	 * @param workflowRun
	 *            the workflow run to set as currently selected. May be
	 *            <code>null</code> if there are no workflow runs .
	 */
	void setSelectedWorkflowRun(String workflowRun);

	/**
	 * Returns the <code>DataflowSelectionModel</code> for the workflow run.
	 * 
	 * @param workflowRun
	 *            the workflow run to return the current selection model for
	 * @return the <code>DataflowSelectionModel</code> for the workflow run
	 */
	DataflowSelectionModel getWorkflowRunSelectionModel(String workflowRun);

	/**
	 * Returns the currently selected Perspective.
	 * 
	 * @return the currently selected Perspective
	 */
	PerspectiveSPI getSelectedPerspective();

	/**
	 * Sets the currently selected Perspective.
	 * 
	 * @param perspective
	 *            the Perspective to set as currently selected
	 */
	void setSelectedPerspective(PerspectiveSPI perspective);
}