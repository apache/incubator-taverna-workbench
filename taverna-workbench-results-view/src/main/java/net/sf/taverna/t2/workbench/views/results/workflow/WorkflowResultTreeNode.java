/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.workbench.views.results.workflow;

import static net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_LIST;
import static net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_REFERENCE;
import static net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_TOP;
import static net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_WAITING;

import java.nio.file.Path;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class WorkflowResultTreeNode extends DefaultMutableTreeNode {
	public enum ResultTreeNodeState {
		RESULT_TOP, RESULT_WAITING, RESULT_LIST, RESULT_REFERENCE
	};

	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(WorkflowResultTreeNode.class);

	private Path path;
	private ResultTreeNodeState state;

	public WorkflowResultTreeNode(Path reference, ResultTreeNodeState state) {
		this.path = reference;
		this.state = state;
	}

	public WorkflowResultTreeNode(ResultTreeNodeState state) {
		this.path = null;
		this.state = state;
	}

	public boolean isState(ResultTreeNodeState state) {
		return this.state.equals(state);
	}

	public ResultTreeNodeState getState() {
		return state;
	}

	public void setState(ResultTreeNodeState state) {
		this.state = state;
	}

	public Path getReference() {
		if (!isState(RESULT_TOP))
			return path;
		if (getChildCount() == 0)
			return null;
		return ((WorkflowResultTreeNode) getChildAt(0)).getReference();
	}

	public void setReference(Path reference) {
		this.path = reference;
	}

	@Override
	public String toString() {
		if (state.equals(RESULT_TOP))
			return "Results:";
		if (state.equals(RESULT_LIST)) {
			if (getChildCount() == 0)
				return "Empty list";
			return "List...";
		}
		if (state.equals(RESULT_WAITING))
			return "Waiting for data";
		return path.toString();
	}

	public int getValueCount() {
		int result = 0;
		if (isState(RESULT_REFERENCE))
			result = 1;
		else if (isState(RESULT_LIST)) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				WorkflowResultTreeNode child = (WorkflowResultTreeNode) getChildAt(i);
				result += child.getValueCount();
			}
		}
		return result;
	}

	public int getSublistCount() {
		int result = 0;
		if (isState(RESULT_LIST)) {
			int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				WorkflowResultTreeNode child = (WorkflowResultTreeNode) getChildAt(i);
				if (child.isState(RESULT_LIST))
					result++;
			}
		}
		return result;
	}
}
