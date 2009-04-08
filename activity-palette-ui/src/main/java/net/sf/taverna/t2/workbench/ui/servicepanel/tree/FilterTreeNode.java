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

import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public class FilterTreeNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1933553584349932151L;
	private Filter currentFilter;
	private boolean passed = true;
	private List<FilterTreeNode> children = new LinkedList<FilterTreeNode>();

	public FilterTreeNode(Object userObject) {
		super(userObject);
		userObject.toString();
	}

	public void setFilter(Filter filter) {
		if ((filter == null) || !filter.isSuperseded()) {
			this.currentFilter = filter;
			passed = false;
			children.clear();
			if (filter == null) {
				passed = true;
				passFilterDown(null);
			} else if (filter.pass(this)) {
				passed = true;
				passFilterDown(null);
			} else {
				passFilterDown(filter);
				passed = children.size() != 0;
			}
		}
	}

	private void passFilterDown(Filter filter) {
		int realChildCount = super.getChildCount();
		for (int i = 0; i < realChildCount; i++) {
			FilterTreeNode realChild = (FilterTreeNode) super.getChildAt(i);
			realChild.setFilter(filter);
			if (realChild.isPassed()) {
				children.add(realChild);
			}
		}
	}

	public void add(FilterTreeNode node) {
		super.add(node);
		node.setFilter(currentFilter);
		// TODO work up
		if (node.isPassed()) {
			children.add(node);
		}
	}

	public int getChildCount() {
		// if (currentFilter == null) {
		// return super.getChildCount();
		// }
		return (children.size());
	}

	public FilterTreeNode getChildAt(int index) {
		// if (currentFilter == null) {
		// return (FilterTreeNode) super.getChildAt(index);
		// }
		return children.get(index);
	}

	public boolean isPassed() {
		return passed;
	}

}
