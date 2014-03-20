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
package net.sf.taverna.zaria;

import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;

import org.jdom.Element;

/**
 * Tree structure over a nested set of Zaria components, yes, this is almost an
 * exact duplicate of TreeNode but as ZPane is a subclass of JComponent we can't
 * have a getParent method (JComponent already contains this) so, annoyingly, we
 * have to invent a duplicate interface avoiding the name collisions. This
 * interface also defines that ZTreeNode implementations must be able to
 * serialise their current state to XML and restore from the same.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 */
public interface ZTreeNode {

	/**
	 * Set current state of this node, including construction of nested
	 * containers, from the specified JDOM Element
	 */
	public void configure(Element e);

	/**
	 * Indicates that the component is about to be discarded, and any cleaning
	 * up should be carried out here.
	 */
	public void discard();

	/**
	 * Return a list of Action objects that can act on this ZTreeNode,
	 * implemented largely by subclasses.
	 */
	public List<Action> getActions();

	/**
	 * Build current state of this node in the form of a JDOM element
	 */
	public Element getElement();

	/**
	 * Get the ZBasePane at the root of the component heirarchy or null if there
	 * isn't one (there will be for all cases where the component is visible)
	 */
	public ZBasePane getRoot();

	/**
	 * Return a list of JComponent items that should be added on the left hand
	 * side of the toolbar when in edit mode
	 */
	public List<Component> getToolbarComponents();

	/**
	 * Get number of immediate children
	 * 
	 * @return int count of immediate ZTreeNode children
	 */
	public int getZChildCount();

	/**
	 * Immediate children
	 * 
	 * @return List<ZTreeNode> of child nodes
	 */
	public List<ZTreeNode> getZChildren();

	/**
	 * Parent ZTreeNode
	 * 
	 * @return parent node or null if this is a root
	 */
	public ZTreeNode getZParent();

	/**
	 * Is this a leaf node?
	 * 
	 * @return whether the node is a leaf
	 */
	public boolean isZLeaf();

	/**
	 * Is this a root node?
	 * 
	 * @return whether the node is a root
	 */
	public boolean isZRoot();

	/**
	 * Set editable status on this node, implementations will recursively set
	 * the status on all children
	 */
	public void setEditable(boolean editable);

	/**
	 * Make sure the given component is visible (if it is a descendant of this
	 * node). This would include flipping tabs and scrolling to the component in
	 * question.
	 * <p>
	 * Return true if the node was made visible.
	 */
	public boolean makeVisible(JComponent component);

	/**
	 * Swap out the given child for the new one
	 * 
	 * @param oldComponent
	 *            the ZTreeNode to remove as a child
	 * @param newComponent
	 *            the ZTreeNode to insert in its place
	 */
	public void swap(ZTreeNode oldComponent, ZTreeNode newComponent);

}
