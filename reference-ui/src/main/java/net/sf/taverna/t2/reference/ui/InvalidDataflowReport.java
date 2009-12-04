package net.sf.taverna.t2.reference.ui;

import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.TokenProcessingEntity;

public class InvalidDataflowReport {

	public static void invalidDataflow(DataflowValidationReport report) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><h3>Workflow failed validation due to:</h3>");
		sb.append(InvalidDataflowReport.constructReport(report));
		InvalidDataflowReport.showErrorDialog(sb.toString(), "Workflow validation report");
	}

	public static void showErrorDialog(final String message, final String title) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.ERROR_MESSAGE);
			}
		});
	}

	static String constructReport(DataflowValidationReport report) {
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

}
