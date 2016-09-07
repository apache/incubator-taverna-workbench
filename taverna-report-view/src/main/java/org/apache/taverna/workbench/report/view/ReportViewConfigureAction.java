/**
 *
 */
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.ui.workflowview.WorkflowView;
import org.apache.taverna.workflowmodel.Processor;

/**
 * @author alanrw
 *
 */
public class ReportViewConfigureAction extends AbstractAction {

	private Processor configuredProcessor = null;
	private MenuManager menuManager;

	public ReportViewConfigureAction() {

	}

	public void setConfiguredProcessor(Processor configuredProcessor, MenuManager menuManager) {
		this.configuredProcessor = configuredProcessor;
		this.menuManager = menuManager;
	}

	public ReportViewConfigureAction(Processor p) {
		super();
		this.configuredProcessor = p;
	}

	public void actionPerformed(ActionEvent e) {
		Action action = WorkflowView.getConfigureAction(configuredProcessor, menuManager);
		if (action != null) {
			action.actionPerformed(e);
		}
	}

}
