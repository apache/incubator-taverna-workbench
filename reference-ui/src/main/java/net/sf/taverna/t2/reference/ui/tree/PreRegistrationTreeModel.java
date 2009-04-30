/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.reference.ui.tree;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * A subclass of DefaultTreeModel which is aware of the depth property of its
 * leaf nodes. This is used to hold and assemble data collections prior to
 * registration through the ReferenceService. The root node is an internal
 * placeholder so that we can handle depth 0 inputs without having to replace
 * the root all the time, it always has one or zero children.
 * <p>
 * Non-leaf nodes always have an empty user object, leaf nodes can be one of
 * <ol>
 * <li>java.io.File</li>
 * <li>java.net.URL</li>
 * <li>String</li>
 * <li>byte[]</li>
 * </ol>
 * The getAsPojo method returns the appropriate object type for the entire
 * contents of this tree, mapping empty nodes to List implementations. It
 * returns null if the root node has zero children.
 * 
 * @author Tom Oinn
 * 
 */
public class PreRegistrationTreeModel extends DefaultTreeModel {

	private static final String INPUT = "Input";

	private static final String LIST_OF_DEPTH = "List of depth";

	private static final String LIST_OF_LISTS_OF_LISTS = "List of lists of lists";

	private static final String LIST_OF_LISTS = "List of lists";

	private static final String LIST = "List";

	private static final String SINGLE_VALUE = "Single value";

	private static final long serialVersionUID = 4133642236173701467L;

	private int depth = 0;

	public PreRegistrationTreeModel(int depth) {
		this(depth, INPUT);
	}
	
	public PreRegistrationTreeModel(int depth, String name) {

		super(new DefaultMutableTreeNode(getRootName(depth, name)));
		this.depth = depth;
	}

	private static String getRootName(int depth, String name) {
		if (depth == 0) {
			return name + ": " + SINGLE_VALUE;
		} else if (depth == 1) {
			return name + ": " + LIST;
		} else if (depth == 2) {
			return name + ": " + LIST_OF_LISTS;
		} else if (depth == 3) {
			return name + ": " + LIST_OF_LISTS_OF_LISTS;
		} else {
			return name + ": " + LIST_OF_DEPTH + " " + depth;
		}
	}

	public int getDepth() {
		return this.depth;
	}

	/**
	 * Get the contents of this tree model as a POJO ready for registration with
	 * the ReferenceService, returns null if the root has no children and throws
	 * IllegalStateException if there are any objects other than File, URL,
	 * String or byte[] at leaf nodes.
	 * 
	 * @return
	 */
	public synchronized Object getAsPojo() {
		if (getChildCount(getRoot()) == 0) {
			return null;
		} else {
			return getAsPojoInner(getChild(getRoot(), 0));
		}
	}

