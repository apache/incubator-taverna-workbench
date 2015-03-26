/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.workbench.ui.views.contextualviews.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import org.apache.taverna.commons.services.ActivityTypeNotFoundException;
import org.apache.taverna.commons.services.InvalidConfigurationException;
import org.apache.taverna.commons.services.ServiceRegistry;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.common.Scufl2Tools;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.port.ActivityPort;
import org.apache.taverna.scufl2.api.port.InputActivityPort;
import org.apache.taverna.scufl2.api.port.OutputActivityPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author alanrw
 */
@SuppressWarnings("serial")
public abstract class ActivityConfigurationPanel extends JPanel {
	private static final Logger logger = Logger.getLogger(ActivityConfigurationPanel.class);
	private final static Scufl2Tools scufl2Tools = new Scufl2Tools();

	// protected final URITools uriTools = new URITools();
	private final Activity activity;
	private final Configuration configuration;
	private final List<ActivityPortConfiguration> inputPorts;
	private final List<ActivityPortConfiguration> outputPorts;
	protected ObjectNode json;

	public ActivityConfigurationPanel(Activity activity) {
		this(activity, scufl2Tools.configurationFor(activity,
				activity.getParent()));
	}

	public ActivityConfigurationPanel(Activity activity,
			Configuration configuration) {
		this.activity = activity;
		this.configuration = configuration;
		inputPorts = new ArrayList<>();
		outputPorts = new ArrayList<>();
	}

	/**
	 * Initializes the configuration panel. This method is also used to discard
	 * any changes and reset the panel to its initial state. Subclasses should
	 * implement this method to set up the panel and must call
	 * <tt>super.initialise()</tt> first.
	 */
	protected void initialise() {
		json = configuration.getJson().deepCopy();
		inputPorts.clear();
		for (InputActivityPort activityPort : activity.getInputPorts())
			inputPorts.add(new ActivityPortConfiguration(activityPort));
		outputPorts.clear();
		for (OutputActivityPort activityPort : activity.getOutputPorts())
			outputPorts.add(new ActivityPortConfiguration(activityPort));
	}

	public abstract boolean checkValues();

	public abstract void noteConfiguration();

	public boolean isConfigurationChanged() {
		noteConfiguration();
		if (portsChanged(inputPorts, activity.getInputPorts().size()))
			return true;
		if (portsChanged(outputPorts, activity.getOutputPorts().size()))
			return true;
		return !json.equals(configuration.getJson());
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public ObjectNode getJson() {
		return json;
	}

	protected void setJson(ObjectNode json) {
		this.json = json;
	}

	public void refreshConfiguration() {
		initialise();
	}

	public void whenOpened() {
	}

	public void whenClosed() {
	}

	/**
	 * Convenience method for getting simple String property values.
	 *
	 * @param name
	 *            the property name
	 * @return the property value
	 */
	protected String getProperty(String name) {
		JsonNode jsonNode = json.get(name);
		if (jsonNode == null)
			return null;
		return json.get(name).asText();
	}

	/**
	 * Convenience method for setting simple String property values.
	 *
	 * @param name
	 *            the property name
	 * @param value
	 *            the property value
	 */
	protected void setProperty(String name, String value) {
		json.put(name, value);
	}

	public List<ActivityPortConfiguration> getInputPorts() {
		return inputPorts;
	}

	public List<ActivityPortConfiguration> getOutputPorts() {
		return outputPorts;
	}

	protected void configureInputPorts(ServiceRegistry serviceRegistry) {
		try {
			Map<String, InputActivityPort> newInputPorts = new HashMap<>();
			for (InputActivityPort port : serviceRegistry
					.getActivityInputPorts(getActivity().getType(), getJson()))
				newInputPorts.put(port.getName(), port);
			List<ActivityPortConfiguration> inputPorts = getInputPorts();
			for (ActivityPortConfiguration portConfig : new ArrayList<>(
					inputPorts))
				if (newInputPorts.containsKey(portConfig.getName())) {
					InputActivityPort port = newInputPorts.remove(portConfig
							.getName());
					portConfig.setDepth(port.getDepth());
				} else
					inputPorts.remove(portConfig);
			for (InputActivityPort newPort : newInputPorts.values())
				inputPorts.add(new ActivityPortConfiguration(newPort.getName(),
						newPort.getDepth()));
		} catch (InvalidConfigurationException | ActivityTypeNotFoundException e) {
			logger.warn("Error configuring input ports", e);
		}
	}

	protected void configureOutputPorts(ServiceRegistry serviceRegistry) {
		try {
			Map<String, OutputActivityPort> newOutputPorts = new HashMap<>();
			for (OutputActivityPort port : serviceRegistry
					.getActivityOutputPorts(getActivity().getType(), getJson()))
				newOutputPorts.put(port.getName(), port);
			List<ActivityPortConfiguration> outputPorts = getOutputPorts();
			for (ActivityPortConfiguration portConfig : new ArrayList<>(
					outputPorts))
				if (newOutputPorts.containsKey(portConfig.getName())) {
					OutputActivityPort port = newOutputPorts.remove(portConfig
							.getName());
					portConfig.setDepth(port.getDepth());
					portConfig.setGranularDepth(port.getGranularDepth());
				} else
					outputPorts.remove(portConfig);
			for (OutputActivityPort newPort : newOutputPorts.values())
				outputPorts.add(new ActivityPortConfiguration(
						newPort.getName(), newPort.getDepth()));
		} catch (InvalidConfigurationException | ActivityTypeNotFoundException e) {
			logger.warn("Error configuring output ports", e);
		}
	}

	private boolean portsChanged(List<ActivityPortConfiguration> portDefinitions, int ports) {
		int checkedPorts = 0;
		for (ActivityPortConfiguration portDefinition : portDefinitions) {
			String portName = portDefinition.getName();
			int portDepth = portDefinition.getDepth();
			ActivityPort activityPort = portDefinition.getActivityPort();
			if (activityPort == null)
				// new port added
				return true;
			if (!activityPort.getName().equals(portName))
				// port name changed
				return true;
			if (!activityPort.getDepth().equals(portDepth))
				// port depth changed
				return true;
			checkedPorts++;
		}
		if (checkedPorts < ports)
			// ports deleted
			return true;
		return false;
	}

	public Activity getActivity() {
		return activity;
	}
}
