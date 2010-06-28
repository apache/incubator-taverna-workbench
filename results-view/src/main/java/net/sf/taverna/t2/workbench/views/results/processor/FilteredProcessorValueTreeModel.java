
package net.sf.taverna.t2.workbench.views.results.processor;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
 
import net.sf.taverna.t2.workbench.views.results.SimpleFilteredTreeModel;
import net.sf.taverna.t2.workbench.views.results.processor.IterationTreeNode.ErrorState;

public class FilteredProcessorValueTreeModel extends SimpleFilteredTreeModel {
 
    public enum FilterType {
	ALL { public String toString() {return "view values";} },
	    RESULTS { public String toString() {return "view results";} },
		ERRORS { public String toString() {return "view errors";} };
    }

    private FilterType filter;

    public FilteredProcessorValueTreeModel(DefaultTreeModel delegate) {
        super(delegate);
	this.filter = FilterType.ALL;
    }
 
    public void setFilter(FilterType filter) {
	this.filter = filter;
    }

    public boolean isShown(Object o) {
	if (!(o instanceof ProcessorResultTreeNode)) {
	    return false;
	}
	ProcessorResultTreeNode node = (ProcessorResultTreeNode) o;
	if (node.getReference() == null) {
	    // root of the model
	    return true;
	}
	if (filter.equals(FilterType.ALL)) {
	    return (true);
	}
	if (filter.equals(FilterType.RESULTS)) {
	    for (Enumeration e = node.depthFirstEnumeration(); e.hasMoreElements();) {
		ProcessorResultTreeNode subNode = (ProcessorResultTreeNode) e.nextElement();
		if ((subNode.getReference() != null) && !subNode.getReference().containsErrors()) {
		    return true;
		}
	    }
	    return false;
	}
	if (filter.equals(FilterType.ERRORS)) {
	    return node.getReference().containsErrors();
	}
	return true;
    }
}
