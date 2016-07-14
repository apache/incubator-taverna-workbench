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

package org.apache.taverna.ui.perspectives.results;

import java.io.File;
import java.nio.file.Path;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.lang.ui.tabselector.Tab;
import org.apache.taverna.lang.ui.tabselector.TabSelectorComponent;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.selection.events.WorkflowRunSelectionEvent;
import org.apache.taverna.platform.run.api.RunService;

/**
 * Component for managing selection of workflow runs.
 * 
 * @author David Withers
 */
public class RunSelectorComponent extends TabSelectorComponent<String> {
	private static final long serialVersionUID = 3679972772159328891L;

	private final RunService runService;
	private final SelectionManager selectionManager;
	private final Path runStore;

	public RunSelectorComponent(RunService runSevice,
			SelectionManager selectionManager, Path runStore) {
		this.runService = runSevice;
		this.selectionManager = selectionManager;
		this.runStore = runStore;
		selectionManager.addObserver(new SelectionManagerObserver());
	}

	private class SelectionManagerObserver extends
			SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof WorkflowRunSelectionEvent) {
				WorkflowRunSelectionEvent event = (WorkflowRunSelectionEvent) message;
				String workflowRun = event.getSelectedWorkflowRun();
				if (workflowRun != null)
					selectObject(workflowRun);
			}
		}
	}

	@Override
	protected Tab<String> createTab(String runID) {
		return new RunTab(runID, selectionManager, runService, runStore);
	}
}
