package net.sf.taverna.t2.reference.ui;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.report.config.ReportManagerConfiguration;
import net.sf.taverna.t2.workbench.report.view.ReportOnWorkflowAction;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class CheckWorkflowStatus {
	
	private static final long RUN_ANYWAY_EXPIRE_MILLIS = 60*60*1000; // 1 hour

	protected static Map<String, Date> runAnyways = Collections.synchronizedMap(new HashMap<String, Date>()); 
	private static ReportManager reportManager = ReportManager.getInstance();
	
	private static ReportManagerConfiguration reportManagerConfig = ReportManagerConfiguration.getInstance();
	
	@SuppressWarnings("static-access")
	public static boolean checkWorkflow(Dataflow dataflow) {
		synchronized (runAnyways) {
			Date runAnyway = runAnyways.remove(dataflow.getIdentifier());
			Date now = new Date();
			if (runAnyway != null && (now.getTime() - runAnyway.getTime()) < RUN_ANYWAY_EXPIRE_MILLIS) {
				// new expiration time (remember we removed it above)
				runAnyways.put(dataflow.getIdentifier(), new Date());
				return true;		
			}
		}
		
		String beforeRunSetting = reportManagerConfig
				.getProperty(ReportManagerConfiguration.BEFORE_RUN);		
		ReportOnWorkflowAction action = new ReportOnWorkflowAction("",
				dataflow, beforeRunSetting
						.equals(ReportManagerConfiguration.FULL_CHECK), false);
		if (reportManager.isReportOutdated(dataflow)) {
			action.validateWorkflow();
		}
		if (!reportManager.isStructurallySound(dataflow)) {
			JOptionPane
					.showMessageDialog(
							MainWindow.getMainWindow(),
							"The workflow has problems and cannot be run - see reports",
							"Workflow problems", JOptionPane.ERROR_MESSAGE);
			showReport();
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
				showReport();
				return false;
			} else {
				runAnyways.put(dataflow.getIdentifier(), new Date());
			}
		} else if (status.equals(Status.WARNING)
				&& queryBeforeRunSetting
						.equals(ReportManagerConfiguration.ERRORS_OR_WARNINGS)) {
			int proceed = JOptionPane.showConfirmDialog(MainWindow.getMainWindow(),
					"The workflow has warnings but can still be run "
							+ "- do you want to proceed?", "Workflow problems",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (proceed != JOptionPane.YES_OPTION) {
				showReport();
				return false;
			} else {
				runAnyways.put(dataflow.getIdentifier(), new Date());
			}
		}
		return true;
	}

	private static void showReport() {		
		Workbench.getInstance().getPerspectives().setWorkflowPerspective();
		Workbench.getInstance().makeNamedComponentVisible("reportView");
	}
}
