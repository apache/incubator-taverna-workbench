package org.apache.taverna.workbench.myexperiment.config;
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

import javax.swing.JPanel;

import org.apache.taverna.configuration.Configurable;
import org.apache.taverna.configuration.ConfigurationManager;
import org.apache.taverna.configuration.ConfigurationUIFactory;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;

/**
 * @author Emmanuel Tagarira, Alan Williams
 */
public class MyExperimentConfigurationUIFactory implements ConfigurationUIFactory {

	private ConfigurationManager configurationManager;

	public boolean canHandle(String uuid) {
		return uuid.equals(getConfigurable().getUUID());
	}

	public JPanel getConfigurationPanel() {
		// FIXME: This is insane.. why would we initialize the UI from here?
		// if (MainComponent.MAIN_COMPONENT == null)
		// MainComponent.MAIN_COMPONENT = new MainComponent(editManager, fileManager);
		return new MyExperimentConfigurationPanel();
	}

	public Configurable getConfigurable() {
		return new MyExperimentConfiguration(configurationManager);
	}

	public void setConfigurationManager(ConfigurationManager configurationManager) {
		this.configurationManager = configurationManager;
	}

}
