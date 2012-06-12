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
import net.sf.taverna.t2.workflowmodel.serialization.xml.impl.AbstractXMLDeserializer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class ServiceDescriptionDeserializer extends AbstractXMLDeserializer
		implements ServiceDescriptionXMLConstants {

	private List<ServiceDescriptionProvider> serviceDescriptionProviders;

	public ServiceDescriptionDeserializer(List<ServiceDescriptionProvider> serviceDescriptionProviders) {
		this.serviceDescriptionProviders = serviceDescriptionProviders;
	}

	public void xmlToServiceRegistry(ServiceDescriptionRegistry registry,
			InputStream serviceDescriptionsInputStream) throws JDOMException, IOException, DeserializationException {

		Document document = null;
		SAXBuilder builder = new SAXBuilder();
		document = builder.build(serviceDescriptionsInputStream);
		xmlToServiceRegistry(registry, document.getRootElement());
	}

	public void xmlToServiceRegistry(ServiceDescriptionRegistry registry,
			File serviceDescriptionsFile) throws DeserializationException {

		FileInputStream serviceDescriptionFileStream = null;
		try {
			serviceDescriptionFileStream = new FileInputStream(
					serviceDescriptionsFile);
		}
		catch(FileNotFoundException ex){
			throw new DeserializationException("Could not locate file " + serviceDescriptionsFile.getAbsolutePath()+ " containing service descriptions.");
		}

		try{
			xmlToServiceRegistry(registry, serviceDescriptionFileStream);
		}
		catch (JDOMException ex1) {
			throw new DeserializationException("Could not deserialize stream containing service descriptions from " + serviceDescriptionsFile.getAbsolutePath(), ex1);
		} catch (IOException ex2) {
			throw new DeserializationException("Could not read stream containing service descriptions from " + serviceDescriptionsFile.getAbsolutePath(), ex2);
		}
		finally{
			try {
				serviceDescriptionFileStream.close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}

	public void xmlToServiceRegistry(ServiceDescriptionRegistry registry,
			URL serviceDescriptionsURL) throws DeserializationException {

		InputStream serviceDescriptionInputStream = null;
		try {
			serviceDescriptionInputStream = serviceDescriptionsURL.openStream();
		}
		catch(IOException ex){
			throw new DeserializationException("Could not open URL "+ serviceDescriptionsURL.toString()+" containing service descriptions.");
		}

		try{
			xmlToServiceRegistry(registry, serviceDescriptionInputStream);
		}
		catch (JDOMException ex1) {
			throw new DeserializationException("Could not deserialize stream containing service descriptions from " + serviceDescriptionsURL.toString(), ex1);
		} catch (IOException ex2) {
			throw new DeserializationException("Could not read stream containing service descriptions from " + serviceDescriptionsURL.toString(), ex2);
		}
		finally{
			try {
				serviceDescriptionInputStream.close();
			} catch (IOException e) {
				// Ignore
			}
		}
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

	//private static Logger logger = Logger.getLogger(ServiceDescriptionDeserializer.class);

	@SuppressWarnings("unchecked")
	public ServiceDescriptionProvider xmlToProvider(Element providerElem)
			throws DeserializationException {

		Element providerIdElem = providerElem.getChild(PROVIDER_IDENTIFIER, T2_WORKFLOW_NAMESPACE);
		String providerId = providerIdElem.getTextTrim();
		ServiceDescriptionProvider provider = null;
		for ( ServiceDescriptionProvider serviceProvider: serviceDescriptionProviders){
			if (serviceProvider.getId().equals(providerId)){
				provider = serviceProvider;
				break;
			}
		}
		if (provider == null){
			throw new DeserializationException("Could not find provider with id "
					+ providerId);
		}

		// So we know the service provider now, but we need a separate instance of
		// that provider for each providerElem. E.g. we can have 2 or more WSDL provider
		// elements and need to return a separate provider instance for each as they
		// will have different configurations.
		ServiceDescriptionProvider providerInstance = null;
		try {
			providerInstance = provider.getClass().newInstance();
		} catch (InstantiationException e) {
			throw new DeserializationException(
					"Can't instantiate provider class " + provider.getClass(), e);
		} catch (IllegalAccessException e) {
			throw new DeserializationException(
					"Can't instantiate provider class " + provider.getClass(), e);
		} catch (ClassCastException e) {
			throw new DeserializationException(
					"Not a ServiceDescriptionProvider: " + provider.getClass(), e);
		}

		// This class loader will know how to load the config bean for the given provider
		ClassLoader classLoader = provider.getClass().getClassLoader();
		Object configBean = createBean(providerElem, classLoader);
		if (configBean != null) {
			try {
				((ConfigurableServiceProvider) providerInstance).configure(configBean);
			} catch (ConfigurationException e) {
				throw new DeserializationException(
						"Could not configure provider " + providerInstance
								+ " using bean " + configBean, e);
			} catch (ClassCastException e) {
				throw new DeserializationException(
						"Not a ConfigurableServiceProvider: "
								+ providerInstance.getClass(), e);
			}
		}
		return providerInstance;
	}

	public Collection<? extends ServiceDescriptionProvider> xmlToServiceRegistryForDefaultServices(
			ServiceDescriptionRegistry registry,
			File defaultConfigurableServiceProvidersFile) throws DeserializationException {
		Document document;
		try {
			FileInputStream serviceDescriptionStream = new FileInputStream(
					defaultConfigurableServiceProvidersFile);
			try {
				SAXBuilder builder = new SAXBuilder();
				document = builder.build(serviceDescriptionStream);
			} finally {
				serviceDescriptionStream.close();
			}
		} catch (JDOMException e) {
			throw new DeserializationException("Can't deserialize "
					+ defaultConfigurableServiceProvidersFile);
		} catch (IOException e) {
			throw new DeserializationException("Can't read "
					+ defaultConfigurableServiceProvidersFile);
		}
		return xmlToServiceRegistryForDefaultServices(registry, document.getRootElement());
	}

	@SuppressWarnings("unchecked")
	public List<ServiceDescriptionProvider> xmlToServiceRegistryForDefaultServices(ServiceDescriptionRegistry registry,
			Element rootElement) throws DeserializationException {

		List<ServiceDescriptionProvider> providersList = new ArrayList<ServiceDescriptionProvider>();
		Element providersElem = rootElement.getChild(PROVIDERS,
				SERVICE_DESCRIPTION_NS);
		if (providersElem != null) {
			for (Element providerElem : (Iterable<Element>) providersElem
					.getChildren(PROVIDER, SERVICE_DESCRIPTION_NS)) {
				ServiceDescriptionProvider serviceProvider = xmlToProvider(providerElem);
				providersList.add(serviceProvider);
			}
		}
		return providersList;
	}
}
