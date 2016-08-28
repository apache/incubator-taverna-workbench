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

import static java.util.Collections.nCopies;
import static org.apache.taverna.workbench.views.monitor.progressreport.WorkflowRunProgressTreeTableModel.Column.values;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.taverna.lang.ui.treetable.AbstractTreeTableModel;
import org.apache.taverna.lang.ui.treetable.TreeTableModel;
import org.apache.taverna.platform.report.ActivityReport;
import org.apache.taverna.platform.report.Invocation;
import org.apache.taverna.platform.report.ProcessorReport;
import org.apache.taverna.platform.report.State;
import org.apache.taverna.platform.report.StatusReport;
import org.apache.taverna.platform.report.WorkflowReport;

/**
 * A TreeTableModel used to display the progress of a workfow run. The workflow
 * and its processors (some of which may be nested) are represented as a tree,
 * where their properties, such as status, start and finish times, number of
 * iterations, etc. are represented as table columns.
 * 
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * @author David Withers
 */
public class WorkflowRunProgressTreeTableModel extends AbstractTreeTableModel {
	public static final String NAME = "Name";
	public static final String STATUS = "Status";
	public static final String AVERAGE_ITERATION_TIME = "Average time per iteration";
	public static final String ITERATIONS = "Queued iterations";
	public static final String ITERATIONS_DONE = "Completed iterations";
	public static final String ITERATIONS_FAILED = "Iterations with errors";
	private static final String HOURS = "h";
	private static final String MINUTES = "m";
	private static final String SECONDS = "s";
	private static final String MILLISECONDS = "ms";
	
	public static String formatMilliseconds(long timeInMiliseconds) {
		double timeInSeconds;
		if (timeInMiliseconds < 1000)
			return timeInMiliseconds + " " + MILLISECONDS;
		NumberFormat numberFormat = NumberFormat.getNumberInstance();
		numberFormat.setMaximumFractionDigits(1);
		numberFormat.setMinimumFractionDigits(1);
		timeInSeconds = timeInMiliseconds / 1000.0;
		if (timeInSeconds < 60)
			return numberFormat.format(timeInSeconds) + " " + SECONDS;
		double timeInMinutes = timeInSeconds / 60.0;
		if (timeInMinutes < 60)
			return numberFormat.format(timeInMinutes) + " " + MINUTES;
		double timeInHours = timeInMinutes / 60.0;
		return numberFormat.format(timeInHours) + " " + HOURS;
	}

	
	public enum Column {
		NAME("Name", TreeTableModel.class), STATUS("Status"), ITERATIONS_QUEUED(
				"Queued iterations"), ITERATIONS_DONE("Iterations done"), ITERATIONS_FAILED(
				"Iterations w/errors"), AVERAGE_ITERATION_TIME(
				"Average time/iteration"), START_TIME(
				"First iteration started", Date.class), FINISH_TIME(
				"Last iteration ended", Date.class);

		private final String label;
		private final Class<?> columnClass;

		Column(String label) {
			this(label, String.class);
		}

		Column(String label, Class<?> columnClass) {
			this.label = label;
			this.columnClass = columnClass;
		}

