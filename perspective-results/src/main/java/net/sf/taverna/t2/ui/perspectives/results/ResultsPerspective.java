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

import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveAllResultsSPI;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import uk.org.taverna.platform.run.api.RunService;

/**
 * @author David Withers
 */
public class ResultsPerspective implements PerspectiveSPI, EventHandler {

	private ResultsPerspectiveComponent resultsPerspectiveComponent;
	private RunMonitor runMonitor;

	private RunService runService;
	private SelectionManager selectionManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;
	private RendererRegistry rendererRegistry;
	private List<SaveAllResultsSPI> saveAllResultsSPIs;
	private List<SaveIndividualResultSPI> saveIndividualResultSPIs;

	@Override
	public String getID() {
		return ResultsPerspective.class.getName();
	}

	@Override
	public JComponent getPanel() {
		if (resultsPerspectiveComponent == null) {
			resultsPerspectiveComponent = new ResultsPerspectiveComponent(runService,
					selectionManager, colourManager, workbenchConfiguration, rendererRegistry,
					saveAllResultsSPIs, saveIndividualResultSPIs);
			runMonitor = new RunMonitor(runService, selectionManager, resultsPerspectiveComponent);
		}
		return resultsPerspectiveComponent;
	}

	@Override
	public ImageIcon getButtonIcon() {
		return WorkbenchIcons.resultsPerspectiveIcon;
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
		if (resultsPerspectiveComponent != null) {
			resultsPerspectiveComponent.handleEvent(event);
		}
	}

	public void setRunService(RunService runService) {
		this.runService = runService;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
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

	public void setSaveIndividualResultSPIs(List<SaveIndividualResultSPI> saveIndividualResultSPIs) {
		this.saveIndividualResultSPIs = saveIndividualResultSPIs;
	}

}
