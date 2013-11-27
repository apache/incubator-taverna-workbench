
/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.views.monitor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.apache.batik.ext.swing.GridBagConstants;

import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.monitor.graph.MonitorGraphComponent;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.TableMonitorComponent;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressTreeTable;

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
@SuppressWarnings("serial")
public class MonitorViewComponent extends JPanel implements Updatable {

	private MonitorGraphComponent monitorGraph;
	private TableMonitorComponent tableMonitorComponent;

	private JTabbedPane tabbedPane;
	private JPanel buttonsPanel;

	public MonitorViewComponent(){
		super();

		setLayout(new BorderLayout());

		tabbedPane = new JTabbedPane();

		buttonsPanel = new JPanel(new GridBagLayout());

//		buttonsPanel.add(new JLabel("Workflow status"));
//
//		buttonsPanel.add(new JButton("Pause"));
//		buttonsPanel.add(new JButton("Cancel"));
//		buttonsPanel.add(new JButton("Show results"));

		add(tabbedPane, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

	}

	public void setMonitorGraph(MonitorGraphComponent monitorGraph) {
		this.monitorGraph = monitorGraph;
		tabbedPane.add("Graph", monitorGraph);
	}

	public void setTableMonitorComponent(TableMonitorComponent tableMonitorComponent) {
		this.tableMonitorComponent = tableMonitorComponent;

		JScrollPane scrollPane = new JScrollPane(tableMonitorComponent,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		tabbedPane.add("Progress report", scrollPane);
	}

	public void addWorkflowRunStatusLabel(JLabel statusLabel){
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 0;
		gbc.gridy = 0;

		gbc.fill = GridBagConstants.NONE;
		buttonsPanel.add(statusLabel, gbc);
	}

	public void addWorkflowPauseButton(JButton workflowRunPauseButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 1;
		gbc.gridy = 0;

		gbc.fill = GridBagConstants.NONE;
		gbc.weightx = 0.0;
		buttonsPanel.add(workflowRunPauseButton, gbc);
	}

	public void addWorkflowCancelButton(JButton workflowRunCancelButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 2;
		gbc.gridy = 0;

		gbc.fill = GridBagConstants.NONE;
		gbc.weightx = 0.0;
		buttonsPanel.add(workflowRunCancelButton, gbc);
	}

	public void addReloadWorkflowButton(JButton reloadWorkflowButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 3;
		gbc.gridy = 0;

		gbc.fill = GridBagConstants.NONE;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstants.EAST;
		buttonsPanel.add(reloadWorkflowButton, gbc);

	}

	public void addIntermediateValuesButton(JButton intermediateValuesButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 4;
		gbc.gridy = 0;

		gbc.fill = GridBagConstants.NONE;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstants.EAST;
		buttonsPanel.add(intermediateValuesButton, gbc);
	}

	public void addWorkflowResultsButton(JButton workflowResultsButton) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = 5;
		gbc.gridy = 0;

		gbc.fill = GridBagConstants.NONE;
		gbc.weightx = 0.0;
		gbc.anchor = GridBagConstants.EAST;
		buttonsPanel.add(workflowResultsButton, gbc);
	}

	@Override
	public void update() {
		Component selectedComponent = tabbedPane.getSelectedComponent();
		if (selectedComponent instanceof Updatable) {
			Updatable updatable = (Updatable) selectedComponent;
			updatable.update();
		}
	}

}
