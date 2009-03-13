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
package net.sf.taverna.t2.workbench.views.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

public class ResultTreeNode implements MutableTreeNode {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ResultTreeNode.class);

	private MutableTreeNode parent;

	private List<MutableTreeNode> children = new ArrayList<MutableTreeNode>();

	private final T2Reference reference;

	private InvocationContext context;

	public T2Reference getReference() {
		return reference;
	}

	public ResultTreeNode(T2Reference reference, InvocationContext context/*, List<String> mimeTypes*/) {
		this.reference = reference;
		this.context = context;
	}

	public Enumeration<MutableTreeNode> children() {
		return Collections.enumeration(children);
	}

	public boolean getAllowsChildren() {
		return false;
	}

	public TreeNode getChildAt(int index) {
//		if (children.size() == 0) {
//			try {
//				// add a node underneath the 'folder' which displays the mime
//				// types and can be right clicked on.  Similar to T1 functionality
//				children.add(new ResultTreeChildNode(mimeTypes, context,
//						reference));
//			} catch (Exception e) {
//
//			}
//		}
		return children.get(index);
	}

	public int getChildCount() {
		return 0;
	}

	public int getIndex(TreeNode node) {
		return children.indexOf(node);
	}

	public TreeNode getParent() {
		return parent;
	}

	public boolean isLeaf() {
		return true;
	}

	public void insert(MutableTreeNode node, int index) {
		children.add(index, node);
	}

	public void remove(int index) {
		children.remove(index);
	}

	public void remove(MutableTreeNode node) {
		children.remove(node);
	}

	public void removeFromParent() {
		parent.remove(this);
	}

	public void setParent(MutableTreeNode node) {
		parent = node;
	}

	public void setUserObject(Object arg0) {

	}

	public String toString() {
		return reference.toString();
	}

	public InvocationContext getContext() {
		return context;
	}

}
