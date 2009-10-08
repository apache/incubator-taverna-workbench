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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

public class ResultTreeNode extends DefaultMutableTreeNode {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ResultTreeNode.class);

	private T2Reference reference;

	public void setReference(T2Reference reference) {
		this.reference = reference;
	}

	private InvocationContext context;
	
	public enum ResultTreeNodeState {RESULT_TOP, RESULT_WAITING, RESULT_LIST, RESULT_REFERENCE};

	private ResultTreeNodeState state;
	
	public ResultTreeNodeState getState() {
		return state;
	}

	public void setState(ResultTreeNodeState state) {
		this.state = state;
	}

	public T2Reference getReference() {
		return reference;
	}

	public ResultTreeNode(T2Reference reference, InvocationContext context/*, List<String> mimeTypes*/) {
		this.reference = reference;
		this.context = context;
		this.state = ResultTreeNodeState.RESULT_REFERENCE;
	}
	
	public ResultTreeNode(ResultTreeNodeState state) {
		this.reference = null;
		this.context = null;
		this.state = state;
	}

	public void setContext(InvocationContext context) {
		this.context = context;
	}

	public String toString() {
		if (state.equals(ResultTreeNodeState.RESULT_TOP)) {
			return "Results:";
		}
		if (state.equals(ResultTreeNodeState.RESULT_LIST)) {
			if (getChildCount() == 0) {
				return "Empty list";
			}
			return "List...";
		}
		if (state.equals(ResultTreeNodeState.RESULT_WAITING)) {
			return "Waiting for data";
		}
		return reference.toString();
	}

	public InvocationContext getContext() {
		return context;
	}
	
	public boolean isState(ResultTreeNodeState state) {
		return this.state.equals(state);
	}

}
