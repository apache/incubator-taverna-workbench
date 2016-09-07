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
