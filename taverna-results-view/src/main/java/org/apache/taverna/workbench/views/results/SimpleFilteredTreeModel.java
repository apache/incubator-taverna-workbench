package org.apache.taverna.workbench.views.results;
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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

@SuppressWarnings("serial")
public abstract class SimpleFilteredTreeModel extends DefaultTreeModel {
	public DefaultTreeModel delegate;

	public SimpleFilteredTreeModel(DefaultTreeModel delegate) {
		super((DefaultMutableTreeNode) delegate.getRoot());
		this.delegate = delegate;
	}

	@Override
	public Object getChild(Object parent, int index) {
		int count = -1;
		for (int i = 0; i < delegate.getChildCount(parent); i++) {
			final Object child = delegate.getChild(parent, i);
			if (isShown(child))
				if (++count == index)
					return child;
		}
		return null;
	}

	protected int getFilteredIndexOfChild(DefaultMutableTreeNode parent, Object child) {
		int count = -1;
		for (int i = 0; i < delegate.getChildCount(parent); i++) {
			final Object c = delegate.getChild(parent, i);
			if (isShown(c)) {
				count++;
				if (c.equals(child))
					return count;
			}
			if (c.equals(child))
				return -1;
		}
		return -1;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return delegate.getIndexOfChild(parent, child);
	}

	@Override
	public int getChildCount(Object parent) {
		int count = 0;
		for (int i = 0; i < delegate.getChildCount(parent); i++) {
			final Object child = delegate.getChild(parent, i);
			if (isShown(child))
				count++;
		}
		return count;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == null)
			return true;
		if (delegate == null)
			return true;
		return delegate.isLeaf(node);
	}

	public abstract boolean isShown(Object o);
}
