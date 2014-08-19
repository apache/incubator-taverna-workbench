/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.servicepanel.servicetree;

import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author alson
 * 
 */
public class ServiceTreeNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3124468283260132857L;

	public ServiceTreeNode(final Object segment) {
		super(segment);
	}

	public Set<ServiceTreeNode> getLeaves() {
		final Set<ServiceTreeNode> result = new HashSet<ServiceTreeNode>();
		if (super.getChildCount() == 0) {
			result.add(this);
		} else {
			for (int i = 0; i < super.getChildCount(); i++) {
				final ServiceTreeNode child = (ServiceTreeNode) super
						.getChildAt(i);
				result.addAll(child.getLeaves());
			}
		}
		return result;
	}

}
