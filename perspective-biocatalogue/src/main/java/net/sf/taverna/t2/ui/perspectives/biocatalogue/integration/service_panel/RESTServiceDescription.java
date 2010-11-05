package net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.service_panel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import net.sf.taverna.t2.lang.beans.PropertyAnnotation;
import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import net.sf.taverna.t2.activities.rest.RESTActivity;
import net.sf.taverna.t2.activities.rest.RESTActivity.DATA_FORMAT;
import net.sf.taverna.t2.activities.rest.RESTActivity.HTTP_METHOD;
import net.sf.taverna.t2.activities.rest.RESTActivityConfigurationBean;
import net.sf.taverna.t2.activities.rest.ui.servicedescription.RESTActivityIcon;

/**
 * This class is solely intended to support import of REST services from BioCatalogue.
 * 
 * @author Sergejs Aleksejevs
 */
public class RESTServiceDescription extends ServiceDescription<RESTActivityConfigurationBean>
{
  private static final int SHORT_DESCRIPTION_MAX_LENGTH = 200;
  
  private static final String FULL_DESCRIPTION = "Full description";
  
  public static final int AMBIGUOUS_ACCEPT_HEADER_VALUE = 100;
  public static final int DEFAULT_ACCEPT_HEADER_VALUE = 110;
  public static final int AMBIGUOUS_CONTENT_TYPE_HEADER_VALUE = 200;
  public static final int DEFAULT_CONTENT_TYPE_HEADER_VALUE = 210;
  
  
	private RESTActivityConfigurationBean serviceConfigBean;
	private String serviceName;
	private String description;
	
	private List<Integer> dataWarnings;
	
	
	/**
	 * Constructor instantiates config bean and pre-populates
	 * it with default values.
	 */
	public RESTServiceDescription()
	{
	  // apply default name in case it won't be set manually later
	  this.serviceName = "REST Service";
	  this.serviceConfigBean = RESTActivityConfigurationBean.getDefaultInstance();
	  this.dataWarnings = new ArrayList<Integer>();
	}
  
  /**
	 * The subclass of Activity which should be instantiated when adding a service
	 * for this description.
	 */
	@Override
	public Class<? extends Activity<RESTActivityConfigurationBean>> getActivityClass() {
		return RESTActivity.class;
	}

	/**
	 * The configuration bean which is to be used for configuring the instantiated activity.
	 * 
	 * Values are to be set through individual setters provided in this class.
	 */
	@Override
	public RESTActivityConfigurationBean getActivityConfiguration() {
		return serviceConfigBean;
	}

	/**
	 * An icon to represent this service type in the service palette.
	 */
	@Override
	public Icon getIcon() {
	  return RESTActivityIcon.getRESTActivityIcon();
	}

	/**
	 * The display name that will be shown in service palette and will
	 * be used as a template for processor name when added to workflow.
	 */
	@Override
	public String getName() {
		return serviceName;
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
	

	/**
	 * The path to this service description in the service palette. Folders
	 * will be created for each element of the returned path.
	 * 
	 * (Shouldn't really be ever used, as instances of different type are
	 *  added into the Service Panel).
	 */
	@Override
	public List<String> getPath() {
		// For deeper paths you may return several strings
		return Arrays.asList(BioCatalogueServiceProvider.PROVIDER_NAME, "REST Service @ " + serviceConfigBean.getUrlSignature());
	}

	/**
	 * Return a list of data values uniquely identifying this service
	 * description (to avoid duplicates). Include only primary key like fields,
	 * ie. ignore descriptions, icons, etc.
	 */
	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.<Object>asList(serviceConfigBean.getUrlSignature(), serviceConfigBean.getHttpMethod());
	}
	
	
	public List<Integer> getDataWarnings() {
	  return dataWarnings;
	}
	
	
	public void setURLSignature(String urlSignature) {
	  this.serviceConfigBean.setUrlSignature(urlSignature);
	}
	
	
	public void setHttpMethod(HTTP_METHOD httpMethod) {
    this.serviceConfigBean.setHttpMethod(httpMethod);
  }
	
	
	public void setAcceptHeaderValue(String acceptHeaderValue) {
    this.serviceConfigBean.setAcceptsHeaderValue(acceptHeaderValue);
  }
	
	
	public void setOutgoingContentType(String outgoingContentType) 
	{
    this.serviceConfigBean.setUrlSignature(outgoingContentType);
    
    // automatically infer data format - string/binary from the content type
    if (outgoingContentType.startsWith("text")) { this.serviceConfigBean.setOutgoingDataFormat(DATA_FORMAT.String); }
    else { this.serviceConfigBean.setOutgoingDataFormat(DATA_FORMAT.Binary); }
  }
	
	
	public void setServiceName(String name) {
	  this.serviceName = name;
	}
	
	
	public void setDescription(String description) {
	  this.description = description;
	}
	
}
