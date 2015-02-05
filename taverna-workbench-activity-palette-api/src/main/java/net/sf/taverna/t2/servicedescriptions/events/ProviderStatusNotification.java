package net.sf.taverna.t2.servicedescriptions.events;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class ProviderStatusNotification extends AbstractProviderNotification {

	public ProviderStatusNotification(ServiceDescriptionProvider provider,
			String message) {
		super(provider, message);
	}

}
