package net.sf.taverna.biocatalogue.model;

import org.apache.log4j.Logger;
import org.biocatalogue.x2009.xml.rest.HttpVerb;

import net.sf.taverna.t2.activities.rest.RESTActivity.HTTP_METHOD;

/**
 * Very simple class for translating HTTP method values returned
 * by the BioCatalogue API into the set of values that are used
 * by the REST activity.
 * 
 * @author Sergejs Aleksejevs
 */
public class HTTPMethodInterpreter
{
  // deny instantiation of this class
  private HTTPMethodInterpreter() { }
  
  public static HTTP_METHOD getHTTPMethodForRESTActivity(HttpVerb.Enum httpVerb)
  {
    switch (httpVerb.intValue()) {
      case HttpVerb.INT_GET: return HTTP_METHOD.GET;
      case HttpVerb.INT_POST: return HTTP_METHOD.POST;
      case HttpVerb.INT_PUT: return HTTP_METHOD.PUT;
      case HttpVerb.INT_DELETE: return HTTP_METHOD.DELETE;
      default:
        String errorMsg = "Unable to translate " + httpVerb.toString() + " to correct representation for REST activity;\n" +
        		              "this HTTP method wasn't supported at the time of implementation.";
        Logger.getLogger(HTTPMethodInterpreter.class).error(errorMsg);
        throw new UnsupportedHTTPMethodException(errorMsg);
    }
  }
  
  
  public static class UnsupportedHTTPMethodException extends IllegalArgumentException
  {
    public UnsupportedHTTPMethodException() {
      /* empty constructor */
    }
    
    public UnsupportedHTTPMethodException(String message) {
      super(message);
    }
  }
}
