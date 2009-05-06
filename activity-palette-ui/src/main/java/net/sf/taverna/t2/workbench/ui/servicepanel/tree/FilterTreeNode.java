/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.servicepanel.tree;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;

public class FilterTreeNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1933553584349932151L;
	private Filter filter;
	private boolean passed = true;
	private List<FilterTreeNode> filteredChildren = new ArrayList<FilterTreeNode>();

	private static Logger logger = Logger
	.getLogger(FilterTreeNode.class);

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
			if (realChild.isPassed()) {
				filteredChildren.add(realChild);
			}
		}
	}

	public void add(FilterTreeNode node) {
		super.add(node);
		node.setFilter(filter);
		// TODO work up
		if (node.isPassed()) {
			filteredChildren.add(node);
		}
	}
	
	@Override
	public void remove(int childIndex) {
		if (filter != null) {
			// as child indexes might be inconsistent..
			throw new IllegalStateException("Can't remove while the filter is active");
		}
		super.remove(childIndex);
	}
	

	public int getChildCount() {
		if (filter == null) {
			return super.getChildCount();
		}
		return (filteredChildren.size());
	}

	public FilterTreeNode getChildAt(int index) {
		if (filter == null) {
			return (FilterTreeNode) super.getChildAt(index);
		}
		return filteredChildren.get(index);
	}

	public boolean isPassed() {
		return passed;
	}
	
	public Set<FilterTreeNode> getLeaves() {
		Set<FilterTreeNode> result = new HashSet<FilterTreeNode>();
		if (super.getChildCount() == 0) {
			result.add(this);
		} else {
		for (int i = 0; i < super.getChildCount(); i++) {
			FilterTreeNode child = (FilterTreeNode) super.getChildAt(i);
			result.addAll(child.getLeaves());
		}
		}
		return result;
	}

	public FilterTreeNode getChildForObject(Object userObject) {
		FilterTreeNode result = null;
		for (int i=0; (i < super.getChildCount()) && (result == null); i++) {
			FilterTreeNode child = (FilterTreeNode) super.getChildAt(i);
			Object nodeObject = child.getUserObject();
//			logger.info("nodeObject is a " + nodeObject.getClass().getCanonicalName() + " - " +
//					"userObject is a " + userObject.getClass().getCanonicalName());
			if (nodeObject.toString().equals(userObject.toString())) {
				result = child;
//				logger.info(nodeObject.toString() + " is equal to " + userObject.toString());
//			} else {
//				logger.info(nodeObject.toString() + " is not equal to " + userObject.toString());
//				
			}
		}
		return result;
	}
	

}
