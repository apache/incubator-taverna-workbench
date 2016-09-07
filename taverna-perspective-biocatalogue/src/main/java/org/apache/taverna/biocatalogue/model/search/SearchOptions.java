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

import java.util.Collections;
import java.util.List;

import org.apache.taverna.biocatalogue.model.Tag;
import org.apache.taverna.biocatalogue.model.Resource.TYPE;
import org.apache.taverna.biocatalogue.ui.SearchOptionsPanel;

/**
 * Instances of this class can store the state of the
 * {@link SearchOptionsPanel} / {@link TagSelectionDialog} in
 * order to help instantiate {@link SearchInstance} objects.
 * 
 * @author Sergejs Aleksejevs
 */
public class SearchOptions
{
  private SearchInstance preconfiguredSearchInstance;
  private SearchInstance.TYPE searchType;
  private String searchString;
  private List<Tag> searchTags;
  private List<TYPE> resourceTypesToSearchFor;
  
  public SearchOptions(String searchString, List<TYPE> searchTypes) {
    this.preconfiguredSearchInstance = null;
    this.searchType = SearchInstance.TYPE.QuerySearch;
    this.searchString = searchString;
    this.searchTags = null;
    this.resourceTypesToSearchFor = searchTypes;
  }
  
  public SearchOptions(List<Tag> searchTags, List<TYPE> searchTypes) {
    this.preconfiguredSearchInstance = null;
    this.searchType = SearchInstance.TYPE.TagSearch;
    this.searchString = null;
    this.searchTags = searchTags;
    this.resourceTypesToSearchFor = searchTypes;
  }
  
  public SearchOptions(SearchInstance preconfiguredSearchInstance) {
    this.preconfiguredSearchInstance = preconfiguredSearchInstance;
    this.searchType = preconfiguredSearchInstance.getSearchType();
    this.searchString = preconfiguredSearchInstance.getSearchString();
    this.searchTags = preconfiguredSearchInstance.getSearchTags();
    this.resourceTypesToSearchFor = Collections.singletonList(preconfiguredSearchInstance.getResourceTypeToSearchFor());
  }
  
  
  public SearchInstance getPreconfiguredSearchInstance() {
    return preconfiguredSearchInstance;
  }
  
  public SearchInstance.TYPE getSearchType() {
    return searchType;
  }
  
  public String getSearchString() {
    return searchString;
  }
  
  public List<Tag> getSearchTags() {
    return searchTags;
  }
  
  public List<TYPE> getResourceTypesToSearchFor() {
    return resourceTypesToSearchFor;
  }
  
}