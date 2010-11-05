package net.sf.taverna.biocatalogue.model;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;

import net.sf.taverna.biocatalogue.model.connectivity.BeansForJSONLiteAPI;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.ui.search_results.RESTMethodListCellRenderer;
import net.sf.taverna.biocatalogue.ui.search_results.SOAPOperationListCellRenderer;
import net.sf.taverna.biocatalogue.ui.search_results.ServiceListCellRenderer;
import net.sf.taverna.t2.workbench.MainWindow;

import org.apache.log4j.Logger;
import org.biocatalogue.x2009.xml.rest.Registry;
import org.biocatalogue.x2009.xml.rest.ResourceLink;
import org.biocatalogue.x2009.xml.rest.RestMethod;
import org.biocatalogue.x2009.xml.rest.RestMethods;
import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.ServiceProvider;
import org.biocatalogue.x2009.xml.rest.Services;
import org.biocatalogue.x2009.xml.rest.SoapOperation;
import org.biocatalogue.x2009.xml.rest.SoapOperations;
import org.biocatalogue.x2009.xml.rest.User;

/**
 * @author Sergejs Aleksejevs
 */
public class Resource
{
  /**
   * A single point of definition of the types of resources that the BioCatalogue plugin
   * "knows" about. This enum provides various details about resource types -
   * display names for single items of that type, names of collections of items of that
   * type, icons to represent the items of a particular type, etc.
   * 
   * @author Sergejs Aleksejevs
   */
  public static enum TYPE
  {
    // the order is important - all these types will appear in the user interface
    // in the same order as listed here
    SOAPOperation (SoapOperation.class, SoapOperations.class, BeansForJSONLiteAPI.SOAPOperationsIndex.class, "WSDL Service", "WSDL Services",
                   "WSDL services can be directly imported into the current workflow or Service Panel",
                   ResourceManager.getIconFromTaverna(ResourceManager.SOAP_OPERATION_ICON), true, true, true, false, true, true, true,
                   SOAPOperationListCellRenderer.class, BioCatalogueClient.API_SOAP_OPERATIONS_URL,
                   new HashMap<String,String>() {{
                   }},
                   new HashMap<String,String>(BioCatalogueClient.API_INCLUDE_ANCESTORS) {{
                     put(BioCatalogueClient.API_PER_PAGE_PARAMETER, ""+BioCataloguePluginConstants.API_DEFAULT_REQUESTED_SOAP_OPERATION_COUNT_PER_PAGE);
                   }},
                   BioCataloguePluginConstants.API_DEFAULT_REQUESTED_SOAP_OPERATION_COUNT_PER_PAGE,
                   BioCatalogueClient.API_SOAP_OPERATION_FILTERS_URL),
                   
    RESTMethod    (RestMethod.class, RestMethods.class, BeansForJSONLiteAPI.RESTMethodsIndex.class, "REST Service", "REST Services",
                   "REST services can be directly imported into the current workflow or Service Panel",
                   ResourceManager.getIconFromTaverna(ResourceManager.REST_METHOD_ICON), true, true, true, false, true, true, true,
                   RESTMethodListCellRenderer.class, BioCatalogueClient.API_REST_METHODS_URL,
                   new HashMap<String,String>() {{
                   }},
                   new HashMap<String,String>(BioCatalogueClient.API_INCLUDE_ANCESTORS) {{
                     put(BioCatalogueClient.API_PER_PAGE_PARAMETER, ""+BioCataloguePluginConstants.API_DEFAULT_REQUESTED_REST_METHOD_COUNT_PER_PAGE);
                   }},
                   BioCataloguePluginConstants.API_DEFAULT_REQUESTED_REST_METHOD_COUNT_PER_PAGE,
                   BioCatalogueClient.API_REST_METHOD_FILTERS_URL),
                   
