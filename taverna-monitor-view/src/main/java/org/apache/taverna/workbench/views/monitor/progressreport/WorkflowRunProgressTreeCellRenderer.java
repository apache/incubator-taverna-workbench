/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.views.monitor.progressreport;

import static org.apache.taverna.workbench.icons.WorkbenchIcons.workflowExplorerIcon;

import java.awt.Component;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.platform.report.ActivityReport;
import org.apache.taverna.platform.report.ProcessorReport;
import org.apache.taverna.platform.report.WorkflowReport;

/**
 * Cell renderer for Workflow Explorer tree.
 *
 * @author Alex Nenadic
 */
@SuppressWarnings("serial")
public class WorkflowRunProgressTreeCellRenderer extends DefaultTreeCellRenderer {
	private ActivityIconManager activityIconManager;

	public WorkflowRunProgressTreeCellRenderer(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);

		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();

		WorkflowRunProgressTreeCellRenderer renderer = (WorkflowRunProgressTreeCellRenderer) result;

		if (userObject instanceof WorkflowReport) // the root node
			renderWorkflowReport(renderer, (WorkflowReport) userObject);
		else if (userObject instanceof ProcessorReport)
			renderProcessorReport(renderer, (ProcessorReport) userObject);

		return result;
	}

	private void renderWorkflowReport(
			WorkflowRunProgressTreeCellRenderer renderer,
			WorkflowReport workflowReport) {
		renderer.setIcon(workflowExplorerIcon);
		renderer.setText(workflowReport.getSubject().getName());
	}

	private void renderProcessorReport(WorkflowRunProgressTreeCellRenderer renderer,
			ProcessorReport processorReport) {
		/*
		 * Get the activity associated with the processor - currently only
		 * one gets displayed
		 */
		Set<ActivityReport> activityReports = processorReport
				.getActivityReports();
		String text = processorReport.getSubject().getName();
		if (!activityReports.isEmpty()) {
			ActivityReport activityReport = activityReports.iterator()
					.next();
			Icon icon = activityIconManager.iconForActivity(activityReport
					.getSubject());
			if (icon != null)
				renderer.setIcon(icon);
		}
		renderer.setText(text);
	}
}
