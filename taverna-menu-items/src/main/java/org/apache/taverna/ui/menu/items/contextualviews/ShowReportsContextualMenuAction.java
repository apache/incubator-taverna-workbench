package org.apache.taverna.ui.menu.items.contextualviews;
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import org.apache.taverna.lang.ui.icons.Icons;
import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.Workbench;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.validation.Status;

public class ShowReportsContextualMenuAction extends AbstractContextualMenuAction {

	private static final String SHOW_REPORTS = "Show validation report";
	private String namedComponent = "reportView";
	private ReportManager reportManager;
	private Workbench workbench;
	private SelectionManager selectionManager;

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ShowReportsContextualMenuAction.class);

	public ShowReportsContextualMenuAction() {
		/** Right below ShowDetailsContextualMenuAction
		 */
		super(ConfigureSection.configureSection, 41);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		WorkflowBundle parent;
		if (getContextualSelection().getParent() instanceof Workflow) {
			parent = ((Workflow)getContextualSelection().getParent()).getParent();
		} else {
			parent = selectionManager.getSelectedWorkflowBundle();
		}
		Status status = Status.OK;
		if (reportManager != null) {
//			status = reportManager.getStatus(parent.getMainProfile(), (WorkflowBean) getContextualSelection().getSelection());
		}

		Icon icon = null;
		if (status == Status.WARNING) {
			icon = Icons.warningIcon;
		} else if (status == Status.SEVERE) {
			icon = Icons.severeIcon;
		}

		return new AbstractAction(SHOW_REPORTS, icon) {
			public void actionPerformed(ActionEvent e) {
				workbench.makeNamedComponentVisible(namedComponent);
			}
		};
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

}
