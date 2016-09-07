package org.apache.taverna.workbench.views.results.processor;
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
import static org.apache.taverna.workbench.views.results.processor.ProcessorResultTreeNode.ProcessorResultTreeNodeState.RESULT_LIST;
import static org.apache.taverna.workbench.views.results.processor.ProcessorResultTreeNode.ProcessorResultTreeNodeState.RESULT_REFERENCE;
import static org.apache.taverna.workbench.views.results.processor.ProcessorResultTreeNode.ProcessorResultTreeNodeState.RESULT_TOP;
import static org.apache.taverna.databundle.DataBundles.isError;

import java.awt.Component;
import java.nio.file.Path;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

/**
 * @author alanrw
 */
@SuppressWarnings("serial")
public class ProcessorResultCellRenderer extends DefaultTreeCellRenderer {
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value,
				selected, expanded, leaf, row, hasFocus);
		if (value instanceof ProcessorResultTreeNode) {
			ProcessorResultTreeNode value2 = (ProcessorResultTreeNode) value;
			String text = "";
			ProcessorResultTreeNode parent = (ProcessorResultTreeNode) value2
					.getParent();
			if (value2.getState() == RESULT_LIST) {
				if (value2.getChildCount() == 0)
					text = "Empty list";
				else {
					text = "List";
					if (parent.getState() != RESULT_TOP)
						text += " " + (parent.getIndex(value2) + 1);
					text += " with " + value2.getValueCount() + " value";
					if (value2.getValueCount() != 1)
						text += "s";
					if (value2.getSublistCount() > 0)
						text += " in " + value2.getSublistCount() + " sublists";
				}
			} else if (value2.getState() == RESULT_REFERENCE)
				text = "Value " + (parent.getIndex(value2) + 1);

			((JLabel) result).setText(text);
			if (containsError(value2))
				result.setForeground(RED);
		}
		return result;
	}

	private static boolean containsError(TreeNode node) {
		boolean result = false;
		if (node instanceof ProcessorResultTreeNode) {
			ProcessorResultTreeNode rtn = (ProcessorResultTreeNode) node;
			Path reference = rtn.getReference();
			if (reference != null && isError(reference))
				result = true;
		}
		int childCount = node.getChildCount();
		for (int i = 0; (i < childCount) && !result; i++)
			result = containsError(node.getChildAt(i));
		return result;
	}
}
