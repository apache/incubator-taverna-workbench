
package org.apache.taverna.workbench.views.results.processor;

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;

import org.apache.taverna.workbench.views.results.SimpleFilteredTreeModel;
import org.apache.taverna.workbench.views.results.processor.IterationTreeNode.ErrorState;

@SuppressWarnings("serial")
public class FilteredIterationTreeModel extends SimpleFilteredTreeModel {
	public enum FilterType {
		ALL {
			@Override
			public String toString() {
				return "View all";
			}
		},
		RESULTS {
			@Override
			public String toString() {
				return "View results";
			}
		},
		ERRORS {
			@Override
			public String toString() {
				return "View errors";
			}
		},
		SKIPPED {
			@Override
			public String toString() {
				return "View skipped";
			}
		};
	}

    private FilterType filter;

	public FilteredIterationTreeModel(DefaultTreeModel delegate) {
		super(delegate);
		this.filter = FilterType.ALL;
	}

	public void setFilter(FilterType filter) {
		this.filter = filter;
	}

	@Override
	public boolean isShown(Object o) {
		if (!(o instanceof IterationTreeNode))
			return false;
		IterationTreeNode node = (IterationTreeNode) o;
		switch (filter) {
		case ALL:
			return true;
		case RESULTS:
			for (Enumeration<?> e = node.depthFirstEnumeration(); e
					.hasMoreElements();) {
				IterationTreeNode subNode = (IterationTreeNode) e.nextElement();
				if (subNode.isLeaf()
						&& subNode.getErrorState().equals(ErrorState.NO_ERRORS)) {
					return true;
				}
			}
			return false;
		case ERRORS:
			for (Enumeration<?> e = node.depthFirstEnumeration(); e
					.hasMoreElements();) {
				IterationTreeNode subNode = (IterationTreeNode) e.nextElement();
				if (subNode.isLeaf()
						&& subNode.getErrorState() == ErrorState.OUTPUT_ERRORS)
					return true;
			}
			return false;
		case SKIPPED:
			for (Enumeration<?> e = node.depthFirstEnumeration(); e
					.hasMoreElements();) {
				IterationTreeNode subNode = (IterationTreeNode) e.nextElement();
				if (subNode.isLeaf()
						&& subNode.getErrorState() == ErrorState.INPUT_ERRORS)
					return true;
			}
			return false;
		default:
			return true;
		}
	}
}
