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

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Sergejs Aleksejevs
 */
public class PackItem extends Resource {
  private int id;
  private User userWhoAddedThisItem;
  private String strComment;

  private boolean bInternalItem;

  private Resource item; // for internal items

  private String strLink; // for external items
  private String strAlternateLink; // for external items

  public PackItem() {
	super();
	this.setItemType(Resource.UNKNOWN); // set to unknown originally; will be changed as soon as the type is known
  }

  public int getID() {
	return id;
  }

  public void setID(int id) {
	this.id = id;
  }

  public void setID(String id) {
	this.id = Integer.parseInt(id);
  }

  public boolean isInternalItem() {
	return this.bInternalItem;
  }

  public void setInternalItem(boolean bIsInternalItem) {
	this.bInternalItem = bIsInternalItem;
  }

  public User getUserWhoAddedTheItem() {
	return this.userWhoAddedThisItem;
  }

  public void setUserWhoAddedTheItem(User userWhoAddedTheItem) {
	this.userWhoAddedThisItem = userWhoAddedTheItem;
  }

  public String getComment() {
	return this.strComment;
  }

  public void setComment(String strComment) {
	this.strComment = strComment;
  }

  public Resource getItem() {
	return this.item;
  }

  public void setItem(Resource item) {
	this.item = item;
  }

  public String getLink() {
	return this.strLink;
  }

  public void setLink(String strLink) {
	this.strLink = strLink;
  }

  public String getAlternateLink() {
	return this.strAlternateLink;
  }

  public void setAlternateLink(String strAlternateLink) {
	this.strAlternateLink = strAlternateLink;
  }

  public static PackItem buildFromXML(Document doc, Logger logger) {
	// if no XML was supplied, return null to indicate an error
	if (doc == null)
	  return (null);

	PackItem p = new PackItem();

	try {
	  Element root = doc.getRootElement();

	  // URI
	  p.setURI(root.getAttributeValue("uri"));

	  // Resource URI
	  p.setResource(root.getAttributeValue("resource"));

	  // Id
	  String strID = root.getChildText("id");
	  if (strID == null || strID.equals("")) {
		strID = "API Error - No pack item ID supplied";
		logger.error("Error while parsing pack item XML data - no ID provided for pack item with uri: \""
			+ p.getURI() + "\"");
	  } else {
		p.setID(strID);
	  }

	  // User who added the item to the pack
	  Element ownerElement = root.getChild("owner");
	  p.setUserWhoAddedTheItem(Util.instantiatePrimitiveUserFromElement(ownerElement));

	  // Date when the item was added to the pack
	  String createdAt = root.getChildText("created-at");
	  if (createdAt != null && !createdAt.equals("")) {
		p.setCreatedAt(MyExperimentClient.parseDate(createdAt));
	  }

	  // Comment
	  Element commentElement = root.getChild("comment");
	  if (commentElement != null) {
		p.setComment(commentElement.getText());
	  }

	  // === UP TO THIS POINT EXTERNAL AND INTERNAL ITEMS HAD THE SAME DATA ===
	  if (root.getName().equals("internal-pack-item")) {
		// record that this is internal item
		p.setInternalItem(true);

		// add a link to a resource for internal items
		Element itemElement = (Element) root.getChild("item").getChildren().get(0);
		if (itemElement != null) {
		  p.setItem(Util.instantiatePrimitiveResourceFromElement(itemElement));
		}

		// now need to replicate title and item type attributes to the pack item object
		// itself - this is required to allow proper sorting of the items
		p.setItemType(p.getItem().getItemType());
		p.setTitle(p.getItem().getTitle());
	  } else {
		// record that this is external item
		p.setInternalItem(false);

		// add links to the external resource for external items
		p.setItemType(Resource.PACK_EXTERNAL_ITEM);
		p.setTitle(root.getChildText("title"));
		p.setLink(root.getChildText("uri"));
		p.setAlternateLink(root.getChildText("alternate-uri"));
	  }

	  logger.debug("Found information for pack item with URI: " + p.getURI());
	} catch (Exception e) {
	  logger.error("Failed midway through creating pack item object from XML", e);
	}

	// return created pack item instance
	return (p);

  }
}
