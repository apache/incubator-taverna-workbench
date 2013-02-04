/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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
package net.sf.taverna.t2.workbench.selection.events;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;

/**
 * {@link SelectionManagerEvent} for changes to the selected WorkflowBundle.
 *
 * @author David Withers
 */
public class WorkflowBundleSelectionEvent implements SelectionManagerEvent {

	private WorkflowBundle previouslySelectedWorkflowBundle;
	private WorkflowBundle selectedWorkflowBundle;

	public WorkflowBundleSelectionEvent(WorkflowBundle previouslySelectedWorkflowBundle, WorkflowBundle selectedWorkflowBundle) {
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
