package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config.BioCataloguePluginConfigurationPanel.ServiceCatalogue;
import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * 
 * 
 * @author Sergejs Aleksejevs
 */
public class BioCataloguePluginConfiguration extends AbstractConfigurable
		implements Observable<BaseURLChangedEvent> {
	public static final String SERVICE_CATALOGUE_BASE_URL = "ServiceCatalogue_Base_URL";
	private String serviceCatalogueFriendlyName;
	public static final String SOAP_OPERATIONS_IN_SERVICE_PANEL = "SOAP_Operations_in_Service_Panel";
	public static final String REST_METHODS_IN_SERVICE_PANEL = "REST_Methods_in_Service_Panel";

	public static final String DEFAULT_SERVICE_CATALOGUE_TYPE = "DEFAULT";
	public static final String USER_ADDED_SERVICE_CATALOGUE_TYPE = "USER_ADDED";
	
	public static final String BIOCATALOGUE_URL = "https://www.biocatalogue.org";
	public static final String BIOCATALOGUE_FRIENDLY_NAME = "BioCatalogue";

	public static final String BIODIVERSITYCATALOGUE_URL = "https://www.biodiversitycatalogue.org";
	public static final String BIODIVERSITYCATALOGUE_FRIENDLY_NAME = "BiodiversityCatalogue";

	public static List<ServiceCatalogue> defaultCatalogues = new ArrayList<ServiceCatalogue>();
	{
		defaultCatalogues.add(new ServiceCatalogue(BIOCATALOGUE_FRIENDLY_NAME,
				BIOCATALOGUE_URL, DEFAULT_SERVICE_CATALOGUE_TYPE));
		defaultCatalogues.add(new ServiceCatalogue(BIODIVERSITYCATALOGUE_FRIENDLY_NAME,
				BIODIVERSITYCATALOGUE_URL,
				DEFAULT_SERVICE_CATALOGUE_TYPE));
	};
	
	private MultiCaster<BaseURLChangedEvent> multiCaster = new MultiCaster<BaseURLChangedEvent>(this);

	private static class Singleton {
		private static BioCataloguePluginConfiguration instance = new BioCataloguePluginConfiguration();
	}

	// private static Logger logger =
	// Logger.getLogger(MyExperimentConfiguration.class);

	private Map<String, String> defaultPropertyMap;

	public static BioCataloguePluginConfiguration getInstance() {
		return Singleton.instance;
	}

	public String getCategory() {
		return "general";
	}

	public Map<String, String> getDefaultPropertyMap() {
		if (defaultPropertyMap == null) {
			defaultPropertyMap = new HashMap<String, String>();
			defaultPropertyMap.put(SERVICE_CATALOGUE_BASE_URL,
					BioCatalogueClient.DEFAULT_API_LIVE_SERVER_BASE_URL);
		}
		return defaultPropertyMap;
	}

	public String getDisplayName() {
		return "Service catalogue";
	}

	public String getFilePrefix() {
		return "ServiceCatalogue";
	}

	public String getUUID() {
		return "4daac25c-bd56-4f90-b909-1e49babe5197";
	}

	/**
	 * Just a "proxy" method - {@link AbstractConfigurable#store()} is not
	 * visible to the users of instances of this class otherwise.
	 */
	public void store() {
		super.store();
	}
	
	@Override
	public synchronized void setProperty(String key, String value){
		super.setProperty(key, value);
		if (key.equals(BioCataloguePluginConfiguration.SERVICE_CATALOGUE_BASE_URL)){
			if (multiCaster == null){
				multiCaster = new MultiCaster<BaseURLChangedEvent>(this); // for some reason multicaster is not initialised the first time this method is called
			}
			multiCaster.notify(new BaseURLChangedEvent());
		}
	}

	@Override
	public void addObserver(Observer<BaseURLChangedEvent> observer) {
		multiCaster.addObserver(observer);
	}

	@Override
	public List<Observer<BaseURLChangedEvent>> getObservers() {
		return multiCaster.getObservers();
	}

	@Override
	public void removeObserver(Observer<BaseURLChangedEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	public String getServiceCatalogueFriendlyName() {
		// Because we did not set the friendly name of the service catalogue
		// initially, we have to do some tricks to get them in here.
		// Later on, make sure you set the friendly name each time you
		// set the SERVICE_CATALOGUE_BASE_URL property.
		if (serviceCatalogueFriendlyName == null){
			if (getInstance().getProperty(SERVICE_CATALOGUE_BASE_URL).equals(BIOCATALOGUE_URL)){
				serviceCatalogueFriendlyName = BIOCATALOGUE_FRIENDLY_NAME;
			}
			else if (getInstance().getProperty(SERVICE_CATALOGUE_BASE_URL).equals(BIODIVERSITYCATALOGUE_URL)){
				serviceCatalogueFriendlyName = BIODIVERSITYCATALOGUE_FRIENDLY_NAME;
			}
		}
		return serviceCatalogueFriendlyName;
	}

	public void setServiceCatalogueFriendlyName(
			String serviceCatalogueFriendlyName) {
		this.serviceCatalogueFriendlyName = serviceCatalogueFriendlyName;
	}
}
