
package net.sf.taverna.t2.workbench.views.results.processor;

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.workbench.views.results.SimpleFilteredTreeModel;
import net.sf.taverna.t2.workbench.views.results.processor.IterationTreeNode.ErrorState;

@SuppressWarnings("serial")
public class FilteredIterationTreeModel extends SimpleFilteredTreeModel {

    public enum FilterType {
	ALL { public String toString() {return "View all";} },
	    RESULTS { public String toString() {return "View results";} },
		ERRORS { public String toString() {return "View errors";} },
		    SKIPPED { public String toString() {return "View skipped";} };
    }

    private FilterType filter;

    public FilteredIterationTreeModel(DefaultTreeModel delegate) {
        super(delegate);
	this.filter = FilterType.ALL;
    }

    public void setFilter(FilterType filter) {
	this.filter = filter;
    }

    public boolean isShown(Object o) {
	if (!(o instanceof IterationTreeNode)) {
	    return false;
	}
	IterationTreeNode node = (IterationTreeNode) o;
	if (filter.equals(FilterType.ALL)) {
	    return (true);
	}
	if (filter.equals(FilterType.RESULTS)) {
	    for (Enumeration<?> e = node.depthFirstEnumeration(); e.hasMoreElements();) {
		IterationTreeNode subNode = (IterationTreeNode) e.nextElement();
		if (subNode.isLeaf() && subNode.getErrorState().equals(ErrorState.NO_ERRORS)) {
		    return true;
		}
	    }
	    return false;
	}
	if (filter.equals(FilterType.ERRORS)) {
	    for (Enumeration e = node.depthFirstEnumeration(); e.hasMoreElements();) {
		IterationTreeNode subNode = (IterationTreeNode) e.nextElement();
		if (subNode.isLeaf() && subNode.getErrorState().equals(ErrorState.OUTPUT_ERRORS)) {
		    return true;
		}
	    }
	    return false;
	}
	if (filter.equals(FilterType.SKIPPED)) {
	    for (Enumeration e = node.depthFirstEnumeration(); e.hasMoreElements();) {
		IterationTreeNode subNode = (IterationTreeNode) e.nextElement();
		if (subNode.isLeaf() && subNode.getErrorState().equals(ErrorState.INPUT_ERRORS)) {
		    return true;
		}
	    }
	    return false;
	}
	return true;
    }
}
