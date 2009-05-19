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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.apache.log4j.Logger;

public class ResultTreeModel extends DefaultTreeModel implements ResultListener {

	private static final long serialVersionUID = 7154527821423588046L;

	private static Logger logger = Logger.getLogger(ResultTreeModel.class);
	
	// Name of the output port this class models results for
	private String portName;
	
	// Output port depth (0 for single result, 1 for list, 2 for list of lists ...)
	int depth;
	
	int depthSeen = -1;

	public ResultTreeModel(String portName, int depth) {
		super(new DefaultMutableTreeNode("Results:"));
		this.portName = portName;
		this.depth = depth;
	}

	public void resultTokenProduced(WorkflowDataToken dataToken, String portName) {
		
		int[] index = dataToken.getIndex();
		if (this.portName.equals(portName)) {
			if (depthSeen == -1) {
				depthSeen = index.length;
			}

			if (index.length >= depthSeen) {
				T2Reference reference = dataToken.getData();

				if (reference.getReferenceType() == T2ReferenceType.IdentifiedList) {

					try {
						IdentifiedList<T2Reference> list = dataToken
								.getContext().getReferenceService()
								.getListService().getList(reference);
						int[] elementIndex = new int[index.length + 1];
						for (int indexElement = 0; indexElement < index.length; indexElement++) {
							elementIndex[indexElement] = index[indexElement];
						}
						int c = 0;
						for (T2Reference id : list) {
							elementIndex[index.length] = c;
							resultTokenProduced(new WorkflowDataToken(dataToken
									.getOwningProcess(), elementIndex, id,
									dataToken.getContext()), portName);
							c++;
						}
						// TODO: display to user.
					} catch (Exception e) {
						logger.error("Error resolving data entity list "
								+ reference, e);
					}
				} else {
					insertNewDataTokenNode(reference, index, dataToken
							.getOwningProcess(), dataToken.getContext());
				}
			}
		}

	}

	public void insertNewDataTokenNode(T2Reference reference, int[] index,
			String owningProcess, InvocationContext context) {
		MutableTreeNode parent = (MutableTreeNode) getRoot();
		if (index.length == depth) {
			if (depth == 0) {
				MutableTreeNode child = getChildAt(parent, 0);
				child = updateChildNodeWithData(reference, owningProcess, parent,
						child, context);
				nodeChanged(child);
			} else {
				parent = getChildAt(parent, 0);
				parent.setUserObject("List...");
				for (int indexElement = 0; indexElement < depth; indexElement++) {
					MutableTreeNode child = getChildAt(parent,
							index[indexElement]);
					if (indexElement == (depth - 1)) { // leaf
						child = updateChildNodeWithData(reference, owningProcess,
								parent, child, context);
					} else { // list
						child.setUserObject("List...");
					}
					nodeChanged(child);
					parent = child;
				}
			}
		} else if (reference.getReferenceType() == T2ReferenceType.ErrorDocument) {
			for (int i = 0; i < depth; i++) {
				parent = getChildAt(parent, 0);
				parent.setUserObject("List...");
				nodeChanged(parent);				
			}
			MutableTreeNode child = getChildAt(parent, 0);
			child = updateChildNodeWithData(reference, owningProcess, parent,
					child, context);
			nodeChanged(child);
		}
	}

	private MutableTreeNode updateChildNodeWithData(T2Reference reference,
			String owningProcess, MutableTreeNode parent,
			MutableTreeNode child, InvocationContext context) {

		int childIndex = parent.getIndex(child);
		child = new ResultTreeNode(reference, context);

		parent.remove(childIndex);
		parent.insert(child, childIndex);

		return child;
	}

	private MutableTreeNode getChildAt(MutableTreeNode parent, int i) {
		int childCount = getChildCount(parent);
		if (childCount <= i) {
			for (int x = childCount; x <= i; x++) {
				insertNodeInto(new DefaultMutableTreeNode("Waiting for data"),
						parent, x);
			}
		}

		return (MutableTreeNode) parent.getChildAt(i);
	}

}
