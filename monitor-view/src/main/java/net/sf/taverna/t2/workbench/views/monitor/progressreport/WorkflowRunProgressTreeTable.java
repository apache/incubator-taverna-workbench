package net.sf.taverna.t2.workbench.views.monitor.progressreport;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.lang.ui.treetable.JTreeTable;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage;
import uk.org.taverna.platform.report.StatusReport;
import uk.org.taverna.platform.report.WorkflowReport;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.WorkflowPort;

@SuppressWarnings("serial")
public class WorkflowRunProgressTreeTable extends JTreeTable {

	private final WorkflowRunProgressTreeTableModel treeTableModel;
	private final DataflowSelectionModel selectionModel;

	private final DataflowSelectionObserver dataflowSelectionObserver;

	public WorkflowRunProgressTreeTable(WorkflowRunProgressTreeTableModel treeTableModel,
			ActivityIconManager activityIconManager, DataflowSelectionModel selectionModel) {
		super(treeTableModel);

		this.treeTableModel = treeTableModel;
		this.selectionModel = selectionModel;

		this.tree.setCellRenderer(new WorkflowRunProgressTreeCellRenderer(activityIconManager));
		this.tree.setEditable(false);
		this.tree.setExpandsSelectedPaths(true);
		this.tree.setDragEnabled(false);
		this.tree.setScrollsOnExpand(false);

		getTableHeader().setReorderingAllowed(false);
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().addListSelectionListener(new TableSelectionListener());

		dataflowSelectionObserver = new DataflowSelectionObserver();
		selectionModel.addObserver(dataflowSelectionObserver);
	}

	@Override
	protected void finalize() throws Throwable {
		selectionModel.removeObserver(dataflowSelectionObserver);
	}

	// Return object in the tree part of this JTreeTable that corresponds to
	// this row. It will either be a workflow (tree root) or a processor.
	public Object getTreeObjectForRow(int row) {
		TreePath path = tree.getPathForRow(row);
		if (path != null) {
			return ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		} else {
			return null;
		}
	}

	public void setSelectedRowForObject(Object workflowObject) {
		// Find the row for the object in the tree
		DefaultMutableTreeNode node = treeTableModel.getNodeForObject(workflowObject);
		if (node != null) {
			TreeNode[] path = node.getPath();
			tree.scrollPathToVisible(new TreePath(path));
			int row = tree.getRowForPath(new TreePath(path));
			if (row >= 0) {
				// Set selected row on the table
				setRowSelectionInterval(row, row);
			}
		}
	}

	private class DataflowSelectionObserver extends SwingAwareObserver<DataflowSelectionMessage> {
		@Override
		public void notifySwing(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) {
			for (Object selection : selectionModel.getSelection()) {
				if (selection instanceof Processor || selection instanceof Workflow) {
					setSelectedRowForObject(selection);
				} else if (selection instanceof WorkflowPort) {
					WorkflowPort workflowPort = (WorkflowPort) selection;
					setSelectedRowForObject(workflowPort.getParent());
				}
			}
		}
	}

	private class TableSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (!e.getValueIsAdjusting()) {
				int selectedRow = getSelectedRow();
				if (selectedRow >= 0) {
					Object selection = getTreeObjectForRow(selectedRow);
					if (selection instanceof StatusReport || selection instanceof WorkflowReport) {
						StatusReport<?,?> statusReport = (StatusReport<?,?>) selection;
						selectionModel.addSelection(statusReport.getSubject());
					}
				}
			}
		}
	}

}
