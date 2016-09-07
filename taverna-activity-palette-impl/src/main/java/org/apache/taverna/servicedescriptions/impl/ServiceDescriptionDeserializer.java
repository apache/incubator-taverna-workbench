package org.apache.taverna.servicedescriptions.impl;
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

import static org.apache.taverna.servicedescriptions.impl.ServiceDescriptionConstants.CONFIGURATION;
import static org.apache.taverna.servicedescriptions.impl.ServiceDescriptionConstants.IGNORED;
import static org.apache.taverna.servicedescriptions.impl.ServiceDescriptionConstants.PROVIDERS;
import static org.apache.taverna.servicedescriptions.impl.ServiceDescriptionConstants.PROVIDER_ID;
import static org.apache.taverna.servicedescriptions.impl.ServiceDescriptionConstants.TYPE;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.servicedescriptions.ConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionRegistry;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ServiceDescriptionDeserializer {
	private List<ServiceDescriptionProvider> serviceDescriptionProviders;

	ServiceDescriptionDeserializer(
			List<ServiceDescriptionProvider> serviceDescriptionProviders) {
		this.serviceDescriptionProviders = serviceDescriptionProviders;
	}

	public void deserialize(ServiceDescriptionRegistry registry,
			File serviceDescriptionsFile) throws DeserializationException {
		try (FileInputStream serviceDescriptionFileStream = new FileInputStream(
				serviceDescriptionsFile)) {
			deserialize(registry, serviceDescriptionFileStream);
		} catch (FileNotFoundException ex) {
			throw new DeserializationException("Could not locate file "
					+ serviceDescriptionsFile.getAbsolutePath()
					+ " containing service descriptions.");
		} catch (IOException ex) {
			throw new DeserializationException(
					"Could not read stream containing service descriptions from "
							+ serviceDescriptionsFile.getAbsolutePath(), ex);
		}
	}

	public void deserialize(ServiceDescriptionRegistry registry,
			URL serviceDescriptionsURL) throws DeserializationException {
		try (InputStream serviceDescriptionInputStream = serviceDescriptionsURL
				.openStream()) {
			deserialize(registry, serviceDescriptionInputStream);
		} catch (FileNotFoundException ex) {
			throw new DeserializationException("Could not open URL "
					+ serviceDescriptionsURL
					+ " containing service descriptions.");
		} catch (IOException ex) {
			throw new DeserializationException(
					"Could not read stream containing service descriptions from "
							+ serviceDescriptionsURL, ex);
		}
	}

	private static final JsonFactory factory = new JsonFactory();

	private void deserialize(ServiceDescriptionRegistry registry,
			InputStream serviceDescriptionsInputStream) throws IOException,
			DeserializationException {
		ObjectNode node = (ObjectNode) new ObjectMapper(factory)
				.readTree(serviceDescriptionsInputStream);
		List<ServiceDescriptionProvider> providers = deserializeProviders(node,
				true);
		for (ServiceDescriptionProvider provider : providers)
			registry.addServiceDescriptionProvider(provider);
	}

	public Collection<? extends ServiceDescriptionProvider> deserializeDefaults(
			ServiceDescriptionRegistry registry,
			File defaultConfigurableServiceProvidersFile)
			throws DeserializationException {
		ObjectNode node;
		try (FileInputStream serviceDescriptionStream = new FileInputStream(
				defaultConfigurableServiceProvidersFile)) {
			node = (ObjectNode) new ObjectMapper(factory)
					.readTree(serviceDescriptionStream);
		} catch (IOException e) {
			throw new DeserializationException("Can't read "
					+ defaultConfigurableServiceProvidersFile);
		}
		return deserializeProviders(node, false);
	}

	private List<ServiceDescriptionProvider> deserializeProviders(
			ObjectNode rootNode, boolean obeyIgnored)
			throws DeserializationException {
		List<ServiceDescriptionProvider> providers = new ArrayList<>();

		ArrayNode providersNode = (ArrayNode) rootNode.get(PROVIDERS);
		if (providersNode != null)
			for (JsonNode provider : providersNode)
				providers.add(deserializeProvider((ObjectNode) provider));

		if (obeyIgnored) {
			ArrayNode ignoredNode = (ArrayNode) rootNode.get(IGNORED);
			if (ignoredNode != null)
				for (JsonNode provider : ignoredNode)
					providers
							.remove(deserializeProvider((ObjectNode) provider));
		}

		return providers;
	}

	private ServiceDescriptionProvider deserializeProvider(
			ObjectNode providerNode) throws DeserializationException {
		String providerId = providerNode.get(PROVIDER_ID).asText().trim();
		ServiceDescriptionProvider provider = null;
		for (ServiceDescriptionProvider serviceProvider : serviceDescriptionProviders)
			if (serviceProvider.getId().equals(providerId)) {
				provider = serviceProvider;
				break;
			}
		if (provider == null)
			throw new DeserializationException(
					"Could not find provider with id " + providerId);

		/*
		 * So we know the service provider now, but we need a separate instance
		 * of that provider for each providerElem. E.g. we can have 2 or more
		 * WSDL provider elements and need to return a separate provider
		 * instance for each as they will have different configurations.
		 */
		ServiceDescriptionProvider instance = provider.newInstance();

		if (instance instanceof ConfigurableServiceProvider)
			try {
				Configuration config = new Configuration();
				config.setType(URI.create(providerNode.get(TYPE).textValue()));
				config.setJson(providerNode.get(CONFIGURATION));
				if (config != null)
					((ConfigurableServiceProvider) instance).configure(config);
			} catch (Exception e) {
				throw new DeserializationException(
						"Could not configure provider " + providerId
								+ " using bean " + providerNode, e);
			}
		return instance;
	}

	@SuppressWarnings("serial")
	static class DeserializationException extends Exception {
		public DeserializationException(String string) {
			super(string);
		}

		public DeserializationException(String string, Exception ex) {
			super(string, ex);
		}
	}
}
