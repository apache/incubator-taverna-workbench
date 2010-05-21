package net.sf.taverna.t2.workbench.views.monitor.progressreport;

import java.util.Date;

import javax.swing.ListSelectionModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.lang.ui.treetable.JTreeTable;
import net.sf.taverna.t2.workflowmodel.Processor;

@SuppressWarnings("serial")
public class WorkflowRunProgressTreeTable extends JTreeTable {

	private WorkflowRunProgressTreeTableModel treeTableModel;

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
		int[] selectedRows = this.tree.getSelectionRows();
		int selectedRow = selectedRows == null ? -1 : selectedRows[0];
		treeTableModel.setValueAt(status, (DefaultMutableTreeNode)treeTableModel.getRoot(), 1);
		if (selectedRow != -1){
			this.tree.setSelectionRow(selectedRow);
		}
	}
	
	public void setWorkflowStartDate(Date date) {	
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		int[] selectedRows = this.tree.getSelectionRows();
		int selectedRow = selectedRows == null ? -1 : selectedRows[0];
		treeTableModel.setValueAt(date, (DefaultMutableTreeNode)treeTableModel.getRoot(), 2);
		if (selectedRow != -1){
			this.tree.setSelectionRow(selectedRow);
		}
	}
	
	public void setWorkflowFinishDate(Date date) {
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		int[] selectedRows = this.tree.getSelectionRows();
		int selectedRow = selectedRows == null ? -1 : selectedRows[0];
		treeTableModel.setValueAt(date, (DefaultMutableTreeNode)treeTableModel.getRoot(), 3);
		if (selectedRow != -1){
			this.tree.setSelectionRow(selectedRow);
		}
	}
	
	public void setStartDateForObject(Object object, Date date){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		int[] selectedRows = this.tree.getSelectionRows();
		int selectedRow = selectedRows == null ? -1 : selectedRows[0];
		treeTableModel.setStartDateForObject(object, date);
		if (selectedRow != -1){
			this.tree.setSelectionRow(selectedRow);
		}
	}
	
	public void setFinishDateForObject(Object object, Date date){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		int[] selectedRows = this.tree.getSelectionRows();
		int selectedRow = selectedRows == null ? -1 : selectedRows[0];
		treeTableModel.setFinishDateForObject(object, date);
		if (selectedRow != -1){
			this.tree.setSelectionRow(selectedRow);
		}
	}
	
	public void setNumberOfIterationsForObject(Object object, Integer iterations){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		int[] selectedRows = this.tree.getSelectionRows();
		int selectedRow = selectedRows == null ? -1 : selectedRows[0];
		treeTableModel.setNumberOfIterationsForObject(object, iterations);
		if (selectedRow != -1){
			this.tree.setSelectionRow(selectedRow);
		}
	}
	
	public void setNumberOfFailedIterationsForObject(Object object, Integer failedIterations){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		int[] selectedRows = this.tree.getSelectionRows();
		int selectedRow = selectedRows == null ? -1 : selectedRows[0];
		treeTableModel.setNumberOfFailedIterationsForObject(object, failedIterations);
		if (selectedRow != -1){
			this.tree.setSelectionRow(selectedRow);
		}
	}
	
	public void setNumberOfIterationsDoneSoFarForObject(Object object, Integer doneIterations){
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		int[] selectedRows = this.tree.getSelectionRows();
		int selectedRow = selectedRows == null ? -1 : selectedRows[0];
		treeTableModel.setNumberOfIterationsDoneSoFarForObject(object, doneIterations);
		if (selectedRow != -1){
			this.tree.setSelectionRow(selectedRow);
		}
	}

	public void setStatusForObject(Processor processor, String status) {
		// Setting value seems to deselect the table row 
		// so we have to remember it and pout it back
		int[] selectedRows = this.tree.getSelectionRows();
		int selectedRow = selectedRows == null ? -1 : selectedRows[0];
		treeTableModel.setStatusForObject(processor, status);		
		if (selectedRow != -1){
			this.tree.setSelectionRow(selectedRow);
		}
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
	
}
