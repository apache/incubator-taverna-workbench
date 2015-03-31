/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.ui.perspectives.results;

import static java.awt.BorderLayout.CENTER;
import static java.awt.BorderLayout.NORTH;
import static java.awt.Font.BOLD;
import static java.lang.Math.round;
import static javax.swing.JSplitPane.VERTICAL_SPLIT;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import org.apache.taverna.lang.ui.tabselector.Tab;
import org.apache.taverna.renderers.RendererRegistry;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.selection.events.SelectionManagerEvent;
import org.apache.taverna.workbench.selection.events.WorkflowRunSelectionEvent;
import org.apache.taverna.workbench.ui.Updatable;
import org.apache.taverna.workbench.views.monitor.graph.MonitorGraphComponent;
import org.apache.taverna.workbench.views.monitor.progressreport.TableMonitorComponent;
import org.apache.taverna.workbench.views.results.ResultsComponent;
import org.apache.taverna.workbench.views.results.saveactions.SaveAllResultsSPI;
import org.apache.taverna.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.log4j.Logger;
import org.osgi.service.event.Event;

import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ResultsPerspectiveComponent extends JPanel implements Updatable {
	private static final Logger logger = Logger.getLogger(ResultsPerspectiveComponent.class);
	private static final String NO_RUNS_MESSAGE = "No workflow runs";
	private static final String RUNS_SELECTED = "RUNS_SELECTED";
	private static final String NO_RUNS_SELECTED = "NO_RUNS_SELECTED";

	private final RunService runService;
	private final SelectionManager selectionManager;
	@SuppressWarnings("unused")
	private final ColourManager colourManager;
	@SuppressWarnings("unused")
	private final ActivityIconManager activityIconManager;
	@SuppressWarnings("unused")
	private final WorkbenchConfiguration workbenchConfiguration;

	private List<Updatable> updatables = new ArrayList<>();
	private CardLayout cardLayout;
	private SelectionManagerObserver selectionManagerObserver;
	private MonitorGraphComponent monitorGraphComponent;
	private TableMonitorComponent tableMonitorComponent;
	private ResultsComponent resultsComponent;
	private RunSelectorComponent runSelectorComponent;

	public ResultsPerspectiveComponent(RunService runService, SelectionManager selectionManager,
			ColourManager colourManager, ActivityIconManager activityIconManager,
			WorkbenchConfiguration workbenchConfiguration, RendererRegistry rendererRegistry,
			List<SaveAllResultsSPI> saveAllResultsSPIs,
			List<SaveIndividualResultSPI> saveIndividualResultSPIs, File runStore) {
		this.runService = runService;
		this.selectionManager = selectionManager;
		this.colourManager = colourManager;
		this.activityIconManager = activityIconManager;
		this.workbenchConfiguration = workbenchConfiguration;

		cardLayout = new CardLayout();
		setLayout(cardLayout);

		JLabel noRunsMessage = new JLabel(NO_RUNS_MESSAGE, JLabel.CENTER);
		Font font = noRunsMessage.getFont();
		if (font != null) {
			font = font.deriveFont(round(font.getSize() * 1.5))
					.deriveFont(BOLD);
			noRunsMessage.setFont(font);
		}
		JPanel noRunsPanel = new JPanel(new BorderLayout());
		noRunsPanel.add(noRunsMessage, CENTER);
		add(noRunsPanel, NO_RUNS_SELECTED);

		monitorGraphComponent = new MonitorGraphComponent(runService,
				colourManager, workbenchConfiguration, selectionManager);
		tableMonitorComponent = new TableMonitorComponent(runService,
				selectionManager, activityIconManager);

		resultsComponent = new ResultsComponent(runService, selectionManager,
				rendererRegistry, saveAllResultsSPIs, saveIndividualResultSPIs);

		updatables.add(monitorGraphComponent);
		updatables.add(tableMonitorComponent);
		updatables.add(resultsComponent);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Graph", monitorGraphComponent);
		tabbedPane.add("Progress report", tableMonitorComponent);

		JSplitPane splitPane = new JSplitPane(VERTICAL_SPLIT);
		splitPane.setBorder(null);
		splitPane.setLeftComponent(tabbedPane);
		splitPane.setRightComponent(resultsComponent);
		splitPane.setDividerLocation(200);

		runSelectorComponent = new RunSelectorComponent(runService,
				selectionManager, runStore);

		JPanel runsPanel = new JPanel(new BorderLayout());
		runsPanel.add(runSelectorComponent, NORTH);
		runsPanel.add(splitPane, CENTER);
		add(runsPanel, RUNS_SELECTED);

		selectionManagerObserver = new SelectionManagerObserver();
		selectionManager.addObserver(selectionManagerObserver);
	}

	@Override
	protected void finalize() throws Throwable {
		selectionManager.removeObserver(selectionManagerObserver);
	}

	@Override
	public void update() {
		for (Updatable updatable : updatables)
			updatable.update();
	}

	private class SelectionManagerObserver extends
			SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (!(message instanceof WorkflowRunSelectionEvent)) return;
			String workflowRun = ((WorkflowRunSelectionEvent) message)
					.getSelectedWorkflowRun();
			if (workflowRun == null) {
				cardLayout.show(ResultsPerspectiveComponent.this,
						NO_RUNS_SELECTED);
				return;
			}

			cardLayout.show(ResultsPerspectiveComponent.this, RUNS_SELECTED);
			runSelectorComponent.selectObject(workflowRun);
			try {
				monitorGraphComponent.setWorkflowRun(workflowRun);
				tableMonitorComponent.setWorkflowRun(workflowRun);
			} catch (InvalidRunIdException e) {
				logger.warn(
						"Failed to create monitor components for workflow run "
								+ workflowRun, e);
			}
		}
	}

	public void handleEvent(Event event) {
		String workflowRun = event.getProperty("RUN_ID").toString();
		switch (event.getTopic()) {
		case RunService.RUN_CLOSED:
		case RunService.RUN_DELETED:
			runSelectorComponent.removeObject(workflowRun);
			monitorGraphComponent.removeWorkflowRun(workflowRun);
			tableMonitorComponent.removeWorkflowRun(workflowRun);
			resultsComponent.removeWorkflowRun(workflowRun);
			if (selectionManager.getSelectedWorkflowRun().equals(workflowRun)) {
				List<String> runs = runService.getRuns();
				if (runs.isEmpty())
					selectionManager.setSelectedWorkflowRun(null);
				else
					selectionManager.setSelectedWorkflowRun(runs.get(0));
			}
			break;
		case RunService.RUN_CREATED:
		case RunService.RUN_OPENED:
			selectionManager.setSelectedWorkflowRun(workflowRun);
			break;
		case RunService.RUN_STOPPED:
		case RunService.RUN_PAUSED:
		case RunService.RUN_STARTED:
		case RunService.RUN_RESUMED:
			Tab<String> tab = runSelectorComponent.getTab(workflowRun);
			if (tab instanceof RunTab) {
				RunTab runTab = (RunTab) tab;
				runTab.updateTabIcon();
			}
			break;
		}
	}
}
