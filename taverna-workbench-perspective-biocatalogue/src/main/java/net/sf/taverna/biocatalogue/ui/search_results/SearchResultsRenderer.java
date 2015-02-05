package net.sf.taverna.biocatalogue.ui.search_results;

import net.sf.taverna.biocatalogue.model.search.SearchInstance;

/**
 * This interfaces avoids coupling of search engine classes
 * directly with the UI classes.
 * 
 * Search engines would send new chunks of search results
 * to the <code>SearchResultsRenderer</code> as soon as
 * they become available.
 * 
 * @author Sergejs Aleksejevs
 */
public interface SearchResultsRenderer
{
  /**
   * Render initial fragment of search results. This includes
   * creating a new <code>ListModel</code> in the results listing
   * and populating it with the first chunk of results and placeholders
   * for those results that haven't been fetched yet.
   * 
//   * @param searchThreadID This is the ID of the thread that initiated search  // FIXME
//   *                       from within the UI component, rather than the ID of
//   *                       the real worker search engine's search thread.
//   *                       It is used to test whether that thread is still active -
//   *                       to determine whether the partial results need to be rendered.
   * @param si The search instance containing partial search results to be rendered. 
   */
  void renderInitialResults(SearchInstance si);
  
  
  /**
   * Update the results listing with a specific fragment of the collection
   * of search results.
   * 
   * @param si The search instance containing partial search results to be rendered.
   * @param startIndex First index in the result collection to update.
   * @param count Number of result listing entries to update.
   *              <br/>
   *              At most <code>count</code> results will be rendered - less can be rendered
   *              if end of result list is reached earlier. <code>count</code> is normally
   *              just a page size for a specific resource type.
   */
  void renderFurtherResults(SearchInstance si, int startIndex, int count);
  
}
