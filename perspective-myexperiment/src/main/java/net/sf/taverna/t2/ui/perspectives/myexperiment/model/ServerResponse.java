package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import org.jdom.Document;

public class ServerResponse
{
  // CONSTANTS
  public static int LOCAL_FAILURE = -1;
  
  // STORAGE
  private int iResponseCode;
  private Document docResponseBody;
  
  public ServerResponse()
  {
    // do nothing - empty constructor
  }
  
  public ServerResponse(int responseCode, Document responseBody)
  {
    super();
    
    this.iResponseCode = responseCode;
    this.docResponseBody = responseBody;
  }
  
  
  public int getResponseCode()
  {
    return (this.iResponseCode);
  }
  
  public void setResponseCode(int responseCode)
  {
    this.iResponseCode = responseCode;
  }
  
  
  public Document getResponseBody()
  {
    return (this.docResponseBody);
  }
  
  public void setResponseBody(Document responseBody)
  {
    this.docResponseBody = responseBody;
  }
}
