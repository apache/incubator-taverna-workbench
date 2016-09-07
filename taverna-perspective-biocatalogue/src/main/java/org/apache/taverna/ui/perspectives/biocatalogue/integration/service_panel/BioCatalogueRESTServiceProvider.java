package org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel;
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

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.apache.log4j.Logger;

//import net.sf.taverna.t2.activities.rest.ui.servicedescription.RESTActivityIcon;
import org.apache.taverna.servicedescriptions.AbstractConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.impl.ServiceDescriptionRegistryImpl;

/**
 * Service provider for REST service added to the Service Panel through the
 * BioCatalogue perspective.
 * 
 * @author Alex Nenadic
 */
public class BioCatalogueRESTServiceProvider extends
	AbstractConfigurableServiceProvider<RESTFromBioCatalogueServiceDescription> {

	public static final String PROVIDER_NAME = "Service Catalogue - selected services";
	  
	private static final URI providerId = URI
	.create("http://taverna.sf.net/2010/service-provider/servicecatalogue/rest");
	
	private static Logger logger = Logger.getLogger(BioCatalogueRESTServiceProvider.class);

	public BioCatalogueRESTServiceProvider(
			RESTFromBioCatalogueServiceDescription restServiceDescription) {
		super(restServiceDescription);
	}
	
	public BioCatalogueRESTServiceProvider() {
		super(new RESTFromBioCatalogueServiceDescription());
	}
	
	@Override
	protected List<? extends Object> getIdentifyingData() {
		return getConfiguration().getIdentifyingData();
	}

	@Override
	public void findServiceDescriptionsAsync(
			FindServiceDescriptionsCallBack callBack) {
	    callBack.status("Starting Service Catalogue REST Service Provider");
		registerNewRESTMethod(getConfiguration(), callBack);
	}

	@Override
	public Icon getIcon() {
//		return RESTActivityIcon.getRESTActivityIcon();
		return getConfiguration().getIcon();
	}

	@Override
	public String getId() {
		return providerId.toString();
	}

	@Override
	public String getName() {
		return "Service Catalogue REST";
	}
	
	@Override
	public String toString() {
		return "Service Catalogue REST service " + getConfiguration().getName();
	}
	
	public static boolean registerNewRESTMethod(
			RESTFromBioCatalogueServiceDescription restServiceDescription,
			FindServiceDescriptionsCallBack callBack)	{
		if (callBack == null) {
			// We are not adding service through a callback and
			// findServiceDescriptionsAsync() -
			// we are adding directly from the BioCatalogue perspective.
			ServiceDescriptionRegistryImpl serviceDescriptionRegistry = ServiceDescriptionRegistryImpl
					.getInstance();
			serviceDescriptionRegistry
					.addServiceDescriptionProvider(new BioCatalogueRESTServiceProvider(
							restServiceDescription));
			return true;
		} else {
			{
				// Add the REST method to the Service Panel through the callback
				callBack.partialResults(Collections
						.singletonList(restServiceDescription));
				callBack.finished();
				return (true);
			}
		}
	}

}
