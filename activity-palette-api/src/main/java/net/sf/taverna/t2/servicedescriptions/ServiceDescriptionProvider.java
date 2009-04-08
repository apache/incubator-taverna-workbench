package net.sf.taverna.t2.servicedescriptions;

import java.util.Collection;

public interface ServiceDescriptionProvider {
	
	@SuppressWarnings("unchecked")
	public Collection<? extends ServiceDescription> getServiceDescriptions();
	
}
