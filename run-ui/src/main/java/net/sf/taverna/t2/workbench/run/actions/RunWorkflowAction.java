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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle;
import net.sf.taverna.t2.annotation.annotationbeans.ExampleValue;
import net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ui.WorkflowLaunchPanel;
import net.sf.taverna.t2.workbench.ModelMapConstants;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
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
				ReferenceService referenceService,
				ProvenanceReporter provenanceReporter) {
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
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}

	public void actionPerformed(ActionEvent e) {
		Object model = ModelMap.getInstance().getModel(
				ModelMapConstants.CURRENT_DATAFLOW);
		if (!(model instanceof Dataflow)) {
			return;
		}
		final Dataflow dataflow = (Dataflow) model;
		Thread t = new Thread("Preparing to run workflow "
				+ dataflow.getLocalName()) {
			public void run() {
				try {
					runDataflow(dataflow);
				} catch (Exception ex) {
					String message = "Could not run workflow "
							+ dataflow.getLocalName();
					logger.warn(message);
					showErrorDialog(ex.getMessage(), message);			
				}
			};
		};
		t.setDaemon(true);
		t.start();		
	}

	protected void runDataflow(Dataflow dataflow) {
		XMLSerializer serialiser = new XMLSerializerImpl();
		XMLDeserializer deserialiser = new XMLDeserializerImpl();
		Dataflow dataflowCopy = null;
		try {
			dataflowCopy = deserialiser.deserializeDataflow(serialiser
					.serializeDataflow(dataflow));
		} catch (SerializationException e1) {
			logger.error("Unable to copy workflow", e1);
		} catch (DeserializationException e1) {
			logger.error("Unable to copy workflow", e1);
		} catch (EditException e1) {
			logger.error("Unable to copy workflow", e1);
		}

		if (dataflowCopy != null) {
			WorkflowLaunchPanel.getDataflowCopyMap()
					.put(dataflowCopy, dataflow);
			// TODO check if the database has been created and create if needed
			// if provenance turned on then add an IntermediateProvLayer to each
			// Processor
			final ReferenceService referenceService = runComponent
					.getReferenceService();
			final ReferenceContext referenceContext = null;
			ProvenanceConnector provenanceConnector = null;
			
			// FIXME: All these run-stuff should be done in a general way so it
			// could also be used when running workflows non-interactively
			if (DataManagementConfiguration.getInstance().isProvenanceEnabled()) {
				String connectorType = DataManagementConfiguration
						.getInstance().getConnectorType();

				for (ProvenanceConnectorFactory factory : ProvenanceConnectorFactoryRegistry
						.getInstance().getInstances()) {
					if (connectorType.equalsIgnoreCase(factory
							.getConnectorType())) {
						provenanceConnector = factory.getProvenanceConnector();
					}
				}

				// slight change, the init is outside but it also means that the
				// init call has to ensure that the dbURL is set correctly
				try {
					if (provenanceConnector != null) {
						provenanceConnector.init();
						provenanceConnector
								.setReferenceService(referenceService);
					}
				} catch (Exception except) {

				}
				
			}
			InvocationContextImplementation context = new InvocationContextImplementation(
					referenceService, provenanceConnector);
			if (provenanceConnector != null) {
				provenanceConnector.setInvocationContext(context);
			}
			final WorkflowInstanceFacade facade;
			try {
				facade = new EditsImpl().createWorkflowInstanceFacade(
						dataflowCopy, context, "");
			} catch (InvalidDataflowException ex) {
				invalidDataflow(ex.getDataflowValidationReport());
				return;
			}

			final List<? extends DataflowInputPort> inputPorts = dataflowCopy
					.getInputPorts();

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (!inputPorts.isEmpty()) {
						showInputDialog(facade, referenceContext);
					} else {
						switchToResultsPerspective();
						runComponent.runDataflow(facade, (Map) null);
					}
				}
			});

		} else {
			showErrorDialog("Unable to make a copy of the workflow to run",
					"Workflow copy failed");
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
		if (report.isWorkflowIncomplete()){
			sb.append("<dt><b>Workflow is incomplete</b></dt>");
			sb.append("<dt><i>(Workflow should contain at least one service or a connected workflow output port)</i>");
		}
		List<? extends TokenProcessingEntity> unsatisfiedEntities = report
				.getUnsatisfiedEntities();
		if (unsatisfiedEntities.size() > 0) {
			sb.append("<dt><b>Invalid services</b>");
			sb
					.append("<dt><i>(Due to feedback loops in the workflow or upstream errors)</i>");
			for (TokenProcessingEntity entity : unsatisfiedEntities) {
				sb.append("<dd>" + entity.getLocalName());
			}
		}
		List<? extends DataflowOutputPort> unresolvedOutputs = report
				.getUnresolvedOutputs();
		if (unresolvedOutputs.size() > 0) {
			boolean foundUnconnected = false;
			for (DataflowOutputPort dataflowOutputPort : unresolvedOutputs) {
				Datalink dl = dataflowOutputPort.getInternalInputPort()
						.getIncomingLink();
				if (dl == null) {
					if (!foundUnconnected) {
						sb.append("<dt><b>Unconnected workflow output ports</b>");
						sb
								.append("<dt><i>(Workflow output ports must be connected to a valid link)</i>");
						foundUnconnected = true;
					}
					sb.append("<dd>" + dataflowOutputPort.getName());
				}
			}
		}
		List<? extends TokenProcessingEntity> failedEntities = report
				.getFailedEntities();
		Set<TokenProcessingEntity> invalidDataflowProcessors = report
				.getInvalidDataflows().keySet();
		if (failedEntities.size() > 0) {
			boolean foundfailure = false;
			for (TokenProcessingEntity entity : failedEntities) {
				if (!invalidDataflowProcessors.contains(entity)) {
					if (!foundfailure) {
						sb.append("<dt><b>Invalid list handling</b>");
						sb
								.append("<dt><i>(Generally dot product with different cardinalities)</i>");
						foundfailure = true;
					}
					sb.append("<dd>" + entity.getLocalName());
				}
			}
		}

		Set<Entry<TokenProcessingEntity, DataflowValidationReport>> invalidDataflows = report
				.getInvalidDataflows().entrySet();
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
	private void showInputDialog(final WorkflowInstanceFacade facade,
			ReferenceContext refContext) {
		// Create and set up the window.

		String title = annotationTools.getAnnotationString(
				facade.getDataflow(), DescriptiveTitle.class, "");
		String dialogTitle = "Workflow ";
		if ((title != null) && (!title.equals(""))) {
			dialogTitle = title + ": ";
		}
		dialogTitle += "input values";
		final JDialog dialog = new JDialog((JFrame) null, dialogTitle, true);
		dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		WorkflowLaunchPanel wlp = new WorkflowLaunchPanel(facade, refContext) {
			@Override
			public void handleLaunch(Map<String, T2Reference> workflowInputs) {
				switchToResultsPerspective();
				runComponent.runDataflow(facade, workflowInputs);
				dialog.dispose();

			}

			@Override
			public void handleCancel() {
				dialog.dispose();
			}
		};
		wlp.setOpaque(true); // content panes must be opaque

		List<DataflowInputPort> inputPorts = new ArrayList<DataflowInputPort>(
				facade.getDataflow().getInputPorts());
		Collections.sort(inputPorts, new PortComparator());
		for (DataflowInputPort input : inputPorts) {
			// input.getAnnotations();

			String portDescription = annotationTools.getAnnotationString(input,
					FreeTextDescription.class, null);
			String portExample = annotationTools.getAnnotationString(input,
					ExampleValue.class, null);

			wlp.addInput(input.getName(), input.getDepth(), portDescription,
					portExample);
		}

		dialog.setContentPane(wlp);

		// Display the window.
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

	static private void showErrorDialog(final String message, final String title) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}

}
