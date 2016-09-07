package org.apache.taverna.ui.perspectives.myexperiment.model;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.security.credentialmanager.UsernamePassword;
import org.apache.taverna.ui.perspectives.myexperiment.MainComponent;
import org.apache.taverna.ui.perspectives.myexperiment.MyExperimentPerspective;
import org.apache.taverna.ui.perspectives.myexperiment.model.SearchEngine.QuerySearchInstance;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;
import org.apache.taverna.configuration.app.ApplicationConfiguration;

/**
 * @author Sergejs Aleksejevs, Emmanuel Tagarira, Jiten Bhagat
 */
public class MyExperimentClient {
  // CONSTANTS
  public static final String DEFAULT_BASE_URL = "http://www.myexperiment.org";
  public static final String PLUGIN_USER_AGENT = "Taverna2-myExperiment-plugin/"
      + MyExperimentPerspective.PLUGIN_VERSION
      + " Java/"
      + System.getProperty("java.version");
  private static final String INI_FILE_NAME = "myexperiment-plugin.ini";

  private static final int EXAMPLE_WORKFLOWS_PACK_ID = 254;

  public static final String INI_BASE_URL = "my_experiment_base_url";
  public static final String INI_AUTO_LOGIN = "auto_login";
  public static final String INI_FAVOURITE_SEARCHES = "favourite_searches";
  public static final String INI_SEARCH_HISTORY = "search_history";
  public static final String INI_TAG_SEARCH_HISTORY = "tag_search_history";
  public static final String INI_PREVIEWED_ITEMS_HISTORY = "previewed_items_history";
  public static final String INI_OPENED_ITEMS_HISTORY = "opened_items_history";
  public static final String INI_UPLOADED_ITEMS_HISTORY = "uploaded_items_history";
  public static final String INI_DOWNLOADED_ITEMS_HISTORY = "downloaded_items_history";
  public static final String INI_COMMENTED_ITEMS_HISTORY = "commented_items_history";
  public static final String INI_DEFAULT_LOGGED_IN_TAB = "default_tab_for_logged_in_users";
  public static final String INI_DEFAULT_ANONYMOUS_TAB = "default_tab_for_anonymous_users";
  public static final String INI_MY_STUFF_WORKFLOWS = "show_workflows_in_my_stuff";
  public static final String INI_MY_STUFF_FILES = "show_files_in_my_stuff";
  public static final String INI_MY_STUFF_PACKS = "show_packs_in_my_stuff";

  private final String DO_PUT = "_DO_UPDATE_SIGNAL_";

  public static boolean baseChangedSinceLastStart = false;

