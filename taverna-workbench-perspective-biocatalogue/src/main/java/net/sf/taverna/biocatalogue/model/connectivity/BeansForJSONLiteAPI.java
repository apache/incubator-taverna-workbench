package net.sf.taverna.biocatalogue.model.connectivity;


/**
 * Binding beans for GSON library to instantiate objects
 * from JSON data obtained from the 'Lite' version of the
 * BioCatalogue JSON API.
 * 
 * @author Sergejs Aleksejevs
 */
public class BeansForJSONLiteAPI
{
  
  public static abstract class ResourceIndex
  {
    public ResourceIndex() { }
    public abstract ResourceLinkWithName[] getResources();
  }
  
  
  public static class SOAPOperationsIndex extends ResourceIndex {
    public SOAPOperationsIndex() { }
    public ResourceLinkWithName[] soap_operations;
    
    public ResourceLinkWithName[] getResources() {
      return soap_operations;
    }
  }
  
  public static class RESTMethodsIndex extends ResourceIndex {
    public RESTMethodsIndex() { }
    public ResourceLinkWithName[] rest_methods;
    
    public ResourceLinkWithName[] getResources() {
      return rest_methods;
    }
  }
  
  public static class ServicesIndex extends ResourceIndex {
    public ServicesIndex() { }
    public ResourceLinkWithName[] services;
    
    public ResourceLinkWithName[] getResources() {
      return services;
    }
  }
  
  public static class ServiceProvidersIndex extends ResourceIndex {
    public ServiceProvidersIndex() { }
    public ResourceLinkWithName[] service_providers;
    
    public ResourceLinkWithName[] getResources() {
      return service_providers;
    }
  }
  
  public static class UsersIndex extends ResourceIndex {
    public UsersIndex() { }
    public ResourceLinkWithName[] users;
    
    public ResourceLinkWithName[] getResources() {
      return users;
    }
  }
  
  
  
  public static class ResourceLinkWithName
  {
    private ResourceLinkWithName() { }
    
    private String resource;
    private String name;
    
    public String getURL() {
      return (this.resource);
    }
    
    public String getName() {
      return (this.name);
    }
  }
  
}
