package net.sf.taverna.biocatalogue.model.connectivity;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.Pair;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.SoapOperationIdentity;
import net.sf.taverna.biocatalogue.model.SoapOperationPortIdentity;
import net.sf.taverna.biocatalogue.model.Util;
import net.sf.taverna.biocatalogue.model.connectivity.BeansForJSONLiteAPI.ResourceIndex;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.integration.config.BioCataloguePluginConfiguration;

import org.apache.log4j.Logger;
import org.biocatalogue.x2009.xml.rest.Annotations;
import org.biocatalogue.x2009.xml.rest.AnnotationsDocument;
import org.biocatalogue.x2009.xml.rest.CollectionCoreStatistics;
import org.biocatalogue.x2009.xml.rest.Filters;
import org.biocatalogue.x2009.xml.rest.FiltersDocument;
import org.biocatalogue.x2009.xml.rest.ResourceLink;
import org.biocatalogue.x2009.xml.rest.RestMethod;
import org.biocatalogue.x2009.xml.rest.RestMethodDocument;
import org.biocatalogue.x2009.xml.rest.RestMethods;
import org.biocatalogue.x2009.xml.rest.RestMethodsDocument;
import org.biocatalogue.x2009.xml.rest.Search;
import org.biocatalogue.x2009.xml.rest.SearchDocument;
import org.biocatalogue.x2009.xml.rest.Service;
import org.biocatalogue.x2009.xml.rest.ServiceDocument;
import org.biocatalogue.x2009.xml.rest.ServiceProvider;
import org.biocatalogue.x2009.xml.rest.ServiceProviderDocument;
import org.biocatalogue.x2009.xml.rest.ServiceProviders;
import org.biocatalogue.x2009.xml.rest.ServiceProvidersDocument;
import org.biocatalogue.x2009.xml.rest.Services;
import org.biocatalogue.x2009.xml.rest.ServicesDocument;
import org.biocatalogue.x2009.xml.rest.SoapInput;
import org.biocatalogue.x2009.xml.rest.SoapInputDocument;
import org.biocatalogue.x2009.xml.rest.SoapOperation;
import org.biocatalogue.x2009.xml.rest.SoapOperationDocument;
import org.biocatalogue.x2009.xml.rest.SoapOperations;
import org.biocatalogue.x2009.xml.rest.SoapOperationsDocument;
import org.biocatalogue.x2009.xml.rest.SoapOutput;
import org.biocatalogue.x2009.xml.rest.SoapOutputDocument;
import org.biocatalogue.x2009.xml.rest.SoapService;
import org.biocatalogue.x2009.xml.rest.SoapServiceDocument;
import org.biocatalogue.x2009.xml.rest.Tag;
import org.biocatalogue.x2009.xml.rest.TagDocument;
import org.biocatalogue.x2009.xml.rest.Tags;
import org.biocatalogue.x2009.xml.rest.TagsDocument;
import org.biocatalogue.x2009.xml.rest.User;
import org.biocatalogue.x2009.xml.rest.UserDocument;
import org.biocatalogue.x2009.xml.rest.Users;
import org.biocatalogue.x2009.xml.rest.UsersDocument;

import com.google.gson.Gson;


/**
 * @author Sergejs Aleksejevs
 */
public class BioCatalogueClient
{
  // ******* CONSTANTS *******
  // plugin details
  public static final String PLUGIN_VERSION = "0.1.1";
  public static final String PLUGIN_USER_AGENT = "Taverna2-BioCatalogue-plugin/" +
                                                 PLUGIN_VERSION +
                                                 " Java/" + System.getProperty("java.version");
  
  public static final String XML_MIME_TYPE = "application/xml";
  public static final String JSON_MIME_TYPE = "application/json";
  public static final String LITE_JSON_MIME_TYPE = "application/biocat-lite+json";
  
  public static final String XML_DATA_FORMAT = ".xml";
  public static final String JSON_DATA_FORMAT = ".json";
  public static final String LITE_JSON_DATA_FORMAT = ".bljson";
  
  
  
  // API URLs
  public static final String DEFAULT_API_SANDBOX_BASE_URL = "http://sandbox.biocatalogue.org";
  public static final String DEFAULT_API_TEST_SERVER_BASE_URL = "http://test.biocatalogue.org";
  public static final String DEFAULT_API_LIVE_SERVER_BASE_URL = "http://www.biocatalogue.org";
  
  private static String BASE_URL;    // BioCatalogue base URL to use (can be updated at runtime)
  
