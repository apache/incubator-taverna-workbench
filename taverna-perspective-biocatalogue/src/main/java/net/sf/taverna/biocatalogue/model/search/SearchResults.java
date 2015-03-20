package net.sf.taverna.biocatalogue.model.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.biocatalogue.model.LoadingResource;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.connectivity.BeansForJSONLiteAPI;
import net.sf.taverna.biocatalogue.model.connectivity.BeansForJSONLiteAPI.ResourceIndex;
import net.sf.taverna.biocatalogue.model.connectivity.BeansForJSONLiteAPI.ResourceLinkWithName;

import org.apache.log4j.Logger;

import org.biocatalogue.x2009.xml.rest.ResourceLink;


/**
 * Generic class for any kinds of search results.
 * 
 * @author Sergejs Aleksejevs
 */
public class SearchResults implements Serializable
{
  private static final long serialVersionUID = 6994685875323246165L;
  
  private transient Logger logger; // don't want to serialise the logger...
  
  private final TYPE typeOfResourcesInTheResultSet;
  private final int totalResultCount;
  
  // Data store for found items
  protected ArrayList<ResourceLink> foundItems;
  private int fullyFetchedItemCount;

  
  
  public SearchResults(TYPE typeOfResourcesInTheResultSet, BeansForJSONLiteAPI.ResourceIndex resourceIndex)
  {
    this.typeOfResourcesInTheResultSet = typeOfResourcesInTheResultSet;
    this.totalResultCount = resourceIndex.getResources().length;
    this.fullyFetchedItemCount = 0;
    
    this.logger = Logger.getLogger(this.getClass());
    
    initialiseSearchResultCollection(resourceIndex);
  }
  
  
  /**
   * The collection of results is initialised to cater for the expected number of
   * values - placeholder with just a name and URL for each of the expected result entries is stored.
   * 
   * @param resourceIndex
   */
  protected void initialiseSearchResultCollection(ResourceIndex resourceIndex)
  {
    foundItems = new ArrayList<ResourceLink>();
    foundItems.ensureCapacity(getTotalMatchingItemCount());
    
    ResourceLinkWithName resourceLink = null;
    for (int i = 0; i < getTotalMatchingItemCount(); i++) {
      resourceLink = resourceIndex.getResources()[i];
      this.foundItems.add(new LoadingResource(resourceLink.getURL(), resourceLink.getName()));
    }
  }
  
  
  public synchronized void addSearchResults(List<ResourceLink> searchResultsData, int positionToStartAddingResults)
  {
    // only update a specific portion of results
    for (int i = 0; i < searchResultsData.size(); i++) {
      this.foundItems.set(i + positionToStartAddingResults, searchResultsData.get(i));
    }
    
    fullyFetchedItemCount += searchResultsData.size();
  }
  
  
  public TYPE getTypeOfResourcesInTheResultSet() {
    return typeOfResourcesInTheResultSet;
  }
  
  
  /**
   * @return List of resources that have matched the search query
   *         and/or specified filtering criteria. 
   */
  public List<ResourceLink> getFoundItems() {
    return (this.foundItems);
  }
  
  
  /**
   * @return Number of resources that have matched the search query
   *         (and/or specified filtering criteria) that have already been
   *         fetched.
   */
  public int getFetchedItemCount() {
    return (this.fullyFetchedItemCount);
  }
  
  
  /**
   * @return Total number of resources that have matched the search query
   *         (and/or specified filtering criteria) - most of these will
   *         likely not be fetched yet.
   */
  public int getTotalMatchingItemCount() {
    return (this.totalResultCount);
  }
  
  
  /**
   * @return Total number of pages in the current result set.
   */
  public int getTotalResultPageNumber() {
    int numberOfResourcesPerPageForThisResourceType = this.getTypeOfResourcesInTheResultSet().getApiResourceCountPerIndexPage();
    return (int)(Math.ceil((double)getTotalMatchingItemCount() / numberOfResourcesPerPageForThisResourceType));
  }
  
  
  /**
   * List of matching items will be partial and populated sequentially
   * based on user actions. Therefore, this method helps to check
   * which list entries are still not populated.
   * 
   * @param startIndex Beginning of the range to check.
   * @param endIndex End of the range to check.
   * @return Zero-based index of the first entry in the list of
   *         matching resources that hasn't been fetched yet.
   *         Will return <code>-1</code> if the provided range
   *         parameters are incorrect or if all items in the
   *         specified range are already available.
   */
  public int getFirstMatchingItemIndexNotYetFetched(int startIndex, int endIndex)
  {
    // check the specified range is correct
    if (startIndex < 0 || endIndex > getTotalMatchingItemCount() - 1) {
      return (-1);
    }
    
    // go through the search results in the specified range
    // in an attempt to find an item that hasn't been fetched
    // just yet
    for (int i = startIndex; i <= endIndex; i++) {
      ResourceLink item = this.foundItems.get(i);
      if (item != null && item instanceof LoadingResource && !((LoadingResource)item).isLoading()) {
        return (i);
      }
    }
    
    // apparently, all items in the provided range are fetched
    return (-1);
  }
  
  
  
  /**
   * @param matchingItemIndex Index of the matching item from search results.
   * @return Index (starting from "1") of page in the search results, where
   *         the matching item with a specified index is located. If the
   *         <code>matchingItemIndex</code> is wrong, <code>-1</code> is returned.
   */
  public int getMatchingItemPageNumberFor(int matchingItemIndex)
  {
    // check the specified index is correct
    if (matchingItemIndex < 0 || matchingItemIndex > getTotalMatchingItemCount() - 1) {
      return (-1);
    }
    
    int resultsPerPageForThisType = this.getTypeOfResourcesInTheResultSet().getApiResourceCountPerIndexPage();
    return (matchingItemIndex / resultsPerPageForThisType + 1);
  }
  
  
  /**
   * @param resultPageNumber Number of the page, for which the calculations are to be done.
   * @return Index of the first result entry on the specified result page. If <code>resultPageNumber</code>
   *         is less than <code>1</code> or greater than the total number of pages in the result set,
   *         a value of <code>-1</code> will be returned.
   */
  public int getFirstItemIndexOn(int resultPageNumber)
  {
    // page number must be in a valid range - starting with 1..onwards
    if (resultPageNumber < 1 || resultPageNumber > getTotalResultPageNumber()) {
      return (-1);
    }
    
    int numberOfResourcesPerPageForThisResourceType = this.getTypeOfResourcesInTheResultSet().getApiResourceCountPerIndexPage();
    return ((resultPageNumber - 1) * numberOfResourcesPerPageForThisResourceType);
  }
  
  
  
  /**
   * Mainly for testing - outputs number of search results per item type.
   */
  public String toString()
  {
    // FIXME
    
//    StringBuilder out = new StringBuilder("Breakdown of item counts by type:\n");
//    for (Map.Entry<Integer,String> itemTypeNamePair : Resource.ALL_SUPPORTED_RESOURCE_COLLECTION_NAMES.entrySet()) {
//      out.append(itemTypeNamePair.getValue() + ": " +getFetchedItemCount(itemTypeNamePair.getKey()) +
//                 "/" + getTotalItemCount(itemTypeNamePair.getKey()) + "\n");
//    }
//    
//    return (out.toString());
    
    return ("search results... not implemented!!!");
  }
  
}
