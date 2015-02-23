package net.sf.taverna.biocatalogue.model;

import org.biocatalogue.x2009.xml.rest.ResourceLink;
import org.biocatalogue.x2009.xml.rest.impl.ResourceLinkImpl;

/**
 * @author Sergejs Aleksejevs
 */
public class LoadingResource extends ResourceLinkImpl
{
  private boolean nowLoading;
  private ResourceLink associatedObj;
  
  public LoadingResource(String resourceURL, String resourceName) {
    super(ResourceLink.type);
    
    associatedObj = ResourceLink.Factory.newInstance();
    associatedObj.setHref(resourceURL);
    associatedObj.setResourceName(resourceName);
    
    this.nowLoading = false;
  }
  
  public String getHref() {
    return (associatedObj.getHref());
  }
  
  public String getResourceName() {
    return (associatedObj.getResourceName());
  }
  
  public boolean isLoading() {
    return (nowLoading);
  }
  public void setLoading(boolean isLoading) {
    this.nowLoading = isLoading;
  }
  
}
