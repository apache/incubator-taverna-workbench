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

package org.apache.taverna.ui.perspectives.results;

import static org.apache.taverna.workbench.icons.WorkbenchIcons.resultsPerspectiveIcon;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.apache.taverna.renderers.RendererRegistry;
import org.apache.taverna.workbench.activityicons.ActivityIconManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.selection.SelectionManager;
import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;
import org.apache.taverna.workbench.views.results.saveactions.SaveAllResultsSPI;
import org.apache.taverna.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
public class ResultsPerspective implements PerspectiveSPI, EventHandler {
	private static final String RUN_STORE_DIRECTORY = "workflow-runs";

	private ResultsPerspectiveComponent resultsPerspectiveComponent;
	@SuppressWarnings("unused")
	private RunMonitor runMonitor;
	private RunService runService;
	private SelectionManager selectionManager;
	private ColourManager colourManager;
	private ActivityIconManager activityIconManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private RendererRegistry rendererRegistry;
	private List<SaveAllResultsSPI> saveAllResultsSPIs;
	private List<SaveIndividualResultSPI> saveIndividualResultSPIs;
	private ApplicationConfiguration applicationConfiguration;

	@Override
	public String getID() {
		return ResultsPerspective.class.getName();
	}

	@Override
	public JComponent getPanel() {
		if (resultsPerspectiveComponent == null) {
			Path runStore = applicationConfiguration.getApplicationHomeDir().resolve(RUN_STORE_DIRECTORY);			
			resultsPerspectiveComponent = new ResultsPerspectiveComponent(
					runService, selectionManager, colourManager,
					activityIconManager, workbenchConfiguration,
					rendererRegistry, saveAllResultsSPIs,
					saveIndividualResultSPIs, runStore);
			runMonitor = new RunMonitor(runService, selectionManager,
					resultsPerspectiveComponent);
		}
		return resultsPerspectiveComponent;
	}

//	public void loadWorkflowRuns(File runStoreDirectory) {
//		if (runStoreDirectory.exists()) {
//			for (File runFile : runStoreDirectory.listFiles(new RunFileFilter())) {
//				try {
//					runService.open(runFile);
//				} catch (IOException e) {
//				}
//			}
//		}
//	}

	@Override
	public ImageIcon getButtonIcon() {
		return resultsPerspectiveIcon;
	}

	@Override
	public String getText() {
		return "Results";
	}

	@Override
	public int positionHint() {
		return 30;
	}

	@Override
	public void handleEvent(Event event) {
		if (resultsPerspectiveComponent != null)
			resultsPerspectiveComponent.handleEvent(event);
	}

	public void setRunService(RunService runService) {
		this.runService = runService;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	public void setWorkbenchConfiguration(
			WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setRendererRegistry(RendererRegistry rendererRegistry) {
		this.rendererRegistry = rendererRegistry;
	}

	public void setSaveAllResultsSPIs(List<SaveAllResultsSPI> saveAllResultsSPIs) {
		this.saveAllResultsSPIs = saveAllResultsSPIs;
	}

	public void setSaveIndividualResultSPIs(
			List<SaveIndividualResultSPI> saveIndividualResultSPIs) {
		this.saveIndividualResultSPIs = saveIndividualResultSPIs;
	}

	public void setApplicationConfiguration(
			ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}
}
