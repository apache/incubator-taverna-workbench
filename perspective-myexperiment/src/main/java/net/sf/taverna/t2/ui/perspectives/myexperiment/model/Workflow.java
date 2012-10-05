// Copyright (C) 2008 The University of Manchester, University of Southampton
// and Cardiff University
package net.sf.taverna.t2.ui.perspectives.myexperiment.model;

import java.net.URI;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Jiten Bhagat, Sergejs Aleksejevs
 */
public class Workflow extends Resource {
  // CONSTANTS
  public static final String MIME_TYPE_TAVERNA_1 = "application/vnd.taverna.scufl+xml";
  public static final String MIME_TYPE_TAVERNA_2 = "application/vnd.taverna.t2flow+xml";

  private int accessType;

  private int version;
  private User uploader;
  private URI preview;
  private URI thumbnail;
  private URI thumbnailBig;
  private URI svg;
  private License license;

  private String visibleType;
  private String contentType;
  private URI contentUri;
  byte[] content;

  private List<Tag> tags;
  private List<Comment> comments;
  private List<Resource> credits;
  private List<Resource> attributions;
  private HashMap<String, ArrayList<HashMap<String, String>>> components;

  public Workflow() {
	super();
	this.setItemType(Resource.WORKFLOW);
  }

  public int getAccessType() {
	return this.accessType;
  }

  public void setAccessType(int accessType) {
	this.accessType = accessType;
  }

  public int getVersion() {
	return version;
  }

  public void setVersion(int version) {
	this.version = version;
  }

  public User getUploader() {
	return uploader;
  }

  public void setUploader(User uploader) {
	this.uploader = uploader;
  }

  public URI getPreview() {
	return preview;
  }

  public void setPreview(URI preview) {
	this.preview = preview;
  }

  public URI getThumbnail() {
	return thumbnail;
  }

  public void setThumbnail(URI thumbnail) {
	this.thumbnail = thumbnail;
  }

  public URI getSvg() {
	return svg;
  }

  public void setSvg(URI svg) {
	this.svg = svg;
  }

  public License getLicense() {
	return license;
  }

  public void setLicense(License license) {
	this.license = license;
  }

  public URI getContentUri() {
	return contentUri;
  }

  public void setContentUri(URI contentUri) {
	this.contentUri = contentUri;
  }

  public String getVisibleType() {
	return this.visibleType;
  }

  public void setVisibleType(String visibleType) {
	this.visibleType = visibleType;
  }

  public String getContentType() {
	return contentType;
  }

  public void setContentType(String contentType) {
	this.contentType = contentType;
  }

  public byte[] getContent() {
	return this.content;
  }

  public void setContent(byte[] content) {
	this.content = content;
  }

  public List<Tag> getTags() {
	return tags;
  }

  public List<Comment> getComments() {
	return comments;
  }

  public List<Resource> getCredits() {
	return credits;
  }

  public List<Resource> getAttributions() {
	return this.attributions;
  }

  public HashMap<String, ArrayList<HashMap<String, String>>> getComponents() {
	return this.components;
  }

  public URI getThumbnailBig() {
	return thumbnailBig;
  }

  public void setThumbnailBig(URI thumbnailBig) {
	this.thumbnailBig = thumbnailBig;
  }

  /**
   * Determines whether the current instance of the workflow is a Taverna 1 or
   * Taverna 2 workflow
   */
  public boolean isTavernaWorkflow() {
	return (contentType.equals(Workflow.MIME_TYPE_TAVERNA_1) || contentType.equals(Workflow.MIME_TYPE_TAVERNA_2));
  }

  public boolean isTaverna1Workflow() {
	return (contentType.equals(Workflow.MIME_TYPE_TAVERNA_1));
  }

