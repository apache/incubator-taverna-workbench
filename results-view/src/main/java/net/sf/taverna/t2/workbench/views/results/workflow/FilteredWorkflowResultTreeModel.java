
package net.sf.taverna.t2.workbench.views.results.workflow;

import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeNode;
 
import net.sf.taverna.t2.workbench.views.results.SimpleFilteredTreeModel;
import net.sf.taverna.t2.workbench.views.results.processor.IterationTreeNode.ErrorState;

public class FilteredWorkflowResultTreeModel extends SimpleFilteredTreeModel implements TreeModelListener{
 
    public enum FilterType {
	ALL { public String toString() {return "view values";} },
	    RESULTS { public String toString() {return "view results";} },
		ERRORS { public String toString() {return "view errors";} };
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

    public boolean isShown(Object o) {
	if (!(o instanceof WorkflowResultTreeNode)) {
	    return false;
	}
	WorkflowResultTreeNode node = (WorkflowResultTreeNode) o;
	if (filter.equals(FilterType.ALL)) {
	    return (true);
	}
	if (filter.equals(FilterType.RESULTS)) {
	    for (Enumeration e = node.depthFirstEnumeration(); e.hasMoreElements();) {
		WorkflowResultTreeNode subNode = (WorkflowResultTreeNode) e.nextElement();
		if ((subNode.getReference() != null) && !subNode.getReference().containsErrors()) {
		    return true;
		}
	    }
	    return false;
	}
	if (filter.equals(FilterType.ERRORS)) {
	    for (Enumeration e = node.depthFirstEnumeration(); e.hasMoreElements();) {
		WorkflowResultTreeNode subNode = (WorkflowResultTreeNode) e.nextElement();
		if ((subNode.getReference() != null) && subNode.getReference().containsErrors()) {
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
	    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) e.getTreePath().getLastPathComponent();
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
	DefaultMutableTreeNode parent = (DefaultMutableTreeNode) e.getTreePath().getLastPathComponent();
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
