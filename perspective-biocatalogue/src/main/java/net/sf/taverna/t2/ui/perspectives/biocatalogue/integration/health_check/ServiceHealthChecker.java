package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.health_check;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.biocatalogue.x2009.xml.rest.ResourceLink;
import org.biocatalogue.x2009.xml.rest.RestMethod;
import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.ServiceTest;
import org.biocatalogue.x2009.xml.rest.SoapOperation;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.Resource;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.biocatalogue.model.SoapProcessorIdentity;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.ui.JClickableLabel;
import net.sf.taverna.biocatalogue.ui.JWaitDialog;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;


/**
 * This class helps with "health checks" of individual Taverna processors
 * (i.e. SOAP operations) and the workflows in general (by iterating through
 * the processors).
 * 
 * @author Sergejs Aleksejevs
 */
public class ServiceHealthChecker
{
    private static Logger logger = Logger.getLogger(ServiceHealthChecker.class);

    // deny creation of instances of this class
  private ServiceHealthChecker() { };
  
  
  // =====================================================================================================
  //                      *** Health Check of Individual Service / Processor ***
  // =====================================================================================================
  
  /**
   * @param serviceURL URL of SOAP service or REST service on BioCatalogue;
   *                   URL should be of the 
   */
  public static void checkServiceByURL(String serviceURL)
  {
    if (serviceURL != null) {
      checkMonitoringStatusRoutine(serviceURL);
    }
    else {
      // for some reason the URL of the service wasn't provided...
      JOptionPane.showMessageDialog(null, "Cannot provide monitoring status for this service - " +
      		                          "unknown service URL", "BioCatalogue Plugin - Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  
  /**
   * @param  
   */
  public static void checkResource(ResourceLink serviceOrOperationOrMethod)
  {
    if (serviceOrOperationOrMethod != null) {
      checkMonitoringStatusRoutine(serviceOrOperationOrMethod);
    }
    else {
      // for some reason resource object wasn't provided...
      JOptionPane.showMessageDialog(null, "Cannot provide monitoring status - " +
                                    "null reference received", "BioCatalogue Plugin - Error", JOptionPane.ERROR_MESSAGE);
    }
  }
  
  
  /**
   * Used when invoked from the workflow diagram - e.g. when a URL of the specific
   * resource on BioCatalogue is not known, but have enough of identifying data
   * to proceed with health check.
   * 
   * @param soapOperationDetails
   */
  public static void checkWSDLProcessor(SoapOperationIdentity soapOperationDetails)
  {
    if (!soapOperationDetails.hasError()) {
      checkMonitoringStatusRoutine(soapOperationDetails);
    }
    else {
      // this error message comes from Integration class extracting SOAP operation details from the contextual selection
      JOptionPane.showMessageDialog(null, soapOperationDetails.getErrorDetails(), "BioCatalogue Plugin - Error", JOptionPane.WARNING_MESSAGE);
    }
  }
  
  
  /**
   * @param serviceOrSoapOperationToCheck Instance of SoapOperationIdentity representing Taverna processor
   *                                      or String representing a URL of the service to check health for.
   */
  private static void checkMonitoringStatusRoutine(final Object serviceOrSoapOperationToCheck)
  {
    // helper variable to determine the kind of check to perform - the difference is minimal:
    // wording in the status messages ("Web Service" | "processor" | "REST Service") and which method to call on
    // the BioCatalogue client to fetch monitoring data
    final boolean bCheckingService = (serviceOrSoapOperationToCheck instanceof String);
    final boolean bCheckingWSDLProcessor = (serviceOrSoapOperationToCheck instanceof SoapOperationIdentity);
    final boolean bCheckingResource = (serviceOrSoapOperationToCheck instanceof ResourceLink);
    
    final StringBuilder itemToCheck = new StringBuilder();
    if (bCheckingService) {
      itemToCheck.append("service");
    }
    else if (bCheckingWSDLProcessor) {
      itemToCheck.append("WSDL service"); 
    }
    else if (bCheckingResource) {
      TYPE resourceType = Resource.getResourceTypeFromResourceURL(((ResourceLink)serviceOrSoapOperationToCheck).getHref());
      itemToCheck.append(resourceType.getTypeName());
    }
    
    // create the wait dialog, but don't make it visible - first need to start the background processing thread
    final JWaitDialog jwd = new JWaitDialog(MainComponent.dummyOwnerJFrame, "Checking "+itemToCheck+" status",
    "Please wait while status of selected "+itemToCheck+" is being checked...");

    new Thread(itemToCheck + " lookup and health check operation") {
      public void run() {
        try
        {
          BioCatalogueClient client = BioCatalogueClient.getInstance();
          Service serviceMonitoringData = null;
          
          // attempt to get monitoring data from BioCatalogue - for this need to identify what type of
          // item was provided as a parameter
          if (bCheckingService) {
            serviceMonitoringData = client.getBioCatalogueServiceMonitoringData((String)serviceOrSoapOperationToCheck);
          }
          else if (bCheckingWSDLProcessor) {
            serviceMonitoringData = client.lookupParentServiceMonitoringData((SoapOperationIdentity)serviceOrSoapOperationToCheck); 
          }
          else if (bCheckingResource) {
            String resourceURL = ((ResourceLink)serviceOrSoapOperationToCheck).getHref();
            TYPE resourceType = Resource.getResourceTypeFromResourceURL(resourceURL);
            
//            if (resourceType == TYPE.Service) {
//              serviceMonitoringData = client.getBioCatalogueServiceMonitoringData(resourceURL);
//            }
//            else
            	if (resourceType == TYPE.SOAPOperation) {
              String parentServiceURL = ((SoapOperation)serviceOrSoapOperationToCheck).getAncestors().getService().getHref();
              serviceMonitoringData = client.getBioCatalogueServiceMonitoringData(parentServiceURL);
            }
            else if (resourceType == TYPE.RESTMethod) {
              String parentServiceURL = ((RestMethod)serviceOrSoapOperationToCheck).getAncestors().getService().getHref();
              serviceMonitoringData = client.getBioCatalogueServiceMonitoringData(parentServiceURL);
            }
            else {
              JOptionPane.showMessageDialog(jwd, "Unexpected resource type - can't execute health check for this",
                  "BioCatalogue Plugin - Error", JOptionPane.ERROR_MESSAGE);
              	logger.error("Biocatalogue Plugin: Could not perform health check for" + resourceType);
            }
          }
          
          
          // need to make this assignment to make the variable final - otherwise unavailable inside the new thread...
          final Service serviceWithMonitoringData = serviceMonitoringData;
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if (serviceWithMonitoringData == null) {
                jwd.setTitle("BioCatalogue Plugin - Information");
                jwd.waitFinished(new JLabel("There is no information about this "+itemToCheck+" in BioCatalogue",
                    UIManager.getIcon("OptionPane.informationIcon"), JLabel.CENTER));
              }
              else if (serviceWithMonitoringData.getLatestMonitoringStatus() == null) {
                jwd.setTitle("BioCatalogue Plugin - Warning");
                jwd.waitFinished(new JLabel("This "+itemToCheck+" is known to BioCatalogue, but no monitoring data was available.",
                    UIManager.getIcon("OptionPane.warningIcon"), JLabel.CENTER));
              }
              else
              {
                // set the overall status message
                String overallStatusLabel = "<html><b>Overall status:</b><br>" +
                         serviceWithMonitoringData.getLatestMonitoringStatus().getMessage() + "<br>" +
                         "(last checked";
                if (serviceWithMonitoringData.getLatestMonitoringStatus().getLastChecked() == null) {
                  overallStatusLabel += ": never";
                }
                else {
                  overallStatusLabel += " at " + BioCatalogueClient.getShortDateFormatter().format(
                      serviceWithMonitoringData.getLatestMonitoringStatus().getLastChecked().getTime());
                }
                overallStatusLabel += ")</html>";
                JLabel jlOverallStatus = new JLabel(overallStatusLabel);
                
                // create panel for additional status messages (e.g. endpoint, wsdl location, etc)
                JPanel jpStatusMessages = new JPanel();
                jpStatusMessages.setLayout(new BoxLayout(jpStatusMessages, BoxLayout.Y_AXIS));
                
                for (ServiceTest test : serviceWithMonitoringData.getMonitoring().getTests().getServiceTestList())
                {
                  // First get the service type
                  String testLabel = "<html><br><b>";
                  if (test.getTestType().getUrlMonitor() != null)
                  {
                    if (test.getTestType().getUrlMonitor().getUrl().endsWith("wsdl")) {
                      // WSDL location test
                      testLabel += "WSDL Location Availability:</b><br>" +
                                   "URL: " + test.getTestType().getUrlMonitor().getUrl();
                    }
                    else {
                      // Endpoint availability test
                      testLabel += "Endpoint Availability:</b><br>" +
                                   "URL: " + test.getTestType().getUrlMonitor().getUrl();
                    }
                  }
                  else if (test.getTestType().getTestScript() != null) {
                    // test script
                    testLabel += "Test Script: " + test.getTestType().getTestScript().getName() + "</b>";
                  }
                  else {
                    testLabel += "Unknown test type</b>";
                  }
                  testLabel += "<br>";
                  
                  // Add service results
                  testLabel += test.getLatestStatus().getMessage() + "</html>";
                  
                  // Add the current test into the test messages panel
                  jpStatusMessages.add(new JLabel(testLabel));
                }
                
                // either way add the overall status on top of everything
                jpStatusMessages.add(jlOverallStatus, 0);
                jpStatusMessages.setBorder(new EmptyBorder(10,10,10,10));
                JScrollPane jspStatusMessages = new JScrollPane(jpStatusMessages);
                jspStatusMessages.setBorder(BorderFactory.createEmptyBorder());
                
                // *** Put everything together ***
                JPanel jpHealthCheckStatus = new JPanel(new BorderLayout(15, 10));
                jpHealthCheckStatus.add(new JLabel(ServiceMonitoringStatusInterpreter.getStatusIcon(serviceWithMonitoringData, false)),
                             BorderLayout.WEST);
                jpHealthCheckStatus.add(jspStatusMessages, BorderLayout.CENTER);
                
                jwd.setTitle("BioCatalogue Plugin - Monitoring Status");
                jwd.waitFinished(jpHealthCheckStatus);
              }
            }
          });
        }
        catch (Exception e) {
          logger.error("Biocatalogue Plugin: Error occurred while checking status of selected", e);
          jwd.setTitle("BioCatalogue Plugin - Error");
          jwd.waitFinished(new JLabel("<html>An unexpected error occurred while checking status of selected " +
                                      itemToCheck + "<br>Please see error log for details...",
                                      UIManager.getIcon("OptionPane.errorIcon"), JLabel.CENTER));
        }
      }
    }.start();
    
    jwd.setVisible(true);
  }
  
}