    Service       (Service.class, Services.class, BeansForJSONLiteAPI.ServicesIndex.class, "Web Service", "Web Services",
                   "<html>Web services represent collections of WSDL services or REST services.<br>" +
                         "They cannot be directly imported into the current workflow or Service Panel,<br>" +
                         "but they may contain much more information about individual WSDL or REST<br>" +
                         "services and also provide some context for their usage.</html>",
                   ResourceManager.getImageIcon(ResourceManager.SERVICE_ICON), true, true, true, false, false, false, true,
                   ServiceListCellRenderer.class, BioCatalogueClient.API_SERVICES_URL, 
                   new HashMap<String,String>(BioCatalogueClient.API_INCLUDE_SUMMARY) {{
                   }},
                   new HashMap<String,String>() {{
                     put(BioCatalogueClient.API_PER_PAGE_PARAMETER, ""+BioCataloguePluginConstants.API_DEFAULT_REQUESTED_WEB_SERVICE_COUNT_PER_PAGE);
                   }},
                   BioCataloguePluginConstants.API_DEFAULT_REQUESTED_WEB_SERVICE_COUNT_PER_PAGE,
                   BioCatalogueClient.API_SERVICE_FILTERS_URL); //,
                   
    // TODO - the following two resource types have been disabled, as no actions for them can be done yet
    //        -- they are still to be implemented; if the following types are uncommented, they will be
    //        automatically searchable and visible in BioCatalogue Exploration tab; ListCellRenderers, however,
    //        would need to be added first.
//    ServiceProvider (ServiceProvider.class, ServiceProviders.class, BeansForJSONLiteAPI.ServiceProvidersIndex.class, "Service Provider", "Service Providers", "",
//                     ResourceManager.getImageIcon(ResourceManager.SERVICE_PROVIDER_ICON), false, false, false, false, false, false, false,
//                     ServiceProviderListCellRenderer.class, BioCatalogueClient.API_SERVICE_PROVIDERS_URL,
//                     new HashMap<String,String>() {{
//                     }},
//                     new HashMap<String,String>() {{
//                       put(BioCatalogueClient.API_PER_PAGE_PARAMETER, ""+BioCataloguePluginConstants.API_DEFAULT_REQUESTED_SERVICE_PROVIDER_COUNT_PER_PAGE);
//                     }},
//                     BioCataloguePluginConstants.API_DEFAULT_REQUESTED_SERVICE_PROVIDER_COUNT_PER_PAGE,
//                     null),
//                     
//    User          (User.class, Users.class, BeansForJSONLiteAPI.UsersIndex.class, "User", "Users", "",
//                   ResourceManager.getImageIcon(ResourceManager.USER_ICON), false, false, true, false, false, false, false,
//                   UserListCellRenderer.class, BioCatalogueClient.API_USERS_URL,
//                   new HashMap<String,String>() {{
//                   }},
//                   new HashMap<String,String>() {{
//                     put(BioCatalogueClient.API_PER_PAGE_PARAMETER, ""+BioCataloguePluginConstants.API_DEFAULT_REQUESTED_USER_COUNT_PER_PAGE);
//                   }},
//                   BioCataloguePluginConstants.API_DEFAULT_REQUESTED_USER_COUNT_PER_PAGE,
//                   BioCatalogueClient.API_USER_FILTERS_URL);
    
    
    private Class xmlbeansGeneratedClass;
    private Class xmlbeansGeneratedCollectionClass;
    private Class<?> jsonLiteAPIBindingBeanClass;
    private String resourceTypeName;
    private String resourceCollectionName;
    private String resourceTabTooltip;
    private Icon icon;
    private boolean defaultType;
    private boolean suitableForTagSearch;
    private boolean suitableForFiltering;
    private boolean suitableForOpeningInPreviewBrowser;
    private boolean suitableForAddingToServicePanel;
    private boolean suitableForAddingToWorkflowDiagram;
    private boolean suitableForHealthCheck;
    private Class<? extends ListCellRenderer> resultListingCellRendererClass;
    private String apiResourceCollectionIndex;
    private Map<String,String> apiResourceCollectionIndexSingleExpandedResourceAdditionalParameters;
    private Map<String,String> apiResourceCollectionIndexAdditionalParameters;
    private int apiResourceCountPerIndexPage;
    private String apiResourceCollectionFilters;
    
