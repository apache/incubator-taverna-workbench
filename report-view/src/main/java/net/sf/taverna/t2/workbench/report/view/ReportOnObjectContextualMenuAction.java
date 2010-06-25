/**********************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
 **********************************************************************/
package net.sf.taverna.t2.workbench.report.view;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.raven.log.Log;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.visit.DataflowCollation;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.jdesktop.swingworker.SwingWorkerCompletionWaiter;

public class ReportOnObjectContextualMenuAction extends AbstractContextualMenuAction {
	private static final ReportManager reportManager = ReportManager.getInstance();
	private static final FileManager fileManager = FileManager.getInstance();
	
	private static final String VALIDATE_SERVICE = "Validate service";
	private String namedComponent = "reportView";

	public static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configure");

	@SuppressWarnings("unused")
	private static Log logger = Log.getLogger(ReportOnObjectContextualMenuAction.class);

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
		Status status = reportManager.getStatus(parent, getContextualSelection().getSelection());
		
		final Dataflow df = parent;
		return new AbstractAction(VALIDATE_SERVICE) {
			public void actionPerformed(ActionEvent e) {
			    Object o = getContextualSelection().getSelection();
			    if (o instanceof Processor) {
				Processor p = (Processor) o;
				ValidateObjectSwingWorker worker = new ValidateObjectSwingWorker(df, p);
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
			StringBuffer sb = new StringBuffer();
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
				message = "Reports on " + name + " contain ";
			} else { // SEVERE
				messageType = JOptionPane.ERROR_MESSAGE;
				message = "Reports on " + name + " contain ";
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
		Workbench workbench = Workbench.getInstance();
		workbench.getPerspectives().setWorkflowPerspective();
		workbench.makeNamedComponentVisible(namedComponent);
	}

}
