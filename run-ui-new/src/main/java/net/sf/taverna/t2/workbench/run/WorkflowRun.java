/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester   
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
package net.sf.taverna.t2.workbench.run;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent;
import net.sf.taverna.t2.workbench.views.monitor.graph.GraphMonitor;
import net.sf.taverna.t2.workbench.views.monitor.graph.IntermediateResultsComponent;
import net.sf.taverna.t2.workbench.views.monitor.graph.MonitorGraphComponent;
import net.sf.taverna.t2.workbench.views.monitor.graph.MonitorGraphPreviousRunComponent;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressMonitor;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressTreeTable;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressTreeTableModel;
import net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultsComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerRegistry;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Representation of a workflow run.
 *
 */
public class WorkflowRun {

	private static final String STATUS_FINISHED = "Finished";
	private static final String STATUS_CANCELLED = "Cancelled";
	private static final String STATUS_RUNNING = "Running";
	private static final String STATUS_PAUSED = "Paused";
	
	private static Logger logger = Logger.getLogger(WorkflowRun.class);

	private static WeakHashMap<String, WeakReference<Dataflow>> loadedDataflows = new WeakHashMap<String, WeakReference<Dataflow>>();

	private WorkflowInstanceFacade facade;

	private Map<String, T2Reference> inputs;

	// Workflow run start date
	private Date date;

	// Interactive progress graph
	private MonitorGraphComponent progressRunGraph;
	
	// Interactive progress table
	private WorkflowRunProgressTreeTable progressRunTable;
	// Index of the last selected row in the WorkflowRunProgressTreeTable
	private int lastSelectedTableRow = -1;
	
	// Tabbed component that contains the progress graph and table
	private MonitorViewComponent monitorViewComponent;

	// Component that contains final workflow results for all ports, 
	// as they are becoming available
	private WorkflowResultsComponent workflowResultsComponent;
	// Map of intermediate results per processor - this only works if provencance is on
	private Map<Processor, IntermediateResultsComponent> intermediateResultsComponents = new HashMap<Processor, IntermediateResultsComponent>();

	// Progress monitor for graph
	private GraphMonitor monitorObserverForGraph;
	
	// Progress monitor for progress report table
	private WorkflowRunProgressMonitor monitorObserverForTable;
	
	// Workflow run progress status and pause/resume and cancel buttons
	private JLabel workflowRunProgressStatusLabel = new JLabel();
	private JButton workflowRunPauseButton = new JButton(new PauseWorkflowRunAction()); // pause or resume
	private JButton workflowRunCancelButton = new JButton(new CancelWorkflowRunAction());
	private JButton workflowResultsButton = new JButton("Show results");

	private int results = 0;

	private Dataflow dataflow;

	// Unique identifier of the workflow run
	private String runId;

	private ProvenanceConnector connector;

	private boolean isProvenanceEnabledForRun = true;
	private boolean isDataSavedInDatabase = true;

	private ReferenceService referenceService;

	private byte[] dataflowBytes = null;

	private String workflowId = null;

