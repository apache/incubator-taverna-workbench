package net.sf.taverna.t2.servicedescriptions.events;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class ProviderWarningNotification extends AbstractProviderNotification {

	public ProviderWarningNotification(ServiceDescriptionProvider provider,
			String message) {
		super(provider, message);
	}

}
