/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.ui.perspectives.results;

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
import net.sf.taverna.t2.lang.ui.tabselector.Tab;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowRunSelectionEvent;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.monitor.graph.MonitorGraphComponent;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.TableMonitorComponent;
import net.sf.taverna.t2.workbench.views.results.ResultsComponent;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

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
