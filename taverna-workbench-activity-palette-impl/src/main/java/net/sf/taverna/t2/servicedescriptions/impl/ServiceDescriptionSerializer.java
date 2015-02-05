package net.sf.taverna.t2.servicedescriptions.impl;

import static net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionConstants.CONFIGURATION;
import static net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionConstants.IGNORED;
import static net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionConstants.PROVIDERS;
import static net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionConstants.PROVIDER_ID;
import static net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionConstants.SERVICE_PANEL_CONFIGURATION;
import static net.sf.taverna.t2.servicedescriptions.impl.ServiceDescriptionConstants.TYPE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ServiceDescriptionSerializer {
	private static Logger logger = Logger
			.getLogger(ServiceDescriptionSerializer.class);

	public void serializeRegistry(ServiceDescriptionRegistry registry, File file)
			throws IOException {
		Set<ServiceDescriptionProvider> ignoreProviders = registry
				.getUserRemovedServiceProviders();
		JsonNode registryElement = serializeRegistry(registry, ignoreProviders);
		try (BufferedOutputStream bufferedOutStream = new BufferedOutputStream(
				new FileOutputStream(file))) {
			bufferedOutStream.write(registryElement.toString()
					.getBytes("UTF-8"));
		}
	}

	/**
	 * Export the whole service registry to an xml file, regardless of who added
	 * the service provider (user or system default). In this case there will be
	 * no "ignored providers" in the saved file.
	 */
	public void serializeFullRegistry(ServiceDescriptionRegistry registry,
			File file) throws IOException {
		JsonNode registryElement = serializeRegistry(registry, ALL_PROVIDERS);
		try (BufferedOutputStream bufferedOutStream = new BufferedOutputStream(
				new FileOutputStream(file))) {
			bufferedOutStream.write(registryElement.toString()
					.getBytes("UTF-8"));
		}
	}

	private static final JsonNodeFactory factory = JsonNodeFactory.instance;
	private static final Set<ServiceDescriptionProvider> ALL_PROVIDERS = null;

	private JsonNode serializeRegistry(ServiceDescriptionRegistry registry,
			Set<ServiceDescriptionProvider> ignoreProviders) {
		ObjectNode overallConfiguration = factory.objectNode();
		overallConfiguration.put(SERVICE_PANEL_CONFIGURATION,
				ignoreProviders != ALL_PROVIDERS ? "full" : "defaults only");
		ArrayNode providers = overallConfiguration.putArray(PROVIDERS);

		for (ServiceDescriptionProvider provider : registry
				.getUserAddedServiceProviders())
			try {
				providers.add(serializeProvider(provider));
			} catch (JDOMException | IOException e) {
				logger.warn("Could not serialize " + provider, e);
			}

		if (ignoreProviders != ALL_PROVIDERS) {
			ArrayNode ignored = overallConfiguration.putArray(IGNORED);
			for (ServiceDescriptionProvider provider : ignoreProviders)
				try {
					ignored.add(serializeProvider(provider));
				} catch (JDOMException | IOException e) {
					logger.warn("Could not serialize " + provider, e);
				}
		}

		return overallConfiguration;
	}

	private JsonNode serializeProvider(ServiceDescriptionProvider provider)
			throws JDOMException, IOException {
		ObjectNode node = factory.objectNode();
		node.put(PROVIDER_ID, provider.getId());

		if (provider instanceof ConfigurableServiceProvider) {
			ConfigurableServiceProvider configurable = (ConfigurableServiceProvider) provider;
			node.put(TYPE, configurable.getConfiguration().getType().toString());
			node.put(CONFIGURATION, configurable.getConfiguration().getJson());
		}
		return node;
	}
}
