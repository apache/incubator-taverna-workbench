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

import org.jdom.Document;

public class ServerResponse {
  // CONSTANTS
  public static int LOCAL_FAILURE = -1;

  // STORAGE
  private int iResponseCode;
  private Document docResponseBody;

  public ServerResponse() {
	// do nothing - empty constructor
  }

  public ServerResponse(int responseCode, Document responseBody) {
	super();

	this.iResponseCode = responseCode;
	this.docResponseBody = responseBody;
  }

  public int getResponseCode() {
	return (this.iResponseCode);
  }

  public void setResponseCode(int responseCode) {
	this.iResponseCode = responseCode;
  }

  public Document getResponseBody() {
	return (this.docResponseBody);
  }

  public void setResponseBody(Document responseBody) {
	this.docResponseBody = responseBody;
  }
}
