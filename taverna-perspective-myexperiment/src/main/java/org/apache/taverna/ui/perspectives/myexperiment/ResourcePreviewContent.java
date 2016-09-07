package org.apache.taverna.ui.perspectives.myexperiment;
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

import javax.swing.JComponent;

import org.apache.taverna.ui.perspectives.myexperiment.model.Resource;

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
