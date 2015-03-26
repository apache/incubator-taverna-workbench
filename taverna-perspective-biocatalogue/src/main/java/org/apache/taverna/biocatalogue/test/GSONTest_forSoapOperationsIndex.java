package org.apache.taverna.biocatalogue.test;

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
