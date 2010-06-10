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
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.MainWindow;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workbench.report.view.ReportOnWorkflowAction;
import net.sf.taverna.t2.workbench.ui.impl.Workbench;
import net.sf.taverna.t2.workflowmodel.Dataflow;

//import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ValidateWorkflowAction extends AbstractAction {

	//private static Logger logger = Logger.getLogger(ValidateWorkflowAction.class);

	private static final String VALIDATE_WORKFLOW = "Validate workflow";

	private FileManager fileManager = FileManager.getInstance();

	protected ReportOnWorkflowAction subAction;

	private ReportManager reportManager = ReportManager.getInstance();

	public ValidateWorkflowAction() {
		super(VALIDATE_WORKFLOW, WorkbenchIcons.searchIcon);
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
		subAction = new ReportOnWorkflowAction("", true, false);
	}

	private String namedComponent = "reportView";
	
	public void actionPerformed(ActionEvent ev) {
		subAction.actionPerformed(ev);
	
		Dataflow dataflow = fileManager.getCurrentDataflow();
		Status status = reportManager.getStatus(dataflow);
		int messageType;
		String message;
		if (status.equals(Status.OK)) {
			messageType = JOptionPane.INFORMATION_MESSAGE;
			message = "Workflow validated OK.";

		} else {
			StringBuffer sb = new StringBuffer();
			Map<Object, Set<VisitReport>> reports = reportManager.getReports(dataflow);
			// Find warnings
			for (Entry<Object, Set<VisitReport>> entry : reports.entrySet()) {
				for (VisitReport report : entry.getValue()) {
					if (report.getStatus().equals(status)) {
						sb.append(entry.getKey());
						sb.append(" ");
						sb.append(report);
					}
				}
			}
			if (status.equals(Status.WARNING)) {				
				messageType = JOptionPane.WARNING_MESSAGE;
				message = "Validation report contains warnings:\n" + sb.toString();
			} else { // SEVERE
				messageType = JOptionPane.ERROR_MESSAGE;
				message = "Validation report contains errors:\n" + sb.toString();
			}
		}
		JOptionPane.showMessageDialog(MainWindow.getMainWindow(), message, "Workflow validation", messageType);
		Workbench workbench = Workbench.getInstance();
		workbench.getPerspectives().setWorkflowPerspective();
		workbench.makeNamedComponentVisible(namedComponent);
	}

}
