package org.apache.taverna.reference.ui;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.SwingUtilities.invokeLater;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.taverna.workflowmodel.DataflowOutputPort;
import org.apache.taverna.workflowmodel.DataflowValidationReport;
import org.apache.taverna.workflowmodel.Datalink;
import org.apache.taverna.workflowmodel.TokenProcessingEntity;

// FIXME This is a t2flow-related class, not a scufl2-related one
public class InvalidDataflowReport {
	public static void invalidDataflow(DataflowValidationReport report) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><h3>Workflow failed validation due to:</h3>");
		sb.append(constructReport(report));
		showErrorDialog(sb.toString(), "Workflow validation report");
	}

	public static void showErrorDialog(final String message, final String title) {
		invokeLater(new Runnable() {
			@Override
			public void run() {
				showMessageDialog(null, message, title, ERROR_MESSAGE);
			}
		});
	}

	static String constructReport(DataflowValidationReport report) {
		StringBuilder sb = new StringBuilder();
		sb.append("<dl>");
		if (report.isWorkflowIncomplete()) {
			sb.append("<dt><b>Workflow is incomplete</b></dt>");
			sb.append("<dt><i>(Workflow should contain at least one service or a connected workflow output port)</i>");
		}
		List<? extends TokenProcessingEntity> unsatisfiedEntities = report
				.getUnsatisfiedEntities();
		if (unsatisfiedEntities.size() > 0) {
			sb.append("<dt><b>Invalid services</b>");
			sb.append("<dt><i>(Due to feedback loops in the workflow or upstream errors)</i>");
			for (TokenProcessingEntity entity : unsatisfiedEntities)
				sb.append("<dd>" + entity.getLocalName());
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
						sb.append("<dt><i>(Workflow output ports must be connected to a valid link)</i>");
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
			for (TokenProcessingEntity entity : failedEntities)
				if (!invalidDataflowProcessors.contains(entity)) {
					if (!foundfailure) {
						sb.append("<dt><b>Invalid list handling</b>");
						sb.append("<dt><i>(Generally dot product with different cardinalities)</i>");
						foundfailure = true;
					}
					sb.append("<dd>" + entity.getLocalName());
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
