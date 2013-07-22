/**
 *
 */
package net.sf.taverna.t2.workbench.views.results.workflow;

import java.awt.Color;
import java.awt.Component;
import java.nio.file.Path;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import uk.org.taverna.databundle.DataBundles;

/**
 * @author alanrw
 */
@SuppressWarnings("serial")
public class PortResultCellRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value, selected, expanded,
				leaf, row, hasFocus);
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Path path = (Path) node.getUserObject();
			String text = "";
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			if (path == null) {
				text = "Waiting for data";
			} else if (DataBundles.isList(path)) {
				if (node.getChildCount() == 0) {
					text = "Empty list";
				} else {
					text = "List";
					if (parent != null) {
						text += " " + (parent.getIndex(node) + 1);
					}
					int valueCount = node.getLeafCount();
					text += " with " + valueCount + " value";
					if (valueCount != 1) {
						text += "s";
					}
					int sublistCount = getSublistCount(node);
					if (sublistCount > 0) {
						text += " in " + sublistCount + " sublists";
					}
				}
			} else {
				int index = 1;
				if (parent != null) {
					index += parent.getIndex(node);
				}
				text = "Value " + index;
			}
			((JLabel) result).setText(text);
			if (containsError(node)) {
				result.setForeground(Color.RED);
			}
		}
		return result;
	}

	public int getSublistCount(DefaultMutableTreeNode node) {
		int result = 0;
		Enumeration<?> children = node.children();
		while (children.hasMoreElements()) {
			Object nextElement = children.nextElement();
			if (nextElement instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) nextElement;
				if (childNode.getChildCount() != 0) {
					result++;
				}
			}
		}
		return result;
	}

	private static boolean containsError(TreeNode node) {
		boolean result = false;
		if (node instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode rtn = (DefaultMutableTreeNode) node;
			Path reference = (Path) rtn.getUserObject();
			if ((reference != null) && (DataBundles.isError(reference))) {
				result = true;
			}
		}
		int childCount = node.getChildCount();
		for (int i = 0; (i < childCount) && !result; i++) {
			result = containsError(node.getChildAt(i));
		}
		return result;
	}
}
