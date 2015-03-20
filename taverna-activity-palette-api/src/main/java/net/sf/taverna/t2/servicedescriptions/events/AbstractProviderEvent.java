package net.sf.taverna.t2.servicedescriptions.events;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public abstract class AbstractProviderEvent extends
		ServiceDescriptionRegistryEvent {
	private final ServiceDescriptionProvider provider;

	public AbstractProviderEvent(ServiceDescriptionProvider provider) {
		this.provider = provider;
	}

	public ServiceDescriptionProvider getProvider() {
		return provider;
	}
}
