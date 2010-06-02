/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.views.results.processor;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.views.results.processor.ProcessorResultTreeNode;

/**
 * @author alanrw
 *
 */
@SuppressWarnings("serial")
public class ProcessorResultCellRenderer extends DefaultTreeCellRenderer {
	public Component getTreeCellRendererComponent(JTree tree,
			Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		if (value instanceof ProcessorResultTreeNode) {
			ProcessorResultTreeNode value2 = (ProcessorResultTreeNode) value;
			String text = "";
			ProcessorResultTreeNode parent = (ProcessorResultTreeNode) value2.getParent();
			if (value2.getState().equals(ProcessorResultTreeNode.ProcessorResultTreeNodeState.RESULT_LIST)) {
				if (value2.getChildCount() == 0) {
					text = "Empty list";
				} else {
				text = "List";
				if (!parent.getState().equals(ProcessorResultTreeNode.ProcessorResultTreeNodeState.RESULT_TOP)) {
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
			} else if (value2.getState().equals(ProcessorResultTreeNode.ProcessorResultTreeNodeState.RESULT_REFERENCE)) {
				text = "Value " + (parent.getIndex(value2) + 1);
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
		if (node instanceof ProcessorResultTreeNode) {
			ProcessorResultTreeNode rtn = (ProcessorResultTreeNode) node;
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
