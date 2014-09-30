package net.sf.taverna.t2.servicedescriptions.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workflowmodel.serialization.xml.impl.AbstractXMLSerializer;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

class ServiceDescriptionSerializer extends AbstractXMLSerializer
		implements ServiceDescriptionXMLConstants{
	private static Logger logger = Logger
			.getLogger(ServiceDescriptionSerializer.class);

	private Element serializeProvider(ServiceDescriptionProvider provider)
			throws JDOMException, IOException {
		Element serviceProviderElem = new Element(PROVIDER, SERVICE_DESCRIPTION_NS);

		Element providerIdElem = new Element(PROVIDER_IDENTIFIER, T2_WORKFLOW_NAMESPACE);
		providerIdElem.setText(provider.getId());
		serviceProviderElem.addContent(providerIdElem);

		if (provider instanceof ConfigurableServiceProvider) {
			ConfigurableServiceProvider configurableServiceProvider = (ConfigurableServiceProvider) provider;
			Object config = configurableServiceProvider.getConfiguration();
			Element configElem = beanAsElement(config);
			serviceProviderElem.addContent(configElem);
		}
		return serviceProviderElem;
	}

	private Element serializeRegistry(ServiceDescriptionRegistry registry,
			Set<ServiceDescriptionProvider> ignoreProviders) {
		Element serviceDescriptionElem = new Element(SERVICE_DESCRIPTIONS,
				SERVICE_DESCRIPTION_NS);

		Element localProvidersElem = new Element(PROVIDERS,
				SERVICE_DESCRIPTION_NS);
		serviceDescriptionElem.addContent(localProvidersElem);

		for (ServiceDescriptionProvider provider : registry
				.getUserAddedServiceProviders())
			try {
				localProvidersElem.addContent(serializeProvider(provider));
			} catch (JDOMException | IOException e) {
				logger.warn("Could not serialize " + provider, e);
			}

		if (ignoreProviders != ALL_PROVIDERS) {
			Element ignoredProvidersElem = new Element(IGNORED_PROVIDERS,
					SERVICE_DESCRIPTION_NS);
			serviceDescriptionElem.addContent(ignoredProvidersElem);

			for (ServiceDescriptionProvider provider : ignoreProviders)
				try {
					ignoredProvidersElem
							.addContent(serializeProvider(provider));
				} catch (JDOMException | IOException e) {
					logger.warn("Could not serialize " + provider, e);
				}
		}
		return serviceDescriptionElem;
	}

	public void serializeRegistry(ServiceDescriptionRegistry registry, File file)
			throws IOException {
		Set<ServiceDescriptionProvider> ignoreProviders = registry
				.getUserRemovedServiceProviders();
		Element registryElement = serializeRegistry(registry, ignoreProviders);
		try (BufferedOutputStream bufferedOutStream = new BufferedOutputStream(
				new FileOutputStream(file))) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			outputter.output(registryElement, bufferedOutStream);
			bufferedOutStream.flush();
		}
	}

	/**
	 * Export the whole service registry to an xml file, regardless of who
	 * added the service provider (user or system default). In this case there
	 * will be no "ignored providers" in the saved file.
	 */
	public void serializeFullRegistry(ServiceDescriptionRegistry registry,
			File file) throws IOException {
		Element registryElement = serializeRegistry(registry, ALL_PROVIDERS);
		try (BufferedOutputStream bufferedOutStream = new BufferedOutputStream(
				new FileOutputStream(file))) {
			XMLOutputter outputter = new XMLOutputter();
			outputter.setFormat(Format.getPrettyFormat());
			outputter.output(registryElement, bufferedOutStream);
			bufferedOutStream.flush();
		}
	}

	private static final Set<ServiceDescriptionProvider> ALL_PROVIDERS = null;
}
