package net.sf.taverna.t2.servicedescriptions.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.AbstractXMLDeserializer;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ServiceDescriptionDeserializer extends AbstractXMLDeserializer
		implements ServiceDescriptionXMLConstants {

	public void xmlToServiceRegistry(ServiceDescriptionRegistry registry,
			File serviceProviderFile) throws DeserializationException {
		Document document;
		try {
			FileInputStream serviceDescriptionStream = new FileInputStream(
					serviceProviderFile);
			try {
				SAXBuilder builder = new SAXBuilder();
				document = builder.build(serviceDescriptionStream);
			} finally {
				serviceDescriptionStream.close();
			}
		} catch (JDOMException e) {
			throw new DeserializationException("Can't deserialize "
					+ serviceProviderFile);
		} catch (IOException e) {
			throw new DeserializationException("Can't read "
					+ serviceProviderFile);
		}
		xmlToServiceRegistry(registry, document.getRootElement());
	}

	@SuppressWarnings("unchecked")
	public void xmlToServiceRegistry(ServiceDescriptionRegistry registry,
			Element rootElement) throws DeserializationException {
		Element providersElem = rootElement.getChild(PROVIDERS,
				SERVICE_DESCRIPTION_NS);
		if (providersElem != null) {
			for (Element providerElem : (Iterable<Element>) providersElem
					.getChildren(PROVIDER, SERVICE_DESCRIPTION_NS)) {
				ServiceDescriptionProvider serviceProvider = xmlToProvider(providerElem);
				registry.addServiceDescriptionProvider(serviceProvider);
			}
		}
		
		Element ignoredProvidersElem = rootElement.getChild(IGNORED_PROVIDERS,
				SERVICE_DESCRIPTION_NS);
		if (ignoredProvidersElem != null) {
			for (Element providerElem : (Iterable<Element>) ignoredProvidersElem
					.getChildren(PROVIDER, SERVICE_DESCRIPTION_NS)) {
				ServiceDescriptionProvider serviceProvider = xmlToProvider(providerElem);
				registry.removeServiceDescriptionProvider(serviceProvider);
			}
		}		
	}

	private static Logger logger = Logger
			.getLogger(ServiceDescriptionDeserializer.class);

	@SuppressWarnings("unchecked")
	public ServiceDescriptionProvider xmlToProvider(Element providerElem)
			throws DeserializationException {
		Element classElem = providerElem.getChild(CLASS, T2_WORKFLOW_NAMESPACE);
		if (classElem == null) {
			throw new RuntimeException("Can't find class for provider");
		}
		String providerClassName = classElem.getTextTrim();

		ClassLoader classLoader = null;
		try {
			classLoader = getRavenLoader(providerElem);
		} catch (ArtifactNotFoundException e) {
			logger.warn("Can't find artifact for provider "
							+ providerClassName);
		} catch (ArtifactStateException e) {
			logger.warn("Can't load artifact for provider "
							+ providerClassName);
		}
		if (classLoader == null) {
			classLoader = getClass().getClassLoader();
		}
		if (classLoader == null) {
			classLoader = ClassLoader.getSystemClassLoader();
		}

		Class<?> providerClass;
		try {
			providerClass = classLoader.loadClass(providerClassName);
		} catch (ClassNotFoundException e) {
			throw new DeserializationException("Can't find provider class "
					+ providerClassName, e);
		}

		ServiceDescriptionProvider provider;
		try {
			provider = (ServiceDescriptionProvider) providerClass.newInstance();
		} catch (InstantiationException e) {
			throw new DeserializationException(
					"Can't instantiate provider class " + providerClassName, e);
		} catch (IllegalAccessException e) {
			throw new DeserializationException(
					"Can't instantiate provider class " + providerClassName, e);
		} catch (ClassCastException e) {
			throw new DeserializationException(
					"Not a ServiceDescriptionProvider: " + providerClassName, e);
		}

		Object configBean = createBean(providerElem, classLoader);
		if (configBean != null) {
			try {
				((ConfigurableServiceProvider) provider).configure(configBean);
			} catch (ConfigurationException e) {
				throw new DeserializationException(
						"Could not configure provider " + provider
								+ " using bean " + configBean, e);
			} catch (ClassCastException e) {
				throw new DeserializationException(
						"Not a ConfigurableServiceProvider: "
								+ providerClassName, e);
			}
		}
		return provider;
	}
}
