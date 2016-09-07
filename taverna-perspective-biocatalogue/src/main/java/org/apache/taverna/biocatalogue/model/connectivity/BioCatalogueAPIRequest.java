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
 * A class to wrap BioCatalogue API requests - will include
 * the type (GET, POST, etc), URL and the data to send
 * if that's a POST / PUT request. 
 * 
 * @author Sergejs Aleksejevs
 */
public class BioCatalogueAPIRequest
{
  public static enum TYPE {
    GET,
    POST,
    PUT,
    DELETE
  }
  
  
  private TYPE requestType;
  private String url;
  private String data;
  
  
  public BioCatalogueAPIRequest(TYPE requestType, String url, String data) {
    this.requestType = requestType;
    this.url = url;
    this.data = data;
  }
  
  
  public TYPE getRequestType() {
    return requestType;
  }
  
  public String getURL(){
    return url;
  }
  public void setURL(String url) {
    this.url = url;
  }
  
  public String getData(){
    return data;
  }
  
}