	private synchronized Object getAsPojoInner(Object child) {
		DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) child;
		Object userObject = childNode.getUserObject();
		if (userObject == null) {
			List<Object> result = new ArrayList<Object>();
			int children = getChildCount(childNode);
			for (int i = 0; i < children; i++) {
				result.add(getAsPojoInner(getChild(childNode, i)));
			}
			return result;
		} else {
			if (userObject instanceof String || userObject instanceof File
					|| userObject instanceof URL
					|| userObject instanceof byte[]) {
				return userObject;
			} else {
				throw new IllegalStateException(
						"Found an illegal object of type '"
								+ userObject.getClass().getCanonicalName()
								+ "' in collection structure.");
			}
		}
	}

	/**
	 * Nodes are leaves if they are not the root node and if they have a user
	 * object defined. All non-leaf nodes are either the root (special case) or
	 * have a null user object.
	 */
	@Override
	public boolean isLeaf(Object o) {
		if (o == getRoot()) {
			return false;
		} else {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) o;
			return (node.getUserObject() != null);
		}
	}

	/**
	 * Add the specified pojo at a particular place in the collection. The
	 * target node is, if required, re-written to ensure the depth property of
	 * the model is maintained. If specified as null the target is assumed to be
	 * the root node.
	 * 
	 * @param target
	 * @param pojo
	 * @param depth
	 */
	@SuppressWarnings("unchecked")
	public synchronized DefaultMutableTreeNode addPojoStructure(MutableTreeNode target,
			Object pojo, int depth) {
		// Firstly check for a null target and set the root node to be the
		// target if so.
		if (target == null) {
			target = (MutableTreeNode) getRoot();
		}
		// Now ensure that the target node has the correct depth. The target
		// node must have depth of (depth - 1) to be correct, this means we can
		// add the collection in place without any problems.
		int targetDepth = getNodeDepth(target);

		if (targetDepth > (depth + 1)) {
			// Need to traverse down the structure to find an appropriate parent
			// node, creating empty nodes as we go if required.
			if (target.getChildCount() == 0) {
				insertNodeInto(new DefaultMutableTreeNode(null), target, 0);
			}
			return addPojoStructure((MutableTreeNode) target.getChildAt(0), pojo,
					depth);
		} else if (targetDepth < (depth + 1)) {
			// Need to traverse up the structure to find an appropriate parent
			// node
			if (target.getParent() == null) {
				throw new IllegalArgumentException(
						"Can't add this pojo, depths are not compatible.");
			}
			return addPojoStructure((MutableTreeNode) target.getParent(), pojo, depth);
		} else if (targetDepth == (depth + 1)) {
			// Found an appropriate parent node, we can insert at position 0
			// here. If this is the root node then we need to clear it first,
			// the root can only have zero or one child nodes.
			if (target == getRoot()) {
				if (target.getChildCount() == 1) {
					removeNodeFromParent((MutableTreeNode) target.getChildAt(0));
				}
			}
			int children = target.getChildCount();
			if (pojo instanceof List) {
				DefaultMutableTreeNode newTarget = new DefaultMutableTreeNode(null);
				insertNodeInto(newTarget, target, children);
				for (Object child : (List<Object>) pojo) {
					addPojoStructure(newTarget, child, depth - 1);
				}
				return newTarget;
			} else {
				// Append to the target node
				DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(pojo);
				insertNodeInto(newChild, target,
						children);
				return newChild;
			}
		} else {
			// Can we really reach this code?
			return null;
		}
	}

	/**
	 * Move a node to another node, used to respond to internal drag and drop
	 * events corresponding to re-arrangements of this collection structure.
	 * Behaviour depends on the relative depths (in t2reference terms where a
	 * leaf node has depth 0) of the source and target nodes
	 * <ol>
	 * <li>If the target is the same depth as the source then this is
	 * interpreted as a request to move the source to become a sibling of the
	 * target positioned immediately after it in the target's parent's child
	 * list.</li>
	 * <li>If the target is a lower depth than the source then the target node
	 * is re-written to be the node on the target's path to the root with the
	 * same depth as the source and treated as above.</li>
	 * <li>If the target is at a higher depth than the source by exactly one
	 * then this is interpreted as a request to insert the source node at the
	 * start of the target's child list.</li>
	 * <li>If the target is at a higher depth by more than one the target node
	 * is rewritten to be the first child of the target node. If the target node
	 * has no children a new node is created, inserted into the target and set
	 * as the target for this method, which is called recursively</li>
	 * </ol>
	 * This method is called before any nodes are modified, and causes the
	 * modifications to take place.
	 * 
	 */
	public synchronized void moveNode(MutableTreeNode source,
			MutableTreeNode target) {
		// Check that we're not dragging onto ourselves!
		if (source.equals(target)) {
			return;
		}
		int targetDepth = getNodeDepth(target);
		int sourceDepth = getNodeDepth(source);
		// Handle drag onto a future sibling
		if (sourceDepth == targetDepth) {

			// Move the source from wherever it currently is and add it as a
			// sibling of the target node at an index one higher.

			removeNodeFromParent(source);
			// Capture the index of the target in its parent
			int targetIndex = getIndexOfChild(target.getParent(), target);
			// Insert the source node into the target's parent at the
			// appropriate index
			insertNodeInto(source, (MutableTreeNode) target.getParent(),
					targetIndex + 1);
		}
		// Traverse up to find a potential sibling node
		else if (targetDepth < sourceDepth) {
			moveNode(source, (MutableTreeNode) target.getParent());
		}
		// Check for a move to an immediate future parent
		else if (targetDepth == (sourceDepth + 1)) {
			// Insert at index 0 in the target, removing from our old parent
			// first

			removeNodeFromParent(source);

			insertNodeInto(source, target, 0);
		}
		// Otherwise traverse, picking the child at index 0 every time and
		// creating a new one if required
		else if (targetDepth > sourceDepth) {
			// Create a new non-leaf node first if needed
			if (target.getChildCount() == 0) {

				insertNodeInto(new DefaultMutableTreeNode(null), target, 0);
			}
			// Recursively try to move the source to the target's child list at
			// position 0
			moveNode(source, (MutableTreeNode) target.getChildAt(0));
		}
	}

	@Override
	public synchronized void removeNodeFromParent(MutableTreeNode node) {
		if (node.getParent() != null) {
			super.removeNodeFromParent(node);
		}
	}

	/**
	 * Return the depth of the specified tree node. Depth is determined by the
	 * length of the path to the root, where a path of length 2 corresponds to
	 * the depth of this model structure. The result is therefore equal to
	 * <code>getDepth() - (getPathToRoot(o).length - 2)</code>
	 * 
	 * @param o
	 * @return
	 */
	private int getNodeDepth(TreeNode o) {
		return getDepth() - (getPathToRoot(o).length - 2);
	}

}
