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

import org.biocatalogue.x2009.xml.rest.ResourceLink;
import org.biocatalogue.x2009.xml.rest.impl.ResourceLinkImpl;

/**
 * @author Sergejs Aleksejevs
 */
public class LoadingResource extends ResourceLinkImpl
{
  private boolean nowLoading;
  private ResourceLink associatedObj;
  
  public LoadingResource(String resourceURL, String resourceName) {
    super(ResourceLink.type);
    
    associatedObj = ResourceLink.Factory.newInstance();
    associatedObj.setHref(resourceURL);
    associatedObj.setResourceName(resourceName);
    
    this.nowLoading = false;
  }
  
  public String getHref() {
    return (associatedObj.getHref());
  }
  
  public String getResourceName() {
    return (associatedObj.getResourceName());
  }
  
  public boolean isLoading() {
    return (nowLoading);
  }
  public void setLoading(boolean isLoading) {
    this.nowLoading = isLoading;
  }
  
}
