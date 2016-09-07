package org.apache.taverna.workbench.iterationstrategy.editor;
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
