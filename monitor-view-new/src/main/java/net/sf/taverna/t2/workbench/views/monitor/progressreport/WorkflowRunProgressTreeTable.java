package net.sf.taverna.t2.workbench.views.monitor.progressreport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.treetable.JTreeTable;
import net.sf.taverna.t2.workbench.views.monitor.WorkflowObjectSelectionMessage;
import net.sf.taverna.t2.workflowmodel.Processor;

@SuppressWarnings("serial")
public class WorkflowRunProgressTreeTable extends JTreeTable implements Observable<WorkflowObjectSelectionMessage>{

	private WorkflowRunProgressTreeTableModel treeTableModel;

	// Multicaster used to notify all interested parties that a selection of 
	// row (and therefore a workflow object) has occurred on the table.
	private MultiCaster<WorkflowObjectSelectionMessage> multiCaster = new MultiCaster<WorkflowObjectSelectionMessage>(this);

	// Index of the last selected row in the WorkflowRunProgressTreeTable. 
	// Need to keep track of it as selections on the table can occur from various
	// events - mouse click, key press or mouse click on the progress run graph.
	private int lastSelectedTableRow = -1;

	public WorkflowRunProgressTreeTable(WorkflowRunProgressTreeTableModel treeTableModel) {
		super(treeTableModel);
		
		this.treeTableModel = treeTableModel;

		this.tree.setCellRenderer(new WorkflowRunProgressTreeCellRenderer());
		this.tree.setEditable(false);
		this.tree.setExpandsSelectedPaths(true);
		this.tree.setDragEnabled(false);
		this.tree.setScrollsOnExpand(false);
		
		getTableHeader().setReorderingAllowed(false);
		//getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
	}

	public void setWorkflowStatus(String status) {	
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		treeTableModel.setValueAt(status, (DefaultMutableTreeNode)treeTableModel.getRoot(), 1);
	}
	
	public void setWorkflowStartDate(Date date) {	
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		SimpleDateFormat sdf = new SimpleDateFormat(
		"yyyy-MM-dd HH:mm:ss");
		treeTableModel.setValueAt(sdf.format(date), (DefaultMutableTreeNode)treeTableModel.getRoot(), 2);
	}
	
	public void setWorkflowFinishDate(Date date) {
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		SimpleDateFormat sdf = new SimpleDateFormat(
		"yyyy-MM-dd HH:mm:ss");
		treeTableModel.setValueAt(sdf.format(date), (DefaultMutableTreeNode)treeTableModel.getRoot(), 3);
	}
	
	public void setStartDateForObject(Object object, Date date){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		treeTableModel.setStartDateForObject(object, date);
	}
	
	public void setFinishDateForObject(Object object, Date date){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		treeTableModel.setFinishDateForObject(object, date);
	}
	
	public void setNumberOfIterationsForObject(Object object, Integer iterations){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		treeTableModel.setNumberOfIterationsForObject(object, iterations);
	}
	
	public void setNumberOfFailedIterationsForObject(Object object, Integer failedIterations){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		treeTableModel.setNumberOfFailedIterationsForObject(object, failedIterations);
	}
	
	public void setNumberOfIterationsDoneSoFarForObject(Object object, Integer doneIterations){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		treeTableModel.setNumberOfIterationsDoneSoFarForObject(object, doneIterations);
	}

	public void setStatusForObject(Processor processor, String status) {
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		treeTableModel.setStatusForObject(processor, status);		
	}

	// Return object in the tree part of this JTreeTable that corresponds to
	// this row. It will either be a dataflow (tree root) or a processor.
	public Object getTreeObjectForRow(int row){
		TreePath path = tree.getPathForRow(row);
		if (path != null){
			return ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
		}
		else{
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
		int row = this.tree.getRowForPath(new TreePath(path));
		// Set selected row on the table
		this.setRowSelectionInterval(row, row);
		lastSelectedTableRow = row;		
	}

}
