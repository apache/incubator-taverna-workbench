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
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

public final class FilterTreeModel extends DefaultTreeModel {

	Filter currentFilter;

	private static Logger logger = Logger
	.getLogger(FilterTreeModel.class);

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

	public int getChildCount(Object parent) {
		if (parent instanceof FilterTreeNode) {
			return (((FilterTreeNode) parent).getChildCount());
		}
		return 0;
	}

	public Object getChild(Object parent, int index) {
		if (parent instanceof FilterTreeNode) {
			return (((FilterTreeNode) parent).getChildAt(index));
		}
		return null;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8931308369832839862L;

	/**
	 * @return the currentFilter
	 */
	public Filter getCurrentFilter() {
		return currentFilter;
	}

	public TreePath getTreePathForObjectPath(List<Object> path) {
		List<FilterTreeNode> resultList = new ArrayList<FilterTreeNode>();
		FilterTreeNode current = (FilterTreeNode) root;
		resultList.add(current);
		for (int i = 1; (i < path.size()) && (current != null); i++) {
//			logger.info("Looking in " + current.getUserObject().toString() + " for " + path[i].toString());
			current = current.getChildForObject(path.get(i));
			if (current != null) {
				resultList.add(current);
			}
		}
		if (current != null) {
			Object[] nodeArray = resultList.toArray();
			return (new TreePath(nodeArray));
		}
		return null;
	}
}
