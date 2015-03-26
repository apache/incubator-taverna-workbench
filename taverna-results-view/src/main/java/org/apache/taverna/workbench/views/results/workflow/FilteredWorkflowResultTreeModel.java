
package org.apache.taverna.workbench.views.results.workflow;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.taverna.workbench.views.results.SimpleFilteredTreeModel;
import org.apache.taverna.databundle.DataBundles;

@SuppressWarnings("serial")
public class FilteredWorkflowResultTreeModel extends SimpleFilteredTreeModel
		implements TreeModelListener {
	public enum FilterType {
		ALL {
			@Override
			public String toString() {
				return "view values";
			}
		},
		RESULTS {
			@Override
			public String toString() {
				return "view results";
			}
		},
		ERRORS {
			@Override
			public String toString() {
				return "view errors";
			}
		};
	}

	private FilterType filter;

	public FilteredWorkflowResultTreeModel(DefaultTreeModel delegate) {
		super(delegate);
		delegate.addTreeModelListener(this);
		this.filter = FilterType.ALL;
	}

	public void setFilter(FilterType filter) {
		this.filter = filter;
	}

	@Override
	public boolean isShown(Object o) {
		if (!(o instanceof WorkflowResultTreeNode))
			return false;
		WorkflowResultTreeNode node = (WorkflowResultTreeNode) o;
		switch (filter) {
		case RESULTS:
			for (Enumeration<?> e = node.depthFirstEnumeration(); e
					.hasMoreElements();) {
				WorkflowResultTreeNode subNode = (WorkflowResultTreeNode) e
						.nextElement();
				if (subNode.getReference() != null
						&& !DataBundles.isError(subNode.getReference()))
					return true;
			}
			return false;
		case ERRORS:
			for (Enumeration<?> e = node.depthFirstEnumeration(); e
					.hasMoreElements();) {
				WorkflowResultTreeNode subNode = (WorkflowResultTreeNode) e
						.nextElement();
				if (subNode.getReference() != null
						&& DataBundles.isError(subNode.getReference()))
					return true;
			}
			return false;
		default: // ALL/null
			return true;
		}
	}

    @Override
	public void treeNodesChanged(TreeModelEvent e) {
    	if (e.getChildren() == null) {
    		nodeChanged((DefaultMutableTreeNode) getRoot());
    		return;
    	}

		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) e
				.getTreePath().getLastPathComponent();
		ArrayList<Integer> indices = new ArrayList<>();
		for (Object o : e.getChildren())
			if (isShown(o))
				indices.add(getFilteredIndexOfChild(parent, o));
		if (!indices.isEmpty())
			nodesChanged(parent, listToArray(indices));
    }

    @Override
	public void treeNodesInserted(TreeModelEvent e) {
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) e
				.getTreePath().getLastPathComponent();
		ArrayList<Integer> indices = new ArrayList<>();
		for (Object o : e.getChildren())
			if (isShown(o))
				indices.add(getFilteredIndexOfChild(parent, o));
		if (!indices.isEmpty())
			nodesWereInserted(parent, listToArray(indices));
	}

	@Override
	public void treeNodesRemoved(TreeModelEvent e) {
	}

	@Override
	public void treeStructureChanged(TreeModelEvent e) {
	}

	private int[] listToArray(ArrayList<Integer> list) {
		int[] result = new int[list.size()];
		int index = 0;
		for (Integer i : list)
			result[index++] = i;
		return result;
	}
}
