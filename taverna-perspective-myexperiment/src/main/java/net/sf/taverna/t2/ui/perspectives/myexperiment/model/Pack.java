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
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;
import net.sf.taverna.t2.ui.perspectives.myexperiment.model.User;

/**
 * @author Sergejs Aleksejevs
 */
public class Pack extends Resource
{
  private int accessType;
  
  private User creator;
  private List<Tag> tags;
  private List<Comment> comments;
  private ArrayList<PackItem> items;
  
  
  public Pack()
  {
    super();
    this.setItemType(Resource.PACK);
  }
  
  public int getAccessType()
  {
    return this.accessType;
  }
  
  public void setAccessType(int accessType)
  {
    this.accessType = accessType;
  }
  
  public User getCreator() {
    return creator;
  }

  public void setCreator(User creator) {
    this.creator = creator;
  }
  
  public List<Tag> getTags()
  {
    return (this.tags);
  }
  
  public List<Comment> getComments()
  {
    return this.comments;
  }
  
  public int getItemCount()
  {
    return this.items.size();
  }
  
  public ArrayList<PackItem> getItems()
  {
    return this.items;
  }
  
  
  /**
   * A helper method to return a set of API elements that are
   * needed to satisfy request of a particular type - e.g. creating
   * a listing of resources or populating full preview, etc.
   * 
   * @param iRequestType A constant value from Resource class.
   * @return Comma-separated string containing values of required API elements.
   */
  public static String getRequiredAPIElements(int iRequestType)
  {
    String strElements = "";
    
    // cases higher up in the list are supersets of those that come below -
    // hence no "break" statements are required, because 'falling through' the
    // switch statement is the desired behaviour in this case
    switch (iRequestType) {
      case Resource.REQUEST_FULL_PREVIEW:
        strElements += "created-at,updated-at,internal-pack-items,external-pack-items,tags,comments,";
      case Resource.REQUEST_FULL_LISTING:
        strElements += "owner,";
      case Resource.REQUEST_SHORT_LISTING:
        strElements += "id,title,description,privileges";
    }
    
    return (strElements);
  }
  
  
  public static Pack buildFromXML(Document doc, MyExperimentClient client, Logger logger)
  {
    // if no XML document was supplied, return NULL
    if(doc == null) return(null);
    
    // call main method which parses XML document starting from root element
    return (Pack.buildFromXML(doc.getRootElement(), client, logger));
  }
  
  
  // class method to build a pack instance from XML
  @SuppressWarnings("unchecked")
  public static Pack buildFromXML(Element docRootElement, MyExperimentClient client, Logger logger)
  {
    // return null to indicate an error if XML document contains no root element 
    if(docRootElement == null) return(null);
    
    Pack p = new Pack();
    
    try {
      // Access type
      p.setAccessType(Util.getAccessTypeFromXMLElement(docRootElement.getChild("privileges")));
      
      // URI
      p.setURI(docRootElement.getAttributeValue("uri"));
      
      // Resource URI
      p.setResource(docRootElement.getAttributeValue("resource"));
      
      // Id
      String id = docRootElement.getChildText("id");
      if (id == null || id.equals("")) {
        id = "API Error - No pack ID supplied";
        logger.error("Error while parsing pack XML data - no ID provided for pack with title: \"" + docRootElement.getChildText("title") + "\"");
      }
      p.setID(Integer.parseInt(id));
      
      // Title
      p.setTitle(docRootElement.getChildText("title"));
      
      // Description
      p.setDescription(docRootElement.getChildText("description"));
      
      // Owner
      Element ownerElement = docRootElement.getChild("owner");
      p.setCreator(Util.instantiatePrimitiveUserFromElement(ownerElement));
      
      // Created at
      String createdAt = docRootElement.getChildText("created-at");
      if (createdAt != null && !createdAt.equals("")) {
        p.setCreatedAt(MyExperimentClient.parseDate(createdAt));
      }
      
      // Updated at
      String updatedAt = docRootElement.getChildText("updated-at");
      if (updatedAt != null && !updatedAt.equals("")) {
        p.setUpdatedAt(MyExperimentClient.parseDate(updatedAt));
      }
      
      // Tags
      p.tags = new ArrayList<Tag>();
      p.getTags().addAll(Util.retrieveTags(docRootElement));
      
      // Comments
      p.comments = new ArrayList<Comment>();
      p.getComments().addAll(Util.retrieveComments(docRootElement, p));
      
      
      // === All items will be stored together in one array ===
      p.items = new ArrayList<PackItem>();
      int iCount = 0;
      
      // adding internal items first
      Element itemsElement = docRootElement.getChild("internal-pack-items");
      if (itemsElement != null) {
        List<Element> itemsNodes = itemsElement.getChildren();
        for (Element e : itemsNodes) {
          Document docCurrentItem = client.getResource(Resource.PACK_INTERNAL_ITEM, e.getAttributeValue("uri"), Resource.REQUEST_DEFAULT_FROM_API);
          PackItem piCurrentItem = PackItem.buildFromXML(docCurrentItem, logger);
          
          p.getItems().add(piCurrentItem);
          iCount++;
        }
      }
      
      // now adding external items
      itemsElement = docRootElement.getChild("external-pack-items");
      if (itemsElement != null) {
        List<Element> itemsNodes = itemsElement.getChildren();
        for (Element e : itemsNodes) {
          Document docCurrentItem = client.getResource(Resource.PACK_EXTERNAL_ITEM, e.getAttributeValue("uri"), Resource.REQUEST_DEFAULT_FROM_API);
          PackItem piCurrentItem = PackItem.buildFromXML(docCurrentItem, logger);
          
          p.getItems().add(piCurrentItem);
          iCount++;
        }
      }
      
      // sort the items after all of those have been added
      Collections.sort(p.getItems());
      
      
      logger.debug("Found information for pack with ID: " + p.getID() + ", Title: " + p.getTitle());
    }
    catch (Exception e) {
      logger.error("Failed midway through creating pack object from XML", e);
   }
    
    // return created pack instance
    return(p);
  }
}
