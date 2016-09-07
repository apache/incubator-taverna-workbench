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

import org.apache.taverna.biocatalogue.model.Util;

/**
 * @author Sergejs Aleksejevs
 */
public abstract class ServerResponse
{
  // this code is to be used when a local failure is encountered and the
  // server response is a blank / invalid one
  public static int LOCAL_FAILURE_CODE = -1;
  
  // server response code - in theory should correspond to HTTP response codes 
  private int iResponseCode;
  
  // URL that was used to make the request
  private String requestURL;
  
  
  public ServerResponse() {
    // do nothing - empty constructor
  }
  
  public ServerResponse(int responseCode, String requestURL) {
    super();
    this.iResponseCode = responseCode;
    this.requestURL = Util.urlDecodeQuery(requestURL);
  }
  
  
  public int getResponseCode() {
    return (this.iResponseCode);
  }
  
  public String getRequestURL() {
    return requestURL;
  }
  
}
