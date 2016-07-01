package org.apache.taverna.workbench.iterationstrategy.editor;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyNode;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyParent;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;

/**
 * An experimental view of of a IterationStrategyNode as a TreeNode.
 * <p>
 * This can be used with a {@link DefaultTreeModel}
 * as an alternative to {@link IterationStrategyTreeModel}. 
 *
 */
public class IterationStrategyTreeNode implements TreeNode {

	private final IterationStrategyNode node;

	public IterationStrategyTreeNode(IterationStrategyNode node) {
		this.node = node;
	}

	@Override
	public IterationStrategyTreeNode getChildAt(int childIndex) {
		if (node instanceof IterationStrategyTopNode) {			
			IterationStrategyNode child = ((IterationStrategyTopNode) node).get(childIndex);
			return new IterationStrategyTreeNode(child);
		}
		throw new IndexOutOfBoundsException("This node has no children: " + node);
	}

	@Override
	public int getChildCount() {
		if (node instanceof IterationStrategyTopNode) {
			return ((IterationStrategyTopNode) node).size();
		}
		return 0;
	}

	@Override
	public TreeNode getParent() {
		IterationStrategyParent parent = node.getParent();
		if (parent == null || ! (parent instanceof IterationStrategyTopNode)) {
			// It might be the IterationStrategyStack, 
			// which we don't show in the tree
			return null;
		}
		return new IterationStrategyTreeNode((IterationStrategyTopNode)parent);
	}

	@Override
	public int getIndex(TreeNode child) {
		if (node instanceof IterationStrategyTopNode) {
			IterationStrategyNode childNode = ((IterationStrategyTreeNode)child).node;
			return ((IterationStrategyTopNode) node).indexOf(childNode);
		}
		return -1;
	}

	@Override
	public boolean getAllowsChildren() {
		return node instanceof IterationStrategyTopNode;
	}

	@Override
	public boolean isLeaf() {	
		return ! getAllowsChildren() || ((IterationStrategyTopNode) node).isEmpty();
	}

	@Override
	public Enumeration<IterationStrategyTreeNode> children() {
		// Yes.. going old-skool with Enumeration and Vector!
		
		Vector<IterationStrategyTreeNode> children = new Vector<>();
		if (node instanceof IterationStrategyTopNode) {			
			for (IterationStrategyNode child : (IterationStrategyTopNode)node) {
				children.add(new IterationStrategyTreeNode(child));
			}
		} 
		return children.elements();
	}

	public IterationStrategyNode getIterationStrategyNode() {
		return node;
	}

	
	
}
