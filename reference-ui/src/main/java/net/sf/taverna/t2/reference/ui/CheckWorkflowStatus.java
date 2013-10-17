package net.sf.taverna.t2.reference.ui;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;

import uk.org.taverna.scufl2.api.profiles.Profile;
import uk.org.taverna.scufl2.validation.Status;

import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.report.config.ReportManagerConfiguration;
import net.sf.taverna.t2.workbench.ui.Workbench;

public class CheckWorkflowStatus {

	private static final long RUN_ANYWAY_EXPIRE_MILLIS = 60*60*1000; // 1 hour

	protected static Map<String, Date> runAnyways = Collections.synchronizedMap(new HashMap<String, Date>());

	private static ReportManagerConfiguration reportManagerConfig;

	public static boolean checkWorkflow(Profile dataflow, Workbench workbench, EditManager editManager, FileManager fileManager, ReportManager reportManager) {
		synchronized (runAnyways) {
			Date runAnyway = runAnyways.remove(dataflow.getIdentifier());
			Date now = new Date();
			if (runAnyway != null && (now.getTime() - runAnyway.getTime()) < RUN_ANYWAY_EXPIRE_MILLIS) {
				// new expiration time (remember we removed it above)
				runAnyways.put(dataflow.getName(), new Date());
				return true;
			}
		}

		String beforeRunSetting = reportManagerConfig
				.getProperty(ReportManagerConfiguration.BEFORE_RUN);
//		ReportOnWorkflowAction action = new ReportOnWorkflowAction("",
//				dataflow, beforeRunSetting
//						.equals(ReportManagerConfiguration.FULL_CHECK), false, editManager, fileManager, reportManager, workbench);
//		if (reportManager.isReportOutdated(dataflow)) {
//			action.validateWorkflow();
//		}
		if (!reportManager.isStructurallySound(dataflow)) {
			JOptionPane
					.showMessageDialog(
							MainWindow.getMainWindow(),
							"The workflow has problems and cannot be run - see reports",
							"Workflow problems", JOptionPane.ERROR_MESSAGE);
			showReport(workbench);
			return false;
		}
		Status status = reportManager.getStatus(dataflow);
		String queryBeforeRunSetting = reportManagerConfig
				.getProperty(ReportManagerConfiguration.QUERY_BEFORE_RUN);
		if (status.equals(Status.SEVERE)
				&& !queryBeforeRunSetting
						.equals(ReportManagerConfiguration.NONE)) {
			Object[] options = { "View validation report", "Run anyway" };

			int proceed = JOptionPane
					.showOptionDialog(
							MainWindow.getMainWindow(),
							"Taverna has detected problems with this workflow. "
									+ "To fix them, please check the validation report.",
							"Workflow problems", JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE, null, options,
							options[0]);
			if (proceed == JOptionPane.YES_OPTION) { // View validation report
				showReport(workbench);
				return false;
			} else {
				runAnyways.put(dataflow.getName(), new Date());
			}
		} else if (status.equals(Status.WARNING)
				&& queryBeforeRunSetting
						.equals(ReportManagerConfiguration.ERRORS_OR_WARNINGS)) {
			int proceed = JOptionPane.showConfirmDialog(MainWindow.getMainWindow(),
					"The workflow has warnings but can still be run "
							+ "- do you want to proceed?", "Workflow problems",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (proceed != JOptionPane.YES_OPTION) {
				showReport(workbench);
				return false;
			} else {
				runAnyways.put(dataflow.getName(), new Date());
			}
		}
		return true;
	}

	private static void showReport(Workbench workbench) {
		workbench.makeNamedComponentVisible("reportView");
	}
}
