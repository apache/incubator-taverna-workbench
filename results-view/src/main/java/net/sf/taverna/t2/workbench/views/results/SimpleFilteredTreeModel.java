
package net.sf.taverna.t2.workbench.views.results;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
 
public abstract class SimpleFilteredTreeModel extends DefaultTreeModel {

    public DefaultTreeModel delegate;
 
    public SimpleFilteredTreeModel(DefaultTreeModel delegate) {
        super((DefaultMutableTreeNode) delegate.getRoot());
        this.delegate = delegate;
    }
 
    public Object getChild(Object parent, int index) {
        int count = -1;
        for (int i = 0; i < delegate.getChildCount(parent); i++) {
            final Object child = delegate.getChild(parent, i);
            if (isShown(child)) {
		count++;
                if (count == index) {
                    return child;
                }
            }
        }
        return null;
    }
 
    private int getFilteredIndexOfChild(DefaultMutableTreeNode parent, Object child) {
	int count = -1;
	for (int i = 0; i < delegate.getChildCount(parent); i++) {
	    final Object c = delegate.getChild(parent, i);
	    if (isShown(c)) {
		count++;
		if (c.equals(child)) {
		    return count;
		}
	    }
	    if (c.equals(child)) {
		return -1;
	    }
	}
	return -1;
    }

    public int getIndexOfChild(Object parent, Object child) {
        return delegate.getIndexOfChild(parent, child);
    }
 
    public int getChildCount(Object parent) {
        int count = 0;
        for (int i = 0; i < delegate.getChildCount(parent); i++) {
            final Object child = delegate.getChild(parent, i);
            if (isShown(child)) {
                count++;
            }
        }
        return count;
    }
 
    public boolean isLeaf(Object node) {
	if (node == null) {
	    return true;
	}
	if (delegate == null) {
	    return true;
	}
        return delegate.isLeaf(node);
    }
 
    public abstract boolean isShown(Object o);
}
