/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.views.monitor.progressreport;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.Updatable;
import org.apache.taverna.platform.report.WorkflowReport;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public class TableMonitorComponent extends JPanel implements Updatable {
	private Map<String, WorkflowRunProgressTreeTable> tableMap = new HashMap<>();
	private Map<String, WorkflowRunProgressTreeTableModel> tableModelMap = new HashMap<>();
	private WorkflowRunProgressTreeTable table;
	private WorkflowRunProgressTreeTableModel tableModel;
	private CardLayout cardLayout;

	private final RunService runService;
	private final SelectionManager selectionManager;
	private final ActivityIconManager activityIconManager;

	public TableMonitorComponent(RunService runService,
			SelectionManager selectionManager,
			ActivityIconManager activityIconManager) {
		this.runService = runService;
		this.selectionManager = selectionManager;
		this.activityIconManager = activityIconManager;

		cardLayout = new CardLayout();
		setLayout(cardLayout);
	}

	public void setWorkflowRun(String workflowRun) throws InvalidRunIdException {
		if (workflowRun != null) {
			if (!tableMap.containsKey(workflowRun))
				addWorkflowRun(workflowRun);
			table = tableMap.get(workflowRun);
			tableModel = tableModelMap.get(workflowRun);
			cardLayout.show(this, String.valueOf(table.hashCode()));
		}
	}

	public void addWorkflowRun(String workflowRun) throws InvalidRunIdException {
		WorkflowReport workflowReport = runService
				.getWorkflowReport(workflowRun);
		WorkflowRunProgressTreeTableModel newTableModel = new WorkflowRunProgressTreeTableModel(
				workflowReport);
		WorkflowRunProgressTreeTable newTable = new WorkflowRunProgressTreeTable(
				newTableModel, activityIconManager,
				selectionManager.getWorkflowRunSelectionModel(workflowRun));

		add(new JScrollPane(newTable), String.valueOf(newTable.hashCode()));
		tableMap.put(workflowRun, newTable);
		tableModelMap.put(workflowRun, newTableModel);
	}

	public void removeWorkflowRun(String workflowRun) {
		WorkflowRunProgressTreeTable removedTable = tableMap
				.remove(workflowRun);
		if (removedTable != null)
			remove(removedTable);
		tableModelMap.remove(workflowRun);
	}

	@Override
	public void update() {
		if (tableModel != null)
			tableModel.update();
	}
}
