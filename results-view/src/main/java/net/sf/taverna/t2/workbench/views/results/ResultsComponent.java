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
package net.sf.taverna.t2.workbench.views.results;

import java.awt.CardLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowRunSelectionEvent;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.results.processor.ProcessorResultsComponent;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;
import net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultsComponent;

import org.apache.log4j.Logger;

import uk.org.taverna.platform.report.WorkflowReport;
import uk.org.taverna.platform.run.api.InvalidRunIdException;
import uk.org.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ResultsComponent extends JPanel implements Updatable {

	private static Logger logger = Logger.getLogger(ResultsComponent.class);

	private final RunService runService;
	private final SelectionManager selectionManager;
	private final RendererRegistry rendererRegistry;
	private final List<SaveAllResultsSPI> saveAllResultsSPIs;
	private final List<SaveIndividualResultSPI> saveIndividualResultSPIs;

	private CardLayout cardLayout = new CardLayout();

	private WorkflowResultsComponent workflowResultsComponent;

	private Map<String, WorkflowResultsComponent> workflowResultsComponents = new HashMap<>();
	private Map<String, ProcessorResultsComponent> processorResultsComponents = new HashMap<>();

	private SelectionManagerObserver selectionManagerObserver = new SelectionManagerObserver();

	public ResultsComponent(RunService runService, SelectionManager selectionManager, RendererRegistry rendererRegistry,
			List<SaveAllResultsSPI> saveAllResultsSPIs,
			List<SaveIndividualResultSPI> saveIndividualResultSPIs) {
		this.runService = runService;
		this.selectionManager = selectionManager;
		this.rendererRegistry = rendererRegistry;
		this.saveAllResultsSPIs = saveAllResultsSPIs;
		this.saveIndividualResultSPIs = saveIndividualResultSPIs;

		setLayout(cardLayout);

		selectionManager.addObserver(selectionManagerObserver);
	}

	@Override
	protected void finalize() throws Throwable {
		selectionManager.removeObserver(selectionManagerObserver);
	}

	@Override
	public void update() {
		if (workflowResultsComponent != null) {
			workflowResultsComponent.update();
		}
	}

	public void setWorkflowRun(String workflowRun) throws InvalidRunIdException {
		if (workflowRun != null) {
			if (!workflowResultsComponents.containsKey(workflowRun)) {
				addWorkflowRun(workflowRun);
			}
			workflowResultsComponent = workflowResultsComponents.get(workflowRun);
			cardLayout.show(this, workflowRun);
		}
	}

	public void addWorkflowRun(String workflowRun) throws InvalidRunIdException {
		WorkflowReport workflowReport = runService.getWorkflowReport(workflowRun);
		WorkflowResultsComponent workflowResultsComponent = new WorkflowResultsComponent(workflowReport,
				rendererRegistry, saveAllResultsSPIs, saveIndividualResultSPIs);
		add(workflowResultsComponent, workflowRun);
		workflowResultsComponents.put(workflowRun, workflowResultsComponent);
	}

	public void removeWorkflowRun(String workflowRun) {
		WorkflowResultsComponent removedWorkflowResultsComponent = workflowResultsComponents.remove(workflowRun);
		if (removedWorkflowResultsComponent != null) {
			remove(removedWorkflowResultsComponent);
		}
	}

	private class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			if (message instanceof WorkflowRunSelectionEvent) {
				try {
					setWorkflowRun(((WorkflowRunSelectionEvent) message).getSelectedWorkflowRun());
				} catch (InvalidRunIdException e) {
					logger.warn("Invalid workflow run", e);
				}
			}
		}
	}

}