  // old format
  private static final DateFormat OLD_DATE_FORMATTER = new SimpleDateFormat(
	      "EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
	  private static final DateFormat OLD_SHORT_DATE_FORMATTER = new SimpleDateFormat(
	      "HH:mm 'on' dd/MM/yyyy", Locale.ENGLISH);

  // universal date formatter
  private static final DateFormat NEW_DATE_FORMATTER = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss Z");

  // SETTINGS
  private String BASE_URL; // myExperiment base URL to use
  private java.io.File fIniFileDir; // a folder, where the INI file will be
  // stored
  private Properties iniSettings; // settings that are read/stored from/to INI
  // file

  // the logger
  private Logger logger = Logger.getLogger(MyExperimentClient.class);

  // authentication settings (and the current user)
  private boolean LOGGED_IN = false;
  private String AUTH_STRING = "";
  private User current_user = null;
private CredentialManager credentialManager;

  // default constructor
  public MyExperimentClient(CredentialManager credentialManager, ApplicationConfiguration applicationConfig) {
	  

    this.credentialManager = credentialManager;
	// === Load INI settings ===
    // but loading settings from INI file, determine what folder is to be used
    // for INI file
	this.fIniFileDir = new java.io.File(applicationConfig
	     .getApplicationHomeDir().toFile(), "conf");

    // load preferences if the INI file exists
    this.iniSettings = new Properties();
    this.loadSettings();

    // === Check if defaults should be applied to override not sensible settings
    // from INI file ===
    // verify that myExperiment BASE URL was read - use default otherwise
    if (BASE_URL == null || BASE_URL.length() == 0)
      BASE_URL = DEFAULT_BASE_URL;
    this.iniSettings.put(INI_BASE_URL, BASE_URL); // store this to settings (if
    // no changes were made - same as before, alternatively default URL)
  }

  // getter for the current status
  public boolean isLoggedIn() {
    return (LOGGED_IN);
  }

  public String getBaseURL() {
    return this.BASE_URL;
  }

  public void setBaseURL(String baseURL) {
    this.BASE_URL = baseURL;
  }

  // getter for the current user, if one is logged in to myExperiment
  public User getCurrentUser() {
    return (this.current_user);
  }

  // setter for the current user (the one that has logged in to myExperiment)
  public void setCurrentUser(User user) {
    this.current_user = user;
  }

  public Properties getSettings() {
    return this.iniSettings;
  }

  // loads all plugin settings from the INI file
  public synchronized void loadSettings() {
    try {
      // === READ SETTINGS ===
      FileInputStream fIniInputStream = new FileInputStream(new java.io.File(
          this.fIniFileDir, this.INI_FILE_NAME));
      this.iniSettings.load(fIniInputStream);
      fIniInputStream.close();

      // set BASE_URL if from INI settings
      this.BASE_URL = this.iniSettings.getProperty(INI_BASE_URL);

    } catch (FileNotFoundException e) {
      this.logger
          .debug("myExperiment plugin INI file was not found, defaults will be used.");

    } catch (IOException e) {
      this.logger.error("Error on reading settings from INI file:\n" + e);
    }
  }

  // writes all plugin settings to the INI file
  private void storeSettings() {

    // === STORE THE SETTINGS ===
    try {
      this.fIniFileDir.mkdirs();
      FileOutputStream fIniOutputStream = new FileOutputStream(
          new java.io.File(this.fIniFileDir, this.INI_FILE_NAME));
      this.iniSettings.store(fIniOutputStream, "Test comment");
      fIniOutputStream.close();
    } catch (IOException e) {
      this.logger.error("Error while trying to store settings to INI file:\n"
          + e);
    }

  }

  public void storeHistoryAndSettings() {
    this.iniSettings.put(MyExperimentClient.INI_FAVOURITE_SEARCHES, Base64
        .encodeObject(MainComponent.MAIN_COMPONENT.getSearchTab()
            .getSearchFavouritesList()));
    this.iniSettings.put(MyExperimentClient.INI_SEARCH_HISTORY, Base64
        .encodeObject(MainComponent.MAIN_COMPONENT.getSearchTab()
            .getSearchHistory()));
    this.iniSettings.put(MyExperimentClient.INI_TAG_SEARCH_HISTORY, Base64
        .encodeObject(MainComponent.MAIN_COMPONENT.getTagBrowserTab()
            .getTagSearchHistory()));
    this.iniSettings.put(MyExperimentClient.INI_PREVIEWED_ITEMS_HISTORY, Base64
        .encodeObject(MainComponent.MAIN_COMPONENT.getPreviewBrowser()
            .getPreviewHistory()));
    this.iniSettings.put(MyExperimentClient.INI_DOWNLOADED_ITEMS_HISTORY,
        Base64.encodeObject(MainComponent.MAIN_COMPONENT.getHistoryBrowser()
            .getDownloadedItemsHistoryList()));
    this.iniSettings.put(MyExperimentClient.INI_OPENED_ITEMS_HISTORY, Base64
        .encodeObject(MainComponent.MAIN_COMPONENT.getHistoryBrowser()
            .getOpenedItemsHistoryList()));
    this.iniSettings.put(MyExperimentClient.INI_UPLOADED_ITEMS_HISTORY, Base64
        .encodeObject(MainComponent.MAIN_COMPONENT.getHistoryBrowser()
            .getUploadedItemsHistoryList()));
    this.iniSettings.put(MyExperimentClient.INI_COMMENTED_ITEMS_HISTORY, Base64
        .encodeObject(MainComponent.MAIN_COMPONENT.getHistoryBrowser()
            .getCommentedOnItemsHistoryList()));

    storeSettings();
  }
  
	private UsernamePassword getUserPass(String urlString) {
		try {
			URI userpassUrl = URI.create(urlString);
			final UsernamePassword userAndPass = credentialManager.getUsernameAndPasswordForService(userpassUrl, true, null);
			return userAndPass;
		} catch (CMException e) {
			throw new RuntimeException("Error in Taverna Credential Manager", e);
		}
	}

  public boolean doLogin() {

    // check if the stored credentials are valid
	  ServerResponse response = null;
    Document doc = null;
    try {
//    	credentialManager.getUsernameAndPasswordForService(new URI(this.BASE_URL), true, null);
    	response = this.doMyExperimentGET(this.BASE_URL + "/whoami.xml");
    } catch (Exception e) {
      this.logger
          .error("Error while attempting to verify login credentials from INI file:\n"
              + e);
    }

	
	if (response.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
		try {
			List<URI> toDelete = credentialManager.getServiceURIsForAllUsernameAndPasswordPairs();
			for (URI uri : toDelete) {
				if (uri.toASCIIString().startsWith(BASE_URL)) {
					credentialManager.deleteUsernameAndPasswordForService(uri);
				}
			}
//			credentialManager.resetAuthCache();
			doc = null;
		} catch (Exception e) {
			logger.error(e);
		}
	} else {
		doc = response.getResponseBody();
	}

    // verify outcomes
    if (doc == null) {
      // login credentials were invalid - revert to not logged in state and
      // disable autologin function;
      // stored credentials will be kept to allow the user to verify and edit
      // them
      // (login screen will be displayed as usual + an error message box will
      // appear)
      this.LOGGED_IN = false;
      this.AUTH_STRING = "";
      this.iniSettings.put(MyExperimentClient.INI_AUTO_LOGIN,
          new Boolean(false).toString());

      javax.swing.JOptionPane.showMessageDialog(null,
          "Your myExperiment login details appear to be incorrect.\n"
              + "Please check your details.");
      return false;
    } else {
  	  UsernamePassword userPass = getUserPass(this.BASE_URL + "/whoami.xml");
	  
      if (userPass == null) {
      	return false;
      }

      // set the system to the "logged in" state from INI file properties
      this.LOGGED_IN = true;
      this.AUTH_STRING = Base64.encodeBytes((userPass.getUsername() + ":" + userPass.getPasswordAsString())
          .getBytes());

      // login credentials were verified successfully; load current user
      String strCurrentUserURI = doc.getRootElement().getAttributeValue("uri");
      try {
        this.current_user = this.fetchCurrentUser(strCurrentUserURI);
        this.logger
            .debug("Logged in to myExperiment successfully with credentials that were loaded from INI file.");
        return true;
      } catch (Exception e) {
        // this is highly unlikely because the login credentials were validated
        // successfully just before this
        this.logger.error("Couldn't fetch user data from myExperiment ("
            + strCurrentUserURI
            + ")", e);
        return false;
      }
    }
  }

  // Simulates a "logout" action. Logging in and out in the plugin is only an
  // abstraction created for user convenience; it is a purely virtual concept,
  // because the
  // myExperiment API is completely stateless - hence, logging out simply
  // consists of "forgetting"
  // the authentication details and updating the state.
  public void doLogout() throws Exception {
    LOGGED_IN = false;
    AUTH_STRING = "";
  }

  /**
   * Generic method to execute GET requests to myExperiment server.
   * 
   * @param strURL
   *          The URL on myExperiment to issue GET request to.
   * @return An object containing XML Document with server's response body and a
   *         response code. Response body XML document might be null if there
   *         was an error or the user wasn't authorised to perform a certain
   *         action. Response code will always be set.
   * @throws Exception
   */
  public ServerResponse doMyExperimentGET(String strURL) throws Exception {
    // open server connection using provided URL (with no modifications to it)
    URL url = new URL(strURL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
    if (LOGGED_IN) {
      // if the user has "logged in", also add authentication details
      conn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);
    }

    // check server's response
    return (doMyExperimentReceiveServerResponse(conn, strURL, true));
  }

  /**
   * Generic method to execute GET requests to myExperiment server.
   * 
   * @param strURL
   *          The URL on myExperiment to POST to.
   * @param strXMLDataBody
   *          Body of the XML data to be POSTed to strURL.
   * @return An object containing XML Document with server's response body and a
   *         response code. Response body XML document might be null if there
   *         was an error or the user wasn't authorised to perform a certain
   *         action. Response code will always be set.
   * @throws Exception
   */
  public ServerResponse doMyExperimentPOST(String strURL, String strXMLDataBody)
      throws Exception {
    // POSTing to myExperiment is only allowed for authorised users
    if (!LOGGED_IN) return (null);

    // open server connection using provided URL (with no modifications to it)
    URL url = new URL(strURL);
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

    // "tune" the connection
    urlConn.setRequestMethod((strURL.contains(DO_PUT) ? "PUT" : "POST"));
    strURL = strURL.replace(DO_PUT, "");
    urlConn.setDoOutput(true);
    urlConn.setRequestProperty("Content-Type", "application/xml");
    urlConn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
    urlConn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);
    // the last line wouldn't be executed if the user wasn't logged in (see
    // above code), so safe to run

    // prepare and PUT/POST XML data
    String strPOSTContent = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
        + strXMLDataBody;
    OutputStreamWriter out = new OutputStreamWriter(urlConn.getOutputStream());
    out.write(strPOSTContent);
    out.close();

    // check server's response
    return (doMyExperimentReceiveServerResponse(urlConn, strURL, false));
  }

