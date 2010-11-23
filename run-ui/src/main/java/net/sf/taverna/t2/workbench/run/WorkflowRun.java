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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.file.DataflowPersistenceHandler;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent;
import net.sf.taverna.t2.workbench.views.monitor.WorkflowObjectSelectionMessage;
import net.sf.taverna.t2.workbench.views.monitor.graph.GraphMonitor;
import net.sf.taverna.t2.workbench.views.monitor.graph.MonitorGraphComponent;
import net.sf.taverna.t2.workbench.views.monitor.graph.MonitorGraphPreviousRunComponent;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressMonitor;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressTreeTable;
import net.sf.taverna.t2.workbench.views.monitor.progressreport.WorkflowRunProgressTreeTableModel;
import net.sf.taverna.t2.workbench.views.results.processor.ProcessorResultsComponent;
import net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultsComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowPort;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerRegistry;
import net.sf.taverna.t2.workbench.file.impl.T2FlowFileType;
import net.sf.taverna.t2.workbench.file.exceptions.OpenException;
import net.sf.taverna.t2.workbench.file.impl.DataflowPersistenceHandlerRegistry;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Representation of a workflow run. It listens to the selection event on the
 * graph so that it can show intermediate results if provenance is on.
 *
 */
public class WorkflowRun implements Observer<WorkflowObjectSelectionMessage>{

	private static final String STATUS_FINISHED = "Finished";
	private static final String STATUS_CANCELLED = "Cancelled";
	private static final String STATUS_RUNNING = "Running";
	private static final String STATUS_PAUSED = "Paused";
	
	public static Logger logger = Logger.getLogger(WorkflowRun.class);

	private static WeakHashMap<String, WeakReference<Dataflow>> loadedDataflows = new WeakHashMap<String, WeakReference<Dataflow>>();

	private WorkflowInstanceFacade facade;

	private Map<String, T2Reference> inputs;

	// Workflow run start date
	private Date date;

	// Interactive progress graph
	private MonitorGraphComponent progressRunGraph;
	
	// Interactive progress table
	private WorkflowRunProgressTreeTable progressRunTable;
	
	// Tabbed component that contains the progress graph and table
	private MonitorViewComponent monitorViewComponent;

	// Component that contains final workflow results for all ports, 
	// as they are becoming available
	private WorkflowResultsComponent workflowResultsComponent;
	
	// Map of intermediate results per processor - this only works if provenance is on
	private Map<Processor, ProcessorResultsComponent> intermediateResultsComponents = new HashMap<Processor, ProcessorResultsComponent>();

	// Progress monitor for graph
	private GraphMonitor monitorObserverForGraph;
	
	// Progress monitor for progress report table
	private WorkflowRunProgressMonitor monitorObserverForTable;
	
	// Workflow run progress status and pause/resume and cancel buttons
	private JLabel workflowRunProgressStatusLabel = new JLabel();
	private JButton workflowRunPauseButton = new JButton(new PauseWorkflowRunAction()); // pause or resume
	private JButton workflowRunCancelButton = new JButton(new CancelWorkflowRunAction());
	private AbstractAction intermediateValuesAction = new RefreshIntermediateValuesAction();
	private JButton intermediateValuesButton = new JButton(intermediateValuesAction);
	private AbstractAction workflowResultsAction = new ShowWorkflowResultsAction();
	private JButton workflowResultsButton = new JButton(workflowResultsAction);

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
		// Do not show the column with total number of iterations as it 
		// does not gets updated till the end of all iterations so it is pointless
		//progressRunTable.removeColumn(progressRunTable.getColumnModel().getColumn(5));
		progressRunTable.setWorkflowStartDate(date);
		progressRunTable.setWorkflowStatus("Running");
		// Start listening for row selections on the table if provenance is enabled 
		// for a run so we can show intermediate results for processors
		if (isProvenanceEnabledForRun){
		    
			MouseSelectionListener mouseListener = new MouseSelectionListener(progressRunTable);
		    KeySelectionListener keyListener = new KeySelectionListener(progressRunTable);
		    progressRunTable.addMouseListener(mouseListener);
		    progressRunTable.addKeyListener(keyListener);
		    
			// Start observing selections on the graph and progress table 
		    // so we can show intermediate results
			progressRunGraph.addObserver(this);
			progressRunTable.addObserver(this);
		}

