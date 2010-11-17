package net.sf.taverna.t2.ui.perspectives.biocatalogue;

import com.thoughtworks.xstream.XStream;

import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config.BioCataloguePluginConfiguration;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.service_panel.BioCatalogueServiceProvider;
import net.sf.taverna.t2.workbench.ShutdownSPI;

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
      // store services that were added to the Service Panel - both REST and SOAP
      XStream xstream = new XStream();
      BioCataloguePluginConfiguration configuration = BioCataloguePluginConfiguration.getInstance();
      configuration.setProperty(
          BioCataloguePluginConfiguration.SOAP_OPERATIONS_IN_SERVICE_PANEL,
          xstream.toXML(BioCatalogueServiceProvider.getRegistereSOAPOperations()));
      configuration.setProperty(
          BioCataloguePluginConfiguration.REST_METHODS_IN_SERVICE_PANEL,
          xstream.toXML(BioCatalogueServiceProvider.getRegisteredRESTMethods()));
      
      
      // save all the plugin's configuration 
      configuration.store();
      
      
      // close API operation log
      MainComponentFactory.getSharedInstance().getBioCatalogueClient().getAPILogWriter().close();
      
      return true;
  }
  
}
