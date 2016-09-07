package org.apache.taverna.workbench.views.monitor.graph;

import static org.apache.taverna.workbench.icons.WorkbenchIcons.closeIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.tickIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.workingIcon;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.apache.taverna.workbench.models.graph.GraphController;
import org.apache.taverna.workbench.ui.Updatable;
import org.apache.taverna.platform.report.ActivityReport;
import org.apache.taverna.platform.report.ProcessorReport;
import org.apache.taverna.platform.report.State;
import org.apache.taverna.platform.report.WorkflowReport;

/**
 * An implementation of the Updatable interface that updates a Graph.
 * 
 * @author David Withers
 */
public class GraphMonitor implements Updatable {
	private static final String STATUS_RUNNING = "Running";
	private static final String STATUS_FINISHED = "Finished";
	private static final String STATUS_CANCELLED = "Cancelled";

	/**
	 * Workflow run status label - we can only tell of workflow is running or is
	 * finished from inside this monitor. If workfow run is stopped or paused -
	 * this will be updated form the run-ui.
	 */
	private JLabel workflowRunStatusLabel;
	/**
	 * Similarly to {@link #workflowRunStatusLabel} - we disable the pause anc
	 * cancel buttons when workflow runs is finished
	 */
	private JButton workflowRunPauseButton;
	private JButton workflowRunCancelButton;
	private GraphController graphController;
	private Set<GraphMonitorNode> processors = new HashSet<>();
	private final WorkflowReport workflowReport;

	public GraphMonitor(GraphController graphController,
			WorkflowReport workflowReport) {
		this.graphController = graphController;
		this.workflowReport = workflowReport;
		createMonitorNodes(workflowReport.getSubject().getName(),
				workflowReport);
		redraw();
	}

	private void createMonitorNodes(String id, WorkflowReport workflowReport) {
		for (ProcessorReport processorReport : workflowReport
				.getProcessorReports()) {
			String processorId = id + processorReport.getSubject().getName();
			processors.add(new GraphMonitorNode(processorId, processorReport,
					graphController));
			for (ActivityReport activityReport : processorReport
					.getActivityReports()) {
				WorkflowReport nestedWorkflowReport = activityReport
						.getNestedWorkflowReport();
				if (nestedWorkflowReport != null)
					createMonitorNodes(processorId, nestedWorkflowReport);
			}
		}
	}

	public void redraw() {
		for (GraphMonitorNode node : processors)
			node.redraw();
	}

	@Override
	public void update() {
		for (GraphMonitorNode node : processors)
			node.update();
		// updateState();
	}

	@SuppressWarnings("unused")
	private void updateState() {
		State state = workflowReport.getState();
		switch (state) {
		case COMPLETED:
		case FAILED:
			workflowRunStatusLabel.setText(STATUS_FINISHED);
			workflowRunStatusLabel.setIcon(tickIcon);
			workflowRunPauseButton.setEnabled(false);
			workflowRunCancelButton.setEnabled(false);
			break;
		case CANCELLED:
			workflowRunStatusLabel.setText(STATUS_CANCELLED);
			workflowRunStatusLabel.setIcon(closeIcon);
			workflowRunPauseButton.setEnabled(false);
			workflowRunCancelButton.setEnabled(false);
			break;
		case RUNNING:
			workflowRunStatusLabel.setText(STATUS_RUNNING);
			workflowRunStatusLabel.setIcon(workingIcon);
		default:
			break;
		}
	}

	// Set the status label that will be updated from this monitor
	public void setWorkflowRunStatusLabel(JLabel workflowRunStatusLabel) {
		this.workflowRunStatusLabel = workflowRunStatusLabel;
	}

	public void setWorkflowRunPauseButton(JButton workflowRunPauseButton) {
		this.workflowRunPauseButton = workflowRunPauseButton;
	}

	public void setWorkflowRunCancelButton(JButton workflowRunCancelButton) {
		this.workflowRunCancelButton = workflowRunCancelButton;
	}
}
