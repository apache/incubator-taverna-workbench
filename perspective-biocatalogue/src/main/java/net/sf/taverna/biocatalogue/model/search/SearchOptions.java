package net.sf.taverna.biocatalogue.model.search;

import java.util.Collections;
import java.util.List;

import net.sf.taverna.biocatalogue.model.Tag;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.ui.SearchOptionsPanel;
import net.sf.taverna.biocatalogue.ui.TagSelectionDialog;

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