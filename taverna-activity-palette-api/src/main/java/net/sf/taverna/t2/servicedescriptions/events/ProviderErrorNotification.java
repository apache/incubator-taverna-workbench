package net.sf.taverna.t2.servicedescriptions.events;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class ProviderErrorNotification extends AbstractProviderNotification {

	private final Throwable cause;

	public ProviderErrorNotification(ServiceDescriptionProvider provider,
			String message, Throwable cause) {
		super(provider, message);
		this.cause = cause;
	}

	public Throwable getCause() {
		return cause;
	}

}
