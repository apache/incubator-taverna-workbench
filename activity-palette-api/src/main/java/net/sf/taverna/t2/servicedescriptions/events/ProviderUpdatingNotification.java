package net.sf.taverna.t2.servicedescriptions.events;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class ProviderUpdatingNotification extends AbstractProviderNotification {

	public ProviderUpdatingNotification(ServiceDescriptionProvider provider) {
		super(provider, "Updating");
	}

}
