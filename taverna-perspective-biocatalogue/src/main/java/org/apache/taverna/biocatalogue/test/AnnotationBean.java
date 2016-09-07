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


public class AnnotationBean
{
  public AnnotationBean() { }
  
  public String self;
  private int version;
  private String created;
  public Annotatable annotatable;
  private Source source;
  private Attribute attribute;
  private Value value;
  
  
  public static class Annotatable
  {
    private Annotatable() { }
    
    private String name;
    public String resource;
    private String type;
  }
  
  public static class Source
  {
    private Source() { }
    
    private String name;
    private String resource;
    private String type;
  }
  
  public static class Attribute
  {
    private Attribute() { }
    
    private String name;
    private String resource;
    private String identifier;
  }
  
  public static class Value
  {
    private Value() { }
    
    private String resource;
    private String type;
    private String content;
  }
}
