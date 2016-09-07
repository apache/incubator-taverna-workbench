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
/*

package org.apache.taverna.workbench.ui.workflowexplorer;

import javax.swing.ImageIcon;

import org.apache.taverna.services.ServiceRegistry;

import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.report.ReportManager;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.zaria.UIComponentFactorySPI;
import org.apache.taverna.workbench.ui.zaria.UIComponentSPI;

/**
 * Workflow Explorer factory.
 * 
 * @author Alex Nenadic
 * @author David Withers
 */
public class WorkflowExplorerFactory implements UIComponentFactorySPI {
	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;
	private ReportManager reportManager;
	private SelectionManager selectionManager;
	private ActivityIconManager activityIconManager;
	private ServiceRegistry serviceRegistry;

	@Override
	public UIComponentSPI getComponent() {
		return new WorkflowExplorer(editManager, fileManager, menuManager,
				reportManager, selectionManager, activityIconManager,
				serviceRegistry);
	}

	@Override
	public ImageIcon getIcon() {
		return null;
	}

	@Override
	public String getName() {
		return "Workflow Explorer";
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setReportManager(ReportManager reportManager) {
		this.reportManager = reportManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
}