    TYPE(Class xmlbeansGeneratedClass, Class xmlbeansGeneratedCollectionClass, Class<?> jsonLiteAPIBindingBeanClass,
        String resourceTypeName, String resourceCollectionName, String resourceTabTooltip, Icon icon,
        boolean defaultType, boolean suitableForTagSearch, boolean suitableForFiltering, boolean suitableForOpeningInPreviewBrowser,
        boolean suitableForAddingToServicePanel, boolean suitableForAddingToWorkflowDiagram,
        boolean suitableForHealthCheck, Class<? extends ListCellRenderer> resultListingCellRendererClass,
        String apiResourceCollectionIndex, Map<String,String> apiResourceCollectionIndexSingleExpandedResourceAdditionalParameters,
        Map<String,String> apiResourceCollectionIndexAdditionalParameters, int apiResourceCountPerIndexListingPage,
        String apiResourceCollectionFilters)
    {
      this.xmlbeansGeneratedClass = xmlbeansGeneratedClass;
      this.xmlbeansGeneratedCollectionClass = xmlbeansGeneratedCollectionClass;
      this.jsonLiteAPIBindingBeanClass = jsonLiteAPIBindingBeanClass;
      this.resourceTypeName = resourceTypeName;
      this.resourceCollectionName = resourceCollectionName;
      this.resourceTabTooltip = resourceTabTooltip;
      this.icon = icon;
      this.defaultType = defaultType;
      this.suitableForTagSearch = suitableForTagSearch;
      this.suitableForFiltering = suitableForFiltering;
      this.suitableForOpeningInPreviewBrowser = suitableForOpeningInPreviewBrowser;
      this.suitableForAddingToServicePanel = suitableForAddingToServicePanel;
      this.suitableForAddingToWorkflowDiagram = suitableForAddingToWorkflowDiagram;
      this.suitableForHealthCheck = suitableForHealthCheck;
      this.resultListingCellRendererClass = resultListingCellRendererClass;
      this.apiResourceCollectionIndex = apiResourceCollectionIndex;
      this.apiResourceCollectionIndexSingleExpandedResourceAdditionalParameters = apiResourceCollectionIndexSingleExpandedResourceAdditionalParameters;
      this.apiResourceCollectionIndexAdditionalParameters = apiResourceCollectionIndexAdditionalParameters;
      this.apiResourceCountPerIndexPage = apiResourceCountPerIndexListingPage;
      this.apiResourceCollectionFilters = apiResourceCollectionFilters;
    }
    
    
    
    public Class getXmlBeansGeneratedClass() {
      return this.xmlbeansGeneratedClass;
    }
    
    /**
     * @return Class that represents collection of resources of this type,
     *         as represented by XmlBeans.
     */
    public Class getXmlBeansGeneratedCollectionClass() {
      return this.xmlbeansGeneratedCollectionClass;
    }
    
    
    /**
     * @return Class of the bean to be used when de-serialising JSON
     *         data received from the 'Lite' BioCatalogue JSON API's index
     *         of resources of this type.  
     */
    public Class<?> getJsonLiteAPIBindingBeanClass() {
      return this.jsonLiteAPIBindingBeanClass;
    }
    
    
    /**
     * @return Display name of a type of a single item belonging to that type.
     *         (E.g. 'User' or 'Service') 
     */
    public String getTypeName() {
      return this.resourceTypeName;
    }
    
    /**
     * @return Display name of a collection of items of this type.
     *         (E.g. 'Users' or 'Services').
     */
    public String getCollectionName() {
      return this.resourceCollectionName;
    }
    
    /**
     * @return HTML-formatted string that can be used as a tooltip
     *         for tabs in BioCatalogue Exploration tab of BioCatalogue
     *         perspective.
     */
    public String getCollectionTabTooltip() {
      return this.resourceTabTooltip;
    }
    
    /**
     * @return Small icon that represents this resource type.
     */
    public Icon getIcon() {
      return this.icon;
    }
    
    /**
     * @return <code>true</code> - if used for search by default;<br/>
     *         <code>false</code> - otherwise.
     */
    public boolean isDefaultSearchType() {
      return this.defaultType;
    }
    
    /**
     * Resources not of all resource types can be searched for by tags (although every resource type
     * can be searched for by a free-text query).
     * 
     * @return <code>true</code> if resources of this type can be searched for by tags,<br/>
     *         <code>false</code> otherwise.
     */
    public boolean isSuitableForTagSearch() {
      return this.suitableForTagSearch;
    }
    