	public String getWorkflowId() {
		return workflowId;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	private String workflowName = "(Unknown)";

	public WorkflowRun(Dataflow dataflow, Date date, String sessionID,
			ReferenceService referenceService) {
		this.date = date;
		this.runId = sessionID;
		this.referenceService = referenceService;
		setDataflow(dataflow);
		String connectorType = DataManagementConfiguration.getInstance()
				.getConnectorType();
		for (ProvenanceConnectorFactory factory : ProvenanceConnectorFactoryRegistry
				.getInstance().getInstances()) {
			if (connectorType.equalsIgnoreCase(factory.getConnectorType())) {
				connector = factory.getProvenanceConnector();
			}
		}

		try {
			if (connector != null) {
				connector.init();
				connector.setSessionID(sessionID);
				connector.setReferenceService(referenceService); // set the ref.
				// service
				// specific
				// to this
				// run
			}
		} catch (Exception except) {

		}
	}

	public WorkflowRun(WorkflowInstanceFacade facade,
			Map<String, T2Reference> inputs, Date date,
			ReferenceService referenceService) {
		this.date = date;
		
		// Create graph monitor for the current run
		progressRunGraph = new MonitorGraphComponent();
		this.facade = facade;
		this.inputs = inputs;
		this.referenceService = referenceService;
		setDataflow(facade.getDataflow());
		connector = (ProvenanceConnector) (facade.getContext()
				.getProvenanceReporter());
		progressRunGraph.setProvenanceConnector(connector);
		progressRunGraph.setReferenceService(referenceService);
		this.runId = facade.getWorkflowRunId();

		// Create progress table for the current workflow run
		progressRunTable = new WorkflowRunProgressTreeTable(
				new WorkflowRunProgressTreeTableModel(facade.getDataflow()));
		progressRunTable.setWorkflowStartDate(date);
		progressRunTable.setWorkflowStatus("Running");
		// Start listening for row selections on the table if provenance is enabled 
		// for a run so we can show intermediate results for processors
		if (isProvenanceEnabledForRun){
		    MouseSelectionListener mouseListener = new MouseSelectionListener(progressRunTable);
		    KeySelectionListener keyListener = new KeySelectionListener(progressRunTable);
		    progressRunTable.addMouseListener(mouseListener);
		    progressRunTable.addKeyListener(keyListener);
		}

		monitorViewComponent = new MonitorViewComponent();
		monitorViewComponent.setMonitorGraph(progressRunGraph);
		monitorViewComponent.setMonitorProgressTable(progressRunTable);
		monitorViewComponent.addWorkflowRunStatusLabel(workflowRunProgressStatusLabel);
		monitorViewComponent.addWorkflowPauseButton(workflowRunPauseButton);
		monitorViewComponent.addWorkflowCancelButton(workflowRunCancelButton);
		monitorViewComponent.addWorkflowResultsButton(workflowResultsButton);

		workflowResultsComponent = new WorkflowResultsComponent();
	}

	public WorkflowRun(byte[] dataflowBytes, String workflowId, String workflowName, Date date,
			String sessionID, ReferenceService referenceService) {
		this((Dataflow) null, date, sessionID, referenceService);
		this.dataflowBytes = dataflowBytes;
		this.workflowId = workflowId;
		this.workflowName = workflowName;
	}

	public void run() {

		monitorObserverForGraph = progressRunGraph.setDataflow(dataflow);
		monitorObserverForTable = new WorkflowRunProgressMonitor(progressRunTable);
		// We use the graph monitor to update the workflow run status label, pause and
		// cancel buttons as this monitor is also used to update the workflow results so we
		// do it from one place
		monitorObserverForGraph.setWorkflowRunStatusLabel(workflowRunProgressStatusLabel);
		monitorObserverForGraph.setWorkflowRunPauseButton(workflowRunPauseButton);
		monitorObserverForGraph.setWorkflowRunCancelButton(workflowRunCancelButton);

		// resultsComponent.setContext(context);
		MonitorManager.getInstance().addObserver(monitorObserverForGraph);
		MonitorManager.getInstance().addObserver(monitorObserverForTable);
		
		// Use the empty context by default to root this facade on the monitor
		// tree

		// Only if this workflow has at least one output port there will be some
		// results to observe.
		// Otherwise, we have to find another way of detecting when a workflow
		// without output ports
		// has finished running - we do that by observing when all processors
		// have finished.
		if (dataflow.getOutputPorts().size() > 0) {
			facade.addResultListener(new ResultListener() {
				public void resultTokenProduced(WorkflowDataToken token,
						String portName) {
					if (token.getIndex().length == 0) {
						results++;
						if (results == dataflow.getOutputPorts().size()) {
							facade.removeResultListener(this);
							MonitorManager.getInstance().removeObserver(
									monitorObserverForGraph);
							monitorObserverForGraph = null;
							results = 0;
						}
					}
				}
			});
		}

		try {
			workflowResultsComponent.register(facade, isProvenanceEnabledForRun);
		} catch (EditException e1) {
			logger.error("Unable to register facade", e1);
		}
		facade.fire();
		if (inputs != null) {
			for (Entry<String, T2Reference> entry : inputs.entrySet()) {
				String portName = entry.getKey();
				T2Reference identifier = entry.getValue();
				int[] index = new int[] {};
				try {
					WorkflowDataToken token = new WorkflowDataToken("", index,
							identifier, facade.getContext());
					facade.pushData(token, portName);
					workflowResultsComponent.pushInputData(token, portName);
				} catch (TokenOrderException e) {
					logger.error("Unable to push data", e);
				}
			}
		}

	}

	@Override
	public String toString() {
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss");
		return workflowName + " " + sdf.format(date);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dataflow == null) ? 0 : dataflow.getInternalIdentier()
						.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final WorkflowRun other = (WorkflowRun) obj;
		if (dataflow == null) {
			if (other.dataflow != null)
				return false;
		} else if (!dataflow.getInternalIdentier().equals(
				other.dataflow.getInternalIdentier()))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		return true;
	}

