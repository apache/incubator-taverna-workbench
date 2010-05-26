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
package net.sf.taverna.t2.workbench.views.results.processor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

//import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState;

import org.apache.log4j.Logger;

/**
 * A node in the processor result tree - can be a single data item, a list of data items or
 * tree root. 
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ProcessorResultTreeNode extends DefaultMutableTreeNode {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ProcessorResultTreeNode.class);
	
	public enum ProcessorResultTreeNodeState {RESULT_TOP, RESULT_LIST, RESULT_REFERENCE};
	
	private ProcessorResultTreeNodeState state;
	private T2Reference reference; // reference to actual data if this node is a data node
	private int listSize; // number of element if this node is a list
	
	private ReferenceService referenceService;

	// Create root node
	public ProcessorResultTreeNode() {
		this.state = ProcessorResultTreeNodeState.RESULT_TOP;
	}
	
	// Create data node
	public ProcessorResultTreeNode(T2Reference reference, ReferenceService referenceService) {
		this.reference = reference;
		this.referenceService = referenceService;
		this.state = ProcessorResultTreeNodeState.RESULT_REFERENCE;
	}
	
	// Create list node
	public ProcessorResultTreeNode(int listSize) {
		this.listSize = listSize;
		this.state = ProcessorResultTreeNodeState.RESULT_LIST;
	}

	public ProcessorResultTreeNodeState getState() {
		return state;
	}

	public void setState(ProcessorResultTreeNodeState state) {
		this.state = state;
	}

	public T2Reference getReference() {
		return reference;
	}

	public void setReference(T2Reference reference) {
		this.reference = reference;
	}

	public String toString() {
		if (state.equals(ProcessorResultTreeNodeState.RESULT_TOP)) {
			return "Results:";
		}
		if (state.equals(ProcessorResultTreeNodeState.RESULT_LIST)) {
			if (getChildCount() == 0) {
				return "Empty list";
			}
			return "List with " + listSize + " values";
		}
		return reference.toString();
	}

	public boolean isState(ProcessorResultTreeNodeState state) {
		return this.state.equals(state);
	}
	
	public int getValueCount() {
		int result = 0;
		if (isState(ProcessorResultTreeNodeState.RESULT_REFERENCE)) {
			result = 1;
		} else if (isState(ProcessorResultTreeNodeState.RESULT_LIST)) {
			int childCount = this.getChildCount();
			for (int i = 0; i < childCount; i++) {
				ProcessorResultTreeNode child = (ProcessorResultTreeNode) this.getChildAt(i);
				result += child.getValueCount();
			}
		}
		return result;
	}

	public int getSublistCount() {
		int result = 0;
		if (isState(ProcessorResultTreeNodeState.RESULT_LIST)) {
			int childCount = this.getChildCount();
			for (int i = 0; i < childCount; i++) {
				ProcessorResultTreeNode child = (ProcessorResultTreeNode) this.getChildAt(i);
				if (child.isState(ProcessorResultTreeNodeState.RESULT_LIST)) {
					result++;
				}
			}
		}
		return result;
	}

	public Object getAsObject() {
		if (reference != null) {
		Identified identified = referenceService
		.resolveIdentifier(reference, null, null);
		if (identified instanceof ErrorDocument) {
			ErrorDocument errorDocument = (ErrorDocument) identified;
			return errorDocument.getMessage();
		}
		}
		if (isState(ProcessorResultTreeNodeState.RESULT_TOP)) {
			if (getChildCount() == 0) {
				return null;
			}
			else {
				return ((ProcessorResultTreeNode) getChildAt(0)).getAsObject();
			}
		}
		if (isState(ProcessorResultTreeNodeState.RESULT_LIST)) {
			List<Object> result = new ArrayList<Object>();
			for (int i = 0; i < getChildCount(); i++) {
				ProcessorResultTreeNode child = (ProcessorResultTreeNode) getChildAt(i);
				result.add (child.getAsObject());
			}
			return result;
		}
		if (reference == null) {
			return null;
		}
//		if (context.getReferenceService() == null) {
//			return null;
//		}
		try {
			Object result = referenceService.renderIdentifier(reference, Object.class, null);
			return result;
		}
		catch (Exception e) {
			// Not good to catch exception but
			return null;
		}
	}

	public boolean isState(ResultTreeNodeState state) {
		return this.state.equals(state);
	}

	public void setListSize(int listSize) {
		this.listSize = listSize;
	}

	public int getListSize() {
		return listSize;
	}

	public ReferenceService getReferenceService() {
		return referenceService;
	}
}
