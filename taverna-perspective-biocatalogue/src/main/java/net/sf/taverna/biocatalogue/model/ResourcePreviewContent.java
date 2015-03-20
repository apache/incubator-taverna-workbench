package net.sf.taverna.biocatalogue.model;

import javax.swing.JComponent;
import javax.swing.JLabel;

import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;

/**
 * Helper class to hold all data about the generated preview.
 * 
 * @author Sergejs Aleksejevs
 */
public class ResourcePreviewContent
{
  private Resource resource;
  private JComponent jcContent;
  
  public ResourcePreviewContent(Resource resource, JComponent content)
  {
    this.resource = resource;
    this.jcContent = content;
  }
  
  public Resource getResource() {
    return(this.resource);
  }
  
  public JComponent getContent() {
    return(this.jcContent);
  }
  
  
  public static ResourcePreviewContent createDummyInstance()
  {
    Resource r = new Resource(BioCatalogueClient.API_USERS_URL + "/1", "Dummy user");
    return (new ResourcePreviewContent(r, new JLabel("dummy content - JLabel")));
  }
}
