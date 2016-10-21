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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

public class FilterTreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1933553584349932151L;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(FilterTreeNode.class);
	
	private Filter filter;
	private boolean passed = true;
	private List<FilterTreeNode> filteredChildren = new ArrayList<>();

	public FilterTreeNode(Object userObject) {
		super(userObject);
		userObject.toString();
	}

	public Filter getFilter() {
		return filter;
	}
	
	public void setFilter(Filter filter) {
		if ((filter == null) || !filter.isSuperseded()) {
			this.filter = filter;
			passed = false;
			filteredChildren.clear();
			if (filter == null) {
				passed = true;
				passFilterDown(null);
			} else if (filter.pass(this)) {
				passed = true;
				passFilterDown(null);
			} else {
				passFilterDown(filter);
				passed = filteredChildren.size() != 0;
			}
		}
	}

	private void passFilterDown(Filter filter) {
		int realChildCount = super.getChildCount();
		for (int i = 0; i < realChildCount; i++) {
			FilterTreeNode realChild = (FilterTreeNode) super.getChildAt(i);
			realChild.setFilter(filter);
			if (realChild.isPassed())
				filteredChildren.add(realChild);
		}
	}

	public void add(FilterTreeNode node) {
		super.add(node);
		node.setFilter(filter);
		// TODO work up
		if (node.isPassed())
			filteredChildren.add(node);
	}
	
	@Override
	public void remove(int childIndex) {
		if (filter != null)
			// as child indexes might be inconsistent..
			throw new IllegalStateException("Can't remove while the filter is active");
		super.remove(childIndex);
	}

	@Override
	public int getChildCount() {
		if (filter == null)
			return super.getChildCount();
		return filteredChildren.size();
	}

	@Override
	public FilterTreeNode getChildAt(int index) {
		if (filter == null)
			return (FilterTreeNode) super.getChildAt(index);
		return filteredChildren.get(index);
	}

	public boolean isPassed() {
		return passed;
	}
	
	public Set<FilterTreeNode> getLeaves() {
		Set<FilterTreeNode> result = new HashSet<>();
		if (super.getChildCount() == 0) {
			result.add(this);
			return result;
		}

		for (int i = 0; i < super.getChildCount(); i++) {
			FilterTreeNode child = (FilterTreeNode) super.getChildAt(i);
			result.addAll(child.getLeaves());
		}
		return result;
	}

	public FilterTreeNode getChildForObject(Object userObject) {
		FilterTreeNode result = null;
		for (int i=0; (i < super.getChildCount()) && (result == null); i++) {
			FilterTreeNode child = (FilterTreeNode) super.getChildAt(i);
			Object nodeObject = child.getUserObject();
//			logger.info("nodeObject is a " + nodeObject.getClass() + " - " +
//					"userObject is a " + userObject.getClass());
			if (nodeObject.toString().equals(userObject.toString())) {
				result = child;
//				logger.info(nodeObject + " is equal to " + userObject);
//			} else {
//				logger.info(nodeObject + " is not equal to " + userObject);
			}
		}
		return result;
	}
}
