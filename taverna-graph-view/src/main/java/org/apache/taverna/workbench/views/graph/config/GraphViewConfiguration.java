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

package org.apache.taverna.workbench.views.graph.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.configuration.AbstractConfigurable;
import org.apache.taverna.configuration.ConfigurationManager;

import org.apache.taverna.workbench.models.graph.Graph.Alignment;
import org.apache.taverna.workbench.models.graph.GraphController.PortStyle;

/**
 * Configuration for the GraphViewComponent.
 * 
 * @author David Withers
 */
public class GraphViewConfiguration extends AbstractConfigurable {
	public static final String PORT_STYLE = "portStyle";
	public static final String ALIGNMENT = "alignment";
	public static final String ANIMATION_ENABLED = "animationEnabled";
	public static final String ANIMATION_SPEED = "animationSpeed";

	private Map<String, String> defaultPropertyMap;

	public GraphViewConfiguration(ConfigurationManager configurationManager) {
		super(configurationManager);
	}

	@Override
	public String getCategory() {
		return "general";
	}

	@Override
	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<>();
			defaultPropertyMap.put(PORT_STYLE, PortStyle.NONE.toString());
			defaultPropertyMap.put(ALIGNMENT, Alignment.VERTICAL.toString());
			defaultPropertyMap.put(ANIMATION_ENABLED, "false");
			defaultPropertyMap.put(ANIMATION_SPEED, "800");
		}
		return defaultPropertyMap;
	}

	@Override
	public String getDisplayName() {
		return "Diagram";
	}

	@Override
	public String getFilePrefix() {
		return "Diagram";
	}

	@Override
	public String getUUID() {
		return "3686BA31-449F-4147-A8AC-0C3F63AFC68F";
	}
}
