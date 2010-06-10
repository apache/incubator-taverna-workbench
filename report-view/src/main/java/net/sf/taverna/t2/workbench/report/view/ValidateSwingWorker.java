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
package net.sf.taverna.t2.workbench.report.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.report.ReportManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;
import org.jdesktop.swingworker.SwingWorker;

public class ValidateSwingWorker extends SwingWorker<Dataflow, String>{

	private static Logger logger = Logger.getLogger(ValidateSwingWorker.class);
	private Dataflow dataflow;
	private final boolean full;
	private final boolean remember;

	public ValidateSwingWorker(Dataflow dataflow, boolean full, boolean remember){
		this.dataflow = dataflow;
		this.full = full;
		this.remember = remember;
	}
	
	@Override
	protected Dataflow doInBackground() throws Exception {

		ReportManager.getInstance().updateReport(dataflow, full, remember);
		checkDisabledActivities(dataflow);
		return dataflow;
	}

	private static boolean checkProcessorDisability(Processor processor, Set<VisitReport> reports, List<Edit<?>> editList) {
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
		if (disabledActivity.configurationWouldWork()) {
		    logger.info(processor.getLocalName() + " is no longer disabled");
		    Edit e = Tools.getEnableDisabledActivityEdit(processor, disabledActivity);
		    if (e != null) {
			editList.add(e);
			return true;
		    }
		    
		}
	    }
	    return false;
	}

	private static void checkDisabledActivities(
			Dataflow d) {
	    Set<Object> reVisit = new HashSet<Object>();
		EditManager editManager = EditManager.getInstance();
		Edits edits = editManager.getEdits();
		List<Edit<?>> editList = new ArrayList<Edit<?>>();
		Map<Object, Set<VisitReport>> reportsEntry = ReportManager.getInstance().getReports(d);
		for (Object o : reportsEntry.keySet()) {
			if (o instanceof Processor) {
			    if (checkProcessorDisability((Processor) o, reportsEntry.get(o), editList)) {
				reVisit.add((Processor)o);
			    }
			}
		}
		if (!editList.isEmpty()) {
			CompoundEdit ce = new CompoundEdit(editList);
			try {
				editManager.doDataflowEdit(d, ce);
				ReportManager.getInstance().updateObjectSetReport(d, reVisit);
			} catch (EditException e) {
				logger.error(e);
			}
		}
	}

}