  public static String API_REGISTRIES_URL;
  public static String API_SERVICE_PROVIDERS_URL;
  public static String API_USERS_URL;
  public static String API_USER_FILTERS_URL;
  public static String API_SERVICES_URL;
  public static String API_SERVICE_FILTERS_URL;
  public static String API_SOAP_OPERATIONS_URL;
  public static String API_SOAP_OPERATION_FILTERS_URL;
  public static String API_REST_METHODS_URL;
  public static String API_REST_METHOD_FILTERS_URL;
  public static String API_TAG_CLOUD_URL;
  public static String API_SEARCH_URL;
  public static String API_LOOKUP_URL;
  
  // URL modifiers
  public static final Map<String,String> API_INCLUDE_SUMMARY = Collections.singletonMap("include","summary");          // for fetching Service
  public static final Map<String,String> API_INCLUDE_ANCESTORS = Collections.singletonMap("include", "ancestors");     // for fetching SOAP Operations and REST Methods
  public static final String[] API_SORT_BY_NAME = {"sort","name"};                   // for tag cloud
  public static final String[] API_SORT_BY_COUNTS = {"sort","counts"};               // for tag cloud
  public static final String[] API_ALSO_INPUTS_OUTPUTS = {"also","inputs,outputs"};  // for annotations on SOAP operation
  
  public static final String API_PER_PAGE_PARAMETER = "per_page";
  public static final String API_PAGE_PARAMETER = "page";
  public static final String API_LIMIT_PARAMETER = "limit";
  public static final String API_SERVICE_MONITORING_URL_SUFFIX = "/monitoring";
  public static final String API_FILTERED_INDEX_SUFFIX = "/filtered_index";
  
  // API Request scope
  public static final String API_SCOPE_PARAMETER = "scope";
  public static final String API_SCOPE_SOAP_OPERATIONS = "soap_operations";
  public static final String API_SCOPE_REST_METHODS = "rest_methods";
  public static final String API_SCOPE_SERVICES = "services";
  public static final String API_SCOPE_SERVICE_PROVIDERS = "service_providers";
  public static final String API_SCOPE_REGISTRIES = "registries";
  public static final String API_SCOPE_USERS = "users";
  
  public static final String API_TAG_PARAMETER = "tag";
  
  public static final String API_LOOKUP_WSDL_LOCATION_PARAMETER = "wsdl_location";
  public static final String API_LOOKUP_OPERATION_NAME_PARAMETER = "operation_name";
  public static final String API_LOOKUP_SOAP_INPUT_NAME_PARAMETER = "input_name";
  public static final String API_LOOKUP_SOAP_OUTPUT_NAME_PARAMETER = "output_name";
  
  
  // *************************
  
  // universal date formatters
  private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
  private static final DateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("HH:mm 'on' dd/MM/yyyy");
  private static final DateFormat API_LOGGING_TIMESTAMP_FORMATTER = DateFormat.getDateTimeInstance();
  
  
  // SETTINGS
  private Properties iniSettings;    // settings that are read/stored from/to INI file
  
  private File fAPIOperationLog;
  private PrintWriter pwAPILogWriter;
  
  // the logger
  private Logger logger = Logger.getLogger(BioCatalogueClient.class);
  
  
  // default constructor
  public BioCatalogueClient()
  {
    // TODO: load any config settings (if necessary)
    
    // load the BioCatalogue API base URL from the plugin's configuration settings
    this.setBaseURL(BioCataloguePluginConfiguration.getInstance().
            getProperty(BioCataloguePluginConfiguration.BIOCATALOGUE_BASE_URL));
    
    // open API operation log file, if necessary
    if (BioCataloguePluginConstants.PERFORM_API_RESPONSE_TIME_LOGGING || 
        BioCataloguePluginConstants.PERFORM_API_XML_DATA_BINDING_TIME_LOGGING )
    {
      try {
        BioCataloguePluginConstants.LOG_FILE_FOLDER.mkdirs(); // just in case this log file was never written - create the folder as well
        fAPIOperationLog = new File(BioCataloguePluginConstants.LOG_FILE_FOLDER, 
                                    BioCataloguePluginConstants.API_OPERATION_LOG_FILENAME);
        pwAPILogWriter = new PrintWriter(new FileOutputStream(fAPIOperationLog, true), true);  // auto-flush makes sure that even if app crashes, log will not be lost
      }
      catch (NullPointerException e) {
        pwAPILogWriter = new PrintWriter(System.out, true);
        System.err.println("ERROR: Folder to log API operation details is unknown (using System.out instead)... Details:");
        e.printStackTrace();
      }
      catch (FileNotFoundException e) {
        System.err.println("ERROR: Couldn't open API operation log file... Details:");
        e.printStackTrace();
      }
    }
  }
  
  
  public String getBaseURL() {
    return this.BASE_URL;
  }
  
