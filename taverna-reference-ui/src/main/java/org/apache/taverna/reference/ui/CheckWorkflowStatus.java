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

import static java.util.Collections.synchronizedMap;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;
import static org.apache.taverna.workbench.MainWindow.getMainWindow;
import static org.apache.taverna.workbench.report.config.ReportManagerConfiguration.BEFORE_RUN;
import static org.apache.taverna.workbench.report.config.ReportManagerConfiguration.ERRORS_OR_WARNINGS;
import static org.apache.taverna.workbench.report.config.ReportManagerConfiguration.NONE;
import static org.apache.taverna.workbench.report.config.ReportManagerConfiguration.QUERY_BEFORE_RUN;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.scufl2.api.profiles.Profile;
import org.apache.taverna.scufl2.validation.Status;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.report.config.ReportManagerConfiguration;
import org.apache.taverna.workbench.ui.Workbench;

public class CheckWorkflowStatus {
	private static final long RUN_ANYWAY_EXPIRE_MILLIS = 60*60*1000; // 1 hour
	protected static Map<String, Date> runAnyways = synchronizedMap(new HashMap<String, Date>());

	private static ReportManagerConfiguration reportManagerConfig;

	public static boolean checkWorkflow(Profile dataflow, Workbench workbench,
			EditManager editManager, FileManager fileManager,
			ReportManager reportManager) {
		synchronized (runAnyways) {
			Date runAnyway = runAnyways.remove(dataflow.getIdentifier());
			Date now = new Date();
			if (runAnyway != null
					&& now.getTime() - runAnyway.getTime() < RUN_ANYWAY_EXPIRE_MILLIS) {
				// new expiration time (remember we removed it above)
				runAnyways.put(dataflow.getName(), new Date());
				return true;
			}
		}

		@SuppressWarnings("unused")
		String beforeRunSetting = reportManagerConfig.getProperty(BEFORE_RUN);
//		ReportOnWorkflowAction action = new ReportOnWorkflowAction("",
//				dataflow, beforeRunSetting
//						.equals(ReportManagerConfiguration.FULL_CHECK), false, editManager, fileManager, reportManager, workbench);
//		if (reportManager.isReportOutdated(dataflow))
//			action.validateWorkflow();
		if (!reportManager.isStructurallySound(dataflow)) {
			showMessageDialog(
					getMainWindow(),
					"The workflow has problems and cannot be run - see reports",
					"Workflow problems", ERROR_MESSAGE);
			showReport(workbench);
			return false;
		}
		Status status = reportManager.getStatus(dataflow);
		String queryBeforeRun = reportManagerConfig
				.getProperty(QUERY_BEFORE_RUN);
		if (status.equals(Status.SEVERE) && !queryBeforeRun.equals(NONE)) {
			Object[] options = { "View validation report", "Run anyway" };
			if (showOptionDialog(
					getMainWindow(),
					"Taverna has detected problems with this workflow. "
							+ "To fix them, please check the validation report.",
					"Workflow problems", YES_NO_OPTION, ERROR_MESSAGE, null,
					options, options[0]) == YES_OPTION) {
				// View validation report
				showReport(workbench);
				return false;
			}
			runAnyways.put(dataflow.getName(), new Date());
		} else if (status.equals(Status.WARNING)
				&& queryBeforeRun.equals(ERRORS_OR_WARNINGS)) {
			if (showConfirmDialog(getMainWindow(),
					"The workflow has warnings but can still be run "
							+ "- do you want to proceed?", "Workflow problems",
					YES_NO_OPTION, WARNING_MESSAGE) != YES_OPTION) {
				showReport(workbench);
				return false;
			}
			runAnyways.put(dataflow.getName(), new Date());
		}
		return true;
	}

	private static void showReport(Workbench workbench) {
		workbench.makeNamedComponentVisible("reportView");
	}
}
