/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.views.results.processor;

import java.awt.Color;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.lineageservice.Dependencies;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

/**
 * Component that shows intermediate results for a processor, including all
 * its iterations.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class ProcessorResultsComponent extends JPanel{

	
	private Dataflow dataflow;
	private Processor processor;
	private WorkflowInstanceFacade facade;
	private ProvenanceConnector provenanceConnector;
	private ReferenceService referenceService;
	private String targetWorkflowID;
	
	private Logger logger = Logger.getLogger(ProcessorResultsComponent.class);
	private List<LineageQueryResultRecord> intermediateValues;

	public ProcessorResultsComponent(){
		super();
		setBorder(new LineBorder(Color.GREEN));
	}

	/**
	 * Intermediate results viewing component for a currently running
	 * workflow when provenance is switched on.
	 */
	public ProcessorResultsComponent(Processor processor,
			Dataflow dataflow, WorkflowInstanceFacade facade,
			ProvenanceConnector connector, ReferenceService referenceService) {
		super();
		setBorder(new LineBorder(Color.GREEN));
		
		this.dataflow = dataflow;
		this.processor = processor;
		this.facade = facade;
		this.provenanceConnector = connector;
		this.referenceService = referenceService;
		
		// Create results tree				
		// Is this processor inside a nested workflow?
		if (processor.getActivityList().get(0) instanceof NestedDataflow) {
			Activity<?> activity = processor.getActivityList().get(0);
			targetWorkflowID = ((NestedDataflow)activity).getNestedDataflow().getInternalIdentifier(false);
			}
		else {	
			targetWorkflowID = dataflow.getInternalIdentifier(false);
		}
		
		try {
			logger.info("Retrieving intermediate results for workflow instance: "
							+ facade.getWorkflowRunId()
							+ " processor: "
							+ processor.getLocalName()
							+ " nested: " + targetWorkflowID);																
			
			ProvenanceAccess provenanceAccess = new ProvenanceAccess(DataManagementConfiguration.getInstance().getConnectorType());
			//TODO use the new provenance access API with the nested workflow if required to get the results
			
			List<ProcessorEnactment> processorInvocations = provenanceAccess.getProcessorEnactments(facade.getWorkflowRunId(), processor.getLocalName());
			for (ProcessorEnactment processorInvocation : processorInvocations){
				processorInvocation.getIteration();
			}
			
			
//			Dependencies fetchPortData = provenanceAccess.fetchPortData(facade.getWorkflowRunId(), targetWorkflowID, processor.getLocalName(), null, null);
//			intermediateValues = fetchPortData.getRecords();
//
//			if (intermediateValues.size() > 0) {
//				for (LineageQueryResultRecord record : intermediateValues) {
//					logger.info("LQRR: "
//							+ record.toString());
//				}
//				provResultsPanel
//						.setLineageRecords(intermediateValues);
//				logger
//						.info("Intermediate results retrieved for workflow instance: "
//								+ facade.getWorkflowRunId()
//								+ " processor: "
//								+ processor.getLocalName()
//								+ " nested: " + targetWorkflowID);										
//			} else {
//				frame.setTitle("Currently no intermediate values for service "
//						+ localName + ". Click \'Fetch values\' to try again.");
//				frame.setVisible(true);
//				
//			}
//
		} catch (Exception e) {
			logger.warn("Could not retrieve intermediate results: "
							+ e.getStackTrace());
			JOptionPane.showMessageDialog(null,
					"Could not retrieve intermediate results:\n"
							+ e,
					"Problem retrieving results",
					JOptionPane.ERROR_MESSAGE);
		}

		
		// Start querying provenance from time to time for new results
		// for this processor until it finishes execution and all
		// its iterations.
		
	}

	public void setLabel(String localName) {
		add(new JLabel("Intermediate results for " + localName));
	}
}
