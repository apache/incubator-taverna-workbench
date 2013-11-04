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
package net.sf.taverna.t2.workbench.views.results;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import javax.swing.JTabbedPane;

import net.sf.taverna.t2.renderers.RendererRegistry;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.Updatable;
import net.sf.taverna.t2.workbench.views.results.saveactions.SaveIndividualResultSPI;
import net.sf.taverna.t2.workbench.views.results.workflow.PortResultsViewTab;
import uk.org.taverna.platform.report.Invocation;
import uk.org.taverna.scufl2.api.port.InputPort;
import uk.org.taverna.scufl2.api.port.Port;

/**
 * View displaying input and output values of an invocation.
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class InvocationView extends JTabbedPane implements Updatable {

	private final RendererRegistry rendererRegistry;

	private final List<SaveIndividualResultSPI> saveIndividualActions;

	private Map<String, PortResultsViewTab> inputPortTabMap = new HashMap<>(),
			outputPortTabMap = new HashMap<>();

	private final Invocation invocation;

	public InvocationView(Invocation invocation,
			RendererRegistry rendererRegistry, List<SaveIndividualResultSPI> saveIndividualActions) {
		this.invocation = invocation;
		this.rendererRegistry = rendererRegistry;
		this.saveIndividualActions = saveIndividualActions;
		init();
	}

	public void init() {
		SortedMap<String, Path> inputs = invocation.getInputs();
		SortedMap<String, Path> outputs = invocation.getOutputs();

		// Input ports
		for (Entry<String, Path> entry : inputs.entrySet()) {
			String name = entry.getKey();
			Path value = entry.getValue();
			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(name, value, rendererRegistry, saveIndividualActions);

			inputPortTabMap.put(name, resultTab);

			addTab(name, WorkbenchIcons.inputIcon, resultTab, "Input port " + name);
		}

		// Output ports
		for (Entry<String, Path> entry : outputs.entrySet()) {
			String name = entry.getKey();
			Path value = entry.getValue();

			// Create a tab containing a tree view of per-port results and a rendering
			// component for displaying individual results
			PortResultsViewTab resultTab = new PortResultsViewTab(name, value, rendererRegistry, saveIndividualActions);
			outputPortTabMap.put(name, resultTab);

			addTab(name, WorkbenchIcons.outputIcon, resultTab, "Output port " + name);
		}
		// Select the first output port tab
		if (!outputs.isEmpty()) {
			setSelectedIndex(inputs.size());
		} else if (!inputs.isEmpty()) {
			setSelectedIndex(0);
		}

		revalidate();

	}

	public void selectPortTab(Port port) {
		PortResultsViewTab tab;
		if (port instanceof InputPort) {
			tab = inputPortTabMap.get(port.getName());
		} else {
			tab = outputPortTabMap.get(port.getName());
		}
		if (tab != null) {
			setSelectedComponent(tab);
		}
	}

	public void update() {
		for (PortResultsViewTab portResultsViewTab : inputPortTabMap.values()) {
			portResultsViewTab.update();
		}
		for (PortResultsViewTab portResultsViewTab : outputPortTabMap.values()) {
			portResultsViewTab.update();
		}
	}

}
