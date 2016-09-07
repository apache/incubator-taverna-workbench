package org.apache.taverna.workbench.views.monitor.progressreport;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.lang.ui.treetable.JTreeTable;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.selection.DataflowSelectionModel;
import org.apache.taverna.workbench.selection.events.DataflowSelectionMessage;
import org.apache.taverna.platform.report.StatusReport;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.port.WorkflowPort;

@SuppressWarnings("serial")
public class WorkflowRunProgressTreeTable extends JTreeTable {
	private final WorkflowRunProgressTreeTableModel treeTableModel;
	private final DataflowSelectionModel selectionModel;
	private final DataflowSelectionObserver dataflowSelectionObserver;

	public WorkflowRunProgressTreeTable(
			WorkflowRunProgressTreeTableModel treeTableModel,
			ActivityIconManager activityIconManager,
			DataflowSelectionModel selectionModel) {
		super(treeTableModel);

		this.treeTableModel = treeTableModel;
		this.selectionModel = selectionModel;

		this.tree.setCellRenderer(new WorkflowRunProgressTreeCellRenderer(
				activityIconManager));
		this.tree.setEditable(false);
		this.tree.setExpandsSelectedPaths(true);
		this.tree.setDragEnabled(false);
		this.tree.setScrollsOnExpand(false);

		getTableHeader().setReorderingAllowed(false);
		getSelectionModel().setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().addListSelectionListener(
				new TableSelectionListener());

		dataflowSelectionObserver = new DataflowSelectionObserver();
		selectionModel.addObserver(dataflowSelectionObserver);
	}

	@Override
	protected void finalize() throws Throwable {
		selectionModel.removeObserver(dataflowSelectionObserver);
	}

	/**
	 * Return object in the tree part of this JTreeTable that corresponds to
	 * this row. It will either be a workflow (tree root) or a processor.
	 */
	public Object getTreeObjectForRow(int row) {
		TreePath path = tree.getPathForRow(row);
		if (path == null)
			return null;
		return ((DefaultMutableTreeNode) path.getLastPathComponent())
				.getUserObject();
	}

	public void setSelectedRowForObject(Object workflowObject) {
		// Find the row for the object in the tree
		DefaultMutableTreeNode node = treeTableModel.getNodeForObject(workflowObject);
		if (node != null) {
			TreeNode[] path = node.getPath();
			tree.scrollPathToVisible(new TreePath(path));
			int row = tree.getRowForPath(new TreePath(path));
			if (row >= 0)
				// Set selected row on the table
				setRowSelectionInterval(row, row);
		}
	}

	private class DataflowSelectionObserver extends SwingAwareObserver<DataflowSelectionMessage> {
		@Override
		public void notifySwing(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) {
			for (Object selection : selectionModel.getSelection()) {
				if (selection instanceof Processor
						|| selection instanceof Workflow)
					setSelectedRowForObject(selection);
				else if (selection instanceof WorkflowPort)
					setSelectedRowForObject(((WorkflowPort) selection)
							.getParent());
			}
		}
	}

	private class TableSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			int selectedRow = getSelectedRow();
			if (selectedRow < 0)
				return;
			Object selection = getTreeObjectForRow(selectedRow);
			if (selection instanceof StatusReport)
				selectionModel.addSelection(((StatusReport<?, ?>) selection)
						.getSubject());
		}
	}
}