  /**
   * Generic method to execute DELETE requests to myExperiment server. This is
   * only to be called when a user is logged in.
   * 
   * @param strURL
   *          The URL on myExperiment to direct DELETE request to.
   * @return An object containing XML Document with server's response body and a
   *         response code. Response body XML document might be null if there
   *         was an error or the user wasn't authorised to perform a certain
   *         action. Response code will always be set.
   * @throws Exception
   */
  public ServerResponse doMyExperimentDELETE(String strURL) throws Exception {
    // open server connection using provided URL (with no modifications to it)
    URL url = new URL(strURL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

    // "tune" the connection
    conn.setRequestMethod("DELETE");
    conn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
    conn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);

    // check server's response
    return (doMyExperimentReceiveServerResponse(conn, strURL, true));
  }

  /**
   * A common method for retrieving myExperiment server's response for both GET
   * and POST requests.
   * 
   * @param conn
   *          Instance of the established URL connection to poll for server's
   *          response.
   * @param strURL
   *          The URL on myExperiment with which the connection is established.
   * @param bIsGetRequest
   *          Flag for identifying type of the request. True when the current
   *          connection executes GET request; false when it executes a POST
   *          request.
   * @return An object containing XML Document with server's response body and a
   *         response code. Response body XML document might be null if there
   *         was an error or the user wasn't authorised to perform a certain
   *         action. Response code will always be set.
   */
  private ServerResponse doMyExperimentReceiveServerResponse(
      HttpURLConnection conn, String strURL, boolean bIsGETRequest)
      throws Exception {
    int iResponseCode = conn.getResponseCode();

    switch (iResponseCode) {
      case HttpURLConnection.HTTP_OK:
        // data retrieval was successful - parse the response XML and return it
        // along with response code
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn
            .getInputStream()));
        Document doc = new SAXBuilder().build(new InputSource(reader));
        reader.close();

