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

import javax.swing.JPanel;

import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationUIFactory;

/**
 * ConfigurationFactory for the GraphViewConfiguration.
 * 
 * @author David Withers
 */
public class GraphViewConfigurationUIFactory implements ConfigurationUIFactory {
	private GraphViewConfiguration graphViewConfiguration;

	@Override
	public boolean canHandle(String uuid) {
		return uuid.equals(getConfigurable().getUUID());
	}

	@Override
	public JPanel getConfigurationPanel() {
		return new GraphViewConfigurationPanel(graphViewConfiguration);
	}

	@Override
	public Configurable getConfigurable() {
		return graphViewConfiguration;
	}

	public void setGraphViewConfiguration(
			GraphViewConfiguration graphViewConfiguration) {
		this.graphViewConfiguration = graphViewConfiguration;
	}
}
