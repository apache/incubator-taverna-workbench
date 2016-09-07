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

import com.google.gson.Gson;

public class GSONTest
{

  public static void main(String[] args) throws Exception
  {
    String json = "[{\"annotatable\":{\"name\":\"IndexerService\",\"resource\":\"http://sandbox.biocatalogue.org/services/2158\",\"type\":\"Service\"},\"self\":\"http://sandbox.biocatalogue.org/annotations/47473\",\"value\":{\"resource\":\"http://sandbox.biocatalogue.org/tags/indexing\",\"type\":\"Tag\",\"content\":\"indexing\"},\"version\":1,\"created\":\"2010-01-13T09:24:04Z\",\"source\":{\"name\":\"Marco Roos\",\"resource\":\"http://sandbox.biocatalogue.org/users/48\",\"type\":\"User\"},\"attribute\":{\"name\":\"Tag\",\"resource\":\"http://sandbox.biocatalogue.org/annotation_attributes/2\",\"identifier\":\"http://www.biocatalogue.org/attribute#Category\"}}]";
    
    Gson gson = new Gson();
    AnnotationBean[] a = gson.fromJson(json, AnnotationBean[].class);
    
    System.out.println("Self URL: " + a[0].self);
    System.out.println("Annotatable resource: " + a[0].annotatable.resource);
  }

}