        return (new ServerResponse(iResponseCode, doc));

      case HttpURLConnection.HTTP_BAD_REQUEST:
        // this was a bad XML request - need full XML response to retrieve the
        // error message from it;
        // Java throws IOException if getInputStream() is used when non HTTP_OK
        // response code was received -
        // hence can use getErrorStream() straight away to fetch the error
        // document
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(
            conn.getErrorStream()));
        Document errorDoc = new SAXBuilder()
            .build(new InputSource(errorReader));
        errorReader.close();

        return (new ServerResponse(iResponseCode, errorDoc));

      case HttpURLConnection.HTTP_UNAUTHORIZED:
        // this content is not authorised for current user
        return (new ServerResponse(iResponseCode, null));

      default:
        // unexpected response code - raise an exception
        throw new IOException("Received unexpected HTTP response code ("
            + conn.getResponseCode() + ") while "
            + (bIsGETRequest ? "fetching data at " : "posting data to ")
            + strURL);
    }
  }

  // a method to fetch a user instance with full details (including avatar
  // image)
  public User fetchCurrentUser(String uri) {
    // fetch user data
    User user = null;
    try {
      Document doc = this.getResource(Resource.USER, uri,
          Resource.REQUEST_FULL_PREVIEW);
      user = User.buildFromXML(doc, logger);
    } catch (Exception ex) {
      logger.error("Failed to fetch user data from myExperiment (" + uri
          + "); exception:\n" + ex);
    }

    // fetch the avatar
    try {
      if (user.getAvatarURI() == null) {
        ImageIcon icon = new ImageIcon(user.getAvatarResource());
        user.setAvatar(icon);
      } else {
        Document doc = this.doMyExperimentGET(user.getAvatarURI())
            .getResponseBody();
        user.setAvatar(doc);
      }
    } catch (Exception ex) {
      logger.error("Failed to fetch user's avatar from myExperiment ("
          + user.getAvatarURI() + "); exception:\n" + ex);
    }

    return (user);
  }

  /**
   * Fetches resource data and returns an XML document containing it.
   * 
   * @param iResourceType
   *          Type of the resource for which the XML data is to be fetched. This
   *          implies which elements are required to be selected.
   * @param strURI
   *          URI of the resource in myExperiment API.
   * @param iRequestType
   *          Determines the level of detail of data to be fetched from the API;
   *          constants for using in this field are defined in Resource class.
   */
  public Document getResource(int iResourceType, String strURI, int iRequestType)
      throws Exception {
    if (iRequestType == Resource.REQUEST_ALL_DATA) {
      // it doesn't matter what kind of resource this is if all available data
      // is requested anyway
      strURI += "&all_elements=yes";
    } else {
      // only required metadata is to be fetched; this depends on the type of
      // the resource
      switch (iResourceType) {
        case Resource.WORKFLOW:
          strURI += "&elements="
              + Workflow.getRequiredAPIElements(iRequestType);
          break;
        case Resource.FILE:
          strURI += "&elements=" + File.getRequiredAPIElements(iRequestType);
          break;
        case Resource.PACK:
          strURI += "&elements=" + Pack.getRequiredAPIElements(iRequestType);
          break;
        case Resource.PACK_INTERNAL_ITEM:
          strURI += "&all_elements=yes"; // TODO determine which are required
          // elements
          break;
        case Resource.PACK_EXTERNAL_ITEM:
          strURI += "&all_elements=yes"; // TODO determine which are required
          // elements
          break;
        case Resource.USER:
          strURI += "&elements=" + User.getRequiredAPIElements(iRequestType);
          break;
        case Resource.GROUP:
          strURI += "&elements=" + Group.getRequiredAPIElements(iRequestType);
          break;
        case Resource.TAG:
          // this should set no elements, because default is desired at the
          // moment -
          // but even having "&elements=" with and empty string at the end will
          // still
          // retrieve default fields from the API
          strURI += "&elements=" + Tag.getRequiredAPIElements(iRequestType);
          break;
        case Resource.COMMENT:
          // this should set no elements, because default is desired at the
          // moment - but even having "&elements=" with and empty string at the
          // end will
          // still retrieve default fields from the API
          strURI += "&elements=" + Comment.getRequiredAPIElements(iRequestType);
          break;
      }
    }

    return (this.doMyExperimentGET(strURI).getResponseBody());
  }

  /**
   * Fetches workflow data from myExperiment.
   * 
   * @param strWorkflowURI
   *          URI of the workflow to be opened.
   * @return Workflow instance containing only workflow data and content type.
   */
  public Workflow fetchWorkflowBinary(String strWorkflowURI) throws Exception {
    // fetch workflows data
    Document doc = this.getResource(Resource.WORKFLOW, strWorkflowURI,
        Resource.REQUEST_WORKFLOW_CONTENT_ONLY);

    // verify that the type of the workflow data is correct
    Element root = doc.getRootElement();
    Workflow w = new Workflow();
    w.setVisibleType(root.getChildText("type"));
    w.setContentType(root.getChildText("content-type"));

    if (!w.isTavernaWorkflow()) { throw new Exception(
        "Unsupported workflow type. Details:\nWorkflow type: "
            + w.getVisibleType() + "\nMime type: " + w.getContentType()); }

    // check that content encoding is correct
    String strEncoding = root.getChild("content").getAttributeValue("encoding");
    String strDataFormat = root.getChild("content").getAttributeValue("type");
    if (!strEncoding.toLowerCase().equals("base64")
        || !strDataFormat.toLowerCase().equals("binary")) { throw new Exception(
        "Unsupported workflow data format. Details:\nContent encoding: "
            + strEncoding + "\nFormat: " + strDataFormat); }

    // all checks seem to be fine, decode workflow data
    byte[] arrWorkflowData = Base64.decode(root.getChildText("content"));
    w.setContent(arrWorkflowData);

    return (w);
  }

  @SuppressWarnings("unchecked")
  public List<Workflow> getExampleWorkflows() {
    List<Workflow> workflows = new ArrayList<Workflow>();

    try {
      String strExampleWorkflowsPackUrl = this.BASE_URL + "/pack.xml?id="
          + EXAMPLE_WORKFLOWS_PACK_ID + "&elements=internal-pack-items";
      Document doc = this.doMyExperimentGET(strExampleWorkflowsPackUrl)
          .getResponseBody();

      if (doc != null) {
        List<Element> allInternalItems = doc.getRootElement().getChild(
            "internal-pack-items").getChildren("workflow");
        for (Element e : allInternalItems) {
          String itemUri = e.getAttributeValue("uri");
          Document itemDoc = this.doMyExperimentGET(itemUri).getResponseBody();
          String workflowUri = itemDoc.getRootElement().getChild("item")
              .getChild("workflow").getAttributeValue("uri");
          Document docCurWorkflow = this.getResource(Resource.WORKFLOW,
              workflowUri, Resource.REQUEST_FULL_LISTING);
          workflows.add(Workflow.buildFromXML(docCurWorkflow, this.logger));
        }
      }
    } catch (Exception e) {
      this.logger.error("Failed to retrieve example workflows", e);
    }

    logger.debug(workflows.size()
        + " example workflows retrieved from myExperiment");

    return (workflows);
  }

  @SuppressWarnings("unchecked")
  public TagCloud getGeneralTagCloud(int size) {
    TagCloud tcCloud = new TagCloud();

    try {
      // assemble tag cloud URL and fetch the XML document
      String strTagCloudURL = BASE_URL + "/tag-cloud.xml?num="
          + (size > 0 ? ("" + size) : "all");
      Document doc = this.doMyExperimentGET(strTagCloudURL).getResponseBody();

      // process all tags and add them to the cloud
      if (doc != null) {
        List<Element> nodes = doc.getRootElement().getChildren("tag");
        for (Element e : nodes) {
          Tag t = new Tag();
          t.setTitle(e.getText());
          t.setTagName(e.getText());
          t.setResource(e.getAttributeValue("resource"));
          t.setURI(e.getAttributeValue("uri"));
          t.setCount(Integer.parseInt(e.getAttributeValue("count")));

          tcCloud.getTags().add(t);
        }
      }
    } catch (Exception e) {
      this.logger.error("ERROR: Failed to get tag cloud.\n", e);
    }

    logger.debug("Tag cloud retrieval successful; fetched "
        + tcCloud.getTags().size() + " tags from myExperiment");
    return (tcCloud);
  }

  public TagCloud getUserTagCloud(User user, int size) {
    TagCloud tcCloud = new TagCloud();

    // iterate through all tags that the user has applied;
    // fetch the title and the number of times that this tag
    // was applied across myExperiment (e.g. overall popularity)
    try {
      // update user tags first (this happens concurrently with the other
      // threads
      // during the load time, hence needs to be synchronised properly)
      synchronized (user.getTags()) {
        user.getTags().clear();
        Document doc = this.getResource(Resource.USER, user.getURI(),
            Resource.REQUEST_USER_APPLIED_TAGS_ONLY);
        Iterator<Element> iNewUserTags = doc.getRootElement().getChild(
            "tags-applied").getChildren().iterator();
        Util.getResourceCollectionFromXMLIterator(iNewUserTags, user.getTags());
      }

      // fetch additional required data about the tags
      Iterator<HashMap<String, String>> iTagsResourcesHashMaps = user.getTags()
          .iterator();
      while (iTagsResourcesHashMaps.hasNext()) {
        // get the tag object uri in myExperiment API
        String strCurTagURI = iTagsResourcesHashMaps.next().get("uri");

        // fetch tag data from myExperiment (namely, number of times that this
        // tag was applied)
        Document doc = this.doMyExperimentGET(strCurTagURI).getResponseBody();
        Element root = doc.getRootElement();

        // create the tag
        Tag t = new Tag();
        t.setTagName(root.getChild("name").getText());
        t.setCount(Integer.parseInt(root.getChild("count").getText()));

        tcCloud.getTags().add(t);
      }

      // a little preprocessing before tag selection - if "size" is set to 0, -1
      // or any negative number, assume the request is for ALL user tags
      if (size <= 0) size = tcCloud.getTags().size();

      // sort the collection by popularity..
      Comparator<Tag> byPopularity = new Tag.ReversePopularityComparator();
      Collections.sort(tcCloud.getTags(), byPopularity);

      // ..take top "size" elements
      int iSelectedTags = 0;
      List<Tag> tagListOfRequiredSize = new ArrayList<Tag>();
      Iterator<Tag> iTags = tcCloud.getTags().iterator();
      while (iTags.hasNext() && iSelectedTags < size) {
        tagListOfRequiredSize.add(iTags.next());
        iSelectedTags++;
      }

      // purge the original tag collection; add only selected tags to it;
      // then sort back in alphabetical order again
      tcCloud.getTags().clear();
      tcCloud.getTags().addAll(tagListOfRequiredSize);
      Comparator<Tag> byAlphabet = new Tag.AlphanumericComparator();
      Collections.sort(tcCloud.getTags(), byAlphabet);
    } catch (Exception e) {
      logger.error("Failed midway through fetching user tags for user ID = "
          + user.getID() + "\n" + e);
    }

    return (tcCloud);
  }

  /**
   * A helper to fetch workflows, files or packs of a specific user. This will
   * only make *one* request to the API, therefore it's faster than getting all
   * the items one by one.
   * 
   * @param user
   *          User instance for which the items are to be fetched.
   * @param iResourceType
   *          One of Resource.WORKFLOW, Resource.FILE, Resource.PACK
   * @param iRequestType
   *          Type of the request - i.e. amount of data to fetch. One of
   *          Resource.REQUEST_SHORT_LISTING, Resource.REQUEST_FULL_LISTING,
   *          Resource.REQUEST_FULL_PREVIEW, Resource.REQUEST_ALL_DATA.
   * @return An XML document containing data about all items in the amount that
   *         was specified.
   */
  public Document getUserContributions(User user, int iResourceType,
      int iRequestType, int page) {
    Document doc = null;
    String strURL = BASE_URL;
    String strElements = "&elements=";

    try {
      // determine query parameters
      switch (iResourceType) {
        case Resource.WORKFLOW:
          strURL += "/workflows.xml?uploader=";
          strElements += Workflow.getRequiredAPIElements(iRequestType);
          break;
        case Resource.FILE:
          strURL += "/files.xml?uploader=";
          strElements += File.getRequiredAPIElements(iRequestType);
          break;
        case Resource.PACK:
          strURL += "/packs.xml?owner=";
          strElements += Workflow.getRequiredAPIElements(iRequestType);
          break;
      }
      
      if (page != 0) {
    	  strElements += "&num=100&page=" + page;
      }

      // create final query URL and retrieve data
      strURL += MyExperimentClient.urlEncodeQuery(user.getResource())
          + strElements;
      doc = this.doMyExperimentGET(strURL).getResponseBody();
    } catch (Exception e) {
      logger.error("ERROR: Failed to fetch user's contributions.");
    }

    return (doc);
  }

  /**
   * Queries myExperiment API for all items that are tagged with particular
   * type.
   * 
   * @param strTag
   *          The tag to search for. This will be URL encoded before submitting
   *          the query.
   * @return XML document containing search results.
   */
  public Document searchByTag(String strTag) {
    Document doc = null;

    try {
      String strUrlEncodedTag = MyExperimentClient.urlEncodeQuery(strTag);
      doc = this.doMyExperimentGET(
          BASE_URL + "/tagged.xml?tag=" + strUrlEncodedTag
              + Util.composeAPIQueryElements(null)).getResponseBody();
    } catch (Exception e) {
      logger
          .error("ERROR: Failed to fetch tagged items from myExperiment. Query tag was '"
              + strTag + "'\n" + e);
    }

    return (doc);
  }

  /**
   * Converts a tag list into tag cloud data by fetching tag application count
   * for each instance in the list.
   * 
   * @param tags
   *          Tag list to work on.
   */
  public void convertTagListIntoTagCloudData(List<Tag> tags) {
    try {
      Document doc = null;

      for (Tag t : tags) {
        doc = this.getResource(Resource.TAG, t.getURI(),
            Resource.REQUEST_ALL_DATA);
        Element rootElement = doc.getRootElement();
        t.setCount(Integer.parseInt(rootElement.getChild("count").getText()));
      }
    } catch (Exception e) {
      logger
          .error("Failed while getting tag application counts when turning tag list into tag cloud data", e);
    }
  }

  /**
   * Fetches the data about user's favourite items and updates the provided user
   * instance with the latest data.
   */
  public void updateUserFavourites(User user) {
    // fetch and update favourites data
    try {
      Document doc = this.getResource(Resource.USER, user.getURI(),
          Resource.REQUEST_USER_FAVOURITES_ONLY);
      List<Resource> newUserFavouritesList = Util.retrieveUserFavourites(doc
          .getRootElement());

      user.getFavourites().clear();
      user.getFavourites().addAll(newUserFavouritesList);
    } catch (Exception ex) {
      logger
          .error("Failed to fetch favourites data from myExperiment for a user (URI: "
              + user.getURI() + "); exception:\n" + ex);
      JOptionPane
          .showMessageDialog(
              null,
              "Couldn't synchronise data about your favourite items with myExperiment.\n"
                  + "You might not be able to add / remove other items to your favourites and.\n"
                  + "Please refresh your profile data manually by clicking 'Refresh' button in 'My Stuff' tab.",
              "myExperiment Plugin - Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * For each comment in the list fetches the user which made the comment, the
   * date when it was made, etc.
   */
  public void updateCommentListWithExtraData(List<Comment> comments) {
    try {
      Document doc = null;

      for (Comment c : comments) {
        doc = this.getResource(Resource.COMMENT, c.getURI(),
            Resource.REQUEST_ALL_DATA);
        Element rootElement = doc.getRootElement();

        Element userElement = rootElement.getChild("author");
        User u = new User();
        u.setTitle(userElement.getText());
        u.setName(userElement.getText());
        u.setResource(userElement.getAttributeValue("resource"));
        u.setURI(userElement.getAttributeValue("uri"));
        c.setUser(u);

        String createdAt = rootElement.getChildText("created-at");
        if (createdAt != null && !createdAt.equals("")) {
          c.setCreatedAt(MyExperimentClient.parseDate(createdAt));
        }
      }
    } catch (Exception e) {
      logger.error("Failed while updating comment list for preview", e);
    }
  }

  public Document searchByQuery(QuerySearchInstance searchQuery) {
    Document doc = null;
    String strSearchURL = null;

    try {
      // this will URL encode the query so that it can be directly inserted into
      // the search URL
      String strUrlEncodedQuery = MyExperimentClient.urlEncodeQuery(searchQuery
          .getSearchQuery());

      // determine which types to include in the search URL
      // (if none are added, any types will be searched for)
      String strSearchFor = "";
      if (searchQuery.getSearchWorkflows()) strSearchFor += "workflow";
      if (searchQuery.getSearchFiles()) strSearchFor += ",file";
      if (searchQuery.getSearchPacks()) strSearchFor += ",pack";
      if (searchQuery.getSearchUsers()) strSearchFor += ",user";
      if (searchQuery.getSearchGroups()) strSearchFor += ",group";
      if (strSearchFor.length() != 0) {
        // some types were added;
        // remove leading comma (if it exists)
        if (strSearchFor.startsWith(","))
          strSearchFor = strSearchFor.replaceFirst(",", "");

        // add parameter prefix
        strSearchFor = "type=" + strSearchFor;
      }

      // assemble all search parameters together
      // (we will definitely have the number of results)
      String strParameters = strSearchFor;
      if (strParameters.length() != 0) strParameters += "&";
      strParameters += "num=" + searchQuery.getResultCountLimit();
      strParameters += Util.composeAPIQueryElements(searchQuery);

      // generate the search URL
      strSearchURL = BASE_URL + "/search.xml?query=" + strUrlEncodedQuery + "&"
          + strParameters;

      // DEBUG
      // javax.swing.JOptionPane.showMessageDialog(null, strSearchURL);

      // execute the search on myExperiment
      doc = this.doMyExperimentGET(strSearchURL).getResponseBody();
    } catch (Exception e) {
      logger
          .error("ERROR: Failed to run search on myExperiment. Query URL was'"
              + strSearchURL + "'\n" + e);
    }

    return (doc);
  }

  public ServerResponse postComment(Resource resource, String strComment) {
    try {
      String strCommentData = "<comment><subject resource=\""
          + resource.getResource() + "\"/><comment>" + strComment
          + "</comment></comment>";
      ServerResponse response = this.doMyExperimentPOST(BASE_URL
          + "/comment.xml", strCommentData);

      if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
        // XML response should contain the new comment that was posted
        Comment cNew = Comment.buildFromXML(response.getResponseBody(), logger);

        // this resource should be commentable on as the comment was posted
        resource.getComments().add(cNew);
      }

      // will return the whole response object so that the application could
      // decide
      // on the next steps
      return (response);
    } catch (Exception e) {
      logger.error("Failed while trying to post a comment for "
          + resource.getURI() + "\n" + e);
      return (new ServerResponse(ServerResponse.LOCAL_FAILURE, null));
    }
  }

  private String prepareWorkflowPostContent(String workflowContent,
      String title, String description, String license, String sharing) {
    String strWorkflowData = "<workflow>";

    if (title.length() > 0) strWorkflowData += "<title>" + title + "</title>";

    if (description.length() > 0)
      strWorkflowData += "<description>" + description + "</description>";

    if (license.length() > 0)
      strWorkflowData += "<license-type>" + license + "</license-type>";

    if (sharing.length() > 0) {
      if (sharing.contains("private")) strWorkflowData += "<permissions />";
      else {
        strWorkflowData += "<permissions><permission>"
            + "<category>public</category>";
        if (sharing.contains("view") || sharing.contains("download"))
          strWorkflowData += "<privilege type=\"view\" />";
        if (sharing.contains("download"))
          strWorkflowData += "<privilege type=\"download\" />";
        strWorkflowData += "</permission></permissions>";
      }
    }

    String encodedWorkflow = "";

    // check the format of the workflow
    String scuflSchemaDef = "xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"";
    String t2flowSchemaDef = "xmlns=\"http://taverna.sf.net/2008/xml/t2flow\"";

    if (workflowContent.length() > 0) {
      String contentType;
      if (workflowContent.contains(scuflSchemaDef)) contentType = "application/vnd.taverna.scufl+xml";
      else if (workflowContent.contains(t2flowSchemaDef)) contentType = "application/vnd.taverna.t2flow+xml";
      else contentType = "";

      encodedWorkflow += "<content-type>" + contentType + "</content-type>"
          + "<content encoding=\"base64\" type=\"binary\">"
          + Base64.encodeBytes(workflowContent.getBytes()) + "</content>";
      strWorkflowData += encodedWorkflow;
    }

    strWorkflowData += "</workflow>";

    return (strWorkflowData);
  }

  private void afterMyExperimentPost(ServerResponse response) {
    // if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
    // // XML response should contain the new workflow that was posted
    // Workflow newWorkflow = Workflow.buildFromXML(response.getResponseBody(),
    // logger);
    //
    // System.out.println("* *** *** *** *" + response.getResponseBody()
    // + "* *** *** *** *");
    // }
  }

  public ServerResponse postWorkflow(String workflowContent, String title,
      String description, String license, String sharing) {
    try {
      String strWorkflowData = prepareWorkflowPostContent(workflowContent,
          title, description, license, sharing);

      ServerResponse response = this.doMyExperimentPOST(BASE_URL
          + "/workflow.xml", strWorkflowData);

      afterMyExperimentPost(response);
      // will return the whole response object so that the application could
      // decide on the next steps
      return (response);
    } catch (Exception e) {
      logger.error("Failed while trying to upload the workflow");
      return (new ServerResponse(ServerResponse.LOCAL_FAILURE, null));
    }
  }

  public ServerResponse updateWorkflowVersionOrMetadata(Resource resource,
      String workflowContent, String title, String description, String license,
      String sharing) {
    try {
      String strWorkflowData = prepareWorkflowPostContent(workflowContent,
          title, description, license, sharing);

      // if strWorkflowFileContent is empty; include version info for PUT (since
      // workflow is being updated)
      // a POST would require data, hence strWorkflowFileContent would not be
      // empty
      String doUpdateStatus = (workflowContent.length() == 0 ? DO_PUT : "");

      ServerResponse response = this.doMyExperimentPOST(BASE_URL
          + "/workflow.xml?id=" + resource.getID() + doUpdateStatus,
          strWorkflowData);

      afterMyExperimentPost(response);
      // will return the whole response object so that the application could
      // decide on the next steps
      return (response);
    } catch (Exception e) {
      logger.error("Failed while trying to upload the workflow");
      return (new ServerResponse(ServerResponse.LOCAL_FAILURE, null));
    }
  }

  public ServerResponse addFavourite(Resource resource) {
    try {
      String strData = "<favourite><object resource=\""
          + resource.getResource() + "\"/></favourite>";
      ServerResponse response = this.doMyExperimentPOST(BASE_URL
          + "/favourite.xml", strData);

      // will return full server response
      return (response);
    } catch (Exception e) {
      logger.error("Failed while trying to add an item (" + resource.getURI()
          + ") to favourites4", e);
      return (new ServerResponse(ServerResponse.LOCAL_FAILURE, null));
    }
  }

  public ServerResponse deleteFavourite(Resource resource) {
    try {
      // deleting a favourite is a two-step process - first need to retrieve the
      // the
      // actual "favourite" object by current user's URL and favourited item's
      // URL
      String strGetFavouriteObjectURL = BASE_URL
          + "/favourites.xml?user="
          + MyExperimentClient.urlEncodeQuery(this.getCurrentUser()
              .getResource()) + "&object="
          + MyExperimentClient.urlEncodeQuery(resource.getResource());
      ServerResponse response = this
          .doMyExperimentGET(strGetFavouriteObjectURL);

      // now retrieve this object's URI from server's response
      Element root = response.getResponseBody().getRootElement();
      String strFavouriteURI = root.getChild("favourite").getAttributeValue(
          "uri");

      // finally, delete the found object
      response = this.doMyExperimentDELETE(strFavouriteURI);

      // will return full server response
      return (response);
    } catch (Exception e) {
      logger.error("Failed while trying to remove an item ("
          + resource.getURI() + ") from favourites\n" + e);
      return (new ServerResponse(ServerResponse.LOCAL_FAILURE, null));
    }
  }
  
  public static Date parseDate(String date) {
	  Date result = null;
	  try {
		  result = OLD_DATE_FORMATTER.parse(date);
	  } catch (ParseException e) {
		  try {
			  result = OLD_SHORT_DATE_FORMATTER.parse(date);
		  }
		  catch (ParseException e1) {
			  try {
				  result = NEW_DATE_FORMATTER.parse(date);
			  } catch (ParseException e2) {
				  result = null;
			  }
		  }
	  }
	  return result;
  }
  
  public static String formatDate(Date date) {
	  return NEW_DATE_FORMATTER.format(date);
  }

  /**
   * Prepares the string to serve as a part of url query to the server.
   * 
   * @param query
   *          The string that needs URL encoding.
   * @return URL encoded string that can be inserted into the request URL.
   */
  private static String urlEncodeQuery(String query) {
    String strRes = "";

    try {
      strRes = URLEncoder.encode(query, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      // do nothing
    }

    return (strRes);
  }
}
