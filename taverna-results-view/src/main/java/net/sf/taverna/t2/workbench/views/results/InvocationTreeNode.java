package net.sf.taverna.t2.workbench.views.results;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.taverna.platform.report.Invocation;

@SuppressWarnings("serial")
public class InvocationTreeNode extends DefaultMutableTreeNode {
	public static enum ErrorState {
		NO_ERRORS,
		INPUT_ERRORS,
		OUTPUT_ERRORS;
	}

	private ErrorState errorState = ErrorState.NO_ERRORS;
	private final Invocation invocation;

	public InvocationTreeNode(Invocation invocation) {
		super(invocation);
		this.invocation = invocation;
	}

	public InvocationTreeNode getParentInvocationTreeNode() {
		TreeNode parentNode = getParent();
		if (parentNode instanceof InvocationTreeNode)
			return (InvocationTreeNode) parentNode;
		return null;
	}

	public Invocation getInvocation() {
		return invocation;
	}

	public String getIndex() {
		StringBuilder sb = new StringBuilder();
		InvocationTreeNode parentNode = getParentInvocationTreeNode();
		if (parentNode != null) {
			String index = parentNode.getIndex();
			if (!index.isEmpty()) {
				sb.append(index);
				sb.append(".");
			}
		}
		int[] index = invocation.getIndex();
		if (index.length == 0)
			sb.append(1);
		else {
			String sep = "";
			for (int i = 0; i < index.length; i++) {
				sb.append(sep).append(index[i]+1);
				sep = ".";
			}
		}
		return sb.toString();
	}

	public Invocation getParentInvocation() {
		InvocationTreeNode parentIterationTreeNode = getParentInvocationTreeNode();
		if (parentIterationTreeNode != null)
			return parentIterationTreeNode.getInvocation();
		return null;
	}

	@Override
	public String toString(){
		boolean isNested = getChildCount() > 0;
		StringBuilder sb = new StringBuilder();
		if (isNested)
			sb.append("Nested invocation ");
		else
			sb.append("Invocation ");
		sb.append(getIndex());
		return sb.toString();
	}

	public void setErrorState(ErrorState errorState) {
		this.errorState = errorState;
		notifyParentErrorState();
	}

	private void notifyParentErrorState() {
		InvocationTreeNode parentIterationTreeNode = getParentInvocationTreeNode();
		if (parentIterationTreeNode == null)
			return;
		if (parentIterationTreeNode.getErrorState().compareTo(errorState) < 0)
			parentIterationTreeNode.setErrorState(errorState);
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
