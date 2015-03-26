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

import java.net.URI;
import java.util.List;

import javax.swing.Icon;

import org.apache.taverna.lang.beans.PropertyAnnotation;
import org.apache.taverna.workbench.edits.Edit;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.scufl2.api.core.Workflow;

public abstract class ServiceDescription extends IdentifiedObject {
	public static final String SERVICE_TEMPLATES = "Service templates";
	private static final String NAME = "Name";
	private static final String SERVICE_CONFIGURATION = "Service configuration";
	private static final String SERVICE_IMPLEMENTATION_URI = "Service implementation URI";
	private static final String DESCRIPTION = "Description";
	public static final String LOCAL_SERVICES = "Local services";

	private String description = "";

	@PropertyAnnotation(expert = true, displayName = SERVICE_IMPLEMENTATION_URI)
	public abstract URI getActivityType();

	@PropertyAnnotation(expert = true, displayName = SERVICE_CONFIGURATION)
	public Configuration getActivityConfiguration() {
		Configuration configuration = new Configuration();
		configuration.setType(getActivityType().resolve("#Config"));
		return configuration;
	}

	@PropertyAnnotation(displayName = DESCRIPTION)
	public String getDescription() {
		return this.description;
	}

	@PropertyAnnotation(expert = true)
	public abstract Icon getIcon();

	@PropertyAnnotation(displayName = NAME)
	public abstract String getName();

	@PropertyAnnotation(expert = true)
	public abstract List<? extends Comparable<?>> getPath();

	@PropertyAnnotation(hidden = true)
	public boolean isTemplateService() {
		return false;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Service description " + getName();
	}

	/**
	 * Any additional edit that needs to be performed upon insertion of an
	 * instance of the ServiceDescription into the {@link Workflow} within the
	 * specified {@link Processor}
	 * 
	 * @param dataflow
	 * @param p
	 * @param a
	 * @return
	 */
	public Edit<?> getInsertionEdit(Workflow dataflow, Processor p, Activity a) {
		return null;
	}
}
