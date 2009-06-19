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
package net.sf.taverna.t2.workbench.run.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.annotation.annotationbeans.ExampleValue;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.provenance.ProvenanceConnectorRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ui.WorkflowLaunchPanel;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.provenance.ProvenanceConfiguration;
import net.sf.taverna.t2.workbench.run.DataflowRunsComponent;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;
import net.sf.taverna.t2.workflowmodel.utils.AnnotationTools;
import net.sf.taverna.t2.workflowmodel.utils.PortComparator;

import org.apache.log4j.Logger;

public class RunWorkflowAction extends AbstractAction {

	private final class InvocationContextImplementation implements
			InvocationContext {
		private final ReferenceService referenceService;
		
		private final ProvenanceReporter provenanceReporter;

		private InvocationContextImplementation(
				ReferenceService referenceService, ProvenanceReporter provenanceReporter) {
			this.referenceService = referenceService;
			this.provenanceReporter = provenanceReporter;
		}

		public ReferenceService getReferenceService() {
			return referenceService;
		}

		public <T> List<? extends T> getEntities(Class<T> entityType) {
			// TODO Auto-generated method stub
			return null;
		}

		public ProvenanceReporter getProvenanceReporter() {
			return provenanceReporter;
		}
	}

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(RunWorkflowAction.class);

	private DataflowRunsComponent runComponent;

	private PerspectiveSPI resultsPerspective;

	public RunWorkflowAction() {
		runComponent = DataflowRunsComponent.getInstance();
		putValue(SMALL_ICON, WorkbenchIcons.runIcon);
		putValue(NAME, "Run workflow...");
		putValue(SHORT_DESCRIPTION, "Run the current workflow");

	}

	public void actionPerformed(ActionEvent e) {
		Object model = ModelMap.getInstance().getModel(
				ModelMapConstants.CURRENT_DATAFLOW);
		if (model instanceof Dataflow) {
			Dataflow dataflow = (Dataflow) model;
			XMLSerializer serialiser = new XMLSerializerImpl();
			XMLDeserializer deserialiser = new XMLDeserializerImpl();
			Dataflow dataflowCopy = null;
			try {
				dataflowCopy = deserialiser.deserializeDataflow(serialiser
						.serializeDataflow(dataflow));
			} catch (SerializationException e1) {
				logger.error("Unable to copy dataflow", e1);
			} catch (DeserializationException e1) {
				logger.error("Unable to copy dataflow", e1);
			} catch (EditException e1) {
				logger.error("Unable to copy dataflow", e1);
			}
			

			if (dataflowCopy != null) {
				WorkflowLaunchPanel.getDataflowCopyMap().put(dataflowCopy, dataflow);
				//TODO check if the database has been created and create if needed
				//if provenance turned on then add an IntermediateProvLayer to each Processor
				final ReferenceService referenceService = runComponent.getReferenceService();
				ReferenceContext referenceContext = null;
				ProvenanceConnector provenanceConnector = null;
				if (ProvenanceConfiguration.getInstance().getProperty("enabled").equalsIgnoreCase("yes")) {
					String connectorType = ProvenanceConfiguration.getInstance().getProperty("connector");

					for (ProvenanceConnector connector:ProvenanceConnectorRegistry.getInstance().getInstances()) {
						if (connectorType.equalsIgnoreCase(connector.getName())) {
							provenanceConnector = connector;
						}
					}
					logger.info("Provenance being captured using: " + 
							provenanceConnector);
					String dbURL = ProvenanceConfiguration.getInstance().getProperty("dbURL");
//					String user = ProvenanceConfiguration.getInstance().getProperty("dbUser");
//					String password = ProvenanceConfiguration.getInstance().getProperty("dbPassword");
					
					if (dbURL != null) {
						//FIXME if dburl does not exist then throw exception
						provenanceConnector.setDbURL(dbURL);	
					}
					//slight change, the init is outside but it also means that the init call has to ensure that the dbURL
					//is set correctly
					provenanceConnector.init();
//					String jdbcString = dbURL + "/T2Provenance" + "?user=" + user + "&password=" + password;
//						provenanceConnector.setDBLocation(dbURL);
//						provenanceConnector.setPassword(password);
//						provenanceConnector.setUser(user);
//					provenanceConnector.setDBLocation(jdbcString);					
//					} 
//					provenanceConnector.init();
					provenanceConnector.setReferenceService(referenceService);
					
				}
				InvocationContextImplementation context = new InvocationContextImplementation(
						referenceService, provenanceConnector);
				if (provenanceConnector != null) {
					provenanceConnector.setInvocationContext(context);
				}
				WorkflowInstanceFacade facade;
				try {
					facade = new EditsImpl().createWorkflowInstanceFacade(
							dataflowCopy, context, "");
				} catch (InvalidDataflowException ex) {
					invalidDataflow(ex.getDataflowValidationReport());
					return;
				}

				List<? extends DataflowInputPort> inputPorts = dataflowCopy
						.getInputPorts();
				if (!inputPorts.isEmpty()) {
					showInputDialog(facade, referenceContext);
				} else {
					switchToResultsPerspective();
					runComponent.runDataflow(facade, (Map) null);
				}

			} else {
				showErrorDialog("Unable to make a copy of the workflow to run",
						"Workflow copy failed");
			}
		}

	}

