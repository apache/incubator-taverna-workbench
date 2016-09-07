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

/**
 * Identifies a SOAP Processor in Taverna terms. Adds a local name
 * attribute to the details available in <code>SoapOperationIdentity</code>.
 * 
 * @author Sergejs Aleksejevs
 */
public class SoapProcessorIdentity extends SoapOperationIdentity
{
  private final String localName;

  public SoapProcessorIdentity(String wsdlLocation, String operationName, String localName) {
    super(wsdlLocation, operationName, null);
    this.localName = localName;
  }
  
  public SoapProcessorIdentity(Object errorDetails) {
    super(errorDetails);
    this.localName = null;
  }
  
  public String getLocalName() {
    return localName;
  }
  
}