    /**
     * Not all resource types are suitable for filtering - for example, there are no
     * filters available for service providers in BioCatalogue.
     * 
     * @return <code>true</code> indicates that tab dedicated to displaying search
     *         results of this resource type can have a filter tree.
     */
    public boolean isSuitableForFiltering() {
      return this.suitableForFiltering;
    }
    
    /**
     * @return <code>true</code> indicates that "Preview" option can be made
     *         available for items of this type, as preview factory would be implemented
     *         for such resources.
     */
    public boolean isSuitableForOpeningInPreviewBrowser() {
      return this.suitableForOpeningInPreviewBrowser;
    }
    
    public boolean isSuitableForAddingToServicePanel() {
      return this.suitableForAddingToServicePanel;
    }
    
    public boolean isSuitableForAddingToWorkflowDiagram() {
      return this.suitableForAddingToWorkflowDiagram;
    }
    
    /**
     * @return <code>true</code> indicates that monitoring data can be obtained
     *         from BioCatalougue for this type of resource.
     */
    public boolean isSuitableForHealthCheck() {
      return this.suitableForHealthCheck;
    }
    
    
    /**
     * This method helps to defer instantiation of ListCellRenderers
     * until they are first accessed - it is because construction of
     * the renderers requires knowledge of all available resource types,
     * therefore they cannot be instantiated until after Resource class
     * has been fully loaded.
     * 
     * @return {@link ListCellRenderer} for this type of resources or
     *         <code>null</code> if an error has occurred during
     *         instantiation of required renderer.
     */
    public ListCellRenderer getResultListingCellRenderer() {
      try {
        return this.resultListingCellRendererClass.newInstance();
      }
      catch (Exception e) {
        Logger.getLogger(Resource.class).error("Unable to instantiate search results ListCellRenderer for " +
                                               this.getCollectionName(), e);
        JOptionPane.showMessageDialog(MainWindow.getMainWindow(), 
            "The plugin was unable to instantiate ListCellRenderer for " + this.getCollectionName() + ".\n\n" +
            "This is likely to make the plugin crash or at least be unable to display search results for\n" +
            "items of this type.\n\nPlease try to restart Taverna.", "BioCatalogue Plugin", JOptionPane.ERROR_MESSAGE);
        return null;
      }
    }
    
    /**
     * @return URL in the BioCatalogue API that provides an index of the collection of
     *         all resources of this type.
     */
    public String getAPIResourceCollectionIndex() {
      return apiResourceCollectionIndex;
    }
    
    /**
     * @return Keys and values for any additional URL parameters that need to be included into the
     *         BioCatalogue API requests that are made in order to fetch all necessary additional
     *         details for a *single* expanded entry in the search results listing. 
     */
    public Map<String,String> getResourceCollectionIndexSingleExpandedResourceAdditionalParameters() {
      return apiResourceCollectionIndexSingleExpandedResourceAdditionalParameters;
    }
    
    /**
     * @return Keys and values for any additional URL parameters that need to be included into the
     *         requests sent to filtered indexes of collections of this type in the BioCatalogue API.
     */
    public Map<String,String> getAPIResourceCollectionIndexAdditionalParameters() {
      return apiResourceCollectionIndexAdditionalParameters;
    }
    
    /**
     * @return Number of resources of this type that one page of search results from
     *         the API will contain.
     */
    public int getApiResourceCountPerIndexPage() {
      return apiResourceCountPerIndexPage;
    }
    
    /**
     * @return BioCatalogue API URL that provides a collection of filters for the
     *         resource of this type. 
     */
    public String getAPIResourceCollectionFiltersURL() {
      return apiResourceCollectionFilters;
    }
    
    
    /**
     * This method is useful for adding / removing tabs into the results view - provides
     * and index for the tabbed view to place a tab, relevant to a particular resource type.
     * This helps to preserve the order of tabs after adding / removing them.
     * 
     * @return Zero-based index of this resource type in the <code>RESOURCE_TYPE</code> enum or 
     *         <code>-1</code> if not found (which is impossible under normal conditions).
     */
    public int index()
    {
      TYPE[] values = TYPE.values();
      for (int i = 0; i < values.length; i++) {
        if (this == values[i]) {
          return (i);
        }
      }
      return (-1);
    }
    
  };
  
  
  
