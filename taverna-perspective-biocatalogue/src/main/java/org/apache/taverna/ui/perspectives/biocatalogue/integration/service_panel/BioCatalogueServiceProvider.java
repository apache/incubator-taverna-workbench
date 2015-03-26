package org.apache.taverna.ui.perspectives.biocatalogue.integration.service_panel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;
import javax.wsdl.Operation;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.apache.taverna.biocatalogue.model.SoapOperationIdentity;
import org.apache.taverna.biocatalogue.model.Util;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityHealthChecker;
import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.apache.taverna.ui.perspectives.biocatalogue.BioCataloguePerspective;
import org.apache.taverna.ui.perspectives.biocatalogue.integration.config.BioCataloguePluginConfiguration;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

public class BioCatalogueServiceProvider implements ServiceDescriptionProvider
{
  public static final String PROVIDER_NAME = "Service Catalogue - selected services";
  
  private static BioCatalogueServiceProvider instanceOfSelf = null;
  private static FindServiceDescriptionsCallBack callBack;
  
  private static List<SoapOperationIdentity> registeredSOAPOperations;
  private static List<RESTFromBioCatalogueServiceDescription> registeredRESTMethods;
  
  private static Logger logger = Logger.getLogger(BioCatalogueServiceProvider.class);
  
  
	public BioCatalogueServiceProvider() {
	  BioCatalogueServiceProvider.instanceOfSelf = this;
	}
	
