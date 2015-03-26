package org.apache.taverna.ui.perspectives.biocatalogue.integration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.biocatalogue.x2009.xml.rest.ResourceLink;
import org.biocatalogue.x2009.xml.rest.RestMethod;
import org.biocatalogue.x2009.xml.rest.RestService;
import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.ServiceTechnologyType;
import org.biocatalogue.x2009.xml.rest.SoapOperation;
import org.biocatalogue.x2009.xml.rest.SoapService;
import org.biocatalogue.x2009.xml.rest.Service.Variants;

import org.apache.taverna.biocatalogue.model.HTTPMethodInterpreter;
import org.apache.taverna.biocatalogue.model.HTTPMethodInterpreter.UnsupportedHTTPMethodException;
import org.apache.taverna.biocatalogue.model.Resource;
import org.apache.taverna.biocatalogue.model.Resource.TYPE;
import org.apache.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import org.apache.taverna.biocatalogue.model.ResourceManager;
import org.apache.taverna.biocatalogue.model.SoapOperationIdentity;
import org.apache.taverna.biocatalogue.model.SoapOperationPortIdentity;
import org.apache.taverna.biocatalogue.model.SoapProcessorIdentity;
import org.apache.taverna.biocatalogue.model.Util;
import net.sf.taverna.t2.activities.rest.RESTActivity;
import net.sf.taverna.t2.activities.rest.RESTActivity.HTTP_METHOD;
import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.servicedescriptions.WSDLServiceDescription;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.perspectives.biocatalogue.MainComponentFactory;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel.BioCatalogueRESTServiceProvider;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel.BioCatalogueWSDLOperationServiceProvider;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel.RESTFromBioCatalogueServiceDescription;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel.WSDLOperationFromBioCatalogueServiceDescription;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * This class contains helpers for deeper integration with Taverna UI.
 * 
 * @author Sergejs Aleksejevs
 */
