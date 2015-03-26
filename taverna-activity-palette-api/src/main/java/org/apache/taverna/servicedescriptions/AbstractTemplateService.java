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

package org.apache.taverna.servicedescriptions;

import static java.util.Collections.singleton;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import org.apache.taverna.scufl2.api.configurations.Configuration;

public abstract class AbstractTemplateService implements
		ServiceDescriptionProvider {
	protected TemplateServiceDescription templateService = new TemplateServiceDescription();

	@Override
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
		callBack.partialResults(singleton(templateService));
		callBack.finished();
	}

	@Override
	public abstract Icon getIcon();

	public URI getActivityType() {
		return null;
	}

	public abstract Configuration getActivityConfiguration();

	public class TemplateServiceDescription extends ServiceDescription {
		@Override
		public Icon getIcon() {
			return AbstractTemplateService.this.getIcon();
		}

		@Override
		public String getName() {
			return AbstractTemplateService.this.getName();
		}

		@Override
		public List<String> getPath() {
			return Arrays.asList(SERVICE_TEMPLATES);
		}

		@Override
		public boolean isTemplateService() {
			return true;
		}

		@Override
		protected List<Object> getIdentifyingData() {
			// Do it by object identity
			return null;
		}

		@Override
		public URI getActivityType() {
			return AbstractTemplateService.this.getActivityType();
		}

		@Override
		public Configuration getActivityConfiguration() {
			return AbstractTemplateService.this.getActivityConfiguration();
		}

		@Override
		public String getDescription() {
			return AbstractTemplateService.this.getDescription();
		}
	}

	@Override
	public String toString() {
		return "Template service " + getName();
	}

	public String getDescription() {
		// Default to an empty string
		return "";
	}
}
