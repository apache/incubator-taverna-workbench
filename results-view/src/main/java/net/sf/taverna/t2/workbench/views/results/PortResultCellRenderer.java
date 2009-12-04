/**
 * 
 */
package net.sf.taverna.t2.workbench.views.results;

import java.awt.Color;
import java.awt.Component;
import java.awt.Label;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.views.results.ResultTreeNode.ResultTreeNodeState;

/**
 * @author alanrw
 *
 */
public class PortResultCellRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof ResultTreeNode) {
			ResultTreeNode value2 = (ResultTreeNode) value;
			String text = "";
			ResultTreeNode parent = (ResultTreeNode) value2.getParent();
			if (value2.getState().equals(ResultTreeNodeState.RESULT_LIST)) {
				text = "List";
				if (!parent.getState().equals(ResultTreeNodeState.RESULT_TOP)) {
					text += "" + (parent.getIndex(value2) + 1);
				}
				text += " with " + value2.getLeafCount() + " values";
				if (value2.getLeafCount() != value2.getChildCount()) {
					text += " in " + value2.getChildCount() + " sublists";
				}
			} else if (value2.getState().equals(ResultTreeNodeState.RESULT_REFERENCE)) {
				text = "Result " + (parent.getIndex(value2) + 1);
			} else if (value2.getState().equals(ResultTreeNodeState.RESULT_WAITING)) {
				text = "Waiting for data";
			}
			((JLabel) result).setText(text);
			if (containsError(value2)) {
				result.setForeground(Color.RED);
			}
		}
		return result;
	}

	private static boolean containsError (TreeNode node) {
		boolean result = false;
		if (node instanceof ResultTreeNode) {
			ResultTreeNode rtn = (ResultTreeNode) node;
			T2Reference reference = rtn.getReference();
			if ((reference != null) && (reference.containsErrors())) {
				result = true;
			}
		}
		int childCount = node.getChildCount();
		for (int i = 0; (i < childCount) && !result; i++ ) {
			result = containsError(node.getChildAt(i));
		}
		return result;
	}
}
