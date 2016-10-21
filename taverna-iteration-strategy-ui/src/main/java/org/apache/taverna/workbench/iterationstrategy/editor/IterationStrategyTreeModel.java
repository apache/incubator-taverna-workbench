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
package org.apache.taverna.workbench.iterationstrategy.editor;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyNode;
import org.apache.taverna.scufl2.api.iterationstrategy.IterationStrategyTopNode;

/**
 * An experimental TreeModel view of a SCUFL2 IterationStrategyTopNode tree.
 * <p>
 * TODO: This class does not yet support the methods 
 * {@link #addTreeModelListener(TreeModelListener)},
 * {@link #removeTreeModelListener(TreeModelListener)}
 * or {@link #valueForPathChanged(TreePath, Object)}.
 *
 */
public class IterationStrategyTreeModel implements TreeModel {

	private IterationStrategyTopNode topNode;

	public IterationStrategyTreeModel(IterationStrategyTopNode topNode) {
		this.topNode = topNode;
	}
	
	@Override
	public IterationStrategyTopNode getRoot() {
		return topNode;
	}

	@Override
	public IterationStrategyNode getChild(Object parent, int index) {
		if (!(parent instanceof IterationStrategyTopNode)) {
			return null;
		}
		try { 
			return ((IterationStrategyTopNode)parent).get(index);
		} catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (!(parent instanceof IterationStrategyTopNode)) {
			return 0;
		}
		return ((IterationStrategyTopNode)parent).size();
	}

	@Override
	public boolean isLeaf(Object node) {
		if (!(node instanceof IterationStrategyTopNode)) {
			return true;
		}
		return ((IterationStrategyTopNode)node).isEmpty();
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == null || child == null) { 
			return -1;
		}
		if (! (parent instanceof IterationStrategyTopNode)) { 
			return -1;
		}
		return ((IterationStrategyTopNode)parent).indexOf(child);
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		// TODO
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// TODO

	}
	
}
