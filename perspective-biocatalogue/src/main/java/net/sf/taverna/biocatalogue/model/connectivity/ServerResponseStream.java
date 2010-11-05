package net.sf.taverna.biocatalogue.model.connectivity;

import java.io.InputStream;

/**
 * This class is a custom version of ServerResponse which contains the
 * InputStream with the the actual server response data.
 * 
 * @author Sergejs Aleksejevs
 */
public class ServerResponseStream extends ServerResponse
{
  private InputStream responseStream;
  
  public ServerResponseStream(int responseCode, InputStream serverResponseStream, String requestURL)
  {
    super(responseCode, requestURL);
    this.setResponseStream(serverResponseStream);
  }
  
  public void setResponseStream(InputStream responseStream)
  {
    this.responseStream = responseStream;
  }
  
  public InputStream getResponseStream()
  {
    return responseStream;
  }
}
