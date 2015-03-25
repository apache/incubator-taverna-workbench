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
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import net.sf.taverna.t2.workbench.selection.events.WorkflowRunSelectionEvent;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.apache.log4j.Logger;

import org.apache.taverna.platform.report.ActivityReport;
import org.apache.taverna.platform.report.ProcessorReport;
import org.apache.taverna.platform.report.WorkflowReport;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.port.Port;

/**
 * Component for displaying the input and output values of workflow and processor invocations.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class ResultsComponent extends JPanel implements Updatable {
	private static final Logger logger = Logger.getLogger(ResultsComponent.class);

	private final RunService runService;
	private final SelectionManager selectionManager;
	private final RendererRegistry rendererRegistry;
	private final List<SaveAllResultsSPI> saveAllResultsSPIs;
	private final List<SaveIndividualResultSPI> saveIndividualResultSPIs;

	private CardLayout cardLayout = new CardLayout();
	private Updatable updatableComponent;
	private Map<String, ReportView> workflowResults = new HashMap<>();
	private Map<String, Map<Processor, ReportView>> processorResults = new HashMap<>();
	private SelectionManagerObserver selectionManagerObserver = new SelectionManagerObserver();
	private String workflowRun;

	public ResultsComponent(RunService runService, SelectionManager selectionManager,
			RendererRegistry rendererRegistry, List<SaveAllResultsSPI> saveAllResultsSPIs,
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
		if (updatableComponent != null)
			updatableComponent.update();
	}

	public void setWorkflowRun(String workflowRun) throws InvalidRunIdException {
		if (workflowRun == null)
			return;
		this.workflowRun = workflowRun;

		DataflowSelectionModel selectionModel = selectionManager
				.getWorkflowRunSelectionModel(workflowRun);
		Set<Object> selectionSet = selectionModel.getSelection();
		if (selectionSet.size() == 1) {
			Object selection = selectionSet.iterator().next();
			if (selection instanceof Processor) {
				showProcessorResults((Processor) selection);
				return;
			}
		}

		showWorkflowResults();
	}

	public void addWorkflowRun(String workflowRun) throws InvalidRunIdException {
		WorkflowReport workflowReport = runService.getWorkflowReport(workflowRun);
		ReportView reportView = new ReportView(workflowReport, rendererRegistry,
				saveAllResultsSPIs, saveIndividualResultSPIs);
		add(reportView, workflowRun);
		workflowResults.put(workflowRun, reportView);
		DataflowSelectionModel selectionModel = selectionManager
				.getWorkflowRunSelectionModel(workflowRun);
		selectionModel.addObserver(new DataflowSelectionObserver());
	}

	public void removeWorkflowRun(String workflowRun) {
		ReportView removedWorkflowResults = workflowResults.remove(workflowRun);
		if (removedWorkflowResults != null)
			remove(removedWorkflowResults);
		Map<Processor, ReportView> removedProcessorResults = processorResults.remove(workflowRun);
		if (removedProcessorResults != null)
			for (ReportView reportView: removedProcessorResults.values())
				remove(reportView);
	}

	private void showWorkflowResults() throws InvalidRunIdException {
		if (!workflowResults.containsKey(workflowRun))
			addWorkflowRun(workflowRun);
		updatableComponent = workflowResults.get(workflowRun);
		cardLayout.show(this, workflowRun);
		update();
	}

	private void showProcessorResults(Processor processor) throws InvalidRunIdException {
		if (!processorResults.containsKey(workflowRun))
			processorResults.put(workflowRun, new HashMap<Processor, ReportView>());
		Map<Processor, ReportView> components = processorResults.get(workflowRun);
		if (!components.containsKey(processor)) {
			WorkflowReport workflowReport = runService.getWorkflowReport(workflowRun);
			ProcessorReport processorReport = findProcessorReport(workflowReport, processor);
			ReportView reportView = new ReportView(processorReport, rendererRegistry,
					saveAllResultsSPIs, saveIndividualResultSPIs);
			components.put(processor, reportView);
			add(reportView, String.valueOf(reportView.hashCode()));
		}
		updatableComponent = components.get(processor);
		cardLayout.show(this, String.valueOf(updatableComponent.hashCode()));
		update();
	}

	private ProcessorReport findProcessorReport(WorkflowReport workflowReport,
			Processor processor) {
		URI workflowIdentifier = workflowReport.getSubject().getIdentifier();
		if (processor.getParent().getIdentifier().equals(workflowIdentifier)) {
			for (ProcessorReport processorReport : workflowReport
					.getProcessorReports())
				if (processorReport.getSubject().getName()
						.equals(processor.getName()))
					return processorReport;
			return null;
		}

		for (ProcessorReport processorReport : workflowReport
				.getProcessorReports())
			for (ActivityReport activityReport : processorReport
					.getActivityReports()) {
				WorkflowReport nestedWorkflowReport = activityReport
						.getNestedWorkflowReport();
				if (nestedWorkflowReport != null) {
					ProcessorReport report = findProcessorReport(
							nestedWorkflowReport, processor);
					if (report != null)
						return report;
				}
			}
		return null;
	}

	private class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender,
				SelectionManagerEvent message) {
			try {
				if (message instanceof WorkflowRunSelectionEvent)
					setWorkflowRun(((WorkflowRunSelectionEvent) message)
							.getSelectedWorkflowRun());
			} catch (InvalidRunIdException e) {
				logger.warn("Invalid workflow run", e);
			}
		}
	}

	private final class DataflowSelectionObserver implements Observer<DataflowSelectionMessage> {
		@Override
		public void notify(Observable<DataflowSelectionMessage> sender,
				DataflowSelectionMessage message) throws Exception {
			if (message.getType() != DataflowSelectionMessage.Type.ADDED)
				return;

			Object element = message.getElement();
			if (element instanceof Processor) {
				showProcessorResults((Processor) element);
				return;
			}

			showWorkflowResults();

			if (element instanceof Port) {
				Port port = (Port) element;
				if (updatableComponent instanceof ReportView) {
					ReportView reportView = (ReportView) updatableComponent;
					reportView.selectPort(port);
				}
			}
		}
	}
}