	public synchronized boolean isDataflowLoaded() {
		return dataflow != null;
	}

	public synchronized Dataflow getDataflow() {
		if (dataflow == null) {
			// See if another DataflowRun already have loaded this workflow
			WeakReference<Dataflow> dataflowRef;
			synchronized (loadedDataflows) {
				dataflowRef = loadedDataflows.get(workflowId);
			}
			if (dataflowRef != null) {
				dataflow = dataflowRef.get();
				// Might be null
			}
		}
		if (dataflow == null && dataflowBytes != null) {
			try {
				SAXBuilder builder = new SAXBuilder();
				Document document = builder.build(new ByteArrayInputStream(
						dataflowBytes));
				Element rootElement = document.getRootElement();
				Dataflow loadedDataflow = XMLDeserializerRegistry.getInstance()
						.getDeserializer().deserializeDataflow(rootElement);
				logger.debug("Loaded dataflow "
						+ loadedDataflow.getInternalIdentier() + " for run "
						+ runId);
				setDataflow(loadedDataflow);
			} catch (Exception e) {
				logger.error("Could not load previous run: " + runId, e);
				// Avoid second attempt
				dataflowBytes = null;
			}
		}
		return dataflow;
	}

	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
		if (dataflow != null) {
			this.workflowName = dataflow.getLocalName();
			this.workflowId = dataflow.getInternalIdentier();
			synchronized (loadedDataflows) {
				loadedDataflows.put(this.workflowId,
						new WeakReference<Dataflow>(dataflow));
			}
		}
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Create the MonitorViewComponent, an interactive component 
	 * containing a graph and a progress table where workflow progress 
	 * can be tracked and which can respond to user clicks to trigger 
	 * showing intermediate or final results in the result-view component.
	 * 
	 * @return the monitorViewComponent
	 */
	public MonitorViewComponent getOrCreateMonitorViewComponent() {
		if (monitorViewComponent == null) {
			monitorViewComponent = new MonitorViewComponent();
			
			// Create graph monitor for a previous run
			progressRunGraph = new MonitorGraphPreviousRunComponent();
			progressRunGraph.setProvenanceConnector(connector);
			progressRunGraph.setReferenceService(referenceService);
			monitorObserverForGraph = progressRunGraph.setDataflow(getDataflow());

			// Create progress table for a previous run
			progressRunTable = new WorkflowRunProgressTreeTable(new WorkflowRunProgressTreeTableModel(dataflow, connector, referenceService, runId));
			progressRunTable.setWorkflowStartDate(date);
			// Start listening for row selections on the table  
			// so we can show intermediate results for processors. 
			// Provenance should be enabled as this is a previous run.
			if (isProvenanceEnabledForRun){
			    MouseSelectionListener mouseListener = new MouseSelectionListener(progressRunTable);
			    KeySelectionListener keyListener = new KeySelectionListener(progressRunTable);
			    progressRunTable.addMouseListener(mouseListener);
			    progressRunTable.addKeyListener(keyListener);
			}
			monitorViewComponent.setMonitorGraph(progressRunGraph);
			monitorViewComponent.setMonitorProgressTable(progressRunTable);
			monitorViewComponent.addWorkflowRunStatusLabel(workflowRunProgressStatusLabel);
			// for previous run status is always "finished" and pause/cancel buttons are disabled
			workflowRunProgressStatusLabel.setText(STATUS_FINISHED);
			workflowRunProgressStatusLabel.setIcon(WorkbenchIcons.greentickIcon);
			monitorViewComponent.addWorkflowPauseButton(workflowRunPauseButton);
			workflowRunPauseButton.setEnabled(false);
			monitorViewComponent.addWorkflowCancelButton(workflowRunCancelButton);
			workflowRunCancelButton.setEnabled(false);
			monitorViewComponent.addWorkflowResultsButton(workflowResultsButton);
			
			workflowResultsComponent = new WorkflowResultsComponent();
			workflowResultsComponent.repopulate(getDataflow(), getRunId(), getDate(),
					getReferenceService(), isProvenanceEnabledForRun);
			// monitorViewComponent.revalidate();
		}
		return monitorViewComponent;
	}

	
//	/**
//	 * Create the MonitorViewComponent, an interactive component 
//	 * containing graph where workflow progress can be tracked and which
//	 * can respond to user clicks on the diagram to trigger showing intermediate
//	 * or final results in result-view component.
//	 * 
//	 * @return the monitorViewComponent
//	 */
//	public MonitorGraphComponent getOrCreateMonitorViewComponent() {
//		if (monitorProgressGraph == null) {
//			monitorProgressGraph = new MonitorGraphPreviousRunComponent();
//			monitorProgressGraph.setProvenanceConnector(connector);
//			monitorProgressGraph.setReferenceService(referenceService);
//			monitorObserver = monitorProgressGraph.setDataflow(getDataflow());
//
//			workflowResultsComponent = new WorkflowResultsComponent();
//			workflowResultsComponent.repopulate(getDataflow(), getRunId(), getDate(),
//					getReferenceService(), isProvenanceEnabledForRun);
//			monitorProgressGraph
//					.setStatus(MonitorGraphComponent.Status.COMPLETE);
//			// monitorViewComponent.revalidate();
//		}
//		return monitorProgressGraph;
//	}
//
//	/**
//	 * Create the ProgressReportComponent, a table view over the processors in a workflow
//	 * containing their status, iterations, starting and finishing times, etc. The component is
//	 * interactive and can respond to user clicks to trigger showing intermediate
//	 * or final results in result-view component.
//	 */
//	public JTable getOrCreateProgressReportComponent() {
//		if (monitorProgressTable == null) {
//			monitorProgressTable = new WorkflowRunProgressTreeTable(new WorkflowRunProgressTreeTableModel(dataflow));
//			//progressReportComponent = new JTreeTable2(new FileSystemModel());
//		}
//		return monitorProgressTable;
//	}
	