  /**
   * Updates the base API URL and also
   * updates derived URLs of sub-URLs
   * (e.g. BASE_URL + /services, etc)
   * 
   * @param baseURL The new value for the BioCatalogue API base URL.
   */
  public void setBaseURL(String baseURL)
  {
    // make sure the base URL doesn't have a slash at the end
    // (otherwise double slashes may occur during URL manipulation)
    while (baseURL.endsWith("/")) { baseURL = baseURL.substring(0, baseURL.length() - 1); }
    
    this.BASE_URL = baseURL;
    
    API_REGISTRIES_URL = BASE_URL + "/registries";
    API_SERVICE_PROVIDERS_URL = BASE_URL + "/service_providers";
    API_USERS_URL = BASE_URL + "/users";
    API_USER_FILTERS_URL = API_USERS_URL + "/filters";
    API_SERVICES_URL = BASE_URL + "/services";
    API_SERVICE_FILTERS_URL = API_SERVICES_URL + "/filters";
    API_SOAP_OPERATIONS_URL = BASE_URL + "/soap_operations";
    API_SOAP_OPERATION_FILTERS_URL = API_SOAP_OPERATIONS_URL + "/filters";
    API_REST_METHODS_URL = BASE_URL + "/rest_methods";
    API_REST_METHOD_FILTERS_URL = API_REST_METHODS_URL + "/filters";
    API_TAG_CLOUD_URL = BASE_URL + "/tags";
    API_SEARCH_URL = BASE_URL + "/search";
    API_LOOKUP_URL = BASE_URL + "/lookup";
  }
  
  public File getAPIOperationLog() {
    return fAPIOperationLog;
  }
  
  public PrintWriter getAPILogWriter() {
    return pwAPILogWriter;
  }
  
  
  // ************ METHODS FOR RETRIEVAL OF SPECIALISED OBJECT FROM THE API VIA XML ************
  
  public Annotations getBioCatalogueAnnotations(String strAnnotationsURL) throws Exception {
    return (parseAPIResponseStream(Annotations.class, doBioCatalogueGET(strAnnotationsURL)));
  }
  
  public Filters getBioCatalogueFilters(String strURL) throws Exception {
    return (parseAPIResponseStream(Filters.class, doBioCatalogueGET(strURL)));
  }
  
  public Services getBioCatalogueServices(String strURL) throws Exception {
    return (parseAPIResponseStream(Services.class, doBioCatalogueGET(strURL)));
  }
  
  public Service getBioCatalogueService(String serviceURL) throws Exception {
    return (parseAPIResponseStream(Service.class, doBioCatalogueGET(serviceURL)));
  }
  
  public Service getBioCatalogueServiceSummary(String serviceURL) throws Exception {
    return (parseAPIResponseStream(Service.class, doBioCatalogueGET(Util.appendAllURLParameters(serviceURL, API_INCLUDE_SUMMARY))));
  }
  
  public Service getBioCatalogueServiceMonitoringData(String serviceURL) throws Exception
  {
    return (parseAPIResponseStream(Service.class,
                                   doBioCatalogueGET(serviceURL + API_SERVICE_MONITORING_URL_SUFFIX))
           );
  }
  
  public SoapService getBioCatalogueSoapService(String soapServiceURL) throws Exception {
    return (parseAPIResponseStream(SoapService.class, doBioCatalogueGET(soapServiceURL)));
  }
  
  public SoapOperation getBioCatalogueSoapOperation(String soapOperationURL) throws Exception {
    return (parseAPIResponseStream(SoapOperation.class, doBioCatalogueGET(soapOperationURL)));
  }
  
  public RestMethod getBioCatalogueRestMethod(String restMethodURL) throws Exception {
    return (parseAPIResponseStream(RestMethod.class, doBioCatalogueGET(restMethodURL)));
  }
  
  public Search getBioCatalogueSearchData(String searchURL) throws Exception {
    return (parseAPIResponseStream(Search.class, doBioCatalogueGET(searchURL)));
  }
  
  public Tag getBioCatalogueTag(String searchByTagURL) throws Exception {
    return (parseAPIResponseStream(Tag.class, doBioCatalogueGET(searchByTagURL)));
  }
  
