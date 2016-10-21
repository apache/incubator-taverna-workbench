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
package org.apache.taverna.workbench.ui.servicepanel.tree;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

public final class FilterTreeModel extends DefaultTreeModel {
	private static final long serialVersionUID = -8931308369832839862L;
	private static final Logger logger = Logger
			.getLogger(FilterTreeModel.class);
	
	Filter currentFilter;

	public FilterTreeModel(FilterTreeNode node) {
		this(node, null);
	}
	
	public FilterTreeModel(FilterTreeNode node, Filter filter) {
		super(node);
		currentFilter = filter;
		node.setFilter(filter);
	}

	public void setFilter(Filter filter) {
		if (root != null) {
			currentFilter = filter;
			((FilterTreeNode) root).setFilter(filter);
			Object[] path = { root };
			fireTreeStructureChanged(this, path, null, null);
		}
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent instanceof FilterTreeNode)
			return (((FilterTreeNode) parent).getChildCount());
		return 0;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent instanceof FilterTreeNode)
			return (((FilterTreeNode) parent).getChildAt(index));
		return null;
	}

	/**
	 * @return the currentFilter
	 */
	public Filter getCurrentFilter() {
		return currentFilter;
	}

	public TreePath getTreePathForObjectPath(List<Object> path) {
		List<FilterTreeNode> resultList = new ArrayList<>();
		FilterTreeNode current = (FilterTreeNode) root;
		resultList.add(current);
		for (int i = 1; (i < path.size()) && (current != null); i++) {
			logger.debug("Looking in " + current.getUserObject() + " for " + path.get(i));
			current = current.getChildForObject(path.get(i));
			if (current != null)
				resultList.add(current);
		}
		if (current != null)
			return new TreePath(resultList.toArray());
		return null;
	}
}
