package net.sf.taverna.t2.servicedescriptions.impl;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.spi.SPIRegistry;

public class ServiceDescriptionRegistryImpl implements
		ServiceDescriptionRegistry {

	private static class Singleton {
		private static final ServiceDescriptionRegistryImpl instance = 
			new ServiceDescriptionRegistryImpl();
	}

	public static ServiceDescriptionRegistryImpl getInstance() {
		return Singleton.instance;
	}

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

	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders() {
		HashSet<ServiceDescriptionProvider> providers = new HashSet<ServiceDescriptionProvider>(
				providerRegistry.getInstances());
		providers.addAll(localDescriptions);
		return providers;
	}

}
