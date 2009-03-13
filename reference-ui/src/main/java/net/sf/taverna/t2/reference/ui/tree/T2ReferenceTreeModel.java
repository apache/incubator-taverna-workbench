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

import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * A TreeModel implementation backed by the T2Reference model. As collections
 * are immutable when registered this can be relatively simple. This model is to
 * be used when viewing results or when previewing input data, for capturing
 * user inputs use the {@link PreRegistrationTreeModel}. All nodes in this
 * model are instances of T2Reference, leaf nodes are references with depth 0.
 * <p>
 * This tree will just contain identifiers in a nested structure, so it makes
 * sense to combine it with some kind of detail view panel to actually show the
 * contents of the reference set and error document leaf nodes.
 * <p>
 * The implementation also assumes that caching is performed elsewhere - it will
 * behave very poorly if no cache is used on the Dao layers as it will
 * repeatedly try to resolve the contents of lists etc. Use a cache unless you
 * are using the in memory Dao implementations in your reference service.
 * 
 * @author Tom Oinn
 */
public class T2ReferenceTreeModel implements TreeModel {

	private ReferenceService referenceService;
	private T2Reference root;

	/**
	 * Construct a new tree model with the specified T2Reference as the root
	 * node.
	 * 
	 * @param rootReference
	 * @param rs
	 */
	public T2ReferenceTreeModel(T2Reference rootReference, ReferenceService rs) {
		this.referenceService = rs;
		this.root = rootReference;
	}

	public Object getChild(Object parent, int index) {
		T2Reference ref = (T2Reference) parent;
		if (ref.getDepth() == 0) {
			throw new IllegalArgumentException(
					"Parent reference has depth 0, cannot get children");
		}
		switch (ref.getReferenceType()) {
		case IdentifiedList:
			List<T2Reference> children = referenceService.getListService()
					.getList(ref);
			return children.get(index);
		case ErrorDocument:
			if (index != 0) {
				throw new IllegalArgumentException(
						"Child index for an error document must be 0");
			}
			return referenceService.getErrorDocumentService().getChild(ref);
		}
		// Will never get here.
		return null;
	}

	public int getChildCount(Object parent) {
		T2Reference ref = (T2Reference) parent;
		switch (ref.getReferenceType()) {
		case IdentifiedList:
			// Lists may have arbitrary numbers of children
			List<T2Reference> children = referenceService.getListService()
					.getList((T2Reference) parent);
			return children.size();
		case ErrorDocument:
			// Error documents with depth > 0 have a single child
			if (ref.getDepth() > 0) {
				return 1;
			} else {
				return 0;
			}
		default:
			// Everything else has no children
			return 0;
		}
	}

	public int getIndexOfChild(Object parent, Object child) {
		// We assume the child is actually in the parent
		T2Reference parentRef = (T2Reference) parent;
		T2Reference childRef = (T2Reference) child;
		switch (parentRef.getReferenceType()) {
		case IdentifiedList:
			List<T2Reference> children = referenceService.getListService()
					.getList(parentRef);
			return children.indexOf(childRef);
		case ErrorDocument:
			return 0;
		default:
			// If we get here something's gone wrong
			throw new IllegalStateException(
					"Unable to determine child location in " + parentRef);
		}
	}

	public Object getRoot() {
		// Return the previously stored root
		return this.root;
	}

	public boolean isLeaf(Object node) {
		// Item is leaf if its depth is zero
		return (((T2Reference) node).getDepth() == 0);
	}

	// Listener methods, no reason to have them here as the model is immutable.

	public void removeTreeModelListener(TreeModelListener l) {
		// Do nothing
	}

	public void addTreeModelListener(TreeModelListener l) {
		// Do nothing

	}

	public void valueForPathChanged(TreePath path, Object newValue) {
		// Never happens, immutable model
	}

}