		monitorViewComponent = new MonitorViewComponent();
		monitorViewComponent.setMonitorGraph(progressRunGraph);
		monitorViewComponent.setMonitorProgressTable(progressRunTable);
		workflowRunProgressStatusLabel.setBorder(new EmptyBorder(0,0,0,10));
		monitorViewComponent.addWorkflowRunStatusLabel(workflowRunProgressStatusLabel);
		monitorViewComponent.addWorkflowPauseButton(workflowRunPauseButton);
		monitorViewComponent.addWorkflowCancelButton(workflowRunCancelButton);
		monitorViewComponent.addIntermediateValuesButton(intermediateValuesButton);
		monitorViewComponent.addWorkflowResultsButton(workflowResultsButton);
		monitorViewComponent.addReloadWorkflowButton(new JButton (new ReloadWorkflowAction(facade.getDataflow())));
		intermediateValuesButton.setEnabled(false);
		//		workflowResultsButton.setEnabled(false);

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
		monitorObserverForTable = new WorkflowRunProgressMonitor(progressRunTable, facade);
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
				+ ((dataflow == null) ? 0 : dataflow.getIdentifier()
						.hashCode());
		result = prime * result + ((runId == null) ? 0 : runId.hashCode());
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
		} else if (!dataflow.getIdentifier().equals(
				other.dataflow.getIdentifier()))
			return false;
		if (runId == null) {
			if (other.runId != null)
				return false;
		} else if (!runId.equals(other.runId))
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
						+ loadedDataflow.getIdentifier() + " for run "
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
			this.workflowId = dataflow.getIdentifier();
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
			// Do not show the column with total number of iterations as it 
			// does not gets updated till the end of all iterations so it is pointless
			//progressRunTable.removeColumn(progressRunTable.getColumnModel().getColumn(5));
			
			// Start listening for row selections on the table  
			// so we can show intermediate results for processors. 
			// Provenance *should be* enabled as this is a previous run.
			if (isProvenanceEnabledForRun){
			    MouseSelectionListener mouseListener = new MouseSelectionListener(progressRunTable);
			    KeySelectionListener keyListener = new KeySelectionListener(progressRunTable);
			    progressRunTable.addMouseListener(mouseListener);
			    progressRunTable.addKeyListener(keyListener);
			    
				// Start observing selections on the graph and progress table 
			    // so we can show intermediate results
				progressRunGraph.addObserver(this);
				progressRunTable.addObserver(this);
			}

			monitorViewComponent.setMonitorGraph(progressRunGraph);
			monitorViewComponent.setMonitorProgressTable(progressRunTable);
			monitorViewComponent.addWorkflowRunStatusLabel(workflowRunProgressStatusLabel);
			// for previous run status is always "finished" and pause/cancel buttons are disabled
			workflowRunProgressStatusLabel.setBorder(new EmptyBorder(0,0,0,10));

			
			workflowRunProgressStatusLabel.setText(STATUS_FINISHED);
			workflowRunProgressStatusLabel.setIcon(WorkbenchIcons.greentickIcon);
			
			monitorViewComponent.addWorkflowPauseButton(workflowRunPauseButton);
			workflowRunPauseButton.setEnabled(false);
			monitorViewComponent.addWorkflowCancelButton(workflowRunCancelButton);
			workflowRunCancelButton.setEnabled(false);
			monitorViewComponent.addIntermediateValuesButton(intermediateValuesButton);
			intermediateValuesButton.setEnabled(false);
			monitorViewComponent.addWorkflowResultsButton(workflowResultsButton);
			monitorViewComponent.addReloadWorkflowButton(new JButton (new ReloadWorkflowAction(getDataflow())));
			//			workflowResultsButton.setEnabled(false);

			// Results for an old wf run - get the results from provenance 
			workflowResultsComponent = new WorkflowResultsComponent(dataflow, runId, referenceService);

		}
		return monitorViewComponent;
	}
	
	public WorkflowRunProgressMonitor getWorkflowRunProgressMonitor() {
		return monitorObserverForTable;
	}
	
	public MonitorGraphComponent getMonitorGraphComponent() {
		return progressRunGraph;
	}

	public JComponent getResultsComponent() {
		if (progressRunTable == null) {
			// still initializing
			return workflowResultsComponent;
		}
	    int row = progressRunTable.getLastSelectedTableRow();
	    if (row != -1) {
		Object object = progressRunTable.getTreeObjectForRow(row);
		if ((object != null) && (object instanceof Processor)){
		    return getIntermediateResultsComponent((Processor)object);
		}
	    }
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

			if (row == -1 || row == treeTable.getLastSelectedTableRow()) {
				return; // nothing selected or we have already shown the right
				// component last
				// time
			}
			
			treeTable.setLastSelectedTableRow(row);
			Object object = treeTable.getTreeObjectForRow(row);
			if (object != null) {
				// Notify anyone interested that a selection occurred on the table
				progressRunTable.triggerWorkflowObjectSelectionEvent(object);
			}
		}
	}
	
	private class KeySelectionListener extends KeyAdapter {
		
		WorkflowRunProgressTreeTable treeTable;

		public KeySelectionListener(WorkflowRunProgressTreeTable treeTable){
			this.treeTable = treeTable;
		}
		public void keyPressed(KeyEvent e) {

			if(e.getKeyCode() == KeyEvent.VK_ENTER){ // consume ENTER key, do nothing
			         e.consume();
			}
			 
			if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN){ // these keys change the row selection
				int row = treeTable.getSelectedRow(); // this seems to give the previously selected row so we have to adjust it!!!				
				
				// row always have to be 0 <= row < treeTable.getRowCount() - 1
				if (e.getKeyCode() == KeyEvent.VK_UP && row > 0){
					row --;
				}
				if (e.getKeyCode() == KeyEvent.VK_DOWN && row < treeTable.getRowCount() - 1 ){
					row ++;
				}

				if (row == treeTable.getLastSelectedTableRow()) {
					return; // nothing selected or we have already shown the
							// right
					// component last
					// time
				}
				
				treeTable.setLastSelectedTableRow(row);
				Object object = treeTable.getTreeObjectForRow(row);
				if (object != null) {
					// Notify anyone interested that a selection occurred on the table
					progressRunTable.triggerWorkflowObjectSelectionEvent(object);					
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
			putValue(SMALL_ICON, WorkbenchIcons.pauseIcon);
		}
		
		public void actionPerformed(ActionEvent e) {
			String text = (String)getValue(NAME);
			if (text.equals("Pause")) {
				putValue(NAME, "Resume");
				putValue(SMALL_ICON, WorkbenchIcons.playIcon);			
				workflowRunProgressStatusLabel.setText(STATUS_PAUSED);
				workflowRunProgressStatusLabel.setIcon(WorkbenchIcons.workingStoppedIcon);
				if (facade != null){ // should not be null but check nevertheless
					facade.pauseWorkflowRun();
				}
				progressRunTable.setWorkflowPaused();
			} 
			else if (text.equals("Resume")){
				putValue(NAME, "Pause");
				putValue(SMALL_ICON, WorkbenchIcons.pauseIcon);
				workflowRunProgressStatusLabel.setText(STATUS_RUNNING);
				workflowRunProgressStatusLabel.setIcon(WorkbenchIcons.workingIcon);
				if (facade != null){ // should not be null but check nevertheless
					facade.resumeWorkflowRun();
				}
				progressRunTable.setWorkflowResumed();
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
			putValue(SMALL_ICON, WorkbenchIcons.closeIcon);
		}
		
		public void actionPerformed(ActionEvent e) {
			workflowRunProgressStatusLabel.setText(STATUS_CANCELLED);
			workflowRunProgressStatusLabel.setIcon(WorkbenchIcons.workingStoppedIcon);
			// Disable the Pause/Resume button
			workflowRunPauseButton.setEnabled(false);
			workflowRunCancelButton.setEnabled(false);
			if (facade != null){ // should not be null but check nevertheless
				facade.cancelWorkflowRun();
			}

			// Stop listening to workflow run's monitors
			monitorObserverForGraph.onDispose();
			monitorObserverForTable.onDispose();

			// Update the progress table to show workflow and processors as cancelled
			progressRunTable.setWorkflowCancelled();
			progressRunTable.refreshTable();
		}
	}
	
	public class RefreshIntermediateValuesAction extends AbstractAction {
	    public RefreshIntermediateValuesAction() {
		super();
		putValue(NAME, "Refresh intermediate values");
		putValue(SMALL_ICON, WorkbenchIcons.refreshIcon);
	    }

	    public void actionPerformed(ActionEvent e) {

		Object o = ResultsPerspectiveComponent.getInstance().getBottomComponent();
		if (o instanceof ProcessorResultsComponent) {
		    ProcessorResultsComponent prc = (ProcessorResultsComponent) o;
		    prc.update();
		}
		((JButton)e.getSource()).getParent().requestFocusInWindow();
	    }
	}

	public class ReloadWorkflowAction extends AbstractAction {
		private FileManager fileManager = FileManager.getInstance();

		private Dataflow dataflow;
		
	    public ReloadWorkflowAction(final Dataflow dataflow) {
	    	super();
	    	this.dataflow = dataflow;
	    	putValue(NAME, "Reload workflow");
	    	putValue(SMALL_ICON, WorkbenchIcons.refreshIcon);
	    }

	    public void actionPerformed(ActionEvent e) {
	    	try {
	    		String id = dataflow.getIdentifier();
	    		boolean found = false;
	    		for (Dataflow d : fileManager.getOpenDataflows()) {
	    			if (d.getIdentifier().equals(id)) {
	    				fileManager.setCurrentDataflow(d);
	    				found = true;
	    				break;
	    			}
	    		}
		    	if (!found) {
	    			fileManager.openDataflow(new T2FlowFileType(), dataflow);
		    	}
	    	}
	    	catch (OpenException ex) {
	    		WorkflowRun.this.logger.error("Failed to reload workflow from run", ex);
	    	}
	    	((JButton)e.getSource()).getParent().requestFocusInWindow();
	    }
	    
	}

	/**
	 * Action to show the final results of this workflow run.
	 */
	@SuppressWarnings("serial")
	public class ShowWorkflowResultsAction extends AbstractAction {

		public ShowWorkflowResultsAction() {
			super();
			putValue(NAME, "Show workflow results");
			putValue(SMALL_ICON, WorkbenchIcons.resultsPerspectiveIcon);
		}

		public void actionPerformed(ActionEvent e) {

		    ResultsPerspectiveComponent rpc = ResultsPerspectiveComponent.getInstance();
			if (workflowResultsComponent != null) {
				rpc.setBottomComponent(
						workflowResultsComponent);
				progressRunTable.setSelectedRowForObject(dataflow);
				progressRunGraph.setSelectedGraphElementForWorkflowObject(dataflow);
				rpc.revalidate();
			}
			((JButton)e.getSource()).getParent().requestFocusInWindow();
			intermediateValuesButton.setEnabled(false);
			//			workflowResultsButton.setEnabled(false);
		}
	}

	public WorkflowInstanceFacade getFacade() {
		return facade;
	}

	public ProcessorResultsComponent getIntermediateResultsComponent(Processor p) {
	    ProcessorResultsComponent intermediateResultsComponent = intermediateResultsComponents
		.get(p);
	    if (intermediateResultsComponent == null) {
		if (facade != null){ // this is a fresh run (i.e. executed during this Taverna session)
		    // Need to create a timer that will update intermediate results 
		    // periodically until workflow stops running
		    intermediateResultsComponent = new ProcessorResultsComponent(facade, 
										 p, dataflow, runId, referenceService);
		}
		else{ // this is an old workflow from provenance - no need to update intermediate
		    intermediateResultsComponent = new ProcessorResultsComponent(p, dataflow, runId, referenceService);
		}
		intermediateResultsComponents.put(p, intermediateResultsComponent);
	    }
	    return intermediateResultsComponent;
	}

	public void notify(Observable<WorkflowObjectSelectionMessage> sender,
			WorkflowObjectSelectionMessage message) throws Exception {

		Object workflowObject = message.getWorkflowObject();
		ResultsPerspectiveComponent rpc = ResultsPerspectiveComponent.getInstance();
		if (workflowObject instanceof Dataflow || workflowObject instanceof DataflowPort) {
			rpc.setBottomComponent(workflowResultsComponent);
			intermediateValuesButton.setEnabled(false);
			//			workflowResultsButton.setEnabled(false);
		} else if (workflowObject instanceof Processor) {
		    // User has selected a processor - show its
		    // intermediate results if provenance is enabled (which it should be!)
			if (isProvenanceEnabledForRun){
			    ProcessorResultsComponent intermediateResultsComponent = getIntermediateResultsComponent((Processor) workflowObject);
			    rpc.setBottomComponent(intermediateResultsComponent);
			    if (facade != null) {
				intermediateValuesButton.setEnabled(true);
			    }
			    //			    workflowResultsButton.setEnabled(true);
			}
		}
		rpc.revalidate();
		if (workflowObject instanceof DataflowPort) {
			DataflowPort dataflowPort = (DataflowPort) workflowObject;
			workflowResultsComponent.selectWorkflowPortTab(dataflowPort);
		}
		
		// If this came from a selection event on the graph
		if (sender instanceof MonitorGraphComponent
				&& (workflowObject instanceof Dataflow || workflowObject instanceof Processor)) {
			// Update the selected row on the progress tree
			progressRunTable.setSelectedRowForObject(workflowObject);
		}
		
		// If this came from a selection event on the table
		if (sender instanceof WorkflowRunProgressTreeTable
				&& (workflowObject instanceof Dataflow || workflowObject instanceof Processor)) {
			// Update the selected element on the graph
			progressRunGraph.setSelectedGraphElementForWorkflowObject(workflowObject);
		}
	}

}
