package net.sf.taverna.biocatalogue.model.connectivity;

import net.sf.taverna.biocatalogue.model.Util;

/**
 * @author Sergejs Aleksejevs
 */
public abstract class ServerResponse
{
  // this code is to be used when a local failure is encountered and the
  // server response is a blank / invalid one
  public static int LOCAL_FAILURE_CODE = -1;
  
  // server response code - in theory should correspond to HTTP response codes 
  private int iResponseCode;
  
  // URL that was used to make the request
  private String requestURL;
  
  
  public ServerResponse() {
    // do nothing - empty constructor
  }
  
  public ServerResponse(int responseCode, String requestURL) {
    super();
    this.iResponseCode = responseCode;
    this.requestURL = Util.urlDecodeQuery(requestURL);
  }
  
  
  public int getResponseCode() {
    return (this.iResponseCode);
  }
  
  public String getRequestURL() {
    return requestURL;
  }
  
}
