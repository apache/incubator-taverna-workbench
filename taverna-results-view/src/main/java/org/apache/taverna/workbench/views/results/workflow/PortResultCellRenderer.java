package org.apache.taverna.workbench.views.results.workflow;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
