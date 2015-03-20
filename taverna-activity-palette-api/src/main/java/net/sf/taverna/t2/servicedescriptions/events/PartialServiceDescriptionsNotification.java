package net.sf.taverna.t2.servicedescriptions.events;

import java.util.Collection;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class PartialServiceDescriptionsNotification extends
		AbstractProviderNotification {
	private final Collection<? extends ServiceDescription> serviceDescriptions;

	public PartialServiceDescriptionsNotification(
			ServiceDescriptionProvider provider,
			Collection<? extends ServiceDescription> serviceDescriptions) {
		super(provider, "Found " + serviceDescriptions.size() + " services");
		this.serviceDescriptions = serviceDescriptions;
	}

	public Collection<? extends ServiceDescription> getServiceDescriptions() {
		return serviceDescriptions;
	}
}