	public WorkflowRunProgressMonitor getWorkflowRunProgressMonitor() {
		return monitorObserverForTable;
	}
	
	public MonitorGraphComponent getMonitorGraphComponent() {
		return progressRunGraph;
	}

	public WorkflowResultsComponent getResultsComponent() {
		return workflowResultsComponent;
	}

	public void setRunId(String runId) {
		this.runId = runId;
	}

	public String getRunId() {
		return runId;
	}

	public void setProvenanceEnabledForRun(boolean isProvenanceEnabledForRun) {
		this.isProvenanceEnabledForRun = isProvenanceEnabledForRun;
	}

	public boolean isProvenanceEnabledForRun() {
		return isProvenanceEnabledForRun;
	}

	public void setDataSavedInDatabase(boolean dataSavedInDatabase) {
		this.isDataSavedInDatabase = dataSavedInDatabase;
	}

	public boolean isDataSavedInDatabase() {
		return isDataSavedInDatabase;
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public ReferenceService getReferenceService() {
		return referenceService;
	}

	private class MouseSelectionListener extends MouseAdapter {
		
		WorkflowRunProgressTreeTable treeTable;

		public MouseSelectionListener(WorkflowRunProgressTreeTable treeTable){
			this.treeTable = treeTable;
		}
		public void mousePressed(MouseEvent e) {

			Point point = e.getPoint();
			int row = treeTable.rowAtPoint(point);

			if (row == -1 || row == lastSelectedTableRow) {
				return; // nothing selected or we have already shown the right
				// component last
				// time
			}
			
			lastSelectedTableRow = row;
			Object object = treeTable.getTreeObjectForRow(row);
			if (object != null) {
				// User has selected the row with the dataflow itself - show
				// final results
				if (object instanceof Dataflow) {
					ResultsPerspectiveComponent.getInstance()
							.setBottomComponent(workflowResultsComponent);
				}
				// User has selected the row with a processor - show its
				// intermediate results if provenance is enabled
				else if (object instanceof Processor) {
					// if (isProvenanceEnabledForRun){ // we only use this
					// listener if provenance is enabled so no need to check
					// here
					IntermediateResultsComponent intermediateResultsComponent = intermediateResultsComponents
							.get((Processor) object);
					if (intermediateResultsComponent == null) {
						intermediateResultsComponent = new IntermediateResultsComponent();
						intermediateResultsComponent
								.setLabel(((Processor) object).getLocalName());
						intermediateResultsComponents.put((Processor) object,
								intermediateResultsComponent);
					}
					ResultsPerspectiveComponent.getInstance()
							.setBottomComponent(intermediateResultsComponent);
					ResultsPerspectiveComponent.getInstance().repaint();
				}
			}
		}
	}
	
	private class KeySelectionListener extends KeyAdapter {
		
		WorkflowRunProgressTreeTable treeTable;

		public KeySelectionListener(WorkflowRunProgressTreeTable treeTable){
			this.treeTable = treeTable;
		}
		public void keyPressed(KeyEvent e) {

			if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN){ // these keys change the row selection
				int row = treeTable.getSelectedRow(); // this seems to give the previously selected row so we have to adjust it!!!				
				
				// row always have to be 0 <= row < treeTable.getRowCount() - 1
				if (e.getKeyCode() == KeyEvent.VK_UP && row > 0){
					row --;
				}
				if (e.getKeyCode() == KeyEvent.VK_DOWN && row < treeTable.getRowCount() - 1 ){
					row ++;
				}

				if (row == lastSelectedTableRow) {
					return; // nothing selected or we have already shown the
							// right
					// component last
					// time
				}
				
				lastSelectedTableRow = row;
				Object object = treeTable.getTreeObjectForRow(row);
				if (object != null) {
					// User has selected the row with the dataflow itself - show
					// final results
					if (object instanceof Dataflow) {
						ResultsPerspectiveComponent.getInstance()
								.setBottomComponent(workflowResultsComponent);
					}
					// User has selected the row with a processor - show its
					// intermediate results if provenance is enabled
					else if (object instanceof Processor) {
						// if (isProvenanceEnabledForRun){ // we only use this
						// listener if provenance is enabled so no need to check
						// here
						IntermediateResultsComponent intermediateResultsComponent = intermediateResultsComponents
								.get((Processor) object);
						if (intermediateResultsComponent == null) {
							intermediateResultsComponent = new IntermediateResultsComponent();
							intermediateResultsComponent
									.setLabel(((Processor) object)
											.getLocalName());
							intermediateResultsComponents.put(
									(Processor) object,
									intermediateResultsComponent);
						}
						ResultsPerspectiveComponent.getInstance()
								.setBottomComponent(
										intermediateResultsComponent);
						ResultsPerspectiveComponent.getInstance().repaint();
					}
				}
			}
		}
	}
	
