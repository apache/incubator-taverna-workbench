package net.sf.taverna.t2.workbench.views.results.processor;

import java.util.Enumeration;

import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.workbench.views.results.SimpleFilteredTreeModel;
import uk.org.taverna.databundle.DataBundles;

@SuppressWarnings("serial")
public class FilteredProcessorValueTreeModel extends SimpleFilteredTreeModel {

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
				if ((subNode.getReference() != null)
						&& !DataBundles.isError(subNode.getReference())) {
					return true;
				}
			}
			return false;
		}
		if (filter.equals(FilterType.ERRORS)) {
			return DataBundles.isError(node.getReference());
		}
		return true;
	}
}
