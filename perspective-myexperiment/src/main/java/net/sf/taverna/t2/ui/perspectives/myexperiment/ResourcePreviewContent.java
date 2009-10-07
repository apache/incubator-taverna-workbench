package net.sf.taverna.t2.ui.perspectives.myexperiment;

import javax.swing.JComponent;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;

/**
 * Helper class to hold all data about the generated preview.
 * 
 * @author Sergejs Aleksejevs
 *
 */
public class ResourcePreviewContent
{
  private Resource resource;
  private JComponent jcContent;
  
  public ResourcePreviewContent()
  {
    // empty constructor
  }
  
  public ResourcePreviewContent(Resource resource, JComponent content)
  {
    this.resource = resource;
    this.jcContent = content;
  }
  
  public Resource getResource()
  {
    return(this.resource);
  }
  
  public int getResourceType()
  {
    return(this.resource.getItemType());
  }
  
  public String getResourceTitle()
  {
    return(this.resource.getTitle());
  }
  
  public String getResourceURL()
  {
    return(this.resource.getResource());
  }
  
  public String getResourceURI()
  {
    return(this.resource.getURI());
  }
  
  public JComponent getContent()
  {
    return(this.jcContent);
  }
}
