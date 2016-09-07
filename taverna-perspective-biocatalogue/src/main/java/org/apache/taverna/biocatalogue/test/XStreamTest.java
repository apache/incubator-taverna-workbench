package org.apache.taverna.biocatalogue.test;
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

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.apache.taverna.biocatalogue.model.SoapOperationIdentity;


public class XStreamTest
{

  public static void main(String[] args)
  {
    List<SoapOperationIdentity> processors = new ArrayList<SoapOperationIdentity>();
    processors.add(new SoapOperationIdentity("http://www.test.com/test.wsdl", "aa", null));
    processors.add(new SoapOperationIdentity("http://www.example.com/example.wsdl", "bb", null));
    
    XStream xstream = new XStream(new DomDriver());
    String xml = xstream.toXML(processors);
    
    System.out.println(xml);
    
    List<SoapOperationIdentity> processorsFromXML = (List<SoapOperationIdentity>)xstream.fromXML(xml);
    System.out.println("\n\n");
    System.out.println(processorsFromXML.get(0).getWsdlLocation() + " - " + processorsFromXML.get(0).getOperationName());
    System.out.println(processorsFromXML.get(1).getWsdlLocation() + " - " + processorsFromXML.get(1).getOperationName());
  }

}
