package org.apache.taverna.workbench.ui.activitypalette;
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
import org.apache.taverna.configuration.ConfigurationUIFactory;

public class ActivityPaletteConfigurationUIFactory implements
		ConfigurationUIFactory {
	private ActivityPaletteConfiguration activityPaletteConfiguration;

	@Override
	public boolean canHandle(String uuid) {
		return uuid != null && uuid.equals(getConfigurable().getUUID());
	}

	@Override
	public Configurable getConfigurable() {
		return activityPaletteConfiguration;
	}

	@Override
	public JPanel getConfigurationPanel() {
		return new ActivityPaletteConfigurationPanel(
				activityPaletteConfiguration);
	}

	public void setActivityPaletteConfiguration(
			ActivityPaletteConfiguration activityPaletteConfiguration) {
		this.activityPaletteConfiguration = activityPaletteConfiguration;
	}
}
