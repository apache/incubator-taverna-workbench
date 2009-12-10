/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.ui.perspectives.myexperiment.MainComponent;
import net.sf.taverna.t2.ui.perspectives.myexperiment.MyExperimentPerspective;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.SearchEngine.QuerySearchInstance;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

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
  private static final int EXAMPLE_WORKFLOWS_PACK_ID = 103;

  public static final String INI_BASE_URL = "my_experiment_base_url";
  public static final String INI_LOGIN = "login";
  public static final String INI_PASSWORD = "password";
  public static final String INI_REMEMBER_ME = "remember_me";
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

  // universal date formatter
  private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
  private static final DateFormat SHORT_DATE_FORMATTER = new SimpleDateFormat("HH:mm 'on' dd/MM/yyyy");

  // SETTINGS
  private String BASE_URL; // myExperiment base URL to use
  private java.io.File fIniFileDir; // a folder, where the INI file will be
  // stored
  private Properties iniSettings; // settings that are read/stored from/to INI
  // file

  // the logger
  private Logger logger;

  // authentication settings (and the current user)
  private boolean LOGGED_IN = false;
  private String AUTH_STRING = "";
  private User current_user = null;

  // default constructor
  public MyExperimentClient() {
  }

  public MyExperimentClient(Logger logger) {
	this();

	this.logger = logger;

	// === Load INI settings ===
	// but loading settings from INI file, determine what folder is to be used for INI file
	if (Util.isRunningInTaverna()) {
	  // running inside Taverna - use its folder to place the config file
	  this.fIniFileDir = new java.io.File(ApplicationRuntime.getInstance().getApplicationHomeDir(), "conf");
	} else {
	  // running outside Taverna, place config file into the user's home
	  // directory
	  this.fIniFileDir = new java.io.File(System.getProperty("user.home"), ".Taverna2-myExperiment Plugin");
	}

	// load preferences if the INI file exists
	this.iniSettings = new Properties();
	this.loadSettings();

	// === Check if defaults should be applied to override not sensible settings from INI file ===
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
  public void loadSettings() {
	try {
	  // === READ SETTINGS ===
	  FileInputStream fIniInputStream = new FileInputStream(new java.io.File(this.fIniFileDir, this.INI_FILE_NAME));
	  this.iniSettings.load(fIniInputStream);
	  fIniInputStream.close();

	  // set BASE_URL if from INI settings
	  this.BASE_URL = this.iniSettings.getProperty(INI_BASE_URL);

	  // === DECRYPT LOGIN AND PASSWORD ===
	  // settings are now read, decrypt login and password before proceeding -
	  // these are encrypted, then Base64 encoded
	  Object oEnctryptedLogin = this.iniSettings.get(MyExperimentClient.INI_LOGIN);
	  String strEncryptedLogin = (oEnctryptedLogin == null ? "" : oEnctryptedLogin.toString());
	  String strLogin = new String(Util.decrypt(new String(Base64.decode(strEncryptedLogin))));

	  Object oEncryptedPassword = this.iniSettings.get(MyExperimentClient.INI_PASSWORD);
	  String strEncryptedPassword = (oEncryptedPassword == null ? "" : oEncryptedPassword.toString());
	  String strPassword = new String(Util.decrypt(new String(Base64.decode(strEncryptedPassword))));

	  this.iniSettings.put(MyExperimentClient.INI_LOGIN, strLogin);
	  this.iniSettings.put(MyExperimentClient.INI_PASSWORD, strPassword);

	  // for security, nullify the variables
	  strLogin = null;
	  strPassword = null;
	} catch (FileNotFoundException e) {
	  this.logger.debug("myExperiment plugin INI file was not found, defaults will be used.");

	  // make sure that in this case login and password are still set in the
	  // "read" settings
	  // (just putting empty strings as values)
	  this.iniSettings.put(MyExperimentClient.INI_LOGIN, "");
	  this.iniSettings.put(MyExperimentClient.INI_PASSWORD, "");
	} catch (IOException e) {
	  this.logger.error("Error on reading settings from INI file:\n" + e);
	}
  }

  // writes all plugin settings to the INI file
  public void storeSettings() {
	// === ENCRYPT LOGIN AND PASSWORD ===
	// it's important to do this before writing these values into the INI file
	String strLogin = this.iniSettings.get(MyExperimentClient.INI_LOGIN).toString();
	String strPass = this.iniSettings.get(MyExperimentClient.INI_PASSWORD).toString();
	this.iniSettings.put(MyExperimentClient.INI_LOGIN, Base64.encodeBytes(Util.encrypt(strLogin)));
	this.iniSettings.put(MyExperimentClient.INI_PASSWORD, Base64.encodeBytes(Util.encrypt(strPass)));

	// === STORE THE SETTINGS ===
	try {
	  this.fIniFileDir.mkdirs();
	  FileOutputStream fIniOutputStream = new FileOutputStream(new java.io.File(this.fIniFileDir, this.INI_FILE_NAME));
	  this.iniSettings.store(fIniOutputStream, "Test comment");
	  fIniOutputStream.close();
	} catch (IOException e) {
	  this.logger.error("Error while trying to store settings to INI file:\n"
		  + e);
	}

	// === REVERT TO UNENCRYPTED VALUES ===
	// (as the storeSettings() can be called multiple times during the run-time
	// of the program,
	// it is crucial to restore the unencrypted values after write operation is
	// completed)
	this.iniSettings.put(MyExperimentClient.INI_LOGIN, strLogin);
	this.iniSettings.put(MyExperimentClient.INI_PASSWORD, strPass);
  }

  public void storeHistoryAndSettings() {
	this.iniSettings.put(MyExperimentClient.INI_FAVOURITE_SEARCHES, Base64.encodeObject(MainComponent.MAIN_COMPONENT.getSearchTab().getSearchFavouritesList()));
	this.iniSettings.put(MyExperimentClient.INI_SEARCH_HISTORY, Base64.encodeObject(MainComponent.MAIN_COMPONENT.getSearchTab().getSearchHistory()));
	this.iniSettings.put(MyExperimentClient.INI_TAG_SEARCH_HISTORY, Base64.encodeObject(MainComponent.MAIN_COMPONENT.getTagBrowserTab().getTagSearchHistory()));
	this.iniSettings.put(MyExperimentClient.INI_PREVIEWED_ITEMS_HISTORY, Base64.encodeObject(MainComponent.MAIN_COMPONENT.getPreviewBrowser().getPreviewHistory()));
	this.iniSettings.put(MyExperimentClient.INI_DOWNLOADED_ITEMS_HISTORY, Base64.encodeObject(MainComponent.MAIN_COMPONENT.getHistoryBrowser().getDownloadedItemsHistoryList()));
	this.iniSettings.put(MyExperimentClient.INI_OPENED_ITEMS_HISTORY, Base64.encodeObject(MainComponent.MAIN_COMPONENT.getHistoryBrowser().getOpenedItemsHistoryList()));
	this.iniSettings.put(MyExperimentClient.INI_UPLOADED_ITEMS_HISTORY, Base64.encodeObject(MainComponent.MAIN_COMPONENT.getHistoryBrowser().getUploadedItemsHistoryList()));
	this.iniSettings.put(MyExperimentClient.INI_COMMENTED_ITEMS_HISTORY, Base64.encodeObject(MainComponent.MAIN_COMPONENT.getHistoryBrowser().getCommentedOnItemsHistoryList()));

	storeSettings();
  }

  // Simulates a "login" action by verifying that login and password are correct
  // and storing the "logged in" state
  // if successful. Stored authentication details will be submitted with all
  // further server requests, hence
  // appearing as a real logging in to the user.
  public boolean doLogin(String strLogin, String strPassword) throws Exception {
	// Base64 encode login and password - then store these in an instance
	// variable
	AUTH_STRING = Base64.encodeBytes((strLogin + ":" + strPassword).getBytes());

	// open the connection and add authentication data to the request
	URL url = new URL(BASE_URL + "/whoami.xml");
	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	conn.setRequestProperty("User-Agent", PLUGIN_USER_AGENT);
	conn.setRequestProperty("Authorization", "Basic " + AUTH_STRING);

	// check server's response
	if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	  // authentication was successful, store success state
	  // (NB! This needs to go before further calls to myExperiment API -
	  // otherwise it will not be possible to fetch even the avatar
	  // of current user because of authorisation.)
	  LOGGED_IN = true;

	  // can fetch the 'current user' - get current user URI from current
	  // response
	  BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	  Document doc = new SAXBuilder().build(new InputSource(reader));
	  reader.close();
	  String currentUserURI = doc.getRootElement().getAttributeValue("uri");

	  // fetch the actual current user data and build instance from that
	  this.current_user = this.fetchCurrentUser(currentUserURI);

	  // return success state
	  return (true);
	} else if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
	  // authentication failed with expected response code
	  return (false);
	} else {
	  // unexpected response code - raise an exception
	  throw new IOException("Received unexpected HTTP response code ("
		  + conn.getResponseCode() + ") while testing login credentials at "
		  + BASE_URL + "/whoami.xml");
	}
  }

  public void doLoginFromStoredCredentials() {
	// check that some non-null values exist for login / password at all
	Object oLogin = this.iniSettings.get(MyExperimentClient.INI_LOGIN);
	Object oPassword = this.iniSettings.get(MyExperimentClient.INI_PASSWORD);
	if (oLogin == null || oPassword == null)
	  return;

	// don't do auto-login if login or password is blank
	String strLogin = oLogin.toString();
	String strPassword = oPassword.toString();
	if (strLogin.length() == 0 || strPassword.length() == 0)
	  return;

	// set the system to the "logged in" state from INI file properties
	this.LOGGED_IN = true;
	this.AUTH_STRING = Base64.encodeBytes((strLogin + ":" + strPassword).getBytes());

	// check if the stored credentials are valid
	Document doc = null;
	try {
	  doc = this.doMyExperimentGET(this.BASE_URL + "/whoami.xml").getResponseBody();
	} catch (Exception e) {
	  this.logger.error("Error while attempting to verify login credentials from INI file:\n"
		  + e);
	}

	// verify outcomes
	if (doc == null) {
	  // login credentials were invalid - revert to not logged in state and disable autologin function;
	  // stored credentials will be kept to allow the user to verify and edit them
	  // (login screen will be displayed as usual + an error message box will appear)
	  this.LOGGED_IN = false;
	  this.AUTH_STRING = "";
	  this.iniSettings.put(MyExperimentClient.INI_AUTO_LOGIN, new Boolean(false).toString());

	  javax.swing.JOptionPane.showMessageDialog(null, "Your myExperiment login details that were stored\n"
		  + "in the configuration file  appear to be incorrect.\n"
		  + "The auto-login feature has been disabled - please\n"
		  + " check your details and log in manually.");
	} else {
	  // login credentials were verified successfully; load current user
	  String strCurrentUserURI = doc.getRootElement().getAttributeValue("uri");
	  try {
		this.current_user = this.fetchCurrentUser(strCurrentUserURI);
		this.logger.debug("Logged in to myExperiment successfully with credentials that were loaded from INI file.");
	  } catch (Exception e) {
		// this is highly unlikely because the login credentials were validated successfully just before this
		this.logger.error("Couldn't fetch user data from myExperiment ("
			+ strCurrentUserURI
			+ ") while making login from stored credentials. Exception:\n" + e);
	  }
	}
  }

  // Simulates a "logout" action. Logging in and out in the plugin is only an
  // abstraction created for user convenience; it is a purely virtual concept, because the
  // myExperiment API is completely stateless - hence, logging out simply consists of "forgetting"
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
  public ServerResponse doMyExperimentPOST(String strURL, String strXMLDataBody) throws Exception {
	// POSTing to myExperiment is only allowed for authorised users
	if (!LOGGED_IN)
	  return (null);

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
	// the last line wouldn't be executed if the user wasn't logged in (see above code), so safe to run

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
  private ServerResponse doMyExperimentReceiveServerResponse(HttpURLConnection conn, String strURL, boolean bIsGETRequest) throws Exception {
	int iResponseCode = conn.getResponseCode();

	switch (iResponseCode) {
	  case HttpURLConnection.HTTP_OK:
		// data retrieval was successful - parse the response XML and return it
		// along with response code
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
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
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
		Document errorDoc = new SAXBuilder().build(new InputSource(errorReader));
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
	  Document doc = this.getResource(Resource.USER, uri, Resource.REQUEST_FULL_PREVIEW);
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
		Document doc = this.doMyExperimentGET(user.getAvatarURI()).getResponseBody();
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
  public Document getResource(int iResourceType, String strURI, int iRequestType) throws Exception {
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
		  // this should set no elements, because default is desired at the moment -
		  // but even having "&elements=" with and empty string at the end will still
		  // retrieve default fields from the API
		  strURI += "&elements=" + Tag.getRequiredAPIElements(iRequestType);
		  break;
		case Resource.COMMENT:
		  // this should set no elements, because default is desired at the
		  // moment - but even having "&elements=" with and empty string at the end will
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
	Document doc = this.getResource(Resource.WORKFLOW, strWorkflowURI, Resource.REQUEST_WORKFLOW_CONTENT_ONLY);

	// verify that the type of the workflow data is correct
	Element root = doc.getRootElement();
	Workflow w = new Workflow();
	w.setVisibleType(root.getChildText("type"));
	w.setContentType(root.getChildText("content-type"));

	if (!w.isTavernaWorkflow()) {
	  throw new Exception("Unsupported workflow type. Details:\nWorkflow type: "
		  + w.getVisibleType() + "\nMime type: " + w.getContentType());
	}

	// check that content encoding is correct
	String strEncoding = root.getChild("content").getAttributeValue("encoding");
	String strDataFormat = root.getChild("content").getAttributeValue("type");
	if (!strEncoding.toLowerCase().equals("base64")
		|| !strDataFormat.toLowerCase().equals("binary")) {
	  throw new Exception("Unsupported workflow data format. Details:\nContent encoding: "
		  + strEncoding + "\nFormat: " + strDataFormat);
	}

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
	  Document doc = this.doMyExperimentGET(strExampleWorkflowsPackUrl).getResponseBody();

	  if (doc != null) {
		List<Element> allInternalItems = doc.getRootElement().getChild("internal-pack-items").getChildren("workflow");
		for (Element e : allInternalItems) {
			String itemUri = e.getAttributeValue("uri");
			Document itemDoc = this.doMyExperimentGET(itemUri).getResponseBody();
			String workflowUri = itemDoc.getRootElement().getChild("item").getChild("workflow").getAttributeValue("uri");
		  Document docCurWorkflow = this.getResource(Resource.WORKFLOW, workflowUri, Resource.REQUEST_FULL_LISTING);
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
		Document doc = this.getResource(Resource.USER, user.getURI(), Resource.REQUEST_USER_APPLIED_TAGS_ONLY);
		Iterator<Element> iNewUserTags = doc.getRootElement().getChild("tags-applied").getChildren().iterator();
		Util.getResourceCollectionFromXMLIterator(iNewUserTags, user.getTags());
	  }

	  // fetch additional required data about the tags
	  Iterator<HashMap<String, String>> iTagsResourcesHashMaps = user.getTags().iterator();
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
	  if (size <= 0)
		size = tcCloud.getTags().size();

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
  public Document getUserContributions(User user, int iResourceType, int iRequestType) {
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
	  doc = this.doMyExperimentGET(BASE_URL + "/tagged.xml?tag="
		  + strUrlEncodedTag + Util.composeAPIQueryElements(null)).getResponseBody();
	} catch (Exception e) {
	  logger.error("ERROR: Failed to fetch tagged items from myExperiment. Query tag was '"
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
		doc = this.getResource(Resource.TAG, t.getURI(), Resource.REQUEST_DEFAULT_FROM_API);
		Element rootElement = doc.getRootElement();
		t.setCount(Integer.parseInt(rootElement.getChild("count").getText()));
	  }
	} catch (Exception e) {
	  logger.error("Failed while getting tag application counts when turning tag list into tag cloud data");
	}
  }

  /**
   * Fetches the data about user's favourite items and updates the provided user
   * instance with the latest data.
   */
  public void updateUserFavourites(User user) {
	// fetch and update favourites data
	try {
	  Document doc = this.getResource(Resource.USER, user.getURI(), Resource.REQUEST_USER_FAVOURITES_ONLY);
	  List<Resource> newUserFavouritesList = Util.retrieveUserFavourites(doc.getRootElement());

	  user.getFavourites().clear();
	  user.getFavourites().addAll(newUserFavouritesList);
	} catch (Exception ex) {
	  logger.error("Failed to fetch favourites data from myExperiment for a user (URI: "
		  + user.getURI() + "); exception:\n" + ex);
	  JOptionPane.showMessageDialog(null, "Couldn't synchronise data about your favourite items with myExperiment.\n"
		  + "You might not be able to add / remove other items to your favourites and.\n"
		  + "Please refresh your profile data manually by clicking 'Refresh' button in 'My Stuff' tab.", "myExperiment Plugin - Error", JOptionPane.ERROR_MESSAGE);
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
		doc = this.getResource(Resource.COMMENT, c.getURI(), Resource.REQUEST_DEFAULT_FROM_API);
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
		  c.setCreatedAt(DATE_FORMATTER.parse(createdAt));
		}
	  }
	} catch (Exception e) {
	  logger.error("Failed while updating comment list for preview");
	}
  }

  public Document searchByQuery(QuerySearchInstance searchQuery) {
	Document doc = null;
	String strSearchURL = null;

	try {
	  // this will URL encode the query so that it can be directly inserted into
	  // the search URL
	  String strUrlEncodedQuery = MyExperimentClient.urlEncodeQuery(searchQuery.getSearchQuery());

	  // determine which types to include in the search URL
	  // (if none are added, any types will be searched for)
	  String strSearchFor = "";
	  if (searchQuery.getSearchWorkflows())
		strSearchFor += "workflow";
	  if (searchQuery.getSearchFiles())
		strSearchFor += ",file";
	  if (searchQuery.getSearchPacks())
		strSearchFor += ",pack";
	  if (searchQuery.getSearchUsers())
		strSearchFor += ",user";
	  if (searchQuery.getSearchGroups())
		strSearchFor += ",group";
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
	  if (strParameters.length() != 0)
		strParameters += "&";
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
	  logger.error("ERROR: Failed to run search on myExperiment. Query URL was'"
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

  private String prepareWorkflowPostContent(String workflowContent, String title, String description, String license, String sharing) {
	String strWorkflowData = "<workflow>";

	if (title.length() > 0)
	  strWorkflowData += "<title>" + title + "</title>";

	if (description.length() > 0)
	  strWorkflowData += "<description>" + description + "</description>";

	if (license.length() > 0)
	  strWorkflowData += "<license-type>" + license + "</license-type>";

	if (sharing.length() > 0) {
	  if (sharing.contains("private"))
		strWorkflowData += "<permissions />";
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
	if (workflowContent.length() > 0) {
	  encodedWorkflow += "<content-type>application/vnd.taverna.t2flow+xml</content-type>"
		  + "<content encoding=\"base64\" type=\"binary\">"
		  + Base64.encodeBytes(workflowContent.getBytes()) + "</content>";
	  strWorkflowData += encodedWorkflow;
	}

	strWorkflowData += "</workflow>";

	return (strWorkflowData);
  }

  private void afterMyExperimentPost(ServerResponse response) {
	//	if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
	//	  // XML response should contain the new workflow that was posted
	//	  Workflow newWorkflow = Workflow.buildFromXML(response.getResponseBody(), logger);
	//
	//	  System.out.println("* *** *** *** *" + response.getResponseBody()
	//		  + "* *** *** *** *");
	//	}
  }

  public ServerResponse postWorkflow(String workflowContent, String title, String description, String license, String sharing) {
	try {
	  String strWorkflowData = prepareWorkflowPostContent(workflowContent, title, description, license, sharing);

	  ServerResponse response = this.doMyExperimentPOST(BASE_URL
		  + "/workflow.xml", strWorkflowData);

	  afterMyExperimentPost(response);
	  // will return the whole response object so that the application could decide on the next steps
	  return (response);
	} catch (Exception e) {
	  logger.error("Failed while trying to upload the workflow");
	  return (new ServerResponse(ServerResponse.LOCAL_FAILURE, null));
	}
  }

  public ServerResponse updateWorkflowVersionOrMetadata(Resource resource, String workflowContent, String title, String description, String license, String sharing) {
	try {
	  String strWorkflowData = prepareWorkflowPostContent(workflowContent, title, description, license, sharing);

	  // if strWorkflowFileContent is empty; include version info for PUT (since workflow is being updated)
	  // a POST would require data, hence strWorkflowFileContent would not be empty
	  String doUpdateStatus = (workflowContent.length() == 0 ? DO_PUT : "");

	  ServerResponse response = this.doMyExperimentPOST(BASE_URL
		  + "/workflow.xml?id=" + resource.getID() + doUpdateStatus, strWorkflowData);

	  afterMyExperimentPost(response);
	  // will return the whole response object so that the application could decide on the next steps
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
		  + MyExperimentClient.urlEncodeQuery(this.getCurrentUser().getResource())
		  + "&object="
		  + MyExperimentClient.urlEncodeQuery(resource.getResource());
	  ServerResponse response = this.doMyExperimentGET(strGetFavouriteObjectURL);

	  // now retrieve this object's URI from server's response
	  Element root = response.getResponseBody().getRootElement();
	  String strFavouriteURI = root.getChild("favourite").getAttributeValue("uri");

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

  public static DateFormat getDateFormatter() {
	return (MyExperimentClient.DATE_FORMATTER);
  }

  public static DateFormat getShortDateFormatter() {
	return (MyExperimentClient.SHORT_DATE_FORMATTER);
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
