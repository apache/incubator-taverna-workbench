package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config;

/**
 * Specifies the Service Catalogue change in name and base URL event.
 *
 */
public class ServiceCatalogueChangedEvent {
	
	private String serviceCatalogueName;
	private String serviceCatalogueURL;
	
	public ServiceCatalogueChangedEvent(String name, String url){
		serviceCatalogueName = name;
		serviceCatalogueURL = url;
	}

	public String getServiceCatalogueName() {
		return serviceCatalogueName;
	}
	
	public String getServiceCatalogueURL() {
		return serviceCatalogueURL;
	}

}
