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
package net.sf.taverna.t2.workbench.views.results.workflow;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultTreeNode.ResultTreeNodeState;

import org.apache.log4j.Logger;

import uk.org.taverna.databundle.DataBundles;
import uk.org.taverna.platform.report.ReportListener;

public class WorkflowResultTreeModel extends DefaultTreeModel implements ReportListener {

	private static final long serialVersionUID = 7154527821423588046L;

	private static Logger logger = Logger.getLogger(WorkflowResultTreeModel.class);

	// Name of the output port this class models results for
	private String portName;

	int depthSeen = -1;

	public WorkflowResultTreeModel(String portName) {
		super(new WorkflowResultTreeNode(ResultTreeNodeState.RESULT_TOP));
		this.portName = portName;
	}

	public void outputAdded(final Path path, final String portName, final int[] index) {
		// Don't slow down workflow execution, do it in GUI thread
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				resultTokenProducedGui(path, portName, index);
			}
		});
	}

	public void resultTokenProducedGui(Path path, String portName, int[] index) {
		if (this.portName.equals(portName)) {
			if (depthSeen == -1) {
				depthSeen = index.length;
			}

			if (index.length >= depthSeen) {
				Path reference = path;
				if (DataBundles.isList(reference)) {
					try {
						WorkflowResultTreeNode parent = (WorkflowResultTreeNode) getRoot();
						parent = getChildAt(parent,0);
						changeState(parent, ResultTreeNodeState.RESULT_LIST);
						for (int i = 0; i < index.length; i++) {
							parent = getChildAt(parent, index[i]);
							changeState(parent, ResultTreeNodeState.RESULT_LIST);
						}

						List<Path> list = DataBundles.getList(reference);
						int[] elementIndex = new int[index.length + 1];
						for (int indexElement = 0; indexElement < index.length; indexElement++) {
							elementIndex[indexElement] = index[indexElement];
						}
						int c = 0;
						for (Path id : list) {
							elementIndex[index.length] = c;
							resultTokenProducedGui(id, portName, elementIndex);
							c++;
						}
//						if (c == 0) {
//							parent.setUserObject("Empty list (depth=" + reference.getDepth() + ")" + reference.getLocalPart());
//							nodeChanged(parent);
//						}
					} catch (NullPointerException | IOException e) {
						logger.error("Error resolving data entity list " + reference, e);
					}
				} else {
					insertNewDataTokenNode(reference, index);
				}
			}
		}

	}

	public void insertNewDataTokenNode(Path reference, int[] index) {
		WorkflowResultTreeNode parent = (WorkflowResultTreeNode) getRoot();
		 if (DataBundles.isError(reference)) {
			 parent = getChildAt(parent, 0);
			 for (int i = 0; i < index.length - 1; i++) {
				 parent = getChildAt(parent, index[i]);
				 parent = getChildAt(parent, 0);
				 changeState(parent, ResultTreeNodeState.RESULT_LIST);
			 }
			 if (index.length > 0) {
				 WorkflowResultTreeNode child = getChildAt(parent, index[index.length-1]);
				 updateNodeWithData(child, reference);
			 } else {
				 updateNodeWithData(parent, reference);
			 }
		 } else {
			int depth = index.length;
			if (index.length == 0) {
				WorkflowResultTreeNode child = getChildAt(parent, 0);
				updateNodeWithData(child, reference);
			} else {
				parent = getChildAt(parent, 0);
				changeState(parent, ResultTreeNodeState.RESULT_LIST);
				for (int indexElement = 0; indexElement < depth; indexElement++) {
					WorkflowResultTreeNode child = getChildAt(parent,
							index[indexElement]);
					if (indexElement == (depth - 1)) { // leaf
						updateNodeWithData(child, reference);
					} else { // list
						child.setState(ResultTreeNodeState.RESULT_LIST);
						nodeChanged(child);
					}
					parent = child;
				}
			}
		 }
	}

	private void updateNodeWithData(WorkflowResultTreeNode node,
			Path reference) {
		node.setState(ResultTreeNodeState.RESULT_REFERENCE);
		node.setReference(reference);
		nodeChanged(node);
	}

	private WorkflowResultTreeNode getChildAt(WorkflowResultTreeNode parent, int i) {
		int childCount = getChildCount(parent);
		if (childCount <= i) {
			for (int x = childCount; x <= i; x++) {
				insertNodeInto(new WorkflowResultTreeNode(WorkflowResultTreeNode.ResultTreeNodeState.RESULT_WAITING),
						parent, x);
			}
		}

		return (WorkflowResultTreeNode) parent.getChildAt(i);
	}

	private void changeState(WorkflowResultTreeNode node, ResultTreeNodeState state) {
		if (!node.isState(state)) {
			node.setState(state);
			nodeChanged(node);
		}

	}

	// Normally used for past workflow runs where data is obtained from provenance
	public void createTree(Path path,  WorkflowResultTreeNode parentNode){
		// If reference contains a list of data references
		if (DataBundles.isList(path)) {
			try {
				List<Path> list = DataBundles.getList(path);
				WorkflowResultTreeNode listNode = new WorkflowResultTreeNode(path, ResultTreeNodeState.RESULT_LIST); // list node
				insertNodeInto(listNode, parentNode, parentNode.getChildCount());
				for (Path ref : list) {
					createTree(ref, listNode);
				}
			} catch (IOException e) {
				logger .error("Error resolving data entity list " + path, e);
			}
		} else { // reference to single data or an error
			// insert data node
		    WorkflowResultTreeNode dataNode = new WorkflowResultTreeNode(path, ResultTreeNodeState.RESULT_REFERENCE); // data node
			insertNodeInto(dataNode, parentNode, parentNode.getChildCount());
		}
	}

}
