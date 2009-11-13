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
package net.sf.taverna.t2.workbench.run;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
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
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.views.monitor.MonitorViewComponent;
import net.sf.taverna.t2.workbench.views.monitor.PreviousRunsComponent;
import net.sf.taverna.t2.workbench.views.results.ResultViewComponent;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;

public class DataflowRun {

	private static Logger logger = Logger
	.getLogger(DataflowRun.class);

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

	public DataflowRun(Dataflow dataflow, Date date, String sessionID) {
		// get the provenance connector and hope it and the reference service
		// are the correct ones!! ie. the user has not changed something etc
		// the reference service is needed to dereference the data so if it is
		// the 'wrong' one then........
		this.date = date;
		this.runId = sessionID;
		monitorViewComponent = new PreviousRunsComponent();
		String connectorType = DataManagementConfiguration.getInstance()
				.getConnectorType();
		ProvenanceConnector provenanceConnector = null;
		for (ProvenanceConnectorFactory factory : ProvenanceConnectorFactoryRegistry
				.getInstance().getInstances()) {
			if (connectorType.equalsIgnoreCase(factory.getConnectorType())) {
				provenanceConnector = factory.getProvenanceConnector();
			}
		}

		try {
			if (provenanceConnector != null) {
				provenanceConnector.init();
				provenanceConnector.setSessionID(sessionID);
			}
		} catch (Exception except) {

		}
		monitorViewComponent.setProvenanceConnector(provenanceConnector);
		monitorObserver = monitorViewComponent.setDataflow(dataflow);

		resultsComponent = new ResultViewComponent();
		monitorViewComponent.setStatus(MonitorViewComponent.Status.COMPLETE);
		monitorViewComponent.revalidate();
		monitorViewComponent.revalidate();
	}

	public DataflowRun(WorkflowInstanceFacade facade,
			Map<String, T2Reference> inputs, Date date) {
		this.date = date;
		monitorViewComponent = new MonitorViewComponent();
		this.facade = facade;
		this.inputs = inputs;
		this.dataflow = facade.getDataflow();
		ProvenanceConnector connector = (ProvenanceConnector) (facade
				.getContext().getProvenanceReporter());
		monitorViewComponent
				.setProvenanceConnector(connector);
		if (connector != null) {
			this.runId = connector.getSessionID();
		} else {
			this.runId = UUID.randomUUID().toString();
		}
		resultsComponent = new ResultViewComponent();
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
			resultsComponent.register(facade);
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
					facade.pushData(new WorkflowDataToken("", index,
							identifier, facade.getContext()), portName);
				} catch (TokenOrderException e) {
					logger.error("Unable to push data", e);
				}
			}
		}

	}
	


	@Override
	public String toString() {
		return dataflow.getLocalName() + " "
				+ DateFormat.getTimeInstance().format(date);
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

	public Dataflow getDataflow() {
		return dataflow;
	}

	public void setDataflow(Dataflow dataflow) {
		this.dataflow = dataflow;
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

}