  public Tags getBioCatalogueTags(String tagsURL) throws Exception {
    return (parseAPIResponseStream(Tags.class, doBioCatalogueGET(tagsURL)));
  }
  
  
  public ResourceLink getBioCatalogueResource(Class<? extends ResourceLink> classOfResourceToFetch, String resourceURL) throws Exception {
    return (parseAPIResponseStream(classOfResourceToFetch, doBioCatalogueGET(resourceURL)));
  }
  
  
  public <T extends ResourceLink> Pair<CollectionCoreStatistics, List<T>> getListOfItemsFromResourceCollectionIndex(
      Class<T> classOfCollectionOfRequiredReturnedObjects, BioCatalogueAPIRequest filteringRequest) throws Exception
  {
    ResourceLink matchingItems = null;
    if (filteringRequest.getRequestType() == BioCatalogueAPIRequest.TYPE.GET) {
      matchingItems = parseAPIResponseStream(classOfCollectionOfRequiredReturnedObjects, doBioCatalogueGET(filteringRequest.getURL()));
    }
    else {
      matchingItems = parseAPIResponseStream(classOfCollectionOfRequiredReturnedObjects,
                           doBioCataloguePOST_SendJSON_AcceptXML(filteringRequest.getURL(), filteringRequest.getData()));
    }
    
    CollectionCoreStatistics statistics = null;
    
    List<T> matchingItemList = new ArrayList<T>();
    
    // SOAP Operations
    if (classOfCollectionOfRequiredReturnedObjects.equals(SoapOperations.class)) {
      SoapOperations soapOperations = (SoapOperations)matchingItems;
      matchingItemList.addAll((Collection<? extends T>)(soapOperations.getResults().getSoapOperationList()));
      statistics = soapOperations.getStatistics();
    }
    
    // REST Methods
    else if (classOfCollectionOfRequiredReturnedObjects.equals(RestMethods.class)) {
      RestMethods restMethods = (RestMethods)matchingItems;
      matchingItemList.addAll((Collection<? extends T>)(restMethods.getResults().getRestMethodList()));
      statistics = restMethods.getStatistics();
    }
    
    // Services
    else if (classOfCollectionOfRequiredReturnedObjects.equals(Services.class)) {
      Services services = (Services)matchingItems;
      matchingItemList.addAll((Collection<? extends T>)(services.getResults().getServiceList()));
      statistics = services.getStatistics();
    }
    
    // Service Providers
    else if (classOfCollectionOfRequiredReturnedObjects.equals(ServiceProviders.class)) {
      ServiceProviders serviceProviders = (ServiceProviders)matchingItems;
      matchingItemList.addAll((Collection<? extends T>)(serviceProviders.getResults().getServiceProviderList()));
      statistics = serviceProviders.getStatistics();
    }
    
    // Users
    else if (classOfCollectionOfRequiredReturnedObjects.equals(Users.class)) {
      Users users = (Users)matchingItems;
      matchingItemList.addAll((Collection<? extends T>)(users.getResults().getUserList()));
      statistics = users.getStatistics();
    }
    
    // no such option - error
    else {
      return null;
    }
    
    return new Pair<CollectionCoreStatistics, List<T>>(statistics, matchingItemList);
  }
  
  
  
  
  /**
   * @param wsdlLocation
   * @param operationName
   * @return SoapOperation instance or <code>null</code> if nothing was found (or error occurred).
   * @throws Exception
   */
  public SoapOperation lookupSoapOperation(SoapOperationIdentity soapOperationDetails) throws Exception
  {
    // first of all check for any problems with input data
    if (soapOperationDetails == null || soapOperationDetails.hasError() ||
        soapOperationDetails.getWsdlLocation() == null || soapOperationDetails.getWsdlLocation().length() == 0 ||
        soapOperationDetails.getOperationName() == null || soapOperationDetails.getOperationName().length() == 0)
    {
      // something's not right - return null
      return (null);
    }
    
    String lookupURL = Util.appendURLParameter(API_LOOKUP_URL, API_LOOKUP_WSDL_LOCATION_PARAMETER, soapOperationDetails.getWsdlLocation());
    lookupURL = Util.appendURLParameter(lookupURL, API_LOOKUP_OPERATION_NAME_PARAMETER, soapOperationDetails.getOperationName());
    
    ServerResponseStream lookupResponse = doBioCatalogueGET(lookupURL);
    if (lookupResponse.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      return null;
    }
    return (parseAPIResponseStream(SoapOperation.class, lookupResponse));
  }
  
  
  public <T extends ResourceLink> T lookupSoapOperationPort(Class<T> requiredResultClass, SoapOperationPortIdentity portDetails) throws Exception
  {
    // first of all check for any problems with port details
    if (portDetails == null || portDetails.hasError() ||
        portDetails.getWsdlLocation() == null || portDetails.getWsdlLocation().length() == 0 ||
        portDetails.getOperationName() == null || portDetails.getOperationName().length() == 0 ||
        portDetails.getPortName() == null || portDetails.getPortName().length() == 0)
    {
      // something's not right - return null
      return (null);
    }
    
    // now check that specified class matches the port type
    if (portDetails.isInput() && !requiredResultClass.equals(SoapInput.class) ||
        !portDetails.isInput() && !requiredResultClass.equals(SoapOutput.class))
    {
      return (null);
    }
    
    String lookupURL = Util.appendURLParameter(API_LOOKUP_URL, API_LOOKUP_WSDL_LOCATION_PARAMETER, portDetails.getWsdlLocation());
    lookupURL = Util.appendURLParameter(lookupURL, API_LOOKUP_OPERATION_NAME_PARAMETER, portDetails.getOperationName());
    if (portDetails.isInput()) {
      lookupURL = Util.appendURLParameter(lookupURL, API_LOOKUP_SOAP_INPUT_NAME_PARAMETER, portDetails.getPortName());
    }
    else {
      lookupURL = Util.appendURLParameter(lookupURL, API_LOOKUP_SOAP_OUTPUT_NAME_PARAMETER, portDetails.getPortName());
    }
    
    ServerResponseStream lookupResponse = doBioCatalogueGET(lookupURL);
    if (lookupResponse.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
      return null;
    }
    return (parseAPIResponseStream(requiredResultClass, lookupResponse));
  }
  
  
  public Service lookupParentService(SoapOperationIdentity soapOperationDetails) throws Exception
  {
    SoapOperation soapOperation = this.lookupSoapOperation(soapOperationDetails);
    if (soapOperation != null) {
      return (getBioCatalogueService(soapOperation.getAncestors().getService().getHref()));
    }
    else {
      // lookup didn't find the SOAP operation or there
      // was some problem with the input data
      return (null);
    }
  }
  
  
  public Service lookupParentServiceMonitoringData(SoapOperationIdentity soapOperationDetails) throws Exception
  {
    SoapOperation soapOperation = this.lookupSoapOperation(soapOperationDetails);
    if (soapOperation != null) {
      return (getBioCatalogueServiceMonitoringData(soapOperation.getAncestors().getService().getHref()));
    }
    else {
      // lookup didn't find the SOAP operation or there
      // was some problem with the input data
      return (null);
    }
  }
  
  
  // ************ METHODS FOR RETRIEVAL OF SPECIALISED OBJECT FROM THE API VIA LITE JSON ************
  
