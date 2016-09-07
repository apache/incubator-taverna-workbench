package org.apache.taverna.ui.perspectives.biocatalogue;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
