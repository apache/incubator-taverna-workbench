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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class File extends Resource {
  private int accessType;

  private User uploader;
  private License license;
  private String filename;
  private String visibleType;
  private String contentType;
  private List<Tag> tags;
  private List<Comment> comments;
  private List<Resource> credits;
  private List<Resource> attributions;

  public File() {
	super();
	this.setItemType(Resource.FILE);
  }

  public int getAccessType() {
	return this.accessType;
  }

  public void setAccessType(int accessType) {
	this.accessType = accessType;
  }

  public List<Tag> getTags() {
	return tags;
  }

  public User getUploader() {
	return uploader;
  }

  public void setUploader(User uploader) {
	this.uploader = uploader;
  }

  public License getLicense() {
	return license;
  }

  public void setLicense(License license) {
	this.license = license;
  }

  public String getFilename() {
	return this.filename;
  }

  public void setFilename(String filename) {
	this.filename = filename;
  }

  public String getContentType() {
	return contentType;
  }

  public void setContentType(String contentType) {
	this.contentType = contentType;
  }

  public String getVisibleType() {
	return this.visibleType;
  }

  public void setVisibleType(String visibleType) {
	this.visibleType = visibleType;
  }

  public List<Comment> getComments() {
	return this.comments;
  }

  public List<Resource> getCredits() {
	return this.credits;
  }

  public List<Resource> getAttributions() {
	return this.attributions;
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
	// switch statement is the desired behaviour in this case
	switch (iRequestType) {
	  case Resource.REQUEST_FULL_PREVIEW:
		strElements += "filename,content-type,created-at,updated-at,"
			+ "license-type,tags,comments,credits,attributions,";
	  case Resource.REQUEST_FULL_LISTING:
		strElements += "uploader,type,";
	  case Resource.REQUEST_SHORT_LISTING:
		strElements += "id,title,description,privileges";
	}

	return (strElements);
  }

  public static File buildFromXML(Document doc, Logger logger) {
	// if no XML document was supplied, return NULL
	if (doc == null)
	  return (null);

	// call main method which parses XML document starting from root element
	return (File.buildFromXML(doc.getRootElement(), logger));
  }

  //class method to build a file instance from XML
  public static File buildFromXML(Element docRootElement, Logger logger) {
	// return null to indicate an error if XML document contains no root element
	if (docRootElement == null)
	  return (null);

	File f = new File();

	try {
	  // Access type
	  f.setAccessType(Util.getAccessTypeFromXMLElement(docRootElement.getChild("privileges")));

	  // URI
	  f.setURI(docRootElement.getAttributeValue("uri"));

	  // Resource URI
	  f.setResource(docRootElement.getAttributeValue("resource"));

	  // Id
	  String id = docRootElement.getChildText("id");
	  if (id == null || id.equals("")) {
		id = "API Error - No file ID supplied";
		logger.error("Error while parsing file XML data - no ID provided for file with title: \""
			+ docRootElement.getChildText("title") + "\"");
	  }
	  f.setID(id);

	  // Filename
	  f.setFilename(docRootElement.getChildText("filename"));

	  // Title
	  f.setTitle(docRootElement.getChildText("title"));

	  // Description
	  f.setDescription(docRootElement.getChildText("description"));

	  // Uploader
	  Element uploaderElement = docRootElement.getChild("uploader");
	  f.setUploader(Util.instantiatePrimitiveUserFromElement(uploaderElement));

	  // Created at
	  String createdAt = docRootElement.getChildText("created-at");
	  if (createdAt != null && !createdAt.equals("")) {
		f.setCreatedAt(MyExperimentClient.parseDate(createdAt));
	  }

	  // Updated at
	  String updatedAt = docRootElement.getChildText("updated-at");
	  if (updatedAt != null && !updatedAt.equals("")) {
		f.setUpdatedAt(MyExperimentClient.parseDate(updatedAt));
	  }

	  // License
	  f.setLicense(License.getInstance(docRootElement.getChildText("license-type")));

	  // Type and Content-Type
	  f.setVisibleType(docRootElement.getChildText("type"));
	  f.setContentType(docRootElement.getChildText("content-type"));

	  // Tags
	  f.tags = new ArrayList<Tag>();
	  f.getTags().addAll(Util.retrieveTags(docRootElement));

	  // Comments
	  f.comments = new ArrayList<Comment>();
	  f.getComments().addAll(Util.retrieveComments(docRootElement, f));

	  // Credits
	  f.credits = new ArrayList<Resource>();
	  f.getCredits().addAll(Util.retrieveCredits(docRootElement));

	  // Attributions
	  f.attributions = new ArrayList<Resource>();
	  f.getAttributions().addAll(Util.retrieveAttributions(docRootElement));

	  logger.debug("Found information for file with ID: " + f.getID()
		  + ", Title: " + f.getTitle());
	} catch (Exception e) {
	  logger.error("Failed midway through creating file object from XML", e);
	}

	// return created file instance
	return (f);
  }
}
