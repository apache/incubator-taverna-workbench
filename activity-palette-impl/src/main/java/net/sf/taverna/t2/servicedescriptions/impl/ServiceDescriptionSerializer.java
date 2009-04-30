package net.sf.taverna.t2.servicedescriptions.impl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.workflowmodel.serialization.xml.AbstractXMLSerializer;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class ServiceDescriptionSerializer extends AbstractXMLSerializer
		implements ServiceDescriptionXMLConstants{
	
	private static Logger logger = Logger
			.getLogger(ServiceDescriptionSerializer.class);

	@SuppressWarnings("unchecked")
	public Element serviceProviderToXML(ServiceDescriptionProvider provider)
			throws JDOMException, IOException {
		Element serviceProviderElem = new Element(PROVIDER,
				SERVICE_DESCRIPTION_NS);
		ClassLoader cl = provider.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			serviceProviderElem
					.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element(CLASS, T2_WORKFLOW_NAMESPACE);
		classNameElement.setText(provider.getClass().getName());
		serviceProviderElem.addContent(classNameElement);

		if (provider instanceof ConfigurableServiceProvider) {
			ConfigurableServiceProvider configurableServiceProvider = (ConfigurableServiceProvider) provider;
			Object config = configurableServiceProvider.getConfiguration();
			Element configElem = beanAsElement(config);
			serviceProviderElem.addContent(configElem);
		}
		return serviceProviderElem;
	}

	public Element serviceRegistryToXML(ServiceDescriptionRegistry registry) {
		Element serviceDescriptionElem = new Element(SERVICE_DESCRIPTIONS,
				SERVICE_DESCRIPTION_NS);
		
		Element localProvidersElem = new Element(PROVIDERS,
				SERVICE_DESCRIPTION_NS);
		serviceDescriptionElem.addContent(localProvidersElem);

		Set<ServiceDescriptionProvider> localProviders = registry
				.getUserAddedServiceProviders();
		for (ServiceDescriptionProvider provider : localProviders) {
			try {
				localProvidersElem.addContent(serviceProviderToXML(provider));
			} catch (JDOMException e) {
				logger.warn("Could not serialize " + provider, e);
			} catch (IOException e) {
				logger.warn("Could not serialize " + provider, e);
			}
		}
		
		Element ignoredProvidersElem = new Element(IGNORED_PROVIDERS,
				SERVICE_DESCRIPTION_NS);
		serviceDescriptionElem.addContent(ignoredProvidersElem);

		Set<ServiceDescriptionProvider> ignoreProviders = registry
				.getUserRemovedServiceProviders();
		for (ServiceDescriptionProvider provider : ignoreProviders) {
			try {
				ignoredProvidersElem.addContent(serviceProviderToXML(provider));
			} catch (JDOMException e) {
				logger.warn("Could not serialize " + provider, e);
			} catch (IOException e) {
				logger.warn("Could not serialize " + provider, e);
			}
		}
		return serviceDescriptionElem;
	}

	public void serviceRegistryToXML(ServiceDescriptionRegistry registry,
			File xmlFile) throws IOException {
		Element registryElement = serviceRegistryToXML(registry);
		BufferedOutputStream bufferedOutStream = new BufferedOutputStream(
				new FileOutputStream(xmlFile));
		XMLOutputter outputter = new XMLOutputter();
		outputter.setFormat(Format.getPrettyFormat());
		outputter.output(registryElement, bufferedOutStream);
		bufferedOutStream.flush();
		bufferedOutStream.close();
	}

}
