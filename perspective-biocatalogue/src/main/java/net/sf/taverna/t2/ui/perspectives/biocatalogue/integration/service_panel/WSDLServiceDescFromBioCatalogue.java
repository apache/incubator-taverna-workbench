package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.service_panel;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.activities.wsdl.servicedescriptions.WSDLActivityIcon;
import net.sf.taverna.t2.lang.beans.PropertyAnnotation;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;

public class WSDLServiceDescFromBioCatalogue extends ServiceDescription
{
  private static final int SHORT_DESCRIPTION_MAX_LENGTH = 200;
  
  private static final String FULL_DESCRIPTION = "Full description";
  
  
  private final String wsdlLocation;
  private final String operationName;
  private final String description;
  
  
  public WSDLServiceDescFromBioCatalogue(String wsdlLocation, String operationName, String description)
  {
    this.wsdlLocation = wsdlLocation;
    this.operationName = operationName;
    this.description = description;
  }
  
  public WSDLServiceDescFromBioCatalogue(SoapOperationIdentity soapOpearationIdentity)
  {
    this.wsdlLocation = soapOpearationIdentity.getWsdlLocation();
    this.operationName = soapOpearationIdentity.getOperationName();
    this.description = soapOpearationIdentity.getDescription();
  }
  
  
	@Override
	public Class getActivityClass() {
		return WSDLActivity.class;
	}

	@Override
	public Object getActivityConfiguration() {
		WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
		bean.setOperation(operationName);
		bean.setWsdl(wsdlLocation);
		return bean;
	}

	@Override
	public Icon getIcon() {
		return WSDLActivityIcon.getWSDLIcon();
	}

	@Override
	public String getName() {
		return (this.operationName);
	}

	/**
	 * Truncates the description if necessary to {@link WSDLServiceDescFromBioCatalogue#SHORT_DESCRIPTION_MAX_LENGTH} --
	 * to get full description, use {@link WSDLServiceDescFromBioCatalogue#getFullDescription()}
	 */
	public String getDescription() {
    if (this.description != null && this.description.length() > SHORT_DESCRIPTION_MAX_LENGTH) {
      return (this.description.substring(0, SHORT_DESCRIPTION_MAX_LENGTH) + "(...)");
    }
    else {
      return this.description;
    }
  }
	
	@PropertyAnnotation(displayName = FULL_DESCRIPTION)
	public String getFullDescription() {
	  return this.description;
	}
	
	@Override
	public List<String> getPath() {
		return Arrays.asList(BioCatalogueServiceProvider.PROVIDER_NAME, "WSDL @ " + this.wsdlLocation);
	}
	
	@Override
	protected List<? extends Object> getIdentifyingData()
	{
	  // This is important - Taverna won't add identical operations
	  // into the Service Panel. These tokens distinguish added items.
		return Arrays.asList(wsdlLocation, operationName);
	}

}
