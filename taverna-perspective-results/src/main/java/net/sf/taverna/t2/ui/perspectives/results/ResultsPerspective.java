/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.ui.perspectives.results;

import static net.sf.taverna.t2.workbench.icons.WorkbenchIcons.resultsPerspectiveIcon;

import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import uk.org.taverna.configuration.app.ApplicationConfiguration;
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
			File runStore = new File(
					applicationConfiguration.getApplicationHomeDir(),
					RUN_STORE_DIRECTORY);
			runStore.mkdirs();
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
