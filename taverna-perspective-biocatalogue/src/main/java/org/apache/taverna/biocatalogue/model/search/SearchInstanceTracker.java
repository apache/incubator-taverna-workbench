package org.apache.taverna.biocatalogue.model.search;
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

import org.apache.taverna.biocatalogue.model.Resource;

/**
 * Implementations of this interface will keep track of
 * current search instances for different resource types
 * (under assumption that {@link SearchInstance} classes
 * can only deal with one resource type per instance).
 * 
 * In the BioCatalogue plugin it is one of the UI components
 * that needs to keep track of active search instances. This
 * interface helps to decouple the search engine model from the
 * UI.
 * 
 * @author Sergejs Aleksejevs
 */
public interface SearchInstanceTracker
{
  /**
   * Clears all records of previous search instances.
   */
  public void clearPreviousSearchInstances();
  
  /**
   * Registers an instance of {@link SearchInstance} class
   * as a current one for a specific resource type.
   * 
   * Repeated calls to this method with the same parameter
   * should overwrite old values.
   * 
   * @param searchType Resource type to associate the registered
   *                   {@link SearchInstance} with.
   */
  public void registerSearchInstance(Resource.TYPE searchType, SearchInstance searchInstance);
  
  
  /**
   * Tests if provided {@link SearchInstance} is registered as the
   * current one.
   * 
   * @param searchType Resource type to perform the test for.
   * @param searchInstance {@link SearchInstance} object that is expected to be
   *                       the current search instance for the specified resource type.
   * @return <code>true</code> - if the provided <code>searchInstance</code> is indeed
   *                       currently registered as the active one for the given resouce type;<br/>
   *         <code>false</code> - otherwise.
   */
  public boolean isCurrentSearchInstance(Resource.TYPE searchType, SearchInstance searchInstance);
  
  
  /**
   * @param searchType
   * @return Currently active {@link SearchInstance} object for the specified resource type.
   */
  public SearchInstance getCurrentSearchInstance(Resource.TYPE searchType);
}