  public boolean isTaverna2Workflow() {
	return (contentType.equals(Workflow.MIME_TYPE_TAVERNA_2));
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
	//
	// cases that follow after the first 'break' statement are to be treated
	// separately - these require individual processing and have nothing to do
	// with joining different elements for various listings / previews
	switch (iRequestType) {
	  case Resource.REQUEST_FULL_PREVIEW:
		strElements += "created-at,updated-at,preview,thumbnail-big,svg,license-type,content-uri,"
			+ "tags,comments,ratings,credits,attributions,components,";
	  case Resource.REQUEST_FULL_LISTING:
		strElements += "uploader,type,";
	  case Resource.REQUEST_SHORT_LISTING:
		strElements += "id,title,thumbnail,description,privileges,content-type";
		break;
	  case Resource.REQUEST_WORKFLOW_CONTENT_ONLY:
		strElements += "type,content-type,content";
		break;
	}

	return (strElements);
  }

  public static Workflow buildFromXML(Document doc, Logger logger) {
	// if no XML document was supplied, return NULL
	if (doc == null)
	  return (null);

	// call main method which parses XML document starting from root element
	return (Workflow.buildFromXML(doc.getRootElement(), logger));
  }

  // class method to build a workflow instance from XML
  @SuppressWarnings("unchecked")
  public static Workflow buildFromXML(Element docRootElement, Logger logger) {
	// return null to indicate an error if XML document contains no root element
	if (docRootElement == null)
	  return (null);

	Workflow w = new Workflow();

	try {
	  // Access type
	  w.setAccessType(Util.getAccessTypeFromXMLElement(docRootElement.getChild("privileges")));

	  // URI
	  w.setURI(docRootElement.getAttributeValue("uri"));

	  // Resource URI
	  w.setResource(docRootElement.getAttributeValue("resource"));

	  // Version
	  String version = docRootElement.getAttributeValue("version");
	  if (version != null && !version.equals("")) {
		w.setVersion(Integer.parseInt(version));
	  }

	  // Id
	  String id = docRootElement.getChildText("id");
	  if (id == null || id.equals("")) {
		id = "API Error - No workflow ID supplied";
		logger.error("Error while parsing workflow XML data - no ID provided for workflow with title: \""
			+ docRootElement.getChildText("title") + "\"");
	  }
	  w.setID(id);

	  // Title
	  w.setTitle(docRootElement.getChildText("title"));

	  // Description
	  w.setDescription(docRootElement.getChildText("description"));

	  // Uploader
	  Element uploaderElement = docRootElement.getChild("uploader");
	  w.setUploader(Util.instantiatePrimitiveUserFromElement(uploaderElement));

	  // Created at
	  String createdAt = docRootElement.getChildText("created-at");
	  if (createdAt != null && !createdAt.equals("")) {
		w.setCreatedAt(MyExperimentClient.parseDate(createdAt));
	  }

	  // Updated at
	  String updatedAt = docRootElement.getChildText("updated-at");
	  if (updatedAt != null && !updatedAt.equals("")) {
		w.setUpdatedAt(MyExperimentClient.parseDate(updatedAt));
	  }

	  // Preview
	  String preview = docRootElement.getChildText("preview");
	  if (preview != null && !preview.equals("")) {
		w.setPreview(new URI(preview));
	  }

	  // Thumbnail
	  String thumbnail = docRootElement.getChildText("thumbnail");
	  if (thumbnail != null && !thumbnail.equals("")) {
		w.setThumbnail(new URI(thumbnail));
	  }

	  // Thumbnail (big)
	  String thumbnailBig = docRootElement.getChildText("thumbnail-big");
	  if (thumbnailBig != null && !thumbnailBig.equals("")) {
		w.setThumbnailBig(new URI(thumbnailBig));
	  }

	  // SVG
	  String svg = docRootElement.getChildText("svg");
	  if (svg != null && !svg.equals("")) {
		w.setSvg(new URI(svg));
	  }

	  // License
	  w.setLicense(License.getInstance(docRootElement.getChildText("license-type")));

	  // Content URI
	  String contentUri = docRootElement.getChildText("content-uri");
	  if (contentUri != null && !contentUri.equals("")) {
		w.setContentUri(new URI(contentUri));
	  }

	  // Type and Content-Type
	  w.setVisibleType(docRootElement.getChildText("type"));
	  w.setContentType(docRootElement.getChildText("content-type"));

	  // Tags
	  w.tags = new ArrayList<Tag>();
	  w.tags.addAll(Util.retrieveTags(docRootElement));

	  // Comments
	  w.comments = new ArrayList<Comment>();
	  w.getComments().addAll(Util.retrieveComments(docRootElement, w));

	  // Credits
	  w.credits = new ArrayList<Resource>();
	  w.getCredits().addAll(Util.retrieveCredits(docRootElement));

	  // Attributions
	  w.attributions = new ArrayList<Resource>();
	  w.getAttributions().addAll(Util.retrieveAttributions(docRootElement));

	  // Components
	  w.components = new HashMap<String, ArrayList<HashMap<String, String>>>();

	  Element componentsElement = docRootElement.getChild("components");
	  if (componentsElement != null) {
		// ** inputs **
		Element sourcesElement = componentsElement.getChild("sources");
		if (sourcesElement != null) {
		  ArrayList<HashMap<String, String>> inputs = new ArrayList<HashMap<String, String>>();
		  List<Element> sourcesNodes = sourcesElement.getChildren();
		  for (Element e : sourcesNodes) {
			HashMap<String, String> curInput = new HashMap<String, String>();
			curInput.put("name", e.getChildText("name"));
			curInput.put("description", e.getChildText("description"));
			inputs.add(curInput);
		  }

		  // put all inputs that were found into the overall component collection
		  w.getComponents().put("inputs", inputs);
		}

		// ** outputs **
		Element outputsElement = componentsElement.getChild("sinks");
		if (outputsElement != null) {
		  ArrayList<HashMap<String, String>> sinks = new ArrayList<HashMap<String, String>>();
		  List<Element> outputsNodes = outputsElement.getChildren();
		  for (Element e : outputsNodes) {
			HashMap<String, String> curOutput = new HashMap<String, String>();
			curOutput.put("name", e.getChildText("name"));
			curOutput.put("description", e.getChildText("description"));
			sinks.add(curOutput);
		  }

		  // put all outputs that were found into the overall component collection
		  w.getComponents().put("outputs", sinks);
		}

		// ** processors **
		Element processorsElement = componentsElement.getChild("processors");
		if (processorsElement != null) {
		  ArrayList<HashMap<String, String>> processors = new ArrayList<HashMap<String, String>>();
		  List<Element> processorsNodes = processorsElement.getChildren();
		  for (Element e : processorsNodes) {
			HashMap<String, String> curProcessor = new HashMap<String, String>();
			curProcessor.put("name", e.getChildText("name"));
			curProcessor.put("type", e.getChildText("type"));
			curProcessor.put("description", e.getChildText("description"));
			processors.add(curProcessor);
		  }

		  // put all processors that were found into the overall component collection
		  w.getComponents().put("processors", processors);
		}

		// ** links **
		Element linksElement = componentsElement.getChild("links");
		if (linksElement != null) {
		  ArrayList<HashMap<String, String>> links = new ArrayList<HashMap<String, String>>();
		  List<Element> linksNodes = linksElement.getChildren();
		  for (Element e : linksNodes) {
			HashMap<String, String> curLink = new HashMap<String, String>();
			String strSource = e.getChild("source").getChildText("node")
				+ (e.getChild("source").getChildText("port") == null ? "" : (":" + e.getChild("source").getChildText("port")));
			curLink.put("source", strSource);
			String strSink = e.getChild("sink").getChildText("node")
				+ (e.getChild("sink").getChildText("port") == null ? "" : (":" + e.getChild("sink").getChildText("port")));
			curLink.put("sink", strSink);
			links.add(curLink);
		  }

		  // put all links that were found into the overall component collection
		  w.getComponents().put("links", links);
		}
	  }

	  logger.debug("Found information for worklow with ID: " + w.getID()
		  + ", Title: " + w.getTitle());
	} catch (Exception e) {
	  logger.error("Failed midway through creating workflow object from XML", e);
	}

	// return created workflow instance
	return (w);
  }

}
