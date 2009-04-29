package net.sf.taverna.t2.servicedescriptions;

import java.io.File;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

public interface ServiceDescriptionRegistry extends
		Observable<ServiceDescriptionRegistryEvent> {

	public void addServiceDescriptionProvider(
			ServiceDescriptionProvider provider);

	public Set<ServiceDescriptionProvider> getUserAddedServiceProviders();
	
	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders();
	
	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders(ServiceDescription sd);

	@SuppressWarnings("unchecked")
	public Set<ServiceDescription> getServiceDescriptions();

	@SuppressWarnings("unchecked")
	public List<ConfigurableServiceProvider> getUnconfiguredServiceProviders();

	public void loadServiceProviders() throws DeserializationException;

	public void loadServiceProviders(File serviceProviderFile)
			throws DeserializationException;

	public void refresh();

	public void removeServiceDescriptionProvider(
			ServiceDescriptionProvider provider);

	public void saveServiceDescriptions();

	public void saveServiceDescriptions(File serviceDescriptionsFile);

	public Set<ServiceDescriptionProvider> getUserRemovedServiceProviders();

	
}
