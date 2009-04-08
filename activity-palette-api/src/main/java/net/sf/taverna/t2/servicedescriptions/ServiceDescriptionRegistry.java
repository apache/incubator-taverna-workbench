package net.sf.taverna.t2.servicedescriptions;

import java.util.Set;

public interface ServiceDescriptionRegistry {
	
	public void addServiceDescriptionProvider(ServiceDescriptionProvider provider);
	
	public Set<ServiceDescriptionProvider> getServiceDescriptionProviders();
	
	@SuppressWarnings("unchecked")
	public Set<ServiceDescription> getServiceDescriptions();

}
