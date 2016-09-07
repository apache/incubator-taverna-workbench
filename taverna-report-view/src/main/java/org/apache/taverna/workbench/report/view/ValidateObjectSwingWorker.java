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
import java.util.List;

import javax.swing.SwingWorker;

import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workflowmodel.CompoundEdit;
import org.apache.taverna.workflowmodel.Dataflow;
import org.apache.taverna.workflowmodel.Edit;
import org.apache.taverna.workflowmodel.EditException;
import org.apache.taverna.workflowmodel.Processor;

import org.apache.log4j.Logger;

public class ValidateObjectSwingWorker extends SwingWorker<String, String>{

	private static Logger logger = Logger.getLogger(ValidateObjectSwingWorker.class);

	private Dataflow df;
	private Processor p;

	private final EditManager editManager;
	private final ReportManager reportManager;

	public ValidateObjectSwingWorker(Dataflow df, Processor p, EditManager editManager, ReportManager reportManager){
	    this.df = df;
	    this.p = p;
		this.editManager = editManager;
		this.reportManager = reportManager;
	}

	@Override
	protected String doInBackground() throws Exception {
	    reportManager.updateObjectReport(df, p);
	    List<Edit<?>> editList = new ArrayList<Edit<?>>();
	    try {
		if (ValidateSwingWorker.checkProcessorDisability(p, reportManager.getReports(df).get(p), editList, editManager, reportManager)) {
			editManager.doDataflowEdit(df, new CompoundEdit(editList));
		    reportManager.updateObjectReport(df, p);
		}
	    }
	    catch (EditException ex) {
		logger.error("Enabled of disabled activity failed", ex);
	    }
	    return "done";
	}

}
