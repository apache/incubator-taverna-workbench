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
 * Trivial class to represent a generic pair of objects.
 * Any types of objects can be used.
 * 
 * @author Sergejs Aleksejevs
 *
 * @param <T1> Type of the first object.
 * @param <T2> Type of the second object.
 */
public class Pair<T1,T2>
{
  private final T1 firstObject;
  private final T2 secondObject;

  public Pair(T1 firstObject, T2 secondObject) {
    this.firstObject = firstObject;
    this.secondObject = secondObject;
  }
  
  public T1 getFirstObject() {
    return firstObject;
  }
  
  public T2 getSecondObject() {
    return secondObject;
  }
  
}
