package net.sf.taverna.biocatalogue.model;

/**
 * Identifies a SOAP service in the most straightforward
 * way - by WSDL location. 
 * 
 * @author Sergejs Aleksejevs
 */
public class SoapServiceIdentity
{
  private final String wsdlLocation;
  
  // this variable holds an object that will be displayable
  private final Object errorDetails;

  public SoapServiceIdentity(String wsdlLocation) {
    this.wsdlLocation = wsdlLocation;
    this.errorDetails = null;
  }
  
  public SoapServiceIdentity(Object errorDetails) {
    this.errorDetails = errorDetails;
    this.wsdlLocation = null;
  }
  
  public String getWsdlLocation() {
    return (wsdlLocation);
  }
  
  public boolean hasError() {
    return (errorDetails != null);
  }
  
  /**
   * @return Returned object contains an object that may be displayed
   *         in a JOptionPane or printed (in other words defining a
   *         sensible way of displaying itself), which has details of
   *         an error that has occurred which prevented from populating
   *         this instance with the actual details of ther SOAP service.
   */
  public Object getErrorDetails() {
    return (errorDetails);
  }
  
}
