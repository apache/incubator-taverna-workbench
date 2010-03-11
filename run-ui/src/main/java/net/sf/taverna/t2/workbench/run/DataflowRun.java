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

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.monitor.MonitorManager;
import net.sf.taverna.t2.monitor.MonitorManager.MonitorMessage;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent;
import net.sf.taverna.t2.workbench.views.monitor.PreviousRunsComponent;
import net.sf.taverna.t2.workbench.views.results.ResultViewComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerRegistry;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Representation of a dataflow run
 *
 */
public class DataflowRun {

	private static Logger logger = Logger.getLogger(DataflowRun.class);

	private static WeakHashMap<String, WeakReference<Dataflow>> loadedDataflows = new WeakHashMap<String, WeakReference<Dataflow>>();

	private WorkflowInstanceFacade facade;

	private Map<String, T2Reference> inputs;

	private Date date;

	private MonitorViewComponent monitorViewComponent;

	private ResultViewComponent resultsComponent;

	private Observer<MonitorMessage> monitorObserver;

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

	public DataflowRun(Dataflow dataflow, Date date, String sessionID,
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

	public DataflowRun(WorkflowInstanceFacade facade,
			Map<String, T2Reference> inputs, Date date,
			ReferenceService referenceService) {
		this.date = date;
		monitorViewComponent = new MonitorViewComponent();
		this.facade = facade;
		this.inputs = inputs;
		this.referenceService = referenceService;
		setDataflow(facade.getDataflow());
		connector = (ProvenanceConnector) (facade.getContext()
				.getProvenanceReporter());
		monitorViewComponent.setProvenanceConnector(connector);
		monitorViewComponent.setReferenceService(referenceService);
		this.runId = facade.getWorkflowRunId();
		resultsComponent = new ResultViewComponent();
	}

	public DataflowRun(byte[] dataflowBytes, String workflowId, String workflowName, Date date,
			String sessionID, ReferenceService referenceService) {
		this((Dataflow) null, date, sessionID, referenceService);
		this.dataflowBytes = dataflowBytes;
		this.workflowId = workflowId;
		this.workflowName = workflowName;
	}

	public void run() {

		monitorObserver = monitorViewComponent.setDataflow(dataflow);

		// resultsComponent.setContext(context);
		MonitorManager.getInstance().addObserver(monitorObserver);
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
									monitorObserver);
							monitorObserver = null;
							results = 0;
						}
					}
				}
			});
		}

		try {
			resultsComponent.register(facade, isProvenanceEnabledForRun);
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
					resultsComponent.pushInputData(token, portName);
				} catch (TokenOrderException e) {
					logger.error("Unable to push data", e);
				}
			}
		}

	}

	@Override
	public String toString() {
		return workflowName + " " + DateFormat.getDateTimeInstance().format(date);
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
		final DataflowRun other = (DataflowRun) obj;
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
	 * Returns the monitorViewComponent.
	 * 
	 * @return the monitorViewComponent
	 */
	public MonitorViewComponent getOrCreateMonitorViewComponent() {
		if (monitorViewComponent == null) {
			monitorViewComponent = new PreviousRunsComponent();
			monitorViewComponent.setProvenanceConnector(connector);
			monitorViewComponent.setReferenceService(referenceService);
			monitorObserver = monitorViewComponent.setDataflow(getDataflow());

			resultsComponent = new ResultViewComponent();
			resultsComponent.repopulate(getDataflow(), getRunId(), getDate(),
					getReferenceService(), isProvenanceEnabledForRun);
			monitorViewComponent
					.setStatus(MonitorViewComponent.Status.COMPLETE);
			// monitorViewComponent.revalidate();
		}
		return monitorViewComponent;
	}

	public MonitorViewComponent getMonitorViewComponent() {
		return monitorViewComponent;
	}

	/**
	 * Returns the resultsComponent.
	 * 
	 * @return the resultsComponent
	 */
	public ResultViewComponent getResultsComponent() {
		return resultsComponent;
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
}
