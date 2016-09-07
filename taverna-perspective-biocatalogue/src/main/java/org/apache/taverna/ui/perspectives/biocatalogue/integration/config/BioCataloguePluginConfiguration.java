package org.apache.taverna.ui.perspectives.biocatalogue.integration.config;
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

import java.util.HashMap;
import java.util.Map;

import uk.org.taverna.configuration.AbstractConfigurable;

import org.apache.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import uk.org.taverna.configuration.ConfigurationManager;

/**
 *
 *
 * @author Sergejs Aleksejevs
 */
public class BioCataloguePluginConfiguration extends AbstractConfigurable
{
  public static final String SERVICE_CATALOGUE_BASE_URL = "ServiceCatalogue_Base_URL";
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
      defaultPropertyMap.put(SERVICE_CATALOGUE_BASE_URL, BioCatalogueClient.DEFAULT_API_LIVE_SERVER_BASE_URL);
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
   * Just a "proxy" method - {@link AbstractConfigurable#store()}
   * is not visible to the users of instances of this class otherwise.
   */
  public void store() {
    super.store();
  }

}
