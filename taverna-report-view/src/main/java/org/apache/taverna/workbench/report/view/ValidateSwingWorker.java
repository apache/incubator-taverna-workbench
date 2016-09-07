package org.apache.taverna.workbench.report.view;
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