  public BeansForJSONLiteAPI.ResourceIndex getBioCatalogueResourceLiteIndex(TYPE resourceType, String resourceIndexURL) throws Exception
  {
    ServerResponseStream response = doBioCatalogueGET_LITE_JSON(resourceIndexURL);
    
    Gson gson = new Gson();
    return (ResourceIndex)(gson.fromJson(new InputStreamReader(response.getResponseStream()), resourceType.getJsonLiteAPIBindingBeanClass()));
  }
  
  
  public BeansForJSONLiteAPI.ResourceIndex postBioCatalogueResourceLiteIndex(TYPE resourceType, String resourceIndexURL, String postData) throws Exception
  {
    ServerResponseStream response = doBioCataloguePOST_SendJSON_AcceptLITEJSON(resourceIndexURL, postData);
    
    Gson gson = new Gson();
    return (ResourceIndex)(gson.fromJson(new InputStreamReader(response.getResponseStream()), resourceType.getJsonLiteAPIBindingBeanClass()));
  }
  
  
  // ************ GENERIC API CONNECTIVITY METHODS ************
  
  /**
   * Generic method to issue GET requests to BioCatalogue server.
   * 
   * This is a convenience method to be used instead of {@link BioCatalogueClient#doBioCatalogueGET_XML(String)}.
   * 
   * @param strURL The URL on BioCatalogue to issue GET request to.
   * @return TODO
   * @throws Exception
   */
  public ServerResponseStream doBioCatalogueGET(String strURL) throws Exception {
    return (doBioCatalogueGET_XML(strURL));
  }
  
  public ServerResponseStream doBioCatalogueGET_XML(String strURL) throws Exception {
    return (doBioCatalogueGET(strURL, XML_MIME_TYPE, XML_DATA_FORMAT));
  }
  
