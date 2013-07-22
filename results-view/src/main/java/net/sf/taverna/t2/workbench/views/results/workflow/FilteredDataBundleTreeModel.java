/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.workbench.views.results.workflow;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.workbench.views.results.SimpleFilteredTreeModel;
import uk.org.taverna.databundle.DataBundles;

@SuppressWarnings("serial")
public class FilteredDataBundleTreeModel extends SimpleFilteredTreeModel implements
		TreeModelListener {

	public enum FilterType {
		ALL {
			public String toString() {
				return "view values";
			}
		},
		RESULTS {
			public String toString() {
				return "view results";
			}
		},
		ERRORS {
			public String toString() {
				return "view errors";
			}
		};
	}

	private FilterType filter;

	public FilteredDataBundleTreeModel(DefaultTreeModel delegate) {
		super(delegate);
		delegate.addTreeModelListener(this);
		this.filter = FilterType.ALL;
	}

	public void setFilter(FilterType filter) {
		this.filter = filter;
	}

	public boolean isShown(Object o) {
		if (!(o instanceof DefaultMutableTreeNode)) {
			return false;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
		Object userObject = node.getUserObject();
		if (!(userObject instanceof Path)) {
			return false;
		}
		Path path = (Path) userObject;
		if (filter.equals(FilterType.ALL)) {
			return (true);
		}
		if (filter.equals(FilterType.RESULTS)) {
			for (Enumeration<?> e = node.depthFirstEnumeration(); e.hasMoreElements();) {
				DefaultMutableTreeNode subNode = (DefaultMutableTreeNode) e.nextElement();
				if ((subNode.getUserObject() != null)
						&& !DataBundles.isError((Path) subNode.getUserObject())) {
					return true;
				}
			}
			return false;
		}
		if (filter.equals(FilterType.ERRORS)) {
			for (Enumeration<?> e = node.depthFirstEnumeration(); e.hasMoreElements();) {
				DefaultMutableTreeNode subNode = (DefaultMutableTreeNode) e.nextElement();
				if ((subNode.getUserObject() != null) && DataBundles.isError((Path) subNode.getUserObject())) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	public void treeNodesChanged(TreeModelEvent e) {
		if (e.getChildren() == null) {
			nodeChanged((DefaultMutableTreeNode) (this.getRoot()));
		} else {
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) e.getTreePath()
					.getLastPathComponent();
			ArrayList<Integer> indices = new ArrayList<Integer>();
			for (Object o : e.getChildren()) {
				if (isShown(o)) {
					indices.add(getFilteredIndexOfChild(parent, o));
				}
			}
			if (!indices.isEmpty()) {
				nodesChanged(parent, listToArray(indices));
			}
		}
	}

	public void treeNodesInserted(TreeModelEvent e) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) e.getTreePath()
				.getLastPathComponent();
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (Object o : e.getChildren()) {
			if (isShown(o)) {
				indices.add(getFilteredIndexOfChild(parent, o));
			}
		}
		if (!indices.isEmpty()) {
			nodesWereInserted(parent, listToArray(indices));
		}
	}

	public void treeNodesRemoved(TreeModelEvent e) {
	}

	public void treeStructureChanged(TreeModelEvent e) {
	}

	private int[] listToArray(ArrayList<Integer> list) {
		int[] result = new int[list.size()];
		int index = 0;
		for (Integer i : list) {
			result[index++] = i;
		}
		return result;
	}
}
