package net.sf.taverna.biocatalogue.model.connectivity;

/**
 * A class to wrap BioCatalogue API requests - will include
 * the type (GET, POST, etc), URL and the data to send
 * if that's a POST / PUT request. 
 * 
 * @author Sergejs Aleksejevs
 */
public class BioCatalogueAPIRequest
{
  public static enum TYPE {
    GET,
    POST,
    PUT,
    DELETE
  }
  
  
  private TYPE requestType;
  private String url;
  private String data;
  
  
  public BioCatalogueAPIRequest(TYPE requestType, String url, String data) {
    this.requestType = requestType;
    this.url = url;
    this.data = data;
  }
  
  
  public TYPE getRequestType() {
    return requestType;
  }
  
  public String getURL(){
    return url;
  }
  public void setURL(String url) {
    this.url = url;
  }
  
  public String getData(){
    return data;
  }
  
}
