/**
 * 
 */
package net.sf.taverna.t2.workbench.report.view;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdesktop.swingworker.SwingWorkerCompletionWaiter;

/**
 * @author alanrw
 * 
 */
public class ReportOnWorkflowAction extends AbstractAction {

	private final boolean includeTimeConsuming;
	private final boolean remember;
	private Dataflow specifiedDataflow;

	private FileManager fileManager = FileManager.getInstance();
	private ReportManager reportManager = ReportManager.getInstance();
	private String namedComponent = "reportView";

	public ReportOnWorkflowAction(String name, boolean includeTimeConsuming,
			boolean remember) {
		super(name);
		this.includeTimeConsuming = includeTimeConsuming;
		this.remember = remember;
		this.specifiedDataflow = null;
	}

	public ReportOnWorkflowAction(String name, Dataflow dataflow,
			boolean includeTimeConsuming, boolean remember) {
		super(name);
		this.specifiedDataflow = dataflow;
		this.includeTimeConsuming = includeTimeConsuming;
		this.remember = remember;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (validateWorkflow()) {
			checkStatus();
		}
	}

	/**
	 * Check the status and pop up a warning if something is wrong.
	 * 
	 */
	public void checkStatus() {
		Dataflow dataflow;
		if (specifiedDataflow == null) {
			dataflow = fileManager.getCurrentDataflow();
		} else {
			dataflow = specifiedDataflow;
		}
		Status status = reportManager.getStatus(dataflow);
		int messageType;
		String message;
		if (status.equals(Status.OK)) {
			messageType = JOptionPane.INFORMATION_MESSAGE;
			message = "Workflow validated OK.";

		} else {
			StringBuffer sb = new StringBuffer();
			Map<Object, Set<VisitReport>> reports = reportManager
					.getReports(dataflow);
			int errorCount = 0;
			int warningCount = 0;
			// Find warnings
			for (Entry<Object, Set<VisitReport>> entry : reports.entrySet()) {
				for (VisitReport report : entry.getValue()) {
				    if (report.getStatus().equals(Status.SEVERE)) {
					errorCount++;
				    } else if (report.getStatus().equals(Status.WARNING)) {
					warningCount++;
				    }
				}
			}
			if (status.equals(Status.WARNING)) {
				messageType = JOptionPane.WARNING_MESSAGE;
				message = "Validation report contains ";
			} else { // SEVERE
				messageType = JOptionPane.ERROR_MESSAGE;
				message = "Validation report contains ";
			        if (errorCount == 1) {
				    message += "one error";
				} else {
				    message += errorCount + " errors";
				}
				if (warningCount != 0) {
				    message += " and ";
				}
			}
			if (warningCount == 1) {
			    message += "one warning";
			} else {
			    message += warningCount + " warnings";
			}
		}
		JOptionPane.showMessageDialog(MainWindow.getMainWindow(), message,
				"Workflow validation", messageType);
		Workbench workbench = Workbench.getInstance();
		workbench.getPerspectives().setWorkflowPerspective();
		workbench.makeNamedComponentVisible(namedComponent);
	}

	/**
	 * Perform validation on workflow.
	 * 
	 * @return <code>true</code> if the validation was not cancelled.
	 */
	public boolean validateWorkflow() {
		Dataflow dataflow;
		if (specifiedDataflow == null) {
			dataflow = fileManager.getCurrentDataflow();
		} else {
			dataflow = specifiedDataflow;
		}
		ValidateSwingWorker validateSwingWorker = new ValidateSwingWorker(
				dataflow, includeTimeConsuming, remember);
		ValidateInProgressDialog dialog = new ValidateInProgressDialog();
		validateSwingWorker
				.addPropertyChangeListener(new SwingWorkerCompletionWaiter(
						dialog));
		validateSwingWorker.execute();

		// Give a chance to the SwingWorker to finish so we do not have to
		// display
		// the dialog if copying of the workflow is quick (so it won't flicker
		// on the screen)
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {

		}
		if (!validateSwingWorker.isDone()) {
			dialog.setVisible(true); // this will block the GUI
		}
		boolean userCancelled = dialog.hasUserCancelled(); // see if user
		// cancelled the
		// dialog

		if (userCancelled) {
			validateSwingWorker.cancel(true);
		}
		return !userCancelled;

	}

}
