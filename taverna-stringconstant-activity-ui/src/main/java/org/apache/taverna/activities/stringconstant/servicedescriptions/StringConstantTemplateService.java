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
package org.apache.taverna.activities.stringconstant.servicedescriptions;

import java.net.URI;

import javax.swing.Icon;

import org.apache.taverna.servicedescriptions.AbstractTemplateService;
import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.scufl2.api.configurations.Configuration;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class StringConstantTemplateService extends AbstractTemplateService {
	private static final URI ACTIVITY_TYPE = URI
			.create("http://ns.taverna.org.uk/2010/activity/constant");
	private static final URI providerId = URI
			.create("http://taverna.sf.net/2010/service-provider/stringconstant");
	public static final String DEFAULT_VALUE = "Add your own value here";
	private static final String STRINGCONSTANT = "Text constant";

	@Override
	public URI getActivityType() {
		return ACTIVITY_TYPE;
	}

	@Override
	public Configuration getActivityConfiguration() {
		Configuration configuration = new Configuration();
		configuration.setType(ACTIVITY_TYPE.resolve("#Config"));
		((ObjectNode) configuration.getJson()).put("string", DEFAULT_VALUE);
		return configuration;
	}

	@Override
	public Icon getIcon() {
		return StringConstantActivityIcon.getStringConstantIcon();
	}

	@Override
	public String getName() {
		return STRINGCONSTANT;
	}

	@Override
	public String getDescription() {
		return "A string value that you can set";
	}

	public static ServiceDescription getServiceDescription() {
		StringConstantTemplateService scts = new StringConstantTemplateService();
		return scts.templateService;
	}

	@Override
	public String getId() {
		return providerId.toString();
	}

	@Override
	public ServiceDescriptionProvider newInstance() {
		return new StringConstantTemplateService();
	}
}
