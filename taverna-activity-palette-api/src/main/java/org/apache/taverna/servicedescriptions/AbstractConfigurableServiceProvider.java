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
package org.apache.taverna.servicedescriptions;

import org.apache.taverna.scufl2.api.configurations.Configuration;

public abstract class AbstractConfigurableServiceProvider extends
		IdentifiedObject implements ConfigurableServiceProvider {
	protected Configuration serviceProviderConfig;

	/**
	 * Construct configurable service provider.
	 * 
	 * @param configTemplate
	 *            Template configuration
	 */
	public AbstractConfigurableServiceProvider(Configuration configTemplate) {
		if (configTemplate == null)
			throw new NullPointerException("Default config can't be null");
		serviceProviderConfig = configTemplate;
	}

	/**
	 * Package access constructor - only used with {@link #clone()} - otherwise
	 * use {@link #AbstractConfigurableServiceProvider(Object)}
	 */
	AbstractConfigurableServiceProvider() {
	}

	@Override
	public AbstractConfigurableServiceProvider clone() {
		AbstractConfigurableServiceProvider provider = (AbstractConfigurableServiceProvider) newInstance();
		Configuration configuration = getConfiguration();
		if (configuration != null)
			provider.configure(configuration);
		return provider;
	}

	@Override
	public synchronized void configure(Configuration conf) {
		if (conf == null)
			throw new IllegalArgumentException("Config can't be null");
		this.serviceProviderConfig = conf;
	}

	@Override
	public Configuration getConfiguration() {
		return serviceProviderConfig;
	}

	@Override
	public String toString() {
		return getName() + " " + getConfiguration();
	}
}
