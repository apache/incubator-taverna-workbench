/**
 * 
 */
package net.sf.taverna.t2.workbench.views.results.workflow;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public class PortResultCellRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof WorkflowResultTreeNode) {
			WorkflowResultTreeNode value2 = (WorkflowResultTreeNode) value;
			String text = "";
			WorkflowResultTreeNode parent = (WorkflowResultTreeNode) value2.getParent();
			if (value2.getState().equals(ResultTreeNodeState.RESULT_LIST)) {
				if (value2.getChildCount() == 0) {
					text = "Empty list";
				} else {
				text = "List";
				if (!parent.getState().equals(ResultTreeNodeState.RESULT_TOP)) {
					text += " " + (parent.getIndex(value2) + 1);
				}
				text += " with " + value2.getValueCount() + " value";
				if (value2.getValueCount() != 1) {
					text += "s";
				}
				if (value2.getSublistCount() > 0) {
					text += " in " + value2.getSublistCount() + " sublists";
				}
				}
			} else if (value2.getState().equals(ResultTreeNodeState.RESULT_REFERENCE)) {
				text = "Value " + (parent.getIndex(value2) + 1);
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
		if (node instanceof WorkflowResultTreeNode) {
			WorkflowResultTreeNode rtn = (WorkflowResultTreeNode) node;
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
