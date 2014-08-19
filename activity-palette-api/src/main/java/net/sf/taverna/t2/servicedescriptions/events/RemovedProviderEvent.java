package net.sf.taverna.t2.servicedescriptions.events;

import java.util.Set;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class RemovedProviderEvent extends AbstractProviderEvent {

	private Set<ServiceDescription> removedDescriptions;

	public RemovedProviderEvent(ServiceDescriptionProvider provider, Set<ServiceDescription> removedDescriptions) {
		super(provider);
		this.removedDescriptions = removedDescriptions;
	}

	/**
	 * @return the removedDescriptions
	 */
	public Set<ServiceDescription> getRemovedDescriptions() {
		return removedDescriptions;
	}
}