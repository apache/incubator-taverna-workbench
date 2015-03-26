package org.apache.taverna.ui.perspectives.biocatalogue;

import com.thoughtworks.xstream.XStream;

import org.apache.taverna.ui.perspectives.biocatalogue.integration.config.BioCataloguePluginConfiguration;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel.BioCatalogueServiceProvider;
import org.apache.taverna.workbench.ShutdownSPI;

/**
 * @author Sergejs Aleksejevs
 */
public class MainComponentShutdownHook implements ShutdownSPI
{
  public int positionHint()
  {
    // all custom plugins are suggested to return a value of > 100;
    // this affects when in the termination process will this plugin
    // be shutdown;
    return 100;
  }
  
  public boolean shutdown()
  {
      // Do not save service providers in BioCatalogue's conf file - they should be saved by Taverna together with 
      // other service providers
	  
//      // store services that were added to the Service Panel - both REST and SOAP
//      XStream xstream = new XStream();
//      
//	  BioCataloguePluginConfiguration configuration = BioCataloguePluginConfiguration.getInstance();
//      
//      configuration.setProperty(
//          BioCataloguePluginConfiguration.SOAP_OPERATIONS_IN_SERVICE_PANEL,
//          xstream.toXML(BioCatalogueServiceProvider.getRegisteredSOAPOperations()));
//      configuration.setProperty(
//          BioCataloguePluginConfiguration.REST_METHODS_IN_SERVICE_PANEL,
//          xstream.toXML(BioCatalogueServiceProvider.getRegisteredRESTMethods()));
//      
//      // save all the plugin's configuration 
//      configuration.store();
//      
//      
//      // close API operation log
//      MainComponentFactory.getSharedInstance().getBioCatalogueClient().getAPILogWriter().close();
//      
      return true;
  }
  
}
