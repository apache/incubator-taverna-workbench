
package org.apache.taverna.workbench.views.monitor;
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

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.SOUTH;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static org.apache.batik.ext.swing.GridBagConstants.EAST;
import static org.apache.batik.ext.swing.GridBagConstants.NONE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.apache.taverna.workbench.ui.Updatable;
import org.apache.taverna.workbench.views.monitor.graph.MonitorGraphComponent;
import org.apache.taverna.workbench.views.monitor.progressreport.TableMonitorComponent;

/**
 * Component that shows the progress of a workflow run, either through a graph or
 * a table shown in separate tabs. For previous runs, it pulls processor and workflow
 * statuses from provenance.
 *
 * Graph and table are interactive, where clicking on them triggers displaying of
 * workflow results or intermediate results in a separate component.
 *
 * It also contains buttons to pause/resume and stop a workflow run.
 *
 */
@SuppressWarnings({"serial","unused"})
public class MonitorViewComponent extends JPanel implements Updatable {
	private MonitorGraphComponent monitorGraph;
	private TableMonitorComponent tableMonitorComponent;

	private JTabbedPane tabbedPane;
	private JPanel buttonsPanel;

	public MonitorViewComponent() {
		super(new BorderLayout());
		tabbedPane = new JTabbedPane();
		buttonsPanel = new JPanel(new GridBagLayout());

//		buttonsPanel.add(new JLabel("Workflow status"));
//
//		buttonsPanel.add(new JButton("Pause"));
//		buttonsPanel.add(new JButton("Cancel"));
//		buttonsPanel.add(new JButton("Show results"));

		add(tabbedPane, CENTER);
		add(buttonsPanel, SOUTH);
	}

	public void setMonitorGraph(MonitorGraphComponent monitorGraph) {
		this.monitorGraph = monitorGraph;
		tabbedPane.add("Graph", monitorGraph);
	}

	public void setTableMonitorComponent(TableMonitorComponent tableMonitorComponent) {
		this.tableMonitorComponent = tableMonitorComponent;

		JScrollPane scrollPane = new JScrollPane(tableMonitorComponent,
				VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

		tabbedPane.add("Progress report", scrollPane);
	}

	public void addWorkflowRunStatusLabel(JLabel statusLabel){
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;

		gbc.fill = NONE;
		buttonsPanel.add(statusLabel, gbc);
	}

	public void addWorkflowPauseButton(JButton workflowRunPauseButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 1;
		gbc.gridy = 0;

		gbc.fill = NONE;
		gbc.weightx = 0.0;
		buttonsPanel.add(workflowRunPauseButton, gbc);
	}

	public void addWorkflowCancelButton(JButton workflowRunCancelButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 2;
		gbc.gridy = 0;

		gbc.fill = NONE;
		gbc.weightx = 0.0;
		buttonsPanel.add(workflowRunCancelButton, gbc);
	}

	public void addReloadWorkflowButton(JButton reloadWorkflowButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 3;
		gbc.gridy = 0;

		gbc.fill = NONE;
		gbc.weightx = 1.0;
		gbc.anchor = EAST;
		buttonsPanel.add(reloadWorkflowButton, gbc);
	}

	public void addIntermediateValuesButton(JButton intermediateValuesButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 4;
		gbc.gridy = 0;

		gbc.fill = NONE;
		gbc.weightx = 1.0;
		gbc.anchor = EAST;
		buttonsPanel.add(intermediateValuesButton, gbc);
	}

	public void addWorkflowResultsButton(JButton workflowResultsButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 5;
		gbc.gridy = 0;

		gbc.fill = NONE;
		gbc.weightx = 0.0;
		gbc.anchor = EAST;
		buttonsPanel.add(workflowResultsButton, gbc);
	}

	@Override
	public void update() {
		Component selectedComponent = tabbedPane.getSelectedComponent();
		if (selectedComponent instanceof Updatable)
			((Updatable) selectedComponent).update();
	}
}
