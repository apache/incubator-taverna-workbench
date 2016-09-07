package org.apache.taverna.biocatalogue.model.connectivity;
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


/**
 * Binding beans for GSON library to instantiate objects
 * from JSON data obtained from the 'Lite' version of the
 * BioCatalogue JSON API.
 * 
 * @author Sergejs Aleksejevs
 */
public class BeansForJSONLiteAPI
{
  
  public static abstract class ResourceIndex
  {
    public ResourceIndex() { }
    public abstract ResourceLinkWithName[] getResources();
  }
  
  
  public static class SOAPOperationsIndex extends ResourceIndex {
    public SOAPOperationsIndex() { }
    public ResourceLinkWithName[] soap_operations;
    
    public ResourceLinkWithName[] getResources() {
      return soap_operations;
    }
  }
  
  public static class RESTMethodsIndex extends ResourceIndex {
    public RESTMethodsIndex() { }
    public ResourceLinkWithName[] rest_methods;
    
    public ResourceLinkWithName[] getResources() {
      return rest_methods;
    }
  }
  
  public static class ServicesIndex extends ResourceIndex {
    public ServicesIndex() { }
    public ResourceLinkWithName[] services;
    
    public ResourceLinkWithName[] getResources() {
      return services;
    }
  }
  
  public static class ServiceProvidersIndex extends ResourceIndex {
    public ServiceProvidersIndex() { }
    public ResourceLinkWithName[] service_providers;
    
    public ResourceLinkWithName[] getResources() {
      return service_providers;
    }
  }
  
  public static class UsersIndex extends ResourceIndex {
    public UsersIndex() { }
    public ResourceLinkWithName[] users;
    
    public ResourceLinkWithName[] getResources() {
      return users;
    }
  }
  
  
  
  public static class ResourceLinkWithName
  {
    private ResourceLinkWithName() { }
    
    private String resource;
    private String name;
    
    public String getURL() {
      return (this.resource);
    }
    
    public String getName() {
      return (this.name);
    }
  }
  
}
