package net.sf.taverna.t2.servicedescriptions.events;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class RemovedProviderEvent extends AbstractProviderEvent {

	public RemovedProviderEvent(ServiceDescriptionProvider provider) {
		super(provider);
	}
}