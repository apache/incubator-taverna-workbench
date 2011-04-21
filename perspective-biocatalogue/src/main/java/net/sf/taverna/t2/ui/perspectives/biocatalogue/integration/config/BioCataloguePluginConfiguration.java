package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

/**
 * 
 * 
 * @author Sergejs Aleksejevs
 */
public class BioCataloguePluginConfiguration extends AbstractConfigurable
{
  public static final String BIOCATALOGUE_BASE_URL = "BioCatalogue_Base_URL";
  public static final String SOAP_OPERATIONS_IN_SERVICE_PANEL = "SOAP_Operations_in_Service_Panel";
  public static final String REST_METHODS_IN_SERVICE_PANEL = "REST_Methods_in_Service_Panel";
  
  
  private static class Singleton {
    private static BioCataloguePluginConfiguration instance = new BioCataloguePluginConfiguration();
  }

  // private static Logger logger = Logger.getLogger(MyExperimentConfiguration.class);
  
  private Map<String, String> defaultPropertyMap;
  
  
  public static BioCataloguePluginConfiguration getInstance() {
    return Singleton.instance;
  }
  
  public String getCategory() {
    return "general";
  }
  
  public Map<String,String> getDefaultPropertyMap() {
    if (defaultPropertyMap == null) {
      defaultPropertyMap = new HashMap<String,String>();
      defaultPropertyMap.put(BIOCATALOGUE_BASE_URL, BioCatalogueClient.DEFAULT_API_LIVE_SERVER_BASE_URL);
    }
    return defaultPropertyMap;
  }
  
  public String getDisplayName() {
    return "BioCatalogue";
  }
  
  public String getFilePrefix() {
    return "BioCatalogue";
  }
  
  public String getUUID() {
    return "4daac25c-bd56-4f90-b909-1e49babe5197";
  }
  
  
  /**
   * Just a "proxy" method - {@link AbstractConfigurable#store()}
   * is not visible to the users of instances of this class otherwise. 
   */
  public void store() {
    super.store();
  }
  
}
