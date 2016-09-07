/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.views.results.workflow;

import static org.apache.taverna.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_LIST;
import static org.apache.taverna.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_REFERENCE;
import static org.apache.taverna.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_TOP;
import static org.apache.taverna.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState.RESULT_WAITING;

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
