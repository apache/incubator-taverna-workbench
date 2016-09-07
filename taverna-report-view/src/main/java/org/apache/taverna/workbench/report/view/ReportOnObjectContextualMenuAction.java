/**********************************************************************
 **********************************************************************/
package org.apache.taverna.workbench.report.view;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.visit.DataflowCollation;
import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.visit.VisitReport.Status;
import org.apache.taverna.workbench.MainWindow;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.ui.SwingWorkerCompletionWaiter;
import org.apache.taverna.workbench.ui.Workbench;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.Processor;

import org.apache.log4j.Logger;


public class ReportOnObjectContextualMenuAction extends AbstractContextualMenuAction {
	private ReportManager reportManager;
	private FileManager fileManager;
	private Workbench workbench;

	private static final String VALIDATE_SERVICE = "Validate service";
	private String namedComponent = "reportView";
	private EditManager editManager;

	public static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configure");

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ReportOnObjectContextualMenuAction.class);

	public ReportOnObjectContextualMenuAction() {
		super(configureSection, 43);
	}

	@Override
	public boolean isEnabled() {
	    return (super.isEnabled() && (getContextualSelection().getSelection() instanceof Processor));
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		Dataflow parent;
		if (getContextualSelection().getParent() instanceof Dataflow) {
			parent = (Dataflow)getContextualSelection().getParent();
		} else {
			parent = fileManager.getCurrentDataflow();
		}

		final Dataflow df = parent;
		return new AbstractAction(VALIDATE_SERVICE) {
			public void actionPerformed(ActionEvent e) {
			    Object o = getContextualSelection().getSelection();
			    if (o instanceof Processor) {
				Processor p = (Processor) o;
				ValidateObjectSwingWorker worker = new ValidateObjectSwingWorker(df, p, editManager, reportManager);
				ValidateObjectInProgressDialog dialog = new ValidateObjectInProgressDialog();
				worker.addPropertyChangeListener(new SwingWorkerCompletionWaiter(dialog));
				worker.execute();

				// Give a chance to the SwingWorker to finish so we do not have to display
				// the dialog if checking of the object is quick (so it won't flicker on the screen)
				try {
				    Thread.sleep(500);
				} catch (InterruptedException ex) {

				}
				if (!worker.isDone()){
				    dialog.setVisible(true); // this will block the GUI
				}

				checkStatus(df, p);
			    }
			}
		};
	}

	/**
	 * Check the status and pop up a warning if something is wrong.
	 *
	 */
    public void checkStatus(Dataflow dataflow, Processor p) {
	Status status = reportManager.getStatus(dataflow, p);
		int messageType;
		String message;
		String name = p.getLocalName();
		if (status.equals(Status.OK)) {
			messageType = JOptionPane.INFORMATION_MESSAGE;
			message = name + " validated OK.";

		} else {
			Set<VisitReport> immediateReports = reportManager
			    .getReports(dataflow, p);
			int errorCount = 0;
			int warningCount = 0;

			Set<VisitReport> reports = new HashSet<VisitReport>();
			for (VisitReport report : immediateReports) {
			    if (report.getKind() instanceof DataflowCollation) {
				reports.addAll(report.getSubReports());
			    } else {
				reports.add(report);
			    }
			}

			// Find warnings
			for (VisitReport report : reports) {
			    if (report.getStatus().equals(Status.SEVERE)) {
				errorCount++;
			    } else if (report.getStatus().equals(Status.WARNING)) {
				warningCount++;
			    }
			}
			if (status.equals(Status.WARNING)) {
				messageType = JOptionPane.WARNING_MESSAGE;
				message = "Validation of " + name + " reported ";
			} else { // SEVERE
				messageType = JOptionPane.ERROR_MESSAGE;
				message = "Validation of " + name + " reported ";
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
			} else if (warningCount > 0) {
			    message += warningCount + " warnings";
			}
		}
		JOptionPane.showMessageDialog(MainWindow.getMainWindow(), message,
				"Service validation", messageType);
		workbench.getPerspectives().setWorkflowPerspective();
		workbench.makeNamedComponentVisible(namedComponent);
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

}
