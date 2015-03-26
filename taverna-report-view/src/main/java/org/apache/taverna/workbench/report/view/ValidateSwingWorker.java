/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
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
package org.apache.taverna.workbench.report.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingWorker;

import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workflowmodel.CompoundEdit;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.Edit;
import org.apache.taverna.workflowmodel.EditException;
import org.apache.taverna.workflowmodel.Processor;
import org.apache.taverna.workflowmodel.processor.activity.Activity;
import org.apache.taverna.workflowmodel.processor.activity.DisabledActivity;
import org.apache.taverna.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;

public class ValidateSwingWorker extends SwingWorker<Dataflow, String>{

	private static Logger logger = Logger.getLogger(ValidateSwingWorker.class);
	private Dataflow dataflow;
	private final boolean full;
	private final boolean remember;
	private final EditManager editManager;
	private final ReportManager reportManager;

	public ValidateSwingWorker(Dataflow dataflow, boolean full, boolean remember, EditManager editManager, ReportManager reportManager){
		this.dataflow = dataflow;
		this.full = full;
		this.remember = remember;
		this.editManager = editManager;
		this.reportManager = reportManager;
	}

	@Override
	protected Dataflow doInBackground() throws Exception {
		reportManager.updateReport(dataflow, full, remember);
		checkDisabledActivities(dataflow, editManager, reportManager);
		return dataflow;
	}

	public static boolean checkProcessorDisability(Processor processor, Set<VisitReport> reports, List<Edit<?>> editList, EditManager editManager, ReportManager reportManager) {
	    boolean isAlreadyDisabled = false;
	    DisabledActivity disabledActivity = null;
	    List<? extends Activity<?>> activityList = processor.getActivityList();
	    for (Activity a : activityList) {
		if (a instanceof DisabledActivity) {
		    isAlreadyDisabled = true;
		    disabledActivity = (DisabledActivity) a;
		    break;
		}
	    }
	    if (isAlreadyDisabled) {
		int severeCount = 0;
		for (VisitReport vr : reports) {
		    if (vr.getStatus().equals(VisitReport.Status.SEVERE)) {
			severeCount++;
		    }
		}
		if ((severeCount <= 1) && disabledActivity.configurationWouldWork()) {
		    logger.info(processor.getLocalName() + " is no longer disabled");
		    Edit e = Tools.getEnableDisabledActivityEdit(processor, disabledActivity, editManager.getEdits());
		    if (e != null) {
			editList.add(e);
			return true;
		    }

		}
	    }
	    return false;
	}

	private static void checkDisabledActivities(
			Dataflow d, EditManager editManager, ReportManager reportManager) {
	    Set<Object> reVisit = new HashSet<Object>();
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		Map<Object, Set<VisitReport>> reportsEntry = reportManager.getReports(d);
		for (Object o : reportsEntry.keySet()) {
			if (o instanceof Processor) {
			    if (checkProcessorDisability((Processor) o, reportsEntry.get(o), editList, editManager, reportManager)) {
				reVisit.add((Processor)o);
			    }
			}
		}
		if (!editList.isEmpty()) {
			CompoundEdit ce = new CompoundEdit(editList);
			try {
				editManager.doDataflowEdit(d, ce);
				reportManager.updateObjectSetReport(d, reVisit);
			} catch (EditException e) {
				logger.error(e);
			}
		}
	}

}