  public ServerResponseStream doBioCatalogueGET_JSON(String strURL) throws Exception {
    return (doBioCatalogueGET(strURL, JSON_MIME_TYPE, JSON_DATA_FORMAT));
  }
  
  public ServerResponseStream doBioCatalogueGET_LITE_JSON(String strURL) throws Exception {
    return (doBioCatalogueGET(strURL, LITE_JSON_MIME_TYPE, LITE_JSON_DATA_FORMAT));
  }
  
  
  public ServerResponseStream doBioCatalogueGET(String strURL, String ACCEPT_HEADER, String REQUESTED_DATA_FORMAT) throws Exception
  {
    // TODO - HACK to speed up processing append .xml / .json / .bljson to all URLs to avoid LinkedData content negotiation
    strURL = Util.appendStringBeforeParametersOfURL(strURL, REQUESTED_DATA_FORMAT);
    
    // open server connection using provided URL (with no further modifications to it)
    URL url = new URL(strURL);
    
    Calendar requestStartedAt = Calendar.getInstance();
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
    conn.setRequestProperty("Accept", ACCEPT_HEADER);
    
//    if(LOGGED_IN) {
//      // if the user has "logged in", also add authentication details
//      conn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);
//    }
    
    // fetch server's response
    ServerResponseStream serverResponse = doBioCatalogueReceiveServerResponse(conn, strURL, true);
    
    if (BioCataloguePluginConstants.PERFORM_API_RESPONSE_TIME_LOGGING) {
      logAPIOperation(requestStartedAt, "GET", serverResponse);
    }
    return (serverResponse);
  }
  
  
  
  public ServerResponseStream doBioCataloguePOST_SendJSON_AcceptXML(String strURL, String strDataBody) throws Exception {
    return (doBioCataloguePOST(strURL, strDataBody, JSON_MIME_TYPE, XML_MIME_TYPE, XML_DATA_FORMAT));
  }
  