	/**
	 * Action to pause/resume this workflow run.
	 */
	@SuppressWarnings("serial")
	public class PauseWorkflowRunAction extends AbstractAction {

		public PauseWorkflowRunAction (){
			super();
			putValue(NAME, "Pause");
		}
		
		public void actionPerformed(ActionEvent e) {
			String text = (String)getValue(NAME);
			if (text.equals("Pause")) {
				putValue(NAME, "Resume");
				workflowRunProgressStatusLabel.setText(STATUS_PAUSED);
				workflowRunProgressStatusLabel.setIcon(WorkbenchIcons.workingStoppedIcon);
			} 
			else if (text.equals("Resume")){
				putValue(NAME, "Pause");
				workflowRunProgressStatusLabel.setText(STATUS_RUNNING);
				workflowRunProgressStatusLabel.setIcon(WorkbenchIcons.workingIcon);
			}	
		}
	}

	/**
	 * Action to cancel this workflow run.
	 */
	@SuppressWarnings("serial")
	public class CancelWorkflowRunAction extends AbstractAction {
		
		public CancelWorkflowRunAction (){
			super();
			putValue(NAME, "Cancel");
		}
		
		public void actionPerformed(ActionEvent e) {
			workflowRunProgressStatusLabel.setText(STATUS_CANCELLED);
			workflowRunProgressStatusLabel.setIcon(WorkbenchIcons.workingStoppedIcon);
			// Disable the Pause/Resume button
			workflowRunPauseButton.setEnabled(false);
		}
	}

	public WorkflowInstanceFacade getFacade() {
		return facade;
	}

}
