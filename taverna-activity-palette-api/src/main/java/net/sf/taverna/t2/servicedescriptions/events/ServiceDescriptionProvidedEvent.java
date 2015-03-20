package net.sf.taverna.t2.servicedescriptions.events;

import java.util.Set;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class ServiceDescriptionProvidedEvent extends AbstractProviderEvent {
	private final Set<ServiceDescription> serviceDescriptions;

	public ServiceDescriptionProvidedEvent(ServiceDescriptionProvider provider,
			Set<ServiceDescription> serviceDescriptions) {
		super(provider);
		this.serviceDescriptions = serviceDescriptions;
	}

	public Set<ServiceDescription> getServiceDescriptions() {
		return serviceDescriptions;
	}
}
