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

package org.apache.taverna.workflow.edits;

import org.apache.taverna.scufl2.api.common.Configurable;
import org.apache.taverna.scufl2.api.configurations.Configuration;

/**
 * An Edit that configures a {@link Configurable} with a given
 * {@link Configuration}.
 * 
 * @author David Withers
 */
public class ConfigureEdit<ConfigurableType extends Configurable> extends
		AbstractEdit<ConfigurableType> {
	private final Configuration oldConfiguration;
	private final Configuration newConfiguration;

	public ConfigureEdit(ConfigurableType configurable,
			Configuration oldConfiguration, Configuration newConfiguration) {
		super(configurable);
		this.oldConfiguration = oldConfiguration;
		this.newConfiguration = newConfiguration;
	}

	@Override
	protected void doEditAction(ConfigurableType configurable) {
		oldConfiguration.setConfigures(null);
		newConfiguration.setConfigures(configurable);
	}

	@Override
	protected void undoEditAction(ConfigurableType configurable) {
		oldConfiguration.setConfigures(configurable);
		newConfiguration.setConfigures(null);
	}
}
