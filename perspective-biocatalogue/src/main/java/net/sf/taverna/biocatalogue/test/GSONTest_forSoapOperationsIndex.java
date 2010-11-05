package net.sf.taverna.biocatalogue.test;

import java.io.InputStreamReader;

import com.google.gson.Gson;

import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.Util;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.model.connectivity.BeansForJSONLiteAPI;

public class GSONTest_forSoapOperationsIndex
{

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception
  {
    BioCatalogueClient client = new BioCatalogueClient(); 
    
    String url = BioCatalogueClient.API_SOAP_OPERATIONS_URL;
//    url = Util.appendURLParameter(url, "q", "blast");
    BeansForJSONLiteAPI.ResourceIndex soapOpIndex = client.getBioCatalogueResourceLiteIndex(Resource.TYPE.SOAPOperation, url);
    
    System.out.println("result count: " + soapOpIndex.getResources().length + "\n\n");
//    System.out.println(soapOpIndex.soap_operations[1].getName() + "\n" + soapOpIndex.soap_operations[1].getURL() + "\n\n");
    
  }

}
