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

import java.util.Collection;

import javax.swing.Icon;

import org.apache.taverna.lang.beans.PropertyAnnotation;

/**
 * A provider of service descriptions
 * 
 * @author Stian Soiland-Reyes
 */
public interface ServiceDescriptionProvider {
	/**
	 * Get all service descriptions.
	 * 
	 * @param callBack
	 */
	void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack);

	/**
	 * @author stain
	 */
	interface FindServiceDescriptionsCallBack {
		void partialResults(
				Collection<? extends ServiceDescription> serviceDescriptions);

		void status(String message);

		void warning(String message);

		void finished();

		void fail(String message, Throwable ex);
	}

	/**
	 * Name of this service description provider, for instance "BioCatalogue" or
	 * "WSDL". This name is typically used in a "Add service..." menu.
	 * 
	 * @return Name of provider
	 */
	String getName();

	@PropertyAnnotation(expert = true)
	abstract Icon getIcon();

	/**
	 * @return unique id of this provider.
	 */
	String getId();

	/**
	 * Create a new copy of this service provider. It <i>need not be
	 * configured</i> at the point where it is returned.
	 * 
	 * @return The copy.
	 */
	ServiceDescriptionProvider newInstance();
}
