package org.apache.taverna.biocatalogue.test;
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

import org.apache.taverna.biocatalogue.model.Resource;
import org.apache.taverna.biocatalogue.model.connectivity.BeansForJSONLiteAPI;
import org.apache.taverna.biocatalogue.model.connectivity.BioCatalogueClient;

public class GSONTest_forSoapOperationsIndex
{

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    BioCatalogueClient client = BioCatalogueClient.getInstance(); 
    
    String url = BioCatalogueClient.API_SOAP_OPERATIONS_URL;
//    url = Util.appendURLParameter(url, "q", "blast");
    BeansForJSONLiteAPI.ResourceIndex soapOpIndex = client.getBioCatalogueResourceLiteIndex(Resource.TYPE.SOAPOperation, url);
    
    System.out.println("result count: " + soapOpIndex.getResources().length + "\n\n");
//    System.out.println(soapOpIndex.soap_operations[1].getName() + "\n" + soapOpIndex.soap_operations[1].getURL() + "\n\n");
    
  }

}
