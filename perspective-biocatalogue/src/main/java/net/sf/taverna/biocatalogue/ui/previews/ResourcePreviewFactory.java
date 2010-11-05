package net.sf.taverna.biocatalogue.ui.previews;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.biocatalogue.x2009.xml.rest.SoapOperation;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.ResourcePreviewContent;
import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;

/**
 * @author Sergejs Aleksejevs
 */
public class ResourcePreviewFactory
{
  // make sure noone is able to instantiate this class
  private ResourcePreviewFactory() {}
  
  public static ResourcePreviewContent createPreview(String previewAction, BioCatalogueClient client, Logger logger)
  {
    JPanel jpPreview = new JPanel(new BorderLayout());
    Resource resourceToPreview = null;
    
    // this variable will only be used for SOAP operation previews
    // (service preview will be initialized with this operation pre-selected)
    String soapOperationNameForServicePreviewInitialisation = null;
    
    // *** Verify request validity ***
    
    // return error message if the action string isn't actually a request for preview
    if(!previewAction.startsWith(BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE) &&
       !previewAction.startsWith(BioCataloguePluginConstants.ACTION_PREVIEW_SOAP_OPERATION_AFTER_LOOKUP))
    {
      logger.error("Bad preview request: \"" + previewAction + "\"");
      jpPreview.add(new JLabel("An error has occurred."));
      resourceToPreview = new Resource(previewAction, "Bad preview request");
      return(new ResourcePreviewContent(resourceToPreview, jpPreview));
    }
    
    // *** Preprocessing ***
    if (previewAction.startsWith(BioCataloguePluginConstants.ACTION_PREVIEW_SOAP_OPERATION_AFTER_LOOKUP)) {
      SoapOperationIdentity soapOperationDetails = SoapOperationIdentity.fromActionString(previewAction);
      if (soapOperationDetails != null) {
        // ok, got the details - lookup the parent service url
        try {
          SoapOperation soapOp = client.lookupSoapOperation(soapOperationDetails);
          
          if (soapOp != null) {
            // lookup was successful - replace the "lookup" action string with the real one
            // (which could have been there for a normal service preview)
            previewAction = BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE + soapOp.getAncestors().getService().getHref();
            
            // also, take a note of the operation name - will be used to initialise the service preview with the correct operation
            soapOperationNameForServicePreviewInitialisation = soapOperationDetails.getOperationName();
          }
          else {
            jpPreview.add(new JLabel("<html><span style=\"color: gray; font-style: italic;\">There is no " +
            		                     "data about this SOAP operation in BioCatalogue.</span></html>",
                                     UIManager.getIcon("OptionPane.informationIcon"),
                                     JLabel.CENTER));
            resourceToPreview = new Resource(previewAction, "SOAP operation was not found");
            return(new ResourcePreviewContent(resourceToPreview, jpPreview));
          }
        }
        catch (Exception e) {
          e.printStackTrace();
          logger.error("An error occurred while looking up SOAP opearation on BioCatalogue; action string was: \"" + previewAction + "\"");
          jpPreview.add(new JLabel("<html>An error has occurred. Soap operation preview was requested,<br>" +
                                   "but lookup on BioCatalogue did not succeed.</html>",
                                   UIManager.getIcon("OptionPane.errorIcon"),
                                   JLabel.CENTER));
          resourceToPreview = new Resource(previewAction, "Preview generation did not succeed");
          return(new ResourcePreviewContent(resourceToPreview, jpPreview));
        }
      }
      else {
        logger.error("Bad preview request: \"" + previewAction + "\"");
        jpPreview.add(new JLabel("<html>An error has occurred. Soap operation preview was requested,<br>" +
        		                     "but incorrect data (WSDL location / operation name)<br>" +
        		                      "was passed into preview browser.</html>",
        		                      UIManager.getIcon("OptionPane.errorIcon"),
                                  JLabel.CENTER));
        resourceToPreview = new Resource(previewAction, "Bad preview request");
        return(new ResourcePreviewContent(resourceToPreview, jpPreview));
      }
    }
    
    // *** Pick preview factory ***
    
    switch(Resource.getResourceTypeFromResourceURL(previewAction)) {
      // FIXME
//      case Resource.SERVICE_TYPE: return generateServicePreview(previewAction, client, logger, jpPreview, soapOperationNameForServicePreviewInitialisation);
//      case Resource.SERVICE_PROVIDER_TYPE:
//      case Resource.USER_TYPE:
//      case Resource.REGISTRY_TYPE:
      default: jpPreview.add(new JLabel("<html><span style=\"color: gray; font-style: italic;\">Previews for " +
      		                              "this type of resources are not yet implemented.</span></html>",
                                        UIManager.getIcon("OptionPane.informationIcon"),
                                        JLabel.CENTER));
               resourceToPreview = new Resource(previewAction, "No preview available");
               return(new ResourcePreviewContent(resourceToPreview, jpPreview));
    }
  }
  
  
  private static ResourcePreviewContent generateServicePreview(String previewAction,
                          BioCatalogueClient client, Logger logger, JPanel jpPreview,
                          String operationNameToInitialisePreview)
  {
    return (new ServicePreviewFactory(client, logger, 
                  MainComponentFactory.getSharedInstance().getPreviewBrowser(), 
                  previewAction, operationNameToInitialisePreview).makePreview());
  }
  
  
}
