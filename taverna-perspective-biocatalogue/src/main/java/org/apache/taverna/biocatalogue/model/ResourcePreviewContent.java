package org.apache.taverna.biocatalogue.model;
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
import javax.swing.JLabel;

import org.apache.taverna.biocatalogue.model.connectivity.BioCatalogueClient;

/**
 * Helper class to hold all data about the generated preview.
 * 
 * @author Sergejs Aleksejevs
 */
public class ResourcePreviewContent
{
  private Resource resource;
  private JComponent jcContent;
  
  public ResourcePreviewContent(Resource resource, JComponent content)
  {
    this.resource = resource;
    this.jcContent = content;
  }
  
  public Resource getResource() {
    return(this.resource);
  }
  
  public JComponent getContent() {
    return(this.jcContent);
  }
  
  
  public static ResourcePreviewContent createDummyInstance()
  {
    Resource r = new Resource(BioCatalogueClient.API_USERS_URL + "/1", "Dummy user");
    return (new ResourcePreviewContent(r, new JLabel("dummy content - JLabel")));
  }
}