	static void invalidDataflow(DataflowValidationReport report) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><h3>Workflow failed validation due to:</h3>");
		sb.append(constructReport(report));
		showErrorDialog(sb.toString(), "Workflow validation report");
	}
	
	static private String constructReport(DataflowValidationReport report) {
		StringBuilder sb = new StringBuilder();
		sb.append("<dl>");
		List<? extends TokenProcessingEntity> unsatisfiedEntities = report
				.getUnsatisfiedEntities();
		if (unsatisfiedEntities.size() > 0) {
			sb.append("<dt><b>Invalid processors</b>");
			sb.append("<dt><i>(Due to cyclic dependencies or upstream errors)</i>");
			for (TokenProcessingEntity entity : unsatisfiedEntities) {
				sb.append("<dd>" + entity.getLocalName());
			}
		}
		List<? extends DataflowOutputPort> unresolvedOutputs = report
				.getUnresolvedOutputs();
		if (unresolvedOutputs.size() > 0) {
			boolean foundUnconnected = false;
			for (DataflowOutputPort dataflowOutputPort : unresolvedOutputs) {
				Datalink dl = dataflowOutputPort.getInternalInputPort().getIncomingLink();
				if (dl == null) {
					if (!foundUnconnected) {
						sb.append("<dt><b>Unconnected workflow outputs</b>");
						sb.append("<dt><i>(Workflow outputs must be connected to a valid link)</i>");
						foundUnconnected = true;
					}
					sb.append("<dd>" + dataflowOutputPort.getName());
				}
			}
		}
		List<? extends TokenProcessingEntity> failedEntities = report
				.getFailedEntities();
		Set<TokenProcessingEntity> invalidDataflowProcessors = report.getInvalidDataflows().keySet();
		if (failedEntities.size() > 0) {
			boolean foundfailure = false;
			for (TokenProcessingEntity entity : failedEntities) {
				if (!invalidDataflowProcessors.contains(entity)) {
					if (!foundfailure) {
						sb.append("<dt><b>Invalid iteration strategies</b>");
						sb.append("<dt><i>(Generally dot product with different cardinalities)</i>");
						foundfailure = true;
					}
					sb.append("<dd>" + entity.getLocalName());
				}
			}
		}
		
		Set<Entry<TokenProcessingEntity, DataflowValidationReport>> invalidDataflows = report.getInvalidDataflows().entrySet();
		if (invalidDataflows.size() > 0) {
			sb.append("<dt><b>Invalid nested workflows</b>");
			for (Entry<TokenProcessingEntity, DataflowValidationReport> entry : invalidDataflows) {
				sb.append("<dd>" + entry.getKey().getLocalName());
				sb.append(constructReport(entry.getValue()));
			}
		}
		sb.append("</dl>");
		return sb.toString();
	}

	private void switchToResultsPerspective() {
		if (resultsPerspective == null) {
			for (PerspectiveSPI perspective : Workbench.getInstance()
					.getPerspectives().getPerspectives()) {
				if (perspective.getText().equalsIgnoreCase("results")) {
					resultsPerspective = perspective;
					break;
				}
			}
		}
		if (resultsPerspective != null) {
			ModelMap.getInstance().setModel(
					ModelMapConstants.CURRENT_PERSPECTIVE, resultsPerspective);
		}
	}

	private AnnotationTools annotationTools = new AnnotationTools();

	
	@SuppressWarnings("serial")
	private void showInputDialog(final WorkflowInstanceFacade facade, ReferenceContext refContext) {
		// Create and set up the window.
		final JFrame frame = new JFrame("Workflow input values");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		WorkflowLaunchPanel wlp = new WorkflowLaunchPanel(facade, refContext) {
			@Override
			public void handleLaunch(Map<String, T2Reference> workflowInputs) {
				switchToResultsPerspective();
				runComponent.runDataflow(facade, workflowInputs);
				frame.dispose();
			
			}
		};
		wlp.setOpaque(true); // content panes must be opaque


		
		List<DataflowInputPort> inputPorts = new ArrayList<DataflowInputPort>(facade.getDataflow().getInputPorts());
		Collections.sort(inputPorts, new PortComparator());
		for (DataflowInputPort input : inputPorts) {
//			input.getAnnotations();
			
			String portDescription = annotationTools.getAnnotationString(input, FreeTextDescription.class, null);
			String portExample = annotationTools.getAnnotationString(input, ExampleValue.class, null);
			
			wlp.addInput(input.getName(), input.getDepth(), portDescription, portExample);
		}

		frame.setContentPane(wlp);
		wlp.setFrame(frame);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	static private void showErrorDialog(String message, String title) {
		JOptionPane.showMessageDialog(null, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

}
