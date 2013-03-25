package net.sf.taverna.t2.servicedescriptions;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

public interface ServiceDescriptionRegistry extends
		Observable<ServiceDescriptionRegistryEvent> {

	public void addServiceDescriptionProvider(ServiceDescriptionProvider provider);

	public Set<ServiceDescriptionProvider> getDefaultServiceDescriptionProviders();

	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders();

	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders(ServiceDescription sd);

	public Set<ServiceDescription> getServiceDescriptions();

	public ServiceDescription getServiceDescription(URI activityType);

	public List<ConfigurableServiceProvider> getUnconfiguredServiceProviders();

	public Set<ServiceDescriptionProvider> getUserAddedServiceProviders();

	public Set<ServiceDescriptionProvider> getUserRemovedServiceProviders();

	public void loadServiceProviders() throws DeserializationException;

	public void loadServiceProviders(File serviceProvidersURL) throws DeserializationException;

	public void loadServiceProviders(URL serviceProvidersURL) throws DeserializationException;

	public void refresh();

	public void removeServiceDescriptionProvider(ServiceDescriptionProvider provider);

	public void saveServiceDescriptions();

	public void saveServiceDescriptions(File serviceDescriptionsFile);

	public void exportCurrentServiceDescriptions(File serviceDescriptionsFile);

	public boolean isDefaultSystemConfigurableProvidersLoaded();

}
