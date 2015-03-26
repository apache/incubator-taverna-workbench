/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package org.apache.taverna.ui.perspectives.results;

import java.io.File;

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
	private final File runStore;

	public RunSelectorComponent(RunService runSevice,
			SelectionManager selectionManager, File runStore) {
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
