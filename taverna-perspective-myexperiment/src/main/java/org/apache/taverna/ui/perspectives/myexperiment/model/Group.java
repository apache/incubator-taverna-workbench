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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import org.apache.taverna.ui.perspectives.myexperiment.model.Resource;
import org.apache.taverna.ui.perspectives.myexperiment.model.User;

/**
 * @author Sergejs Aleksejevs
 */
public class Group extends Resource
{
  private User admin;
  
  private List<Tag> tags;
  private List<Comment> comments;
  private List<User> members;
  private List<Resource> sharedItems;
  
  
  public Group()
  {
    super();
    this.setItemType(Resource.GROUP);
  }
  
  public User getAdmin() {
    return admin;
  }

  public void setAdmin(User admin) {
    this.admin = admin;
  }
  
  public List<Tag> getTags() {
    return this.tags;
  }
  
  public List<Comment> getComments()
  {
    return this.comments;
  }
  
  public int getSharedItemCount()
  {
    return this.sharedItems.size();
  }
  
  public int getMemberCount()
  {
    return this.members.size();
  }
  
  public List<Resource> getSharedItems()
  {
    return this.sharedItems;
  }
  
  public List<User> getMembers()
  {
    return this.members;
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
        strElements += "created-at,updated-at,members,shared-items,tags,comments,";
      case Resource.REQUEST_FULL_LISTING:
        strElements += "owner,";
      case Resource.REQUEST_SHORT_LISTING:
        strElements += "id,title,description";
    }
    
    return (strElements);
  }
  
  
  public static Group buildFromXML(Document doc, Logger logger)
  {
    // if no XML document was supplied, return NULL
    if(doc == null) return(null);
    
    // call main method which parses XML document starting from root element
    return (Group.buildFromXML(doc.getRootElement(), logger));
  }
  
  
  //class method to build a group instance from XML
  @SuppressWarnings("unchecked")
  public static Group buildFromXML(Element docRootElement, Logger logger)
  {
    // return null to indicate an error if XML document contains no root element
    if(docRootElement == null) return(null);
    
    Group g = new Group();

    try {
      // URI
      g.setURI(docRootElement.getAttributeValue("uri"));
      
      // Resource URI
      g.setResource(docRootElement.getAttributeValue("resource"));
      
      // Id
      String id = docRootElement.getChildText("id");
      if (id == null || id.equals("")) {
        id = "API Error - No group ID supplied";
        logger.error("Error while parsing group XML data - no ID provided for group with title: \"" + docRootElement.getChildText("title") + "\"");
      }
      g.setID(Integer.parseInt(id));
      
      // Title
      g.setTitle(docRootElement.getChildText("title"));
      
      // Description
      g.setDescription(docRootElement.getChildText("description"));
      
      // Owner
      Element ownerElement = docRootElement.getChild("owner");
      g.setAdmin(Util.instantiatePrimitiveUserFromElement(ownerElement));
      
      // Created at
      String createdAt = docRootElement.getChildText("created-at");
      if (createdAt != null && !createdAt.equals("")) {
        g.setCreatedAt(MyExperimentClient.parseDate(createdAt));
      }
      
      // Updated at
      String updatedAt = docRootElement.getChildText("updated-at");
      if (updatedAt != null && !updatedAt.equals("")) {
        g.setUpdatedAt(MyExperimentClient.parseDate(updatedAt));
      }
      
      
      // Tags
      g.tags = new ArrayList<Tag>();
      g.getTags().addAll(Util.retrieveTags(docRootElement));
      
      // Comments
      g.comments = new ArrayList<Comment>();
      g.getComments().addAll(Util.retrieveComments(docRootElement, g));
      
      // Members
      g.members = new ArrayList<User>();
      
      Element membersElement = docRootElement.getChild("members");
      if (membersElement != null) {
        List<Element> memberNodes = membersElement.getChildren();
        for (Element e : memberNodes) {
          g.getMembers().add(Util.instantiatePrimitiveUserFromElement(e));
        }
      }
      // sort the items after all items have been added
      Collections.sort(g.getMembers());
      
      
      // Shared Items
      g.sharedItems = new ArrayList<Resource>();
      
      Element sharedItemsElement = docRootElement.getChild("shared-items");
      if (sharedItemsElement != null) {
        List<Element> itemsNodes = sharedItemsElement.getChildren();
        for (Element e : itemsNodes) {
          g.getSharedItems().add(Util.instantiatePrimitiveResourceFromElement(e));
        }
      }
      // sort the items after all items have been added
      Collections.sort(g.getSharedItems());
      
      
      logger.debug("Found information for group with ID: " + g.getID() + ", Title: " + g.getTitle());
    }
    catch (Exception e) {
      logger.error("Failed midway through creating group object from XML", e);
    }
    
    // return created group instance
    return(g);
  }
}
