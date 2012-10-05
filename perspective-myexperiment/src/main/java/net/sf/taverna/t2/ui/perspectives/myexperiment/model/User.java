// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University
package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
public class User extends Resource {
  private String name;
  private String city;
  private String country;
  private String email;
  private String website;

  private ImageIcon avatar;
  private String avatar_uri;
  private String avatar_resource;

  private ArrayList<HashMap<String, String>> workflows;
  private ArrayList<HashMap<String, String>> files;
  private ArrayList<HashMap<String, String>> packs;
  private ArrayList<HashMap<String, String>> friends;
  private ArrayList<HashMap<String, String>> groups;
  private ArrayList<HashMap<String, String>> tags;
  private ArrayList<Resource> favourites;

  public User() {
	super();
	this.setItemType(Resource.USER);
  }

  public String getName() {
	return name;
  }

  public void setName(String name) {
	this.name = name;
	this.setTitle(name); // this will allow to use name/title interchangeably
  }

  public String getCity() {
	return city;
  }

  public void setCity(String city) {
	this.city = city;
  }

  public String getCountry() {
	return country;
  }

  public void setCountry(String country) {
	this.country = country;
  }

  public String getEmail() {
	return email;
  }

  public void setEmail(String email) {
	this.email = email;
  }

  public String getWebsite() {
	return website;
  }

  public void setWebsite(String website) {
	this.website = website;
  }

  public String getAvatarURI() {
	return avatar_uri;
  }

  public void setAvatarURI(String avatar_uri) {
	this.avatar_uri = avatar_uri;
  }

  public ImageIcon getAvatar() {
	return avatar;
  }

  // creates avatar from the XML of it
  public void setAvatar(Document doc) {
	Element root = doc.getRootElement();
	String strAvatarData = root.getChild("data").getText();

	this.avatar = new ImageIcon(Base64.decode(strAvatarData));
  }

  public void setAvatar(ImageIcon avatar) {
	this.avatar = avatar;
  }

  public String getAvatarResource() {
	return avatar_resource;
  }

  public void setAvatarResource(String avatar_resource) {
	this.avatar_resource = avatar_resource;
  }

  public ArrayList<HashMap<String, String>> getWorkflows() {
	return workflows;
  }

  public ArrayList<HashMap<String, String>> getFiles() {
	return files;
  }

  public ArrayList<HashMap<String, String>> getPacks() {
	return packs;
  }

  public ArrayList<HashMap<String, String>> getFriends() {
	return friends;
  }

  public ArrayList<HashMap<String, String>> getGroups() {
	return groups;
  }

  public ArrayList<Resource> getFavourites() {
	return favourites;
  }

  public ArrayList<HashMap<String, String>> getTags() {
	return this.tags;
  }

  /**
   * A helper method to return a set of API elements that are needed to satisfy
   * request of a particular type - e.g. creating a listing of resources or
   * populating full preview, etc.
   * 
   * @param iRequestType
   *          A constant value from Resource class.
   * @return Comma-separated string containing values of required API elements.
   */
  public static String getRequiredAPIElements(int iRequestType) {
	String strElements = "";

	// cases higher up in the list are supersets of those that come below -
	// hence no "break" statements are required, because 'falling through' the
	// switch statement is the desired behaviour in this case;
	//
	// cases after first 'break' statement are separate ones and hence are treated
	// individually
	switch (iRequestType) {
	  case Resource.REQUEST_FULL_PREVIEW:
		strElements += "created-at,updated-at,email,website,city,country,"
			+ "friends,groups,workflows,files,packs,favourited,tags-applied,";
	  case Resource.REQUEST_FULL_LISTING:
		strElements += ""; // essentially the same as short listing
	  case Resource.REQUEST_SHORT_LISTING:
		strElements += "id,name,description,avatar";
		break;
	  case Resource.REQUEST_USER_FAVOURITES_ONLY:
		strElements += "favourited";
		break;
	  case Resource.REQUEST_USER_APPLIED_TAGS_ONLY:
		strElements += "tags-applied";
		break;
	}

	return (strElements);
  }

  public static User buildFromXML(Document doc, Logger logger) {
	// if no XML document was supplied, return NULL
	if (doc == null)
	  return (null);

	// call main method which parses XML document starting from root element
	return (User.buildFromXML(doc.getRootElement(), logger));
  }

  // class method to build a user instance from XML
  @SuppressWarnings("unchecked")
  public static User buildFromXML(Element docRootElement, Logger logger) {
	// can't make any processing if root element is NULL
	if (docRootElement == null)
	  return (null);

	// create instance and parse the XML otherwise
	User user = new User();

	try {
	  // store all simple values
	  user.setURI(docRootElement.getAttributeValue("uri"));
	  user.setResource(docRootElement.getAttributeValue("resource"));
	  user.setID(docRootElement.getChildText("id"));
	  user.setName(docRootElement.getChildText("name"));
	  user.setTitle(user.getName()); // to allow generic handling of all resources - for users 'title' will replicate the 'name'
	  user.setDescription(docRootElement.getChild("description").getText());
	  user.setCity(docRootElement.getChildText("city"));
	  user.setCountry(docRootElement.getChildText("country"));
	  user.setEmail(docRootElement.getChildText("email"));
	  user.setWebsite(docRootElement.getChildText("website"));

	  // avatar URI in the API
	  Element avatarURIElement = docRootElement.getChild("avatar");
	  if (avatarURIElement != null) {
		user.setAvatarURI(avatarURIElement.getAttributeValue("uri"));
	  }

	  // avatar resource on myExperiment
	  Element avatarElement = docRootElement.getChild("avatar");
	  if (avatarElement != null) {
		user.setAvatarResource(avatarElement.getAttributeValue("resource"));
	  }

	  // Created at
	  String createdAt = docRootElement.getChildText("created-at");
	  if (createdAt != null && !createdAt.equals("")) {
		user.setCreatedAt(MyExperimentClient.parseDate(createdAt));
	  }

	  // Updated at
	  String updatedAt = docRootElement.getChildText("updated-at");
	  if (updatedAt != null && !updatedAt.equals("")) {
		user.setUpdatedAt(MyExperimentClient.parseDate(updatedAt));
	  }

	  // store workflows
	  user.workflows = new ArrayList<HashMap<String, String>>();
	  Element workflowsElement = docRootElement.getChild("workflows");
	  if (workflowsElement != null) {
		Iterator<Element> iWorkflows = workflowsElement.getChildren().iterator();
		Util.getResourceCollectionFromXMLIterator(iWorkflows, user.workflows);
	  }

	  // store files
	  user.files = new ArrayList<HashMap<String, String>>();
	  Element filesElement = docRootElement.getChild("files");
	  if (filesElement != null) {
		Iterator<Element> iFiles = filesElement.getChildren().iterator();
		Util.getResourceCollectionFromXMLIterator(iFiles, user.files);
	  }

	  // store packs
	  user.packs = new ArrayList<HashMap<String, String>>();
	  Element packsElement = docRootElement.getChild("packs");
	  if (packsElement != null) {
		Iterator<Element> iPacks = packsElement.getChildren().iterator();
		Util.getResourceCollectionFromXMLIterator(iPacks, user.packs);
	  }

	  // store friends
	  user.friends = new ArrayList<HashMap<String, String>>();
	  Element friendsElement = docRootElement.getChild("friends");
	  if (filesElement != null) {
		Iterator<Element> iFriends = friendsElement.getChildren().iterator();
		Util.getResourceCollectionFromXMLIterator(iFriends, user.friends);
	  }

	  // store groups
	  user.groups = new ArrayList<HashMap<String, String>>();
	  Element groupsElement = docRootElement.getChild("groups");
	  if (groupsElement != null) {
		Iterator<Element> iGroups = groupsElement.getChildren().iterator();
		Util.getResourceCollectionFromXMLIterator(iGroups, user.groups);
	  }

	  // store tags
	  user.tags = new ArrayList<HashMap<String, String>>();
	  Element tagsElement = docRootElement.getChild("tags-applied");
	  if (tagsElement != null) {
		Iterator<Element> iTags = tagsElement.getChildren().iterator();
		Util.getResourceCollectionFromXMLIterator(iTags, user.tags);
	  }

	  // store favourites
	  user.favourites = new ArrayList<Resource>();
	  user.favourites.addAll(Util.retrieveUserFavourites(docRootElement));

	} catch (Exception e) {
	  logger.error("Failed midway through creating user object from XML", e);
	}

	return (user);
  }
}
