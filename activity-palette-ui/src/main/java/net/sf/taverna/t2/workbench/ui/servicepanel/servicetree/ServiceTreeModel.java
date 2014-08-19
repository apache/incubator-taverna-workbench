/**
 * 
 */
package net.sf.taverna.t2.workbench.ui.servicepanel.servicetree;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workbench.ui.servicepanel.ServiceFilter;

import org.apache.log4j.Logger;

/**
 * @author alson
 * 
 */
public class ServiceTreeModel extends DefaultTreeModel {

	public static final String AVAILABLE_SERVICES = "Available services";

	public static final String NO_MATCHING_SERVICES = "No matching services";

	public static final String MATCHING_SERVICES = "Matching services";

	private static final ServiceTreeNode ROOT = new ServiceTreeNode(
			AVAILABLE_SERVICES);

	public static final String MOBY_OBJECTS = "MOBY Objects";

	@SuppressWarnings("unchecked")
	private static Comparator<Object> comparator = new ServicePathElementComparator();

	private static Logger logger = Logger.getLogger(ServiceTreeModel.class);

	private final Map<ServiceDescription<?>, ServiceTreeNode> serviceDescriptionToNode = new HashMap<ServiceDescription<?>, ServiceTreeNode>();

	public ServiceTreeModel() {
		super(ROOT);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8669321454833989964L;

	@SuppressWarnings("rawtypes")
	public synchronized void addServiceDescriptions(
			final Collection<? extends ServiceDescription> addedDescriptions) {
		for (final ServiceDescription sd : addedDescriptions) {
			addServiceDescription(sd);
		}
	}

	@SuppressWarnings("rawtypes")
	private void addServiceDescription(final ServiceDescription sd) {
		final List path = sd.getPath();
		ServiceTreeNode currentParent = ROOT;
		for (final Object segment : path) {
			currentParent = findOrCreateChild(currentParent, segment);
		}
		findOrCreateChild(currentParent, sd);
	}

	private ServiceTreeNode findOrCreateChild(
			final ServiceTreeNode currentParent, final Object segment) {
		final int childCount = currentParent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final ServiceTreeNode child = (ServiceTreeNode) currentParent
					.getChildAt(i);
			final int comparison = comparator.compare(child.getUserObject(),
					segment);
			if (comparison < 0) {
				continue;
			}
			if (comparison == 0) {
				return child;
			}
			if (comparison > 0) {
				final ServiceTreeNode newChild = createAndInsertNewChild(
						currentParent, segment, i);

				return newChild;
			}
		}
		final ServiceTreeNode newChild = createAndInsertNewChild(currentParent,
				segment, currentParent.getChildCount());
		return newChild;
	}

	/**
	 * @param currentParent
	 * @param segment
	 * @param i
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private ServiceTreeNode createAndInsertNewChild(
			final ServiceTreeNode currentParent, final Object segment,
			final int i) {
		final ServiceTreeNode newChild = new ServiceTreeNode(segment);
		if (segment instanceof ServiceDescription) {
			serviceDescriptionToNode
					.put((ServiceDescription) segment, newChild);
		}
		this.insertNodeInto(newChild, currentParent, i);
		logger.info("New child inserted into " + currentParent.toString()
				+ " at " + i);
		return newChild;
	}

	public void removeServiceDescription(final ServiceDescription<?> sd) {
		ServiceTreeNode node = serviceDescriptionToNode.get(sd);
		if (node == null) {
			return;
		}
		ServiceTreeNode parent = (ServiceTreeNode) node.getParent();
		this.removeNodeFromParent(node);
		while ((parent != null) && (parent.getChildCount() == 0)) {
			node = parent;
			parent = (ServiceTreeNode) node.getParent();
			this.removeNodeFromParent(node);
		}

	}

	@SuppressWarnings("rawtypes")
	protected static class ServicePathElementComparator implements Comparator {
		@Override
		public int compare(final Object o1, final Object o2) {
			final String o1ToCompare = (o1 instanceof ServiceDescription ? ((ServiceDescription) o1)
					.getName() : o1.toString());
			final String o2ToCompare = (o2 instanceof ServiceDescription ? ((ServiceDescription) o2)
					.getName() : o2.toString());
			if (o1ToCompare.equalsIgnoreCase(o2ToCompare)) {
				return 0;
			}
			if (o1ToCompare.equals(ServiceDescription.SERVICE_TEMPLATES)) {
				return -1;
			}
			if (o2ToCompare.equals(ServiceDescription.SERVICE_TEMPLATES)) {
				return 1;
			}
			if (o1ToCompare.equals(ServiceDescription.LOCAL_SERVICES)) {
				return -1;
			}
			if (o2ToCompare.equals(ServiceDescription.LOCAL_SERVICES)) {
				return 1;
			}
			if (o1ToCompare.equals(MOBY_OBJECTS)) {
				return -1;
			}
			if (o2ToCompare.equals(MOBY_OBJECTS)) {
				return 1;
			}
			return o1ToCompare.compareToIgnoreCase(o2ToCompare);
		}
	}

	public ServiceTreeModel cloneWithFilter(final ServiceFilter filter) {
		if (filter == null) {
			return this;
		}
		if (filter != null) {
			final String filterString = filter.getFilterString();
			if ((filterString == null) || filterString.isEmpty()) {
				return this;
			}
		}

		final ServiceTreeModel clonedModel = new ServiceTreeModel();
		clonedModel.setRoot(cloneAndFilterNode(
				(ServiceTreeNode) this.getRoot(), filter, false));
		if (clonedModel.getRoot() == null) {
			clonedModel.setRoot(new ServiceTreeNode(NO_MATCHING_SERVICES));
		} else {
			((ServiceTreeNode) clonedModel.getRoot())
					.setUserObject(MATCHING_SERVICES);
		}
		return clonedModel;

	}

	private ServiceTreeNode cloneAndFilterNode(final ServiceTreeNode node,
			final ServiceFilter filter, final boolean forcePass) {
		final ServiceTreeNode clonedNode = (ServiceTreeNode) node.clone();
		if (forcePass || filter.pass(node)) {
			final int count = node.getChildCount();
			for (int i = 0; i < count; i++) {
				final ServiceTreeNode child = (ServiceTreeNode) node
						.getChildAt(i);
				clonedNode.add(cloneAndFilterNode(child, filter, true));
			}
			return clonedNode;
		} else {
			final int count = node.getChildCount();
			for (int i = 0; i < count; i++) {
				final ServiceTreeNode child = (ServiceTreeNode) node
						.getChildAt(i);
				final ServiceTreeNode possibleClonedChild = cloneAndFilterNode(
						child, filter, false);
				if (possibleClonedChild != null) {
					clonedNode.add(possibleClonedChild);
				}
			}
			if (clonedNode.getChildCount() > 0) {
				return clonedNode;
			}
			return null;
		}

	}

}
