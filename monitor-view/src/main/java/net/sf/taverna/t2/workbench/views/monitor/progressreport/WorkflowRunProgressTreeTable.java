package net.sf.taverna.t2.workbench.views.monitor.progressreport;

import static net.sf.taverna.t2.workbench.views.results.processor.ProcessorResultsComponent.formatMilliseconds;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.treetable.JTreeTable;
import net.sf.taverna.t2.workbench.views.monitor.WorkflowObjectSelectionMessage;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressTreeTableModel.Column;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class WorkflowRunProgressTreeTable extends JTreeTable implements Observable<WorkflowObjectSelectionMessage>{

	private static Logger logger = Logger
			.getLogger(WorkflowRunProgressTreeTable.class);

	private WorkflowRunProgressTreeTableModel treeTableModel;

	// Multicaster used to notify all interested parties that a selection of 
	// row (and therefore a workflow object) has occurred on the table.
	private MultiCaster<WorkflowObjectSelectionMessage> multiCaster = new MultiCaster<WorkflowObjectSelectionMessage>(this);

	// Index of the last selected row in the WorkflowRunProgressTreeTable. 
	// Need to keep track of it as selections on the table can occur from various
	// events - mouse click, key press or mouse click on the progress run graph.
	private int lastSelectedTableRow = -1;

	private Runnable refreshRunnable = null;
	public WorkflowRunProgressTreeTable(WorkflowRunProgressTreeTableModel treeTableModel) {
		super(treeTableModel);
		
		this.treeTableModel = treeTableModel;

		final WorkflowRunProgressTreeTableModel model = treeTableModel;
		this.tree.setCellRenderer(new WorkflowRunProgressTreeCellRenderer());
		this.tree.setEditable(false);
		this.tree.setExpandsSelectedPaths(true);
		this.tree.setDragEnabled(false);
		this.tree.setScrollsOnExpand(false);
		
		getTableHeader().setReorderingAllowed(false);
		//getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
	    }
	    catch (InterruptedException e) {
		logger.error("refresh of table interrupted", e);
	    }
	    catch (InvocationTargetException e) {
		logger.error("invocation of table refresh failed", e);
	    }
	}

	public void setWorkflowStatus(String status) {	
		treeTableModel.setValueAt(status, (DefaultMutableTreeNode)treeTableModel.getRoot(), Column.STATUS);
	}
	
	public void setWorkflowStartDate(Date date) {	
		treeTableModel.setValueAt(date,
				(DefaultMutableTreeNode) treeTableModel.getRoot(),
				Column.START_TIME);
	}
	
	public void setWorkflowFinishDate(Date date) {
		treeTableModel.setValueAt(date,
				(DefaultMutableTreeNode) treeTableModel.getRoot(),
				Column.FINISH_TIME);
	}
	
	public void setWorkflowInvocationTime(long averageInvocationTime) {
		treeTableModel.setValueAt(formatMilliseconds(averageInvocationTime),
				(DefaultMutableTreeNode) treeTableModel.getRoot(),
				Column.AVERAGE_ITERATION_TIME);
	}
	
	public void setProcessorStartDate(Processor processor, Date date){
		treeTableModel.setProcessorStartDate(processor, date);
	}
	
	public void setProcessorFinishDate(Processor processor, Date date){
		treeTableModel.setProcessorFinishDate(processor, date);
	}

	public void setProcessorAverageInvocationTime(Processor processor, long averageInvocationTime) {
		treeTableModel.setProcessorAverageInvocationTime(processor, averageInvocationTime);		
	}
	
	public void setProcessorNumberOfQueuedIterations(Processor processor, Integer iterations){
		treeTableModel.setProcessorNumberOfQueuedIterations(processor, iterations);
	}
	
	public void setProcessorNumberOfFailedIterations(Processor processor, Integer failedIterations){
		treeTableModel.setProcessorNumberOfFailedIterations(processor, failedIterations);
	}
	
	public void setProcessorNumberOfIterationsDoneSoFar(Processor processor, Integer doneIterations){
		treeTableModel.setProcessorNumberOfIterationsDoneSoFar(processor, doneIterations);
	}

	public void setProcessorStatus(Processor processor, String status) {
		treeTableModel.setProcessorStatus(processor, status);		
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
		this.tree.scrollPathToVisible(new TreePath(path));
		int row = this.tree.getRowForPath(new TreePath(path));
		if (row > 0) {
			// Set selected row on the table
			this.setRowSelectionInterval(row, row);
		}
		lastSelectedTableRow = row;		
	}

	public Date getProcessorStartDate(Processor processor) {
		return treeTableModel.getProcessorStartDate(processor);
	}

	public Date getWorkflowStartDate() {		
		return (Date) treeTableModel.getValueAt((DefaultMutableTreeNode)treeTableModel.getRoot(), Column.START_TIME);		
	}

	// Update the progress table to show workflow and processors as cancelled
	public void setWorkflowCancelled() {

		setWorkflowStatus(WorkflowRunProgressTreeTableModel.STATUS_CANCELLED);
		for (Processor processor : treeTableModel.getProcessors()){
			if (!treeTableModel.getProcessorStatus(processor).equals(WorkflowRunProgressTreeTableModel.STATUS_FINISHED)){
				setProcessorStatus(processor, WorkflowRunProgressTreeTableModel.STATUS_CANCELLED);
			}
		}
	}
	
	// Update the progress table to show workflow and currently running processors as paused.
	public void setWorkflowPaused() {

		setWorkflowStatus(WorkflowRunProgressTreeTableModel.STATUS_PAUSED);
		for (Processor processor : treeTableModel.getProcessors()){
			if (treeTableModel.getProcessorStatus(processor).equals(WorkflowRunProgressTreeTableModel.STATUS_RUNNING) ||
					treeTableModel.getProcessorStatus(processor).equals(WorkflowRunProgressTreeTableModel.STATUS_PENDING)){
				setProcessorStatus(processor, WorkflowRunProgressTreeTableModel.STATUS_PAUSED);
			}
		}
	}

	// Update the progress table to show workflow and currently paused processors as running.
	public void setWorkflowResumed() {

		setWorkflowStatus(WorkflowRunProgressTreeTableModel.STATUS_RUNNING);
		for (Processor processor : treeTableModel.getProcessors()){
			if (treeTableModel.getProcessorStatus(processor).equals(WorkflowRunProgressTreeTableModel.STATUS_PAUSED)){
				if (treeTableModel.getProcessorNumberOfIterationsDoneSoFar(processor) == 0 &&
						treeTableModel.getProcessorNumberOfQueuedIterations(processor) == 0){
					setProcessorStatus(processor, WorkflowRunProgressTreeTableModel.STATUS_PENDING);
				}
				else{
					setProcessorStatus(processor, WorkflowRunProgressTreeTableModel.STATUS_RUNNING);
				}
			}
		}
	}
}
