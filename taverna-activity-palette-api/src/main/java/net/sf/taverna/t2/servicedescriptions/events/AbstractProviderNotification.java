package net.sf.taverna.t2.servicedescriptions.events;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class AbstractProviderNotification extends AbstractProviderEvent {

	private final String message;

	public AbstractProviderNotification(ServiceDescriptionProvider provider, String message) {
		super(provider);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
