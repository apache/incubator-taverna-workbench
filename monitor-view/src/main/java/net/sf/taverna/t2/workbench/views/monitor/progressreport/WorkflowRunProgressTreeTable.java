package net.sf.taverna.t2.workbench.views.monitor.progressreport;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.treetable.JTreeTable;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.views.monitor.WorkflowObjectSelectionMessage;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class WorkflowRunProgressTreeTable extends JTreeTable implements
		Observable<WorkflowObjectSelectionMessage> {

	private static Logger logger = Logger.getLogger(WorkflowRunProgressTreeTable.class);

	private WorkflowRunProgressTreeTableModel treeTableModel;

	// Multicaster used to notify all interested parties that a selection of
	// row (and therefore a workflow object) has occurred on the table.
	private MultiCaster<WorkflowObjectSelectionMessage> multiCaster = new MultiCaster<WorkflowObjectSelectionMessage>(
			this);

	// Index of the last selected row in the WorkflowRunProgressTreeTable.
	// Need to keep track of it as selections on the table can occur from various
	// events - mouse click, key press or mouse click on the progress run graph.
	private int lastSelectedTableRow = -1;

	private Runnable refreshRunnable = null;

	public WorkflowRunProgressTreeTable(WorkflowRunProgressTreeTableModel treeTableModel,
			ActivityIconManager activityIconManager) {
		super(treeTableModel);

		this.treeTableModel = treeTableModel;

		final WorkflowRunProgressTreeTableModel model = treeTableModel;
		this.tree.setCellRenderer(new WorkflowRunProgressTreeCellRenderer(activityIconManager));
		this.tree.setEditable(false);
		this.tree.setExpandsSelectedPaths(true);
		this.tree.setDragEnabled(false);
		this.tree.setScrollsOnExpand(false);

		getTableHeader().setReorderingAllowed(false);
		// getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		refreshRunnable = new Runnable() {
			public void run() {
				model.refresh();
			}
		};

	}

	public void refreshTable() {
		try {
			if (!SwingUtilities.isEventDispatchThread()) {
				SwingUtilities.invokeAndWait(refreshRunnable);
			} else {
				refreshRunnable.run();
			}
		} catch (InterruptedException e) {
			logger.error("refresh of table interrupted", e);
		} catch (InvocationTargetException e) {
			logger.error("invocation of table refresh failed", e);
		}
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

	public void addObserver(Observer<WorkflowObjectSelectionMessage> observer) {
		multiCaster.addObserver(observer);
	}

	public void removeObserver(Observer<WorkflowObjectSelectionMessage> observer) {
		multiCaster.removeObserver(observer);
	}

	public void triggerWorkflowObjectSelectionEvent(Object workflowObject) {
		multiCaster.notify(new WorkflowObjectSelectionMessage(workflowObject));
	}

	public List<Observer<WorkflowObjectSelectionMessage>> getObservers() {
		return multiCaster.getObservers();
	}

	public void setLastSelectedTableRow(int lastSelectedTableRow) {
		this.lastSelectedTableRow = lastSelectedTableRow;
	}

	public int getLastSelectedTableRow() {
		return lastSelectedTableRow;
	}

	public void setSelectedRowForObject(Object workflowObject) {
		// Find the row for the object in the tree
		DefaultMutableTreeNode node = treeTableModel.getNodeForObject(workflowObject);
		TreeNode[] path = node.getPath();
		this.tree.scrollPathToVisible(new TreePath(path));
		int row = this.tree.getRowForPath(new TreePath(path));
		if (row > 0) {
			// Set selected row on the table
			this.setRowSelectionInterval(row, row);
		}
		lastSelectedTableRow = row;
	}

}