  public ServerResponseStream doBioCataloguePOST_SendJSON_AcceptLITEJSON(String strURL, String strDataBody) throws Exception {
    return (doBioCataloguePOST(strURL, strDataBody, JSON_MIME_TYPE, LITE_JSON_MIME_TYPE, LITE_JSON_DATA_FORMAT));
  }
  
  
  /**
   * Generic method to execute POST requests to BioCatalogue server.
   * 
   * @param strURL The URL on BioCatalogue to POST to. 
   * @param strDataBody Body of the message to be POSTed to <code>strURL</code>. 
   * @return An object containing server's response body as an InputStream and
   *         a response code.
   * @param CONTENT_TYPE_HEADER MIME type of the sent data.
   * @param ACCEPT_HEADER MIME type of the data to be received.
   * @param REQUESTED_DATA_FORMAT
   * @throws Exception
   */
  public ServerResponseStream doBioCataloguePOST(String strURL, String strDataBody, String CONTENT_TYPE_HEADER,
                                                 String ACCEPT_HEADER, String REQUESTED_DATA_FORMAT) throws Exception
  {
    // TODO - HACK to speed up processing append .xml / .json / .bljson to all URLs to avoid LinkedData content negotiation
    strURL = Util.appendStringBeforeParametersOfURL(strURL, REQUESTED_DATA_FORMAT);
    
    // open server connection using provided URL (with no further modifications to it)
    URL url = new URL (strURL);
    
    Calendar requestStartedAt = Calendar.getInstance();
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
    urlConn.setRequestMethod("POST");
    urlConn.setDoOutput(true);
    urlConn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
    urlConn.setRequestProperty("Content-Type", CONTENT_TYPE_HEADER);
    urlConn.setRequestProperty("Accept", ACCEPT_HEADER);
    
    // prepare and POST XML data
    OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
    out.write(strDataBody);
    out.close();
    
    
    // fetch server's response
    ServerResponseStream serverResponse = doBioCatalogueReceiveServerResponse(urlConn, strURL, false);
    
    if (BioCataloguePluginConstants.PERFORM_API_RESPONSE_TIME_LOGGING) {
      logAPIOperation(requestStartedAt, "POST", serverResponse);
    }
    return (serverResponse);
  }
  
  
  /**
   * Generic method to execute DELETE requests to myExperiment server.
   * This is only to be called when a user is logged in. 
   * 
   * @param strURL The URL on myExperiment to direct DELETE request to.
   * @return An object containing XML Document with server's response body and
   *         a response code. Response body XML document might be null if there
   *         was an error or the user wasn't authorised to perform a certain action.
   *         Response code will always be set.
   * @throws Exception
   */
  /*public ServerResponse doMyExperimentDELETE(String strURL) throws Exception
  {
    // open server connection using provided URL (with no modifications to it)
    URL url = new URL(strURL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    
    // "tune" the connection
    conn.setRequestMethod("DELETE");
    conn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
    conn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);
    
    // check server's response
    return (doMyExperimentReceiveServerResponse(conn, strURL, true));
  }*/
  
  
  /**
   * A common method for retrieving BioCatalogue server's response for both
   * GET and POST requests.
   * 
   * @param conn Instance of the established URL connection to poll for server's response.
   * @param strURL The URL on BioCatalogue with which the connection is established.
   * @param bIsGetRequest Flag for identifying type of the request. True when the current 
   *        connection executes GET request; false when it executes a POST / DELETE request.
   * @return TODO
   */
  @SuppressWarnings("unchecked")
  private ServerResponseStream doBioCatalogueReceiveServerResponse(HttpURLConnection conn, String strURL, boolean bIsGETRequest) throws Exception
  {
    int iResponseCode = conn.getResponseCode();
    
    switch (iResponseCode)
    {
      case HttpURLConnection.HTTP_OK:
        // regular operation path - simply return the reference to the data input stream
        return (new ServerResponseStream(iResponseCode, conn.getInputStream(), strURL));
        
      case HttpURLConnection.HTTP_BAD_REQUEST:
        // this was a bad XML request - need full XML response to retrieve the error message from it;
        // Java throws IOException if getInputStream() is used when non HTTP_OK response code was received -
        // hence can use getErrorStream() straight away to fetch the error document
        return (new ServerResponseStream(iResponseCode, conn.getErrorStream(), strURL));
        
      case HttpURLConnection.HTTP_UNAUTHORIZED:
        // this content is not authorised for current user
        return (new ServerResponseStream(iResponseCode, null, strURL));
      
      case HttpURLConnection.HTTP_NOT_FOUND:
        // nothing was found at the provided URL
        return (new ServerResponseStream(iResponseCode, conn.getErrorStream(), strURL));
      
      default:
        // unexpected response code - raise an exception
        throw new IOException("Received unexpected HTTP response code (" + iResponseCode + ") while " +
            (bIsGETRequest ? "fetching data at " : "posting data to ") + strURL);
    }
  }
  
  
  /**
   * This method is here to make sure that *all* parsing of received input stream data
   * from the API is parsed ("bound") into Java objects in a central place - so it's
   * possible to measure performance of XmlBeans for various inputs.
   * 
   * NB! There is a serious limitation in Java's generics. Generic methods cannot
   *     access any of the static context of the classes of type parameters, because
   *     it wasn't designed for this. The only purpose of type parameters is compile-time
   *     type-checking.
   *     This means that even though all classes that could potentially be supplied as a
   *     type-parameter would have certain static functionality, it's not possible to access
   *     that through using the type-parameter like it's done in normal polymorhic situations.
   *     Therefore, some switching based on the class of the type-parameter for this method is
   *     done...
   * 
   * @param <T>
   * @param classOfRequiredReturnedObject Class of the object that the caller expects to receive
   *                                      after parsing provided server's response. For example,
   *                                      a call to /tags.xml return the <pre>[tags]...[/tags]</pre>
   *                                      document. <code>TagsDocument</code> should be used to access
   *                                      its static factory and parse the input stream - the return
   *                                      value will have type <code>Tags</code> -- <code>Tags.class</code>
   *                                      is the required input value for this parameter in this situation then.
   * @param serverResponse This object should contain the input stream obtained from the API in return
   *                       to the call on some URL.
   * @return               InputStream data parsed into the Java object of the supplied type [T].
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private <T extends ResourceLink> T parseAPIResponseStream(Class<T> classOfRequiredReturnedObject, ServerResponseStream serverResponse) throws Exception
  {
    T parsedObject = null;
    InputStream xmlInputStream = serverResponse.getResponseStream();
    
    // choose a factory to parse the response and perform parsing
    Calendar parsingStartedAt = Calendar.getInstance();
    if (classOfRequiredReturnedObject.equals(Annotations.class)) {
      parsedObject = (T)AnnotationsDocument.Factory.parse(xmlInputStream).getAnnotations();
    }
    else if (classOfRequiredReturnedObject.equals(Filters.class)) {
      parsedObject = (T)FiltersDocument.Factory.parse(xmlInputStream).getFilters();
    }
    else if (classOfRequiredReturnedObject.equals(RestMethods.class)) {
      parsedObject = (T)RestMethodsDocument.Factory.parse(xmlInputStream).getRestMethods();
    }
    else if (classOfRequiredReturnedObject.equals(RestMethod.class)) {
      parsedObject = (T)RestMethodDocument.Factory.parse(xmlInputStream).getRestMethod();
    }
    else if (classOfRequiredReturnedObject.equals(Search.class)) {
      parsedObject = (T)SearchDocument.Factory.parse(xmlInputStream).getSearch();
    }
    else if (classOfRequiredReturnedObject.equals(Services.class)) {
      parsedObject = (T)ServicesDocument.Factory.parse(xmlInputStream).getServices();
    }
    else if (classOfRequiredReturnedObject.equals(Service.class)) {
      parsedObject = (T)ServiceDocument.Factory.parse(xmlInputStream).getService();
    }
    else if (classOfRequiredReturnedObject.equals(ServiceProviders.class)) {
      parsedObject = (T)ServiceProvidersDocument.Factory.parse(xmlInputStream).getServiceProviders();
    }
    else if (classOfRequiredReturnedObject.equals(ServiceProvider.class)) {
      parsedObject = (T)ServiceProviderDocument.Factory.parse(xmlInputStream).getServiceProvider();
    }
    else if (classOfRequiredReturnedObject.equals(SoapOperations.class)) {
      parsedObject = (T)SoapOperationsDocument.Factory.parse(xmlInputStream).getSoapOperations();
    }
    else if (classOfRequiredReturnedObject.equals(SoapOperation.class)) {
      parsedObject = (T)SoapOperationDocument.Factory.parse(xmlInputStream).getSoapOperation();
    }
    else if (classOfRequiredReturnedObject.equals(SoapService.class)) {
      parsedObject = (T)SoapServiceDocument.Factory.parse(xmlInputStream).getSoapService();
    }
    else if (classOfRequiredReturnedObject.equals(SoapInput.class)) {
      parsedObject = (T)SoapInputDocument.Factory.parse(xmlInputStream).getSoapInput();
    }
    else if (classOfRequiredReturnedObject.equals(SoapOutput.class)) {
      parsedObject = (T)SoapOutputDocument.Factory.parse(xmlInputStream).getSoapOutput();
    }
    else if (classOfRequiredReturnedObject.equals(Tags.class)) {
      parsedObject = (T)TagsDocument.Factory.parse(xmlInputStream).getTags();
    }
    else if (classOfRequiredReturnedObject.equals(Tag.class)) {
      parsedObject = (T)TagDocument.Factory.parse(xmlInputStream).getTag();
    }
    else if (classOfRequiredReturnedObject.equals(Users.class)) {
      parsedObject = (T)UsersDocument.Factory.parse(xmlInputStream).getUsers();
    }
    else if (classOfRequiredReturnedObject.equals(User.class)) {
      parsedObject = (T)UserDocument.Factory.parse(xmlInputStream).getUser();
    }
    
     
    // log the operation if necessary
    if (BioCataloguePluginConstants.PERFORM_API_XML_DATA_BINDING_TIME_LOGGING) {
      logAPIOperation(parsingStartedAt, null, serverResponse);
    }
    
    return (parsedObject);
  }
  
  
  // ************ HELPERS ************
  
  public static DateFormat getDateFormatter() {
    return(BioCatalogueClient.DATE_FORMATTER);
  }
  
  public static DateFormat getShortDateFormatter() {
    return(BioCatalogueClient.SHORT_DATE_FORMATTER);
  }
  
  
  /**
   * This is a helper to facilitate performance monitoring of the API usage.
   * 
   * @param opearationStartedAt Instance of Calendar initialised with the date/time value of
   *                            when the logged operation was started.
   * @param requestType "GET" or "POST" to indicate that this was the actual URL connection with the BioCatalogue server
   *                    to fetch some data; <code>null</code> to indicate an xml-binding operation using XmlBeans.
   * @param serverResponse Will be used to extract the request URL.
   */
  private void logAPIOperation(Calendar opearationStartedAt, String requestType, ServerResponseStream serverResponse)
  {
    // just in case check that the writer was initialised
    if (pwAPILogWriter != null) {
      pwAPILogWriter.println(API_LOGGING_TIMESTAMP_FORMATTER.format(opearationStartedAt.getTime()) + ", " +
                             (System.currentTimeMillis() - opearationStartedAt.getTimeInMillis()) + ", " +
                             (requestType == null ? "xml_parsing" : requestType) + ", " +
                             serverResponse.getRequestURL());
    }
  }
  
}