  // ----------------------------- RESOURCE CLASS -------------------------------
  
  
  // current resource data
  private final TYPE resourceType;
  private final String resourceURL;
  private final String resourceTitle;
  
  
  public Resource(String resourceURL, String resourceTitle)
  {
    this.resourceURL = extractPureResourceURLFromPreviewActionCommand(resourceURL);
    this.resourceTitle = resourceTitle;
    this.resourceType = getResourceTypeFromResourceURL(resourceURL);
  }
  
  public TYPE getType() {
    return resourceType;
  }
  
  public String getURL() {
    return resourceURL;
  }

  public String getTitle() {
    return resourceTitle;
  }
  
  
  
  public boolean equals(Object other)
  {
    if (other instanceof Resource)
    {
      // compare by all components
      Resource otherRes = (Resource)other;
      return (this.resourceType == otherRes.resourceType &&
              this.resourceTitle.equals(otherRes.resourceTitle) &&
              this.resourceURL.equals(otherRes.resourceURL));
    }
    else {
      // other object is of different type
      return (false);
    }
  }
  
  
  /**
   * @param url Either URL of the resource in BioCatalogue or preview action command
   *            ({@link BioCataloguePluginConstants#ACTION_PREVIEW_RESOURCE}).
   * @return Type of this resource according to the BioCatalogue URL that points to this
   *         resource or <code>null</code> if the type of the resource couldn't be determined.
   */
  public static TYPE getResourceTypeFromResourceURL(String url)
  {
    String pureURL = extractPureResourceURLFromPreviewActionCommand(url);
    
    if (pureURL.startsWith(BioCatalogueClient.API_SERVICES_URL))               return(TYPE.Service);
    else if (pureURL.startsWith(BioCatalogueClient.API_SOAP_OPERATIONS_URL))   return(TYPE.SOAPOperation);
    else if (pureURL.startsWith(BioCatalogueClient.API_REST_METHODS_URL))      return(TYPE.RESTMethod);
//    else if (pureURL.startsWith(BioCatalogueClient.API_SERVICE_PROVIDERS_URL)) return(TYPE.ServiceProvider);   // TODO - re-enable these lines as soon as ServiceProvider and User type are started to be used
//    else if (pureURL.startsWith(BioCatalogueClient.API_USERS_URL))             return(TYPE.User);
    else {
      return (null);
    }
  }
  
  
  /**
   * @param previewActionCommand Either resource preview action command or a 'pure' resource URL already.
   * @return A "pure" resource URL in BioCatalogue with the action prefix
   *         ({@link BioCataloguePluginConstants#ACTION_PREVIEW_RESOURCE}) removed. 
   */
  public static String extractPureResourceURLFromPreviewActionCommand(String previewActionCommand)
  {
    return (previewActionCommand.startsWith(BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE) ?
            previewActionCommand.substring(BioCataloguePluginConstants.ACTION_PREVIEW_RESOURCE.length()) :
            previewActionCommand);
  }
  
  
  /**
   * @param resource
   * @return Display name for listings of items.
   */
  public static String getDisplayNameForResource(ResourceLink resource)
  {
    if (resource instanceof SoapOperation) {
      return ((SoapOperation)resource).getName();
    }
    else if (resource instanceof RestMethod)
    {
      RestMethod restMethod = (RestMethod)resource;
      return (restMethod.getName() == null || restMethod.getName().length() == 0 ?
              restMethod.getEndpointLabel() :
              restMethod.getName());
    }
    else if (resource instanceof Service) {
      return ((Service)resource).getName();
    }
    else if (resource instanceof ServiceProvider) {
      return ((ServiceProvider)resource).getName();
    }
    else if (resource instanceof User) {
      return ((User)resource).getName();
    }
    else if (resource instanceof Registry) {
      return ((Registry)resource).getName();
    }
    else if (resource instanceof LoadingResource) {
      return (resource.getResourceName());
    }
    else {
      return ("ERROR: ITEM NOT RECOGNISED - Item is of known generic type from the BioCatalogue Plugin, but not specifically recognised" + resource.toString());
    }
  }
  
}
