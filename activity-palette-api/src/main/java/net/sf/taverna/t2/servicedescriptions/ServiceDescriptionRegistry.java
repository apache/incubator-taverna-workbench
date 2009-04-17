package net.sf.taverna.t2.servicedescriptions;

import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.servicedescriptions.events.ServiceDescriptionRegistryEvent;

public interface ServiceDescriptionRegistry extends
		Observable<ServiceDescriptionRegistryEvent> {

	public void addServiceDescriptionProvider(
			ServiceDescriptionProvider provider);

	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders();

	@SuppressWarnings("unchecked")
	public Set<ServiceDescription> getServiceDescriptions();

	@SuppressWarnings("unchecked")
	public List<ConfigurableServiceProvider> getUnconfiguredServiceProviders();

	public void removeServiceDescriptionProvider(
			ServiceDescriptionProvider provider);

}
