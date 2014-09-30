package net.sf.taverna.t2.servicedescriptions.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

class ServiceDescriptionDeserializer
		extends
		net.sf.taverna.t2.workflowmodel.serialization.xml.impl.AbstractXMLDeserializer
		implements ServiceDescriptionXMLConstants {
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
		} catch (JDOMException ex) {
			throw new DeserializationException(
					"Could not deserialize stream containing service descriptions from "
							+ serviceDescriptionsFile.getAbsolutePath(), ex);
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
					+ serviceDescriptionsURL.toString()
					+ " containing service descriptions.");
		} catch (JDOMException ex1) {
			throw new DeserializationException(
					"Could not deserialize stream containing service descriptions from "
							+ serviceDescriptionsURL.toString(), ex1);
		} catch (IOException ex2) {
			throw new DeserializationException(
					"Could not read stream containing service descriptions from "
							+ serviceDescriptionsURL.toString(), ex2);
		}
	}

	private void deserialize(ServiceDescriptionRegistry registry,
			InputStream serviceDescriptionsInputStream) throws JDOMException,
			IOException, DeserializationException {
		Document document = null;
		SAXBuilder builder = new SAXBuilder();
		document = builder.build(serviceDescriptionsInputStream);

		List<ServiceDescriptionProvider> providers = deserializeProviders(
				document.getRootElement(), true);
		for (ServiceDescriptionProvider provider : providers)
			registry.addServiceDescriptionProvider(provider);
	}

	@SuppressWarnings("unchecked")
	private List<ServiceDescriptionProvider> deserializeProviders(
			Element rootElement, boolean obeyIgnored)
			throws DeserializationException {
		List<ServiceDescriptionProvider> providers = new ArrayList<>();
		Element providersElem = rootElement.getChild(PROVIDERS,
				SERVICE_DESCRIPTION_NS);
		if (providersElem != null)
			for (Element providerElem : (Iterable<Element>) providersElem
					.getChildren(PROVIDER, SERVICE_DESCRIPTION_NS)) {
				ServiceDescriptionProvider serviceProvider = deserializeProvider(providerElem);
				providers.add(serviceProvider);
			}

		if (obeyIgnored) {
			Element ignoredProvidersElem = rootElement.getChild(
					IGNORED_PROVIDERS, SERVICE_DESCRIPTION_NS);
			if (ignoredProvidersElem != null)
				for (Element providerElem : (Iterable<Element>) ignoredProvidersElem
						.getChildren(PROVIDER, SERVICE_DESCRIPTION_NS)) {
					ServiceDescriptionProvider serviceProvider = deserializeProvider(providerElem);
					providers.remove(serviceProvider);
				}
		}

		return providers;
	}

	public Collection<? extends ServiceDescriptionProvider> deserializeDefaults(
			ServiceDescriptionRegistry registry,
			File defaultConfigurableServiceProvidersFile)
			throws DeserializationException {
		Document document;
		try (FileInputStream serviceDescriptionStream = new FileInputStream(
				defaultConfigurableServiceProvidersFile)) {
			SAXBuilder builder = new SAXBuilder();
			document = builder.build(serviceDescriptionStream);
		} catch (JDOMException e) {
			throw new DeserializationException("Can't deserialize "
					+ defaultConfigurableServiceProvidersFile);
		} catch (IOException e) {
			throw new DeserializationException("Can't read "
					+ defaultConfigurableServiceProvidersFile);
		}
		return deserializeProviders(document.getRootElement(), false);
	}

	@SuppressWarnings("unchecked")
	private ServiceDescriptionProvider deserializeProvider(Element providerElem)
			throws DeserializationException {
		Element providerIdElem = providerElem.getChild(PROVIDER_IDENTIFIER, T2_WORKFLOW_NAMESPACE);
		String providerId = providerIdElem.getTextTrim();
		ServiceDescriptionProvider provider = null;
		for (ServiceDescriptionProvider serviceProvider : serviceDescriptionProviders)
			if (serviceProvider.getId().equals(providerId)) {
				provider = serviceProvider;
				break;
			}
		if (provider == null)
			throw new DeserializationException("Could not find provider with id "
					+ providerId);

		/*
		 * So we know the service provider now, but we need a separate instance
		 * of that provider for each providerElem. E.g. we can have 2 or more
		 * WSDL provider elements and need to return a separate provider
		 * instance for each as they will have different configurations.
		 */
		ServiceDescriptionProvider providerInstance = null;
		try {
			providerInstance = provider.getClass().newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new DeserializationException(
					"Can't instantiate provider class " + provider.getClass(), e);
		} catch (ClassCastException e) {
			throw new DeserializationException(
					"Not a ServiceDescriptionProvider: " + provider.getClass(), e);
		}

		// This class loader will know how to load the config bean for the given provider
		ClassLoader classLoader = provider.getClass().getClassLoader();
		Object configBean = createBean(providerElem, classLoader);
		try {
			if (configBean != null)
				((ConfigurableServiceProvider<Object>) providerInstance)
						.configure(configBean);
		} catch (ConfigurationException e) {
			throw new DeserializationException("Could not configure provider "
					+ providerInstance + " using bean " + configBean, e);
		} catch (ClassCastException e) {
			throw new DeserializationException(
					"Not a ConfigurableServiceProvider: "
							+ providerInstance.getClass(), e);
		}
		return providerInstance;
	}
}
