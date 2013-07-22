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

import java.nio.file.Path;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class WorkflowResultTreeNode extends DefaultMutableTreeNode {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(WorkflowResultTreeNode.class);

	public enum ResultTreeNodeState {
		RESULT_TOP, RESULT_WAITING, RESULT_LIST, RESULT_REFERENCE
	};

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
		if (isState(ResultTreeNodeState.RESULT_TOP)) {
			if (getChildCount() == 0) {
				return null;
			} else {
				return ((WorkflowResultTreeNode) getChildAt(0)).getReference();
			}
		} else {
			return path;
		}
	}

	public void setReference(Path reference) {
		this.path = reference;
	}

	public String toString() {
		if (state.equals(ResultTreeNodeState.RESULT_TOP)) {
			return "Results:";
		}
		if (state.equals(ResultTreeNodeState.RESULT_LIST)) {
			if (getChildCount() == 0) {
				return "Empty list";
			}
			return "List...";
		}
		if (state.equals(ResultTreeNodeState.RESULT_WAITING)) {
			return "Waiting for data";
		}
		return path.toString();
	}

	public int getValueCount() {
		int result = 0;
		if (isState(ResultTreeNodeState.RESULT_REFERENCE)) {
			result = 1;
		} else if (isState(ResultTreeNodeState.RESULT_LIST)) {
			int childCount = this.getChildCount();
			for (int i = 0; i < childCount; i++) {
				WorkflowResultTreeNode child = (WorkflowResultTreeNode) this.getChildAt(i);
				result += child.getValueCount();
			}
		}
		return result;
	}

	public int getSublistCount() {
		int result = 0;
		if (isState(ResultTreeNodeState.RESULT_LIST)) {
			int childCount = this.getChildCount();
			for (int i = 0; i < childCount; i++) {
				WorkflowResultTreeNode child = (WorkflowResultTreeNode) this.getChildAt(i);
				if (child.isState(ResultTreeNodeState.RESULT_LIST)) {
					result++;
				}
			}
		}
		return result;
	}

}
