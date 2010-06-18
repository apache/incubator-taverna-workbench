package net.sf.taverna.t2.workbench.views.results.processor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;

public class IterationTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = -7522904828725470216L;

	private List<Integer> iteration;
	private boolean hasErrors = false;

	private static Logger logger = Logger.getLogger(IterationTreeNode.class);
	
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
	
	public List<Integer> getParentIteration() {
		TreeNode parentNode = getParent();
		if (parentNode instanceof IterationTreeNode) {
			IterationTreeNode iterationTreeNode = (IterationTreeNode) parentNode;
			return iterationTreeNode.getIteration();
		}
		return null;		
	}
	
	public boolean hasErrors() {
		if (hasErrors) {
			return true;
		}
		for (int i=0; i<getChildCount(); i++) {
			TreeNode child = getChildAt(i);
			if (! (child instanceof IterationTreeNode)) {
				logger.error("Unexpected child: " + child);
				continue;
			}
			IterationTreeNode iterationTreeNode = (IterationTreeNode) child;
			if (iterationTreeNode.hasErrors()) {
				hasErrors = true;
				return true;
			}			
		}
		return false;
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
}