public class Integration
{
  private static final Logger logger = Logger.getLogger(Integration.class);
  
  
  // deny instantiation of this class
  private Integration() { }
  
  
  /**
   * Adds a processor to the current workflow.
   * The processor is specified by WSDL location and the operation name.
   * 
   * @param processorResource Resource to add to the current workflow.
   * @return Outcome of inserting the processor into the current workflow as a
   *         HTML-formatted string (with no opening and closing HTML tags).
   */
  public static JComponent insertProcessorIntoCurrentWorkflow(ResourceLink processorResource)
  {
    // check if this type of resource can be added to workflow diagram
    TYPE resourceType = Resource.getResourceTypeFromResourceURL(processorResource.getHref());
    if (resourceType.isSuitableForAddingToWorkflowDiagram()) {
      switch (resourceType) {
        case SOAPOperation:
          SoapOperation soapOp = (SoapOperation) processorResource;
          try {
            SoapService soapService = BioCatalogueClient.getInstance().
                                        getBioCatalogueSoapService(soapOp.getAncestors().getSoapService().getHref());
            
            try {
              WSDLServiceDescription myServiceDescription = new WSDLServiceDescription();
              myServiceDescription.setOperation(soapOp.getName());
              myServiceDescription.setUse("literal"); // or "encoded"
              myServiceDescription.setStyle("document"); // or "rpc"
              myServiceDescription.setURI(new URI(soapService.getWsdlLocation()));
              myServiceDescription.setDescription(StringEscapeUtils.escapeHtml(soapService.getDescription()));  // TODO - not sure where this is used
              
              if (WorkflowView.importServiceDescription(myServiceDescription, false) != null) {
                return (new JLabel("Selected " + TYPE.SOAPOperation.getTypeName() + " was successfully added to the current workflow",
                                 ResourceManager.getImageIcon(ResourceManager.TICK_ICON), JLabel.CENTER));
              }
              else {
                return (new JLabel("<html><center>Taverna was unable to add selected " + TYPE.SOAPOperation.getTypeName() + 
                    " as a service to the current workflow.<br>This could be because the service is currently not accessible.</center></html>",
                    ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
              }
            }
            catch (URISyntaxException e)
            {
              logger.error("Couldn't add " + TYPE.SOAPOperation + " to the current workflow", e);
              return (new JLabel("<html>Could not add the selected " + TYPE.SOAPOperation.getTypeName() + " to the current workflow.<br>" +
                                    		"Log file will containt additional details about this error.</html>",
                                    		ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
            }
            
          }
          catch (Exception e) {
            logger.error("Failed to fetch required details to add this " + TYPE.SOAPOperation + " into the current workflow.", e);
            return (new JLabel("<html>Failed to fetch required details to add this<br>" +
                                      TYPE.SOAPOperation.getTypeName() + " into the current workflow.</html>",
                                      ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
          }
          
        case RESTMethod:
          // received object may only contain limited data, therefore need to fetch full details first
          try {
            RestMethod restMethod = BioCatalogueClient.getInstance().
                                                getBioCatalogueRestMethod(processorResource.getHref());
            
            // actual import of the service into the workflow
            RESTFromBioCatalogueServiceDescription restServiceDescription = createRESTServiceDescriptionFromRESTMethod(restMethod);
            WorkflowView.importServiceDescription(restServiceDescription, false);
            
            // prepare result of the operation to be shown in the the waiting dialog window
            String warnings = extractWarningsFromRESTServiceDescription(restServiceDescription, false);
            JLabel outcomes = new JLabel("<html>Selected " + TYPE.RESTMethod.getTypeName() + " was successfully added to the current workflow" + warnings + "</html>",
                                         ResourceManager.getImageIcon(warnings.length() > 0 ? ResourceManager.WARNING_ICON : ResourceManager.TICK_ICON),
                                         JLabel.CENTER);
            outcomes.setIconTextGap(20);
            return (outcomes);
          }
          catch (UnsupportedHTTPMethodException e) {
            logger.error(e);
            return (new JLabel(e.getMessage(), ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
          }
          catch (Exception e) {
            logger.error("Failed to fetch required details to add this " + TYPE.RESTMethod + " as a service to the current workflow.", e);
            return (new JLabel("<html>Failed to fetch required details to add this " + TYPE.RESTMethod.getTypeName() + "<br>" +
            		                      "as a service to the current workflow.</html>",
                                      ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
          }
        
        // type not currently supported, but maybe in the future?
        default: return (new JLabel("Adding " + resourceType.getCollectionName() + " to the current workflow is not yet possible",
                                     ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
      }
    }
    
    // definitely not supported type
    return (new JLabel("<html>It is not possible to add resources of the provided type<br>" +
                              "into the current workflow.</html>",
                              ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
  }
  
  
  /**
   * 
   * @param processorResource
   * @return Outcome of inserting the processor into the current workflow as a
   *         HTML-formatted string (with no opening and closing HTML tags).
   */
  public static JComponent insertProcesorIntoServicePanel(ResourceLink processorResource)
  {
    // check if this type of resource can be added to Service Panel
    TYPE resourceType = Resource.getResourceTypeFromResourceURL(processorResource.getHref());
    if (resourceType.isSuitableForAddingToServicePanel()) {
      switch (resourceType) {
        case SOAPOperation:
          SoapOperation soapOp = (SoapOperation) processorResource;
          try {
            SoapService soapService = BioCatalogueClient.getInstance().
                                        getBioCatalogueSoapService(soapOp.getAncestors().getSoapService().getHref());
            SoapOperationIdentity soapOpId = new SoapOperationIdentity(soapService.getWsdlLocation(), soapOp.getName(), StringEscapeUtils.escapeHtml(soapOp.getDescription()));
            WSDLOperationFromBioCatalogueServiceDescription wsdlOperationDescription = new WSDLOperationFromBioCatalogueServiceDescription(soapOpId);
            BioCatalogueWSDLOperationServiceProvider.registerWSDLOperation(wsdlOperationDescription, null);
            
            return (new JLabel("Selected SOAP operation has been successfully added to the Service Panel.", 
                               ResourceManager.getImageIcon(ResourceManager.TICK_ICON), JLabel.CENTER));
          }
          catch (Exception e) {
            logger.error("Failed to fetch required details to add this SOAP service into the Service Panel.", e);
            return (new JLabel("Failed to fetch required details to add this " +
                               "SOAP service into the Service Panel.", ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
          }
          
        case RESTMethod:
          try {
            // received object may only contain limited data, therefore need to fetch full details first
            RestMethod restMethod = BioCatalogueClient.getInstance().
                                                  getBioCatalogueRestMethod(processorResource.getHref());
            RESTFromBioCatalogueServiceDescription restServiceDescription = createRESTServiceDescriptionFromRESTMethod(restMethod);
            
            // actual insertion of the REST method into Service Panel
            BioCatalogueRESTServiceProvider.registerNewRESTMethod(restServiceDescription, null);
            
            // prepare result of the operation to be shown in the the waiting dialog window
            String warnings = extractWarningsFromRESTServiceDescription(restServiceDescription, true);
            JLabel outcomes = new JLabel("<html>Selected REST method has been successfully added to the Service Panel" + warnings + "</html>", 
                                         ResourceManager.getImageIcon(warnings.length() > 0 ? ResourceManager.WARNING_ICON : ResourceManager.TICK_ICON),
                                         JLabel.CENTER);
            outcomes.setIconTextGap(20);
            return (outcomes);
          }
          catch (UnsupportedHTTPMethodException e) {
            logger.error(e);
            return (new JLabel(e.getMessage(), ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
          }
          catch (Exception e) {
            logger.error("Failed to fetch required details to add this REST service into the Service Panel.", e);
            return (new JLabel("Failed to fetch required details to add this " +
                "REST service into the Service Panel.", ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
          }
        
        // type not currently supported, but maybe in the future?
        default: return (new JLabel("Adding " + resourceType.getCollectionName() + " to the Service Panel is not yet possible",
            ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
      }
    }
    
    // definitely not supported type
    return (new JLabel("<html>It is not possible to add resources of the provided type<br>" +
                              "into the Service Panel.</html>",
                              ResourceManager.getImageIcon(ResourceManager.ERROR_ICON), JLabel.CENTER));
  }
  
  /**
   * Inserts all operations of the given parent SOAP or REST Web service resource link
   * into Service Panel. Works for SOAP operations only at the moment.
   * 
   * @return Outcome of inserting operations into Service Panel as a
   *         HTML-formatted string (with no opening and closing HTML tags).
   */
  public static JComponent insertAllOperationsIntoServicePanel(ResourceLink serviceResource)
  {
		// Check if this type of resource is a parent SOAP Web service
		// whose operations can be added to the Service Panel
		TYPE resourceType = Resource
				.getResourceTypeFromResourceURL(serviceResource.getHref());
		
		Service service = null;
		if (resourceType == TYPE.SOAPOperation) {
			SoapService soapService = ((SoapOperation) serviceResource)
					.getAncestors().getSoapService();

			// Get the WSDL URL of the SOAP service
			String wsdlURL = soapService.getWsdlLocation();

			// Import this WSDL into Service panel - it will add all
			// of
			// its operations
			if (BioCatalogueWSDLOperationServiceProvider.registerWSDLService(
					wsdlURL, null)) {
				return (new JLabel(
						"Operation(s) of the SOAP service have been successfully added to the Service Panel.",
						ResourceManager.getImageIcon(ResourceManager.TICK_ICON),
						JLabel.CENTER));
			} else {
				return (new JLabel(
						"Failed to insert the operations of the SOAP service "
								+ " to the Service Panel.", ResourceManager
								.getImageIcon(ResourceManager.ERROR_ICON),
						JLabel.CENTER));
			}
		} else if (resourceType == TYPE.RESTMethod) {
			RestService restService = ((RestMethod) serviceResource)
			.getAncestors().getRestService();
			for (RestMethod method : restService.getMethods().getRestMethodList()) {
				
			}
		}

		return (new JLabel(
				"<html>It is not possible to add resources of the provided type<br>"
						+ "into the Service Panel.</html>", ResourceManager
						.getImageIcon(ResourceManager.ERROR_ICON),
				JLabel.CENTER));
	}

  /**
   * Instantiates a {@link RESTFromBioCatalogueServiceDescription} object from the {@link RestMethod}
   * XML data obtained from BioCatalogue API.
   * 
   * @param restMethod
   * @return
   */
  public static RESTFromBioCatalogueServiceDescription createRESTServiceDescriptionFromRESTMethod(RestMethod restMethod) throws UnsupportedHTTPMethodException
  {
    // if the type of the HTTP method is not supported, an exception will be throws
    HTTP_METHOD httpMethod = HTTPMethodInterpreter.getHTTPMethodForRESTActivity(restMethod.getHttpMethodType());
    
    RESTFromBioCatalogueServiceDescription restServiceDescription = new RESTFromBioCatalogueServiceDescription();
    restServiceDescription.setServiceName(Resource.getDisplayNameForResource(restMethod));
    restServiceDescription.setDescription(StringEscapeUtils.escapeHtml(restMethod.getDescription()));
    restServiceDescription.setHttpMethod(httpMethod);
    restServiceDescription.setURLSignature(restMethod.getUrlTemplate());
    
    int outputRepresentationCount = restMethod.getOutputs().getRepresentations().getRestRepresentationList().size();
    if (outputRepresentationCount > 0) {
      if (outputRepresentationCount > 1) {
        restServiceDescription.getDataWarnings().add(RESTFromBioCatalogueServiceDescription.AMBIGUOUS_ACCEPT_HEADER_VALUE);
      }
      restServiceDescription.setAcceptHeaderValue(restMethod.getOutputs().getRepresentations().getRestRepresentationList().get(0).getContentType());
    }
    else {
      restServiceDescription.getDataWarnings().add(RESTFromBioCatalogueServiceDescription.DEFAULT_ACCEPT_HEADER_VALUE);
    }
    
    int inputRepresentationCount = restMethod.getInputs().getRepresentations().getRestRepresentationList().size();
    if (inputRepresentationCount > 0) {
      if (inputRepresentationCount > 1) {
        restServiceDescription.getDataWarnings().add(RESTFromBioCatalogueServiceDescription.AMBIGUOUS_CONTENT_TYPE_HEADER_VALUE);
      }
      restServiceDescription.setOutgoingContentType(restMethod.getInputs().getRepresentations().getRestRepresentationList().get(0).getContentType());
    }
    else if (RESTActivity.hasMessageBodyInputPort(httpMethod)) {
      restServiceDescription.getDataWarnings().add(RESTFromBioCatalogueServiceDescription.DEFAULT_CONTENT_TYPE_HEADER_VALUE);
    }
    
    return (restServiceDescription);
  }
  
  
  /**
   * @param restServiceDescription {@link RESTFromBioCatalogueServiceDescription} to process.
   * @param addingToServicePanel <code>true</code> indicates that the warning messages
   *                             will assume that the processor is added to the service panel;
   *                             <code>false</code> would mean that the processor is added to
   *                             the current workflow.
   * @return An HTML-formatted string (with no opening-closing HTML tags) that lists
   *         any warnings that have been recorded during the {@link RESTFromBioCatalogueServiceDescription}
   *         object creation. Empty string will be returned if there are no warnings.
   */
  public static String extractWarningsFromRESTServiceDescription(RESTFromBioCatalogueServiceDescription restServiceDescription,
      boolean addingToServicePanel)
  {
    String messageSuffix = addingToServicePanel ?
                           " once you add it into the workflow" :
                           "";
    
    String warnings = "";
    if (restServiceDescription.getDataWarnings().contains(RESTFromBioCatalogueServiceDescription.AMBIGUOUS_ACCEPT_HEADER_VALUE)) {
        warnings += "<br><br>Service Catalogue description of this REST method contains more than one<br>" +
                            "representation of the method's outputs - the first one was used.<br>" +
                            "Please check value of the 'Accept' header in the configuration<br>" +
                            "of the imported service" + messageSuffix + ".";
    }
    else if (restServiceDescription.getDataWarnings().contains(RESTFromBioCatalogueServiceDescription.DEFAULT_ACCEPT_HEADER_VALUE)) {
      warnings += "<br><br>Service Catalogue description of this REST method does not contain any<br>" +
                          "representations of the method's outputs - default value was used.<br>" +
                          "Please check value of the 'Accept' header in the configuration<br>" +
                          "of the imported service" + messageSuffix + ".";
    }
    
    if (restServiceDescription.getDataWarnings().contains(RESTFromBioCatalogueServiceDescription.AMBIGUOUS_CONTENT_TYPE_HEADER_VALUE)) {
        warnings += "<br><br>Service Catalogue description of this REST method contains more than one<br>" +
                            "representation of the method's input data - the first one was used.<br>" +
                            "Please check value of the 'Content-Type' header in the configuration<br>" +
                            "of the imported service" + messageSuffix + ".";
    }
    else if (restServiceDescription.getDataWarnings().contains(RESTFromBioCatalogueServiceDescription.DEFAULT_CONTENT_TYPE_HEADER_VALUE)) {
      warnings += "<br><br>Service Catalogue description of this REST method does not contain any<br>" +
                          "representations of the method's input data - default value was used.<br>" +
                          "Please check value of the 'Content-Type' header in the configuration<br>" +
                          "of the imported service" + messageSuffix + ".";
    }
    
    if (warnings.length() > 0) {
      warnings = "<br><br>WARNINGS:" + warnings;
    }
    
    return (warnings);
  }
  
  
  
  /**
   * @param activityPort Probably comes from contextual selection - must be either
   *         ActivityInputPort or ActivityOutputPort.
   * @return SOAP input / output port details (WSDL location, operation name, port name) from
   *         ActivityInputPort/ActivityOutputPort which is obtained from contextual selection in the Dataflow.
   */
  public static <T extends Port> SoapOperationPortIdentity extractSoapOperationPortDetailsFromActivityInputOutputPort(T activityPort)
  {
    // check that we have the correct instance of Port here - either ActivityInputPort or ActivityOutputPort
    boolean hasInputPort;
    if (activityPort instanceof ActivityInputPort) {
      hasInputPort = true;
    }
    else if (activityPort instanceof ActivityOutputPort) {
      hasInputPort = false;
    }
    else {
      // ERROR - wrong type supplied
      return new SoapOperationPortIdentity("Activity port from the contextual selection was not of correct type. Impossible to create preview.");
    }
    
    // get parent processor details
    Dataflow currentDataflow = FileManager.getInstance().getCurrentDataflow();
    Collection<Processor> processors = null;
    if (hasInputPort) {
      processors = Tools.getProcessorsWithActivityInputPort(currentDataflow, (ActivityInputPort)activityPort);
    }
    else {
      processors = Tools.getProcessorsWithActivityOutputPort(currentDataflow, (ActivityOutputPort)activityPort);
    }
    
    // TODO - doesn't take into account that it's possible to have several
    SoapOperationIdentity soapOperationDetails = extractSoapOperationDetailsFromProcessor(processors.toArray(new Processor[]{})[0]);
    
    // if no error happened, add port details and return
    if (!soapOperationDetails.hasError()) {
      return (new SoapOperationPortIdentity(soapOperationDetails.getWsdlLocation(),
                                             soapOperationDetails.getOperationName(),
                                             activityPort.getName(), hasInputPort));
    }
    else {
      // error...
      return (new SoapOperationPortIdentity(soapOperationDetails.getErrorDetails()));
    }
  }
  
  
  /**
   * Uses contextual selection to extract WSDL location and operation name of the
   * currently selected processor within the Design view of current workflow. 
   * 
   * @param contextualSelection Selection that was made in the Design view.
   * @return Details of the SOAP operation that acts as a processor wrapped into
   *         this single instance. If any problems occurred while performing
   *         contextual selection analysis, these are also recorded into the same
   *         instance - before using the returned value the caller must check
   *         <code>SoapOperationIdentity.hasError()</code> value.
   */
  public static SoapOperationIdentity extractSoapOperationDetailsFromProcessorContextualSelection(ContextualSelection contextualSelection)
  {
    if (!(contextualSelection.getSelection() instanceof Processor)) {
      return (new SoapOperationIdentity("ERROR: It is only possible to extract " +
      		"SOAP operation details from the service."));
    }
    
    // now we know it's a Processor
    Processor processor = (Processor)contextualSelection.getSelection();
    return (extractSoapOperationDetailsFromProcessor(processor));
  }
  
  
  /**
   * Worker method for <code>extractSoapOperationDetailsFromProcessorContextualSelection()</code>.
   * 
   * @param processor
   * @return
   */
  public static SoapOperationIdentity extractSoapOperationDetailsFromProcessor(Processor processor)
  {
    List<? extends Activity> activityList = (List<? extends Activity>) processor.getActivityList();
    
    if (activityList == null || activityList.size() == 0) {
      return (new SoapOperationIdentity("ERROR: Selected processor doesn't have any activities - " +
          "impossible to extract SOAP operation details."));
    }
    else {
      // take only the first activity - TODO: figure out what should be done here...
      Activity activity = activityList.get(0);
      if (activity instanceof WSDLActivity) {
        WSDLActivity a = (WSDLActivity)activity;
        return (new SoapOperationIdentity(a.getConfiguration().getWsdl(), a.getConfiguration().getOperation(), null));
      }
      else {
        return (new SoapOperationIdentity("Service Catalogue integration only works with WSDL Activities at the moment"));
      }
    }
  }
  
  
  /**
   * @param contextualSelection
   * @return A list of all WSDL activities (the only supported processors by BioCatalogue plugin for now).
   */
  public static List<SoapProcessorIdentity> extractSupportedProcessorsFromDataflow(ContextualSelection contextualSelection)
  {
    // check that there was a correct contextual selection
    if (!(contextualSelection.getSelection() instanceof Dataflow)) {
      logger.error("It is only possible to extract supported services from the workflow.");
      return (new ArrayList<SoapProcessorIdentity>());
    }
    
    // first extract all processors
    Dataflow dataflow = (Dataflow)contextualSelection.getSelection();
    List<? extends Processor> allProcessors = dataflow.getEntities(Processor.class);
    
    // now filter out any processors that are not WSDL activities
    List<SoapProcessorIdentity> supportedProcessors = new ArrayList<SoapProcessorIdentity>();
    for (Processor proc : allProcessors) {
      List<? extends Activity> activityList = (List<? extends Activity>) proc.getActivityList();
      if (activityList != null && activityList.size() > 0) {
        // take only the first activity - TODO: figure out what should be done here...
        Activity activity = activityList.get(0);
        if (activity instanceof WSDLActivity) {
          WSDLActivity a = (WSDLActivity)activity;
          supportedProcessors.add(new SoapProcessorIdentity(a.getConfiguration().getWsdl(),
                                                            a.getConfiguration().getOperation(),
                                                            proc.getLocalName()));
        }
      }
    }
    
    // return all found processors
    return (supportedProcessors);
  }
  
}
