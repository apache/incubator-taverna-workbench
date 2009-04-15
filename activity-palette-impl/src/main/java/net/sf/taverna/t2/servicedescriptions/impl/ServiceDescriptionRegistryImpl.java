package net.sf.taverna.t2.servicedescriptions.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.ConfigurationException;

import org.apache.log4j.Logger;

public class ServiceDescriptionRegistryImpl implements
		ServiceDescriptionRegistry {

	private static class Singleton {
		private static final ServiceDescriptionRegistryImpl instance = new ServiceDescriptionRegistryImpl();
	}

	public static ServiceDescriptionRegistryImpl getInstance() {
		return Singleton.instance;
	}

	public static Logger logger = Logger
			.getLogger(ServiceDescriptionRegistryImpl.class);

	public SPIRegistry<ServiceDescriptionProvider> providerRegistry = new SPIRegistry<ServiceDescriptionProvider>(
			ServiceDescriptionProvider.class);

	private Set<ServiceDescriptionProvider> localDescriptions = new HashSet<ServiceDescriptionProvider>();

	public void addServiceDescriptionProvider(
			ServiceDescriptionProvider provider) {
		localDescriptions.add(provider);
	}

	@SuppressWarnings("unchecked")
	public Set<ServiceDescription> getServiceDescriptions() {
		Set<ServiceDescription> serviceDescriptions = new HashSet<ServiceDescription>();
		for (ServiceDescriptionProvider provider : getServiceDescriptionProviders()) {
			serviceDescriptions.addAll(provider.getServiceDescriptions());
		}
		return serviceDescriptions;
	}

	public <ConfigBean> List<ConfigBean> getConfigurationsFor(
			ConfigurableServiceDescriptionProvider<ConfigBean> provider) {
		return provider.getDefaultConfigurations();
	}

	@SuppressWarnings("unchecked")
	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders() {
		HashSet<ServiceDescriptionProvider> providers = new HashSet<ServiceDescriptionProvider>();
		for (ServiceDescriptionProvider provider : providerRegistry
				.getInstances()) {
			if (provider instanceof ConfigurableServiceDescriptionProvider) {
				ConfigurableServiceDescriptionProvider template = ((ConfigurableServiceDescriptionProvider) provider);
				List<Object> configurables = getConfigurationsFor(template);
				for (Object config : configurables) {
					// Make a copy that we can configure
					ConfigurableServiceDescriptionProvider configurableProvider = template
							.clone();
					try {
						configurableProvider.configure(config);
					} catch (ConfigurationException e) {
						logger.warn("Can't configure provider "
								+ configurableProvider + " with " + config);
						continue;
					}
					providers.add(configurableProvider);
				}
			} else {
				providers.add(provider);
			}
		}
		providers.addAll(localDescriptions);
		return providers;
	}

}
