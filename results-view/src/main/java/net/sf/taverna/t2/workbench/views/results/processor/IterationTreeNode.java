package net.sf.taverna.t2.workbench.views.results.processor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class IterationTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -7522904828725470216L;

	public static enum ErrorState {
		NO_ERRORS,
		INPUT_ERRORS,
		OUTPUT_ERRORS;
	}

	private ErrorState errorState = ErrorState.NO_ERRORS;

	private List<Integer> iteration;

	public IterationTreeNode() {
		this.setIteration(new ArrayList<Integer>());
	}

	public IterationTreeNode(List<Integer> iteration) {
		this.setIteration(iteration);
	}

	public void setIteration(List<Integer> iteration) {
		this.iteration = iteration;
	}

	public List<Integer> getIteration() {
		return iteration;
	}

	public IterationTreeNode getParentIterationTreeNode() {
		TreeNode parentNode = getParent();
		if (parentNode instanceof IterationTreeNode) {
			return (IterationTreeNode) parentNode;
		}
		return null;
	}

	public List<Integer> getParentIteration() {
		IterationTreeNode parentIterationTreeNode = getParentIterationTreeNode();
		if (parentIterationTreeNode != null) {
			return parentIterationTreeNode.getIteration();
		}
		return null;
	}


	public String toString(){
		boolean isNested = getChildCount() > 0;
		StringBuilder sb = new StringBuilder();
		if (! getIteration().isEmpty() || isNested) {
			// Iteration 3.1.3
			if (isNested) {
				sb.append("Nested iteration ");
			} else {
				if (getUserObject() == null) {
					sb.append("Waiting for iteration ");
				} else {
					sb.append("Iteration ");
				}
			}
			for (Integer index : getIteration()) {
				sb.append(index+1);
				sb.append(".");
			}
			if (! getIteration().isEmpty()) {
				// Remove last .
				sb.delete(sb.length()-1, sb.length());
			}
		} else {
			sb.append("Invocation");
		}

		return sb.toString();
	}

	public void setErrorState(ErrorState errorState) {
		this.errorState = errorState;
		notifyParentErrorState();
	}

	private void notifyParentErrorState() {
		IterationTreeNode parentIterationTreeNode = getParentIterationTreeNode();
		if (parentIterationTreeNode == null) {
			return;
		}
		if (parentIterationTreeNode.getErrorState().compareTo(errorState) < 0) {
			parentIterationTreeNode.setErrorState(errorState);
		}
	}

	@Override
	public void setParent(MutableTreeNode newParent) {
		super.setParent(newParent);
		notifyParentErrorState();
	}

	public ErrorState getErrorState() {
		return errorState;
	}
}