	@SuppressWarnings("unchecked")
  public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack)
	{
		BioCatalogueServiceProvider.callBack = callBack;
    callBack.status("Starting Service Catalogue Service Provider");
		
    // --- Initilise the service provider with stored services ---
    
    // read stored settings
    // NB! it's crucial to set the custom classloader, otherwise XStream would fail,
    //     as it would attempt to use the default one, which wouldn't know about the
    //     plugin's classes
    logger.info("Starting to deserialise the list of services stored in the configuration file");
    XStream xstream = new XStream(new DomDriver());
    xstream.setClassLoader(BioCataloguePerspective.class.getClassLoader());
    
    BioCataloguePluginConfiguration configuration = BioCataloguePluginConfiguration.getInstance();
    
    // *** load stored SOAP operations ***
    String loadedSOAPServicesXMLString = configuration.getProperty(BioCataloguePluginConfiguration.SOAP_OPERATIONS_IN_SERVICE_PANEL);
    
    Object loadedSOAPServices = (loadedSOAPServicesXMLString == null ?
                                 null :
                                 xstream.fromXML(loadedSOAPServicesXMLString));
    
    registeredSOAPOperations = (loadedSOAPServices == null || !(loadedSOAPServices instanceof List<?>) ?
                                new ArrayList<SoapOperationIdentity>() :
                                (List<SoapOperationIdentity>)loadedSOAPServices
                               );
    logger.info("Deserialised " + registeredSOAPOperations.size() + Util.pluraliseNoun("SOAP operation", registeredSOAPOperations.size()));
    
    // prepare the correct format of data for initialisation
    List<ServiceDescription> results = new ArrayList<ServiceDescription>();
    for (SoapOperationIdentity opId : registeredSOAPOperations) {
      results.add(new WSDLOperationFromBioCatalogueServiceDescription(opId));
    }
    
    
    // *** load stored REST methods ***
    String loadedRESTMethodsXMLString = configuration.getProperty(BioCataloguePluginConfiguration.REST_METHODS_IN_SERVICE_PANEL);
    
    Object loadedRESTMethods = (loadedRESTMethodsXMLString == null ?
                                null :
                                xstream.fromXML(loadedRESTMethodsXMLString));
    
    registeredRESTMethods = (loadedRESTMethods == null || !(loadedRESTMethods instanceof List<?>) ?
                             new ArrayList<RESTFromBioCatalogueServiceDescription>() :
                             (List<RESTFromBioCatalogueServiceDescription>)loadedRESTMethods);
    logger.info("Deserialised " + registeredRESTMethods.size() + Util.pluraliseNoun("REST method", registeredRESTMethods.size()));
    
    results.addAll(registeredRESTMethods);
		
    
		// *** send the services to the Service Panel ***
		callBack.partialResults(results);
		
		
		// NB! This is to be called when it is known that no more items will be added - 
		// it's never true for this provider, as items may be added on user request
		// at any time!
		//
		// callBack.finished();
	}
	
	public Icon getIcon() {
		return null;
	}
	
	public String getName(){
	  // TODO - not sure where this is used
		return "My dummy service";
	}
	
	public String getId() {
    return "http://www.taverna.org.uk/2010/services/servicecatalogue";
  }
	
	
	/**
	 * Adds a new "processor" - i.e. a WSDL operation into the main Service Panel.
	 * 
	 * @param wsdlLocation URL of the WSDL location of the operation to add.
	 * @param operationName Name of the operation within specified WSDL document.
	 * @return True if the operation was added;
	 *         false if the service provided was not yet initiliased (unlikely) or
	 *         when supplied strings were empty/null.
	 */
	public static boolean registerNewWSDLOperation(SoapOperationIdentity soapOperationDetails)
	{
	  if (BioCatalogueServiceProvider.instanceOfSelf == null || soapOperationDetails == null ||
	      soapOperationDetails.getWsdlLocation() == null || soapOperationDetails.getWsdlLocation().length() == 0 ||
	      soapOperationDetails.getOperationName() == null || soapOperationDetails.getOperationName().length() == 0)
	  {
	    // the service provider hasn't been initialised yet
	    // OR not all details available
	    return (false);
	  }
	  else
	  {
	    // record the newly added operation in the internal list
	    registeredSOAPOperations.add(soapOperationDetails);
	    
	    // add the provided operation to the Service Panel
	    ServiceDescription service = new WSDLOperationFromBioCatalogueServiceDescription(soapOperationDetails);
	    BioCatalogueServiceProvider.callBack.partialResults(Collections.singletonList(service));
	    return (true);
	  }
	    
	}
	
	/**
	 * Adds a SOAP/WSDL service and all of its operations into the Taverna's Service Panel.
	 */
	public static boolean registerNewWSDLService(String wsdlURL)
	{
	  if (BioCatalogueServiceProvider.instanceOfSelf == null || wsdlURL == null)
	  {
	    // the service provider hasn't been initialised yet
	    // OR not all details available
	    return (false);
	  }
	  else
	  {
		  // Do the same thing as in the WSDL service provider
			callBack.status("Service Catalogue service provider: Parsing wsdl: " + wsdlURL);
			WSDLParser parser = null;
			try {
				parser = new WSDLParser(wsdlURL);
				List<Operation> operations = parser.getOperations();
				callBack.status("Found " + operations.size() + " WSDL operations of service "
						+ wsdlURL);
				List<WSDLOperationFromBioCatalogueServiceDescription> items = new ArrayList<WSDLOperationFromBioCatalogueServiceDescription>();
				for (Operation operation : operations) {
					WSDLOperationFromBioCatalogueServiceDescription item;
					try {
						String operationName = operation.getName();
						String operationDesc = parser.getOperationDocumentation(operationName);
						String use = parser.getUse(operationName);
						String style = parser.getStyle();
						if (!WSDLActivityHealthChecker.checkStyleAndUse(style, use)) {
							logger.warn("Unsupported style and use combination " + style + "/" + use + " for operation " + operationName + " from " + wsdlURL);
							continue;
						}
						item = new WSDLOperationFromBioCatalogueServiceDescription(wsdlURL, operationName, operationDesc);
						items.add(item);
						
					    // Record the newly added operation in the internal list
						SoapOperationIdentity soapOperationDetails = new SoapOperationIdentity(wsdlURL, operationName, operationDesc);
					    registeredSOAPOperations.add(soapOperationDetails);
					} catch (UnknownOperationException e) {
						String message = "Encountered an unexpected operation name:"
								+ operation.getName();
						callBack.fail(message, e);
					    return false;
					}
				}
				callBack.partialResults(items);
				callBack.finished();
				return true;
			} catch (ParserConfigurationException e) {
				String message = "Error configuring the WSDL parser";
				callBack.fail(message, e);
			    return false;
			} catch (WSDLException e) {
				String message = "There was an error with the wsdl: " + wsdlURL;
				callBack.fail(message, e);
			    return false;
			} catch (IOException e) {
				String message = "There was an IO error parsing the wsdl: " + wsdlURL
						+ " Possible reason: the wsdl location was incorrect.";
				callBack.fail(message, e);
			    return false;
			} catch (SAXException e) {
				String message = "There was an error with the XML in the wsdl: "
						+ wsdlURL;
				callBack.fail(message, e);
			    return false;
			} catch (IllegalArgumentException e) { // a problem with the wsdl url
				String message = "There was an error with the wsdl: " + wsdlURL + " "
						+ "Possible reason: the wsdl location was incorrect.";
				callBack.fail(message, e);
			    return false;
			} catch (Exception e) { // anything else we did not expect
				String message = "There was an error with the wsdl: " + wsdlURL;
				callBack.fail(message, e);
			    return false;
			}
		}
	    
	}
	
	
	public static boolean registerNewRESTMethod(RESTFromBioCatalogueServiceDescription restServiceDescription)
	{
	  if (restServiceDescription == null) {
	    return (false);
	  }
	  else
	  {
	    // record the newly added method in the internal list
	    registeredRESTMethods.add(restServiceDescription);
	    
	    // add the provided method to the Service Panel
	    BioCatalogueServiceProvider.callBack.partialResults(Collections.singletonList(restServiceDescription));
	    return (true);
	  }
	}
	
	
	public static List<SoapOperationIdentity> getRegisteredSOAPOperations() {
	  return (registeredSOAPOperations);
	}
	
	public static List<RESTFromBioCatalogueServiceDescription> getRegisteredRESTMethods() {
    return (registeredRESTMethods);
  }
  
	
	/**
	 * Clears internal lists of stored SOAP operations / REST methods.
	 * Therefore, once Taverna is restarted, the stored services will
	 * be effectively "forgotten".
	 */
	public static void clearRegisteredServices() {
	  registeredRESTMethods.clear();
	  registeredSOAPOperations.clear();
	}
	
}
