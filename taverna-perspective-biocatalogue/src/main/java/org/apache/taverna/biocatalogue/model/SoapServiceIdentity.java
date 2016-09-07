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
 * Identifies a SOAP service in the most straightforward
 * way - by WSDL location. 
 * 
 * @author Sergejs Aleksejevs
 */
public class SoapServiceIdentity
{
  private final String wsdlLocation;
  
  // this variable holds an object that will be displayable
  private final Object errorDetails;

  public SoapServiceIdentity(String wsdlLocation) {
    this.wsdlLocation = wsdlLocation;
    this.errorDetails = null;
  }
  
  public SoapServiceIdentity(Object errorDetails) {
    this.errorDetails = errorDetails;
    this.wsdlLocation = null;
  }
  
  public String getWsdlLocation() {
    return (wsdlLocation);
  }
  
  public boolean hasError() {
    return (errorDetails != null);
  }
  
  /**
   * @return Returned object contains an object that may be displayed
   *         in a JOptionPane or printed (in other words defining a
   *         sensible way of displaying itself), which has details of
   *         an error that has occurred which prevented from populating
   *         this instance with the actual details of their SOAP service.
   */
  public Object getErrorDetails() {
    return (errorDetails);
  }
  
}
