package net.sf.taverna.t2.servicedescriptions.events;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class AddedProviderEvent extends AbstractProviderEvent {

	public AddedProviderEvent(ServiceDescriptionProvider provider) {
		super(provider);
	}
}