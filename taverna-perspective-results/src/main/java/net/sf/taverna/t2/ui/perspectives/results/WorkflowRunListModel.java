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
package net.sf.taverna.t2.ui.perspectives.results;

import javax.swing.AbstractListModel;

import org.apache.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
public class WorkflowRunListModel extends AbstractListModel<String> {
	private static final long serialVersionUID = 6899849120823569185L;

	private final RunService runService;

	public WorkflowRunListModel(RunService runService) {
		this.runService = runService;
	}

	@Override
	public int getSize() {
		return runService.getRuns().size();
	}

	@Override
	public String getElementAt(int index) {
		return runService.getRuns().get(index);
	}

	/**
	 * @param runID
	 */
	public void runAdded(String runID) {
		int index = runService.getRuns().indexOf(runID);
		if (index >= 0)
			fireIntervalAdded(this, index, index);
	}

	/**
	 * @param runID
	 */
	public void runRemoved(String runID) {
		int index = runService.getRuns().indexOf(runID);
		if (index >= 0)
			fireIntervalRemoved(this, index, index);
	}
}
