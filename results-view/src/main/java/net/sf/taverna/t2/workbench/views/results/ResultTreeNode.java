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
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
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
	
	public int getValueCount() {
		int result = 0;
		if (isState(ResultTreeNodeState.RESULT_REFERENCE)) {
			result = 1;
		} else if (isState(ResultTreeNodeState.RESULT_LIST)) {
			int childCount = this.getChildCount();
			for (int i = 0; i < childCount; i++) {
				ResultTreeNode child = (ResultTreeNode) this.getChildAt(i);
				result += child.getValueCount();
			}
		}
		return result;
	}

	public int getSublistCount() {
		int result = 0;
		if (isState(ResultTreeNodeState.RESULT_LIST)) {
			int childCount = this.getChildCount();
			for (int i = 0; i < childCount; i++) {
				ResultTreeNode child = (ResultTreeNode) this.getChildAt(i);
				if (child.isState(ResultTreeNodeState.RESULT_LIST)) {
					result++;
				}
			}
		}
		return result;
	}

	public Object getAsObject() {
		if (reference != null) {
		Identified identified = context.getReferenceService()
		.resolveIdentifier(reference, null, context);
		if (identified instanceof ErrorDocument) {
			ErrorDocument errorDocument = (ErrorDocument) identified;
			return errorDocument.getMessage();
		}
		}
		if (isState(ResultTreeNodeState.RESULT_WAITING)) {
			return (new String("Waiting"));
		}
		if (isState(ResultTreeNodeState.RESULT_TOP)) {
			if (getChildCount() == 0) {
				return null;
			}
			else {
				return ((ResultTreeNode) getChildAt(0)).getAsObject();
			}
		}
		if (isState(ResultTreeNodeState.RESULT_LIST)) {
			List result = new ArrayList();
			for (int i = 0; i < getChildCount(); i++) {
				ResultTreeNode child = (ResultTreeNode) getChildAt(i);
				result.add (child.getAsObject());
			}
			return result;
		}
		if (reference == null) {
			return null;
		}
		if (context.getReferenceService() == null) {
			return null;
		}
		try {
			Object result = context.getReferenceService().renderIdentifier(reference, Object.class, context);
			return result;
		}
		catch (Exception e) {
			// Not good to catch exception but
			return null;
		}
	}
}
