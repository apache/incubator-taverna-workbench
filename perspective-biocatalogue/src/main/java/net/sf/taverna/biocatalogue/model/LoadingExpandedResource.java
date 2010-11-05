package net.sf.taverna.biocatalogue.model;

import org.biocatalogue.x2009.xml.rest.ResourceLink;
import org.biocatalogue.x2009.xml.rest.impl.ResourceLinkImpl;

/**
 * @author Sergejs Aleksejevs
 */
public class LoadingExpandedResource extends ResourceLinkImpl
{
  private boolean nowLoading;
  private ResourceLink associatedObj;
  
  public LoadingExpandedResource(ResourceLink associatedObj)
  {
    super(ResourceLink.type);
    
    this.associatedObj = associatedObj;
    this.nowLoading = true;
  }
  
  public ResourceLink getAssociatedObj() {
    return associatedObj;
  }
  
  public boolean isLoading() {
    return (nowLoading);
  }
  public void setLoading(boolean isLoading) {
    this.nowLoading = isLoading;
  }
  
  public String getHref() {
    return (associatedObj.getHref());
  }
  
  public String getResourceName() {
    return (associatedObj.getResourceName());
  }
}

