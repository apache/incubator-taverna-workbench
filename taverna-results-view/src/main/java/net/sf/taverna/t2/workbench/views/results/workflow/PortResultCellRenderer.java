package net.sf.taverna.t2.workbench.views.results.workflow;

import static java.awt.Color.RED;

import java.awt.Component;
import java.nio.file.Path;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import org.apache.taverna.databundle.DataBundles;

/**
 * @author alanrw
 */
@SuppressWarnings("serial")
public class PortResultCellRenderer extends DefaultTreeCellRenderer {
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			renderPortResult(result, node, (Path) node.getUserObject());
		}
		return result;
	}

	private void renderPortResult(Component result,
			DefaultMutableTreeNode node, Path path) {
		String text = "";
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		if (path == null)
			text = "Waiting for data";
		else if (DataBundles.isList(path))
			text = describeList(node, parent);
		else {
			int index = 1;
			if (parent != null)
				index += parent.getIndex(node);
			text = "Value " + index;
		}
		((JLabel) result).setText(text);
		if (containsError(node))
			result.setForeground(RED);
	}

	private String describeList(DefaultMutableTreeNode node, TreeNode parent) {
		if (node.getChildCount() == 0)
			return "Empty list";
		StringBuilder builder = new StringBuilder("List");
		if (parent != null)
			builder.append(" ").append(parent.getIndex(node) + 1);
		int valueCount = node.getLeafCount();
		builder.append(" with ").append(valueCount).append(" value");
		if (valueCount != 1)
			builder.append("s");
		int sublistCount = getSublistCount(node);
		if (sublistCount > 0) {
			builder.append(" in ").append(sublistCount).append(" sublist");
			if (sublistCount != 1)
				builder.append("s");
		}
		return builder.toString();
	}

	public int getSublistCount(TreeNode node) {
		int result = 0;
		Enumeration<?> children = node.children();
		while (children.hasMoreElements()) {
			Object nextElement = children.nextElement();
			if (nextElement instanceof TreeNode) {
				TreeNode childNode = (TreeNode) nextElement;
				if (childNode.getChildCount() != 0)
					result++;
			}
		}
		return result;
	}

	private static boolean containsError(TreeNode node) {
		boolean result = false;
		if (node instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode rtn = (DefaultMutableTreeNode) node;
			Path reference = (Path) rtn.getUserObject();
			if (reference != null && DataBundles.isError(reference))
				result = true;
		}
		int childCount = node.getChildCount();
		for (int i = 0; (i < childCount) && !result; i++)
			result |= containsError(node.getChildAt(i));
		return result;
	}
}
