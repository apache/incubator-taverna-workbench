package net.sf.taverna.t2.servicedescriptions.events;

import java.util.Collection;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class PartialServiceDescriptionsNotification extends
		AbstractProviderNotification {

	@SuppressWarnings("unchecked")
	private final Collection<? extends ServiceDescription> serviceDescriptions;

	@SuppressWarnings("unchecked")
	public PartialServiceDescriptionsNotification(
			ServiceDescriptionProvider provider,
			Collection<? extends ServiceDescription> serviceDescriptions) {
		super(provider, "Found " + serviceDescriptions.size() + " services");
		this.serviceDescriptions = serviceDescriptions;
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends ServiceDescription> getServiceDescriptions() {
		return serviceDescriptions;
	}

}
