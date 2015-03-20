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
package net.sf.taverna.t2.ui.perspectives.myexperiment;

import javax.swing.JComponent;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Resource;

/**
 * Helper class to hold all data about the generated preview.
 * 
 * @author Sergejs Aleksejevs
 * 
 */
public class ResourcePreviewContent
{
  private Resource resource;
  private JComponent jcContent;
  
  public ResourcePreviewContent()
  {
    // empty constructor
  }
  
  public ResourcePreviewContent(Resource resource, JComponent content)
  {
    this.resource = resource;
    this.jcContent = content;
  }
  
  public Resource getResource()
  {
    return(this.resource);
  }
  
  public int getResourceType()
  {
    return(this.resource.getItemType());
  }
  
  public String getResourceTitle()
  {
    return(this.resource.getTitle());
  }
  
  public String getResourceURL()
  {
    return(this.resource.getResource());
  }
  
  public String getResourceURI()
  {
    return(this.resource.getURI());
  }
  
  public JComponent getContent()
  {
    return(this.jcContent);
  }
}
