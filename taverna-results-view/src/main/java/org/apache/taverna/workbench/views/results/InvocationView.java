package org.apache.taverna.workbench.views.results;
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

import static org.apache.taverna.workbench.icons.WorkbenchIcons.inputIcon;
import static org.apache.taverna.workbench.icons.WorkbenchIcons.outputIcon;

import java.awt.Component;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.JTabbedPane;

import org.apache.taverna.renderers.RendererRegistry;
import org.apache.taverna.workbench.ui.Updatable;
import org.apache.taverna.workbench.views.results.saveactions.SaveIndividualResultSPI;
import org.apache.taverna.workbench.views.results.workflow.PortResultsViewTab;
import org.apache.taverna.platform.report.Invocation;
import org.apache.taverna.scufl2.api.common.Ported;
import org.apache.taverna.scufl2.api.port.InputPort;
import org.apache.taverna.scufl2.api.port.Port;

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
			RendererRegistry rendererRegistry,
			List<SaveIndividualResultSPI> saveIndividualActions) {
		this.invocation = invocation;
		this.rendererRegistry = rendererRegistry;
		this.saveIndividualActions = saveIndividualActions;
		init();
	}

	public void init() {
		SortedMap<String, Path> inputs = invocation.getInputs();
		SortedMap<String, Path> outputs = invocation.getOutputs();
		Ported ported = invocation.getReport().getSubject();

		// Input ports
		for (Port port : ported.getInputPorts()) {
			String name = port.getName();
			Path value = inputs.get(name);
			/*
			 * Create a tab containing a tree view of per-port results and a
			 * rendering component for displaying individual results
			 */
			PortResultsViewTab resultTab = new PortResultsViewTab(port, value,
					rendererRegistry, saveIndividualActions);

			inputPortTabMap.put(name, resultTab);

			addTab(name, inputIcon, resultTab, "Input port " + name);
		}

		// Output ports
		for (Port port : ported.getOutputPorts()) {
			String name = port.getName();
			Path value = outputs.get(name);
			/*
			 * Create a tab containing a tree view of per-port results and a
			 * rendering component for displaying individual results
			 */
			PortResultsViewTab resultTab = new PortResultsViewTab(port, value,
					rendererRegistry, saveIndividualActions);
			outputPortTabMap.put(name, resultTab);

			addTab(name, outputIcon, resultTab, "Output port " + name);
		}
		// Select the first output port tab
		if (!outputs.isEmpty())
			setSelectedIndex(inputs.size());
		else if (!inputs.isEmpty())
			setSelectedIndex(0);

		revalidate();
	}

	public void selectPortTab(Port port) {
		PortResultsViewTab tab;
		if (port instanceof InputPort)
			tab = inputPortTabMap.get(port.getName());
		else
			tab = outputPortTabMap.get(port.getName());
		if (tab != null)
			setSelectedComponent(tab);
	}

	public Port getSelectedPort() {
		Component selectedComponent = getSelectedComponent();
		if (selectedComponent instanceof PortResultsViewTab) {
			PortResultsViewTab portView = (PortResultsViewTab) selectedComponent;
			return portView.getPort();
		}
		return null;
	}

	@Override
	public void update() {
		for (PortResultsViewTab portResultsViewTab : inputPortTabMap.values())
			portResultsViewTab.update();
		for (PortResultsViewTab portResultsViewTab : outputPortTabMap.values())
			portResultsViewTab.update();
	}
}