		public Class<?> getColumnClass() {
			return columnClass;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	// Table data (maps workflow element nodes to column data associated with them)
	private final Map<DefaultMutableTreeNode, List<Object>> data = new HashMap<>();
	private final Map<Object, DefaultMutableTreeNode> nodeForObject = new HashMap<>();
	private final DefaultMutableTreeNode rootNode;

	public WorkflowRunProgressTreeTableModel(WorkflowReport workflowReport) {
		super(new DefaultMutableTreeNode(workflowReport));
		rootNode = (DefaultMutableTreeNode) this.getRoot();
		createTree(workflowReport, rootNode);
	}

	private void createTree(WorkflowReport workflowReport, DefaultMutableTreeNode root) {
		// If this is the root of the tree rather than a root of the nested sub-tree
		if (root.equals(rootNode)) {
			List<Object> columnData = new ArrayList<>(nCopies(values().length,
					null));
			setColumnValues(workflowReport, columnData);
			nodeForObject.put(workflowReport, root);
			nodeForObject.put(workflowReport.getSubject(), root);
			data.put(root, columnData);
		}
		// One row for each processor
		for (ProcessorReport processorReport : workflowReport.getProcessorReports()) {
			List<Object> columnData = new ArrayList<>(nCopies(values().length,
					null));
			DefaultMutableTreeNode processorNode = new DefaultMutableTreeNode(
					processorReport);
			setColumnValues(processorReport, columnData);
			nodeForObject.put(processorReport, processorNode);
			nodeForObject.put(processorReport.getSubject(), processorNode);
			data.put(processorNode, columnData);
			root.add(processorNode);

			Set<ActivityReport> activityReports = processorReport.getActivityReports();
			if (activityReports.size() == 1) {
				WorkflowReport nestedWorkflowReport = activityReports.iterator().next()
						.getNestedWorkflowReport();
				if (nestedWorkflowReport != null)
					// create sub-tree
					createTree(nestedWorkflowReport, processorNode);
			}
		}
	}

	public DefaultMutableTreeNode getNodeForObject(Object workflowObject) {
		return nodeForObject.get(workflowObject);
	}

	
	public void setColumnValues(StatusReport<?, ?> report, List<Object> columns) {
		if (report instanceof WorkflowReport) {
			WorkflowReport workflowReport = (WorkflowReport) report;

			State state = workflowReport.getState();
			Date startTime = workflowReport.getStartedDate();
			Date finishTime = workflowReport.getCompletedDate();

			columns.set(Column.NAME.ordinal(), workflowReport.getSubject().getName());
			columns.set(Column.STATUS.ordinal(), state);
			columns.set(Column.ITERATIONS_DONE.ordinal(), "-");
			columns.set(Column.ITERATIONS_FAILED.ordinal(), "-");
			columns.set(Column.ITERATIONS_QUEUED.ordinal(), "-");
			columns.set(Column.START_TIME.ordinal(), startTime);
			columns.set(Column.FINISH_TIME.ordinal(), finishTime);
			if (startTime != null && finishTime != null)
				columns.set(Column.AVERAGE_ITERATION_TIME.ordinal(),
						formatMilliseconds(finishTime.getTime() - finishTime.getTime()));
			else
				columns.set(Column.AVERAGE_ITERATION_TIME.ordinal(), "-");
		} else if (report instanceof ProcessorReport) {
			ProcessorReport processorReport = (ProcessorReport) report;

			State state = processorReport.getState();
			SortedSet<Invocation> invocations = processorReport
					.getInvocations();

			columns.set(Column.NAME.ordinal(), processorReport.getSubject()
					.getName());
			columns.set(Column.STATUS.ordinal(), state);
			columns.set(Column.ITERATIONS_QUEUED.ordinal(),
					processorReport.getJobsQueued());
			columns.set(Column.ITERATIONS_DONE.ordinal(),
					processorReport.getJobsCompleted());
			columns.set(Column.ITERATIONS_FAILED.ordinal(),
					processorReport.getJobsCompletedWithErrors());

			if (invocations.isEmpty()) {
				columns.set(Column.START_TIME.ordinal(), null);
				columns.set(Column.FINISH_TIME.ordinal(), null);
				columns.set(Column.AVERAGE_ITERATION_TIME.ordinal(), null); // iteration
			} else {
				Date earliestStartTime = invocations.first().getStartedDate();
				Date latestFinishTime = invocations.first().getCompletedDate();
				long totalInvocationTime = 0;
				int finishedInvocations = 0;

				for (Invocation invocation : invocations) {
					// Get the earliest start time of all invocations
					Date startTime = invocation.getStartedDate();
					if (startTime != null) {
						if (startTime.before(earliestStartTime))
							earliestStartTime = startTime;
						// Get the latest finish time of all invocations
						Date finishTime = invocation.getCompletedDate();
						if (finishTime != null) {
							if (finishTime.after(latestFinishTime)) {
								latestFinishTime = finishTime;
								totalInvocationTime += finishTime.getTime() - startTime.getTime();
							}
							finishedInvocations++;
						}
					}
				}

				columns.set(Column.START_TIME.ordinal(), earliestStartTime);
				columns.set(Column.FINISH_TIME.ordinal(), latestFinishTime);
				if (finishedInvocations > 0) {
					long averageTime = totalInvocationTime / finishedInvocations;
					columns.set(Column.AVERAGE_ITERATION_TIME.ordinal(),
							formatMilliseconds(averageTime));
				} else
					columns.set(Column.AVERAGE_ITERATION_TIME.ordinal(), "-");
			}
		}
	}

	public void update() {
		update(rootNode);
		fireTreeNodesChanged(rootNode, rootNode.getPath(), null, null);
	}

	private void update(DefaultMutableTreeNode node) {
		setColumnValues((StatusReport<?, ?>) node.getUserObject(), data.get(node));
		for (int i = 0; i < node.getChildCount(); i++)
			update((DefaultMutableTreeNode) node.getChildAt(i));
	}

	//
	// The TreeModel interface
	//

	@Override
	public int getChildCount(Object node) {
		return ((TreeNode) node).getChildCount();
	}

	@Override
	public Object getChild(Object node, int i) {
		return ((TreeNode) node).getChildAt(i);
	}

	//
	// The TreeTableNode interface.
	//

	@Override
	public int getColumnCount() {
		return values().length;
	}

	@Override
	public String getColumnName(int column) {
		return values()[column].getLabel();
	}

	@Override
	public Class<?> getColumnClass(int column) {
		return values()[column].getColumnClass();
	}

	public Object getValueAt(Object node, Column column) {
		return getValueAt(node, column.ordinal());
	}

	@Override
	public Object getValueAt(Object node, int column) {
		List<Object> columnValues = data.get(node);
		if (columnValues == null)
			return null;
		return columnValues.get(column);
	}
}