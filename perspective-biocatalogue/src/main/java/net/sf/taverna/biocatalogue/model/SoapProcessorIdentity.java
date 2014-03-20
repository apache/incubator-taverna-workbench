package net.sf.taverna.biocatalogue.model;

/**
 * Identifies a SOAP Processor in Taverna terms. Adds a local name
 * attribute to the details available in <code>SoapOperationIdentity</code>.
 * 
 * @author Sergejs Aleksejevs
 */
public class SoapProcessorIdentity extends SoapOperationIdentity
{
  private final String localName;

  public SoapProcessorIdentity(String wsdlLocation, String operationName, String localName) {
    super(wsdlLocation, operationName, null);
    this.localName = localName;
  }
  
  public SoapProcessorIdentity(Object errorDetails) {
    super(errorDetails);
    this.localName = null;
  }
  
  public String getLocalName() {
    return localName;
  }
  
}
