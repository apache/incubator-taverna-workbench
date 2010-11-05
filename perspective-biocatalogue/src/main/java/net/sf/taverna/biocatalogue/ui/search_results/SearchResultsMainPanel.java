package net.sf.taverna.biocatalogue.ui.search_results;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.search.SearchInstance;
import net.sf.taverna.biocatalogue.model.search.SearchInstanceTracker;
import net.sf.taverna.biocatalogue.model.search.SearchOptions;
import net.sf.taverna.biocatalogue.model.search.ServiceFilteringSettings;
import net.sf.taverna.biocatalogue.ui.JPanelWithOverlay;
import net.sf.taverna.biocatalogue.ui.SearchHistoryAndFavouritesPanel;
import net.sf.taverna.biocatalogue.ui.filtertree.FilterTreePane;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;

import org.apache.log4j.Logger;

/**
 * This class represents the main panel that deals with the status
 * and results of the current search.
 * 
 * It has a status label, spinner to depict search in progress,
 * actual search results split into tabs by their type, a toolbar
 * with search history, favourite searches settings, favourite filters,
 * ability to restart last search, etc.
 * 
 * @author Sergejs Aleksejevs
 */
public class SearchResultsMainPanel extends JPanel implements ActionListener, SearchInstanceTracker
{
  private final MainComponent pluginPerspectiveMainComponent;
  private final SearchResultsMainPanel instanceOfSelf;
  private Logger logger;
  
  private LinkedHashMap<TYPE, JComponent> searchResultTabs;
  private Map<TYPE, SearchResultsListingPanel> searchResultListings;
  
  // holds a reference to the instance of the search instances in the current context
  // that should be active at the moment (will aid early termination of older searches
  // when new ones are started)
  private Map<TYPE, SearchInstance> currentSearchInstances;
  
  // holds a map of references to the current instances of filter trees per resource type
  private Map<TYPE, FilterTreePane> currentFilterPanes;
  
  
  // COMPONENTS
  private JTabbedPane tabbedSearchResultPanel;
  
  private JPanelWithOverlay searchResultsWithSearchHistoryAndFavouritesOverlay;
  private SearchHistoryAndFavouritesPanel searchHistoryAndFavouritesPanel;
  
  // toolbar and action buttons for it
  private JToolBar tbSearchActions;
  protected JToggleButton bToggleSearchHistory;
  protected JButton bRefreshLastSearch;
  protected JButton bClearSearchResults;
  
  
  public SearchResultsMainPanel()
  {
    this.instanceOfSelf = this;
    this.pluginPerspectiveMainComponent = MainComponentFactory.getSharedInstance();
    this.logger = Logger.getLogger(SearchResultsMainPanel.class);
    
    this.currentSearchInstances = new HashMap<TYPE,SearchInstance>();
    
    this.searchResultListings = new HashMap<TYPE, SearchResultsListingPanel>();
    this.currentFilterPanes = new HashMap<TYPE,FilterTreePane>();
    this.searchResultTabs = new LinkedHashMap<TYPE, JComponent>(); // crucial to preserve the order -- so that these tabs always appear in the UI in the same order!
    initialiseResultTabsMap();
    
    initialiseUI();
  }
  
  
  private void initialiseUI()
  {
    // create a panel for tabbed listings of search results
    this.tabbedSearchResultPanel = new JTabbedPane();
    reloadResultTabsFromMap();
    
    // FIXME - create the overlay with search history
//    // create panel with search history and favourite searches -
//    // wrap both of them into a panel with overlay
//    searchHistoryAndFavouritesPanel = new SearchHistoryAndFavouritesPanel(this);
//    searchResultsWithSearchHistoryAndFavouritesOverlay = 
//      new JPanelWithOverlay(tabbedSearchResultPanel, searchHistoryAndFavouritesPanel, JPanelWithOverlay.HORIZONTAL_SPLIT, false, true, true);
    
    // pack all main components together
    JPanel jpMainResultsPanel = new JPanel(new BorderLayout());
    jpMainResultsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 3));
    
    // FIXME - delete next line and reenable one below it to place the overlay over the main tabbed results panel 
    jpMainResultsPanel.add(tabbedSearchResultPanel, BorderLayout.CENTER);
//    jpMainResultsPanel.add(searchResultsWithSearchHistoryAndFavouritesOverlay, BorderLayout.CENTER);
    
    
    // --- Create action toolbar ---
    // FIXME - create action toolbar!
//    bToggleSearchHistory = new JToggleButton(ResourceManager.getImageIcon(ResourceManager.HISTORY_ICON));
//    bToggleSearchHistory.setToolTipText("View your favourite searches and search history");
//    bToggleSearchHistory.addActionListener(new ActionListener() {
//      public void actionPerformed(ActionEvent e) {
//        searchResultsWithSearchHistoryAndFavouritesOverlay.setOverlayVisible(bToggleSearchHistory.isSelected());
//      }
//    });
//    searchResultsWithSearchHistoryAndFavouritesOverlay.registerOverlayActivationToggleButton(bToggleSearchHistory);
//    
//    bRefreshLastSearch = new JButton(ResourceManager.getImageIcon(ResourceManager.REFRESH_ICON));
//    bRefreshLastSearch.setToolTipText("Run previous search once again");
//    bRefreshLastSearch.addActionListener(this);
//    bRefreshLastSearch.setEnabled(false);
//    
//    bClearSearchResults = new JButton(ResourceManager.getImageIcon(ResourceManager.CLEAR_ICON));
//    bClearSearchResults.setToolTipText("Clear the search results (and stop current search if it is running)");
//    bClearSearchResults.addActionListener(this);
//    bClearSearchResults.setEnabled(false);
//    
//    tbSearchActions = new JToolBar(JToolBar.VERTICAL);
//    tbSearchActions.setBorderPainted(true);
//    tbSearchActions.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 3));
//    tbSearchActions.setFloatable(false);
//    tbSearchActions.addSeparator(new Dimension(0, 31));  // creates vertical spacing, so that toolbar buttons start at the top of results tabbed pane
//    tbSearchActions.add(bToggleSearchHistory);
//    tbSearchActions.addSeparator();
//    tbSearchActions.add(bRefreshLastSearch);
//    tbSearchActions.add(bClearSearchResults);

    // --- Put together all parts ---
    // main components in the middle, toolbar on the right
    this.setMinimumSize(new Dimension(450, 50));
    this.setLayout(new BorderLayout());
    this.add(jpMainResultsPanel, BorderLayout.CENTER);
    
    // FIXME - add toolbar to the main window!
//    this.add(tbSearchActions, BorderLayout.EAST);
  }
  
  
  
  // ----- Hiding / Showing tabs for various search result types -----
  
  /**
   * Dynamically populates the map of resource types and components that represent these types
   * in the tabbed pane -- this is only to be done once during the initialisation.
   */
  private void initialiseResultTabsMap()
  {
    for (TYPE t : TYPE.values()) {
      toggleResultTabsInMap(t, t.isDefaultSearchType());
    }
  }
  
  
  /**
   * Adds or removes a tab for a specified type of resource.
   * 
   * @param type Resource type for which the tab is to be added / removed.
   * @param doShowTab Defines whether to add or remove tab for this resource type.
   */
  public void toggleResultTabsInMap(TYPE type, boolean doShowTab)
  {
    JPanel jpResultTabContent = null;
    
    if (doShowTab)
    {
      jpResultTabContent = new JPanel(new GridLayout());
      
      // decide if this resource type supports filtering
      if (type.isSuitableForFiltering()) {
          FilterTreePane filterTreePane = new FilterTreePane(type);
          this.currentFilterPanes.put(type, filterTreePane);
      }
      else {
        // not suitable for filtering - record this in a map
        this.currentFilterPanes.put(type, null);
      }
      
      
      SearchResultsListingPanel resultsListingPanel = new SearchResultsListingPanel(type, this);
      this.searchResultListings.put(type, resultsListingPanel);
      
      if (this.currentFilterPanes.get(type) == null) {
        jpResultTabContent.add(resultsListingPanel);
      }
      else {
        JSplitPane spFiltersAndResultListing = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        spFiltersAndResultListing.setLeftComponent(this.currentFilterPanes.get(type));
        spFiltersAndResultListing.setRightComponent(resultsListingPanel);
        jpResultTabContent.add(spFiltersAndResultListing);
      }
    }
    else {
      // tab for this type is being hidden - just remove the references
      // to the search result listing and to filter pane 
      this.searchResultListings.put(type, null);
      this.currentFilterPanes.put(type, null);
    }
    
    this.searchResultTabs.put(type, jpResultTabContent);
  }
  
  
  /**
   * (Re-)loads the user interface from the internal map.
   */
  public void reloadResultTabsFromMap()
  {
    Component selectedTabsComponent = tabbedSearchResultPanel.getSelectedComponent();
    tabbedSearchResultPanel.removeAll();
    for (TYPE type : this.searchResultTabs.keySet()) {
      JComponent c = this.searchResultTabs.get(type);
      if (c != null) {
        tabbedSearchResultPanel.addTab(type.getCollectionName(), type.getIcon(), c, type.getCollectionTabTooltip());
      }
    }
    
    // attempt to re-select the same tab that was open before reloading
    try {
      tabbedSearchResultPanel.setSelectedComponent(selectedTabsComponent);
    }
    catch (IllegalArgumentException e) {
      // failed - probably previously selected tab got removed - select the first one
      tabbedSearchResultPanel.setSelectedIndex(0);
    }
  }
  
  
  /**
   * @param resourceType Resource type to look for.
   * @return Current index of the tab in the results tabbed pane view
   *         that holds a component showing search results for this type.
   *         Returns <code>-1</code> if requested type is not currently displayed.
   */
  protected int getTabIndexForResourceType(TYPE resourceType) {
    return (tabbedSearchResultPanel.indexOfComponent(searchResultTabs.get(resourceType)));
  }
  
  
  // ----- ------
  
  
  /**
   * This method is intended to be called when filter options in one of the tabs change.
   * It starts the new filtering operation.
   * 
   * Effectively it sets the filtering parameters for the SearchInstance
   * and then starts a new search with that {@link SearchInstance} wrapped into {@link SearchOptions}.
   * 
   * @param resourceType Resource type for which the new filtering operation is started
   * @param filteringSettings Filtering settings for the current filtering operation
   *                          obtained from the filter tree (or favourite filters).
   */
  public void startNewFiltering(TYPE resourceType, ServiceFilteringSettings filteringSettings)
  {
    SearchInstance siPreviousSearchForThisType = getCurrentSearchInstance(resourceType);
    
    // pass on the filtering parameters to the relevant search instance (this will overwrite the old ones if any were present!)
    if (siPreviousSearchForThisType == null) {
      // no filterings have been done earlier for this resource type;
      // we'll need a new (blank) query search SearchInstance and
      // wrap it into a service filtering SearchInstance
      siPreviousSearchForThisType = new SearchInstance(new SearchInstance("", resourceType), filteringSettings);
    }
    else {
      if (!siPreviousSearchForThisType.isServiceFilteringSearch()) {
        // just wrap existing search instance that was (probably) transferred from the Search tab
        // into another SearchInstance that explicitly deals with service filtering
        siPreviousSearchForThisType = new SearchInstance(siPreviousSearchForThisType, filteringSettings);
      }
      else {
        // previous search instance dealt with filtering -
        // simply update the filtering settings (but before that
        // run a 'deep copy' of the original search instance, so
        // that the new one gets a new reference; this will aid
        // in early termination of older filterings)
        siPreviousSearchForThisType = siPreviousSearchForThisType.deepCopy();
        siPreviousSearchForThisType.setFilteringSettings(filteringSettings);
      }
    }
    
    // proceed with "search" as usual - it will treat this search instance differently
    // from "ordinary" search
    startNewSearch(new SearchOptions(siPreviousSearchForThisType));
  }
  
  
  /**
   * Worker method responsible for starting a new search via the API.
   * 
   * This method is to be used when a *new* search is started. It will
   * mainly make updates to the UI and store the new search in the history.
   */
  public void startNewSearch(final SearchOptions searchOptions)
  {
    try
    {
      for (final TYPE resourceType : searchOptions.getResourceTypesToSearchFor())
      {
        SearchInstance si = null;
        switch (searchOptions.getSearchType()) {
          case QuerySearch: si = new SearchInstance(searchOptions.getSearchString(), resourceType);
                            resetAllFilterPanes();
                            break;
                            
          case TagSearch:   if (resourceType.isSuitableForTagSearch()) {
                              si = new SearchInstance(searchOptions.getSearchTags(), resourceType);
                              resetAllFilterPanes();
                            }
                            else {
                              // FIXME implement this... - show "no results" in the appropriate tab
                              JOptionPane.showMessageDialog(null, "'" + resourceType.getTypeName() + "' resource type is not suitable for tag search");
                            }
                            break;
                            
          case Filtering:   if (resourceType.isSuitableForFiltering()) {
                              si = searchOptions.getPreconfiguredSearchInstance();
                            }
                            else {
                              // FIXME implement this... - show "no results" in the appropriate tab
                              JOptionPane.showMessageDialog(null, "'" + resourceType.getTypeName() + "' resource type is not suitable for filtering");
                            }
                            break;
        }
        
        // Record 'this' search instance and set it as the new "primary" one for
        // this resource type;
        // (this way it if a new search thread starts afterwards, it is possible to
        //  detect this and stop the 'older' search, because it is no longer relevant)
        registerSearchInstance(resourceType, si);
        
        // start spinner icon for this tab to indicate search in progress - also show status message
        setSpinnerIconForTab(resourceType);
        setDefaultTitleForTab(resourceType);
        searchResultListings.get(resourceType).setSearchStatusText("Searching for " + si.getDescriptionStringForSearchStatus() + "...", true);
        
        
        // start the actual search
        final SearchInstance siToStart = si;
        new Thread(searchOptions.getSearchType() + " of " + resourceType.getCollectionName() + " via the API") {
          public void run() {
            siToStart.startNewSearch(instanceOfSelf, new CountDownLatch(1), searchResultListings.get(resourceType));  // FIXME - the new countdown latch is never used...
          }
        }.start();
      }
    }
    catch (Exception e) {
      logger.error("Error while searching via BioCatalogue API. Error details attached.", e);
    }
    
    
    
    // FIXME
//    // search was initiated - allow to re-run it at any time now
//    bRefreshLastSearch.setEnabled(true);
//    
//    // NB! this is required for search to be treated as "new" one - it could be that
//    //     this method is called as a search from history/favourites, but this *must*
//    //     appear as a new search either way
//    searchInstance.clearSearchResults();
//    
//    // store these search settings as the current search
//    this.siPreviousSearch = searchInstance;
//    
//    // update search history (but only do so when working within the Search Tab)
//    this.searchHistoryAndFavouritesPanel.addToSearchHistory(searchInstance);
//    
//    // now call another worker method to perform the remainder of search operations
//    // which are common for new searches and fetching more results
//    startSearch(searchOptions);
  }
  
  
  
  /**
   * Clears selection of filtering criteria and collapses any expanded nodes
   * in all filter tree panes.<br/><br/>
   * 
   * To be used for resetting all filter panes when the new query / tag
   * search starts.
   */
  private void resetAllFilterPanes() {
    for (FilterTreePane filterTreePane : this.currentFilterPanes.values()) {
      if (filterTreePane != null) {
        filterTreePane.clearSelection();
        filterTreePane.collapseAll();
      }
    }
  }
  
  
  protected void setSpinnerIconForTab(TYPE resourceType) {
    tabbedSearchResultPanel.setIconAt(getTabIndexForResourceType(resourceType), ResourceManager.getImageIcon(ResourceManager.SPINNER));
  }
  
  protected void setDefaultIconForTab(TYPE resourceType) {
    this.tabbedSearchResultPanel.setIconAt(getTabIndexForResourceType(resourceType), resourceType.getIcon());
  }
  
  
  /**
   * Same as {@link SearchResultsMainPanel#setDefaultTitleForTab(TYPE)},
   * but allows to append a specified string at the end of the default title.
   * 
   * @param resourceType
   * @param suffix
   */
  protected void setDefaultTitleForTabWithSuffix(TYPE resourceType, String suffix) {
    tabbedSearchResultPanel.setTitleAt(getTabIndexForResourceType(resourceType),
        resourceType.getCollectionName() + (suffix == null ? "" : suffix) );
  }
  
  
  /**
   * Sets default title for a tab that contains panel representing 
   * search results of the specified resource type. Default title
   * is just a name of the collections of resources in that tab. 
   * 
   * @param resourceType 
   */
  protected void setDefaultTitleForTab(TYPE resourceType) {
    setDefaultTitleForTabWithSuffix(resourceType, null);
  }
  
  
  /**
   * @param resourceType Resource type for which the search result listing panel is requested.
   * @return Reference to the requested panel or <code>null</code> if a tab for the specified
   *         <code>resourceType</code> does not exist.
   */
  protected SearchResultsListingPanel getResultsListingFor(TYPE resourceType) {
    return (this.searchResultListings.get(resourceType));
  }
  
  
  /**
   * @param resourceType Resource type for which filter tree pane is to be returned.
   * @return Reference to the requested filter tree pane or <code>null</code> if
   *         there is no search result tab for the specified <code>resourceType</code>
   *         (or if that <code>resourceType</code> does not support filtering).
   */
  protected FilterTreePane getFilterTreePaneFor(TYPE resourceType) {
    return (this.currentFilterPanes.get(resourceType));
  }
  
  
  /**
   * @return An instance of the JPanel that holds search history,
   *         favourite searches and filters.
   */
  public SearchHistoryAndFavouritesPanel getHistoryAndFavouritesPanel() {
    return (this.searchHistoryAndFavouritesPanel);
  }
  
  
  /**
   * @return An instance of the JPanel with the overlay of search history and
   *         favourite searches.
   */
  public JPanelWithOverlay getHistoryAndFavouritesOverlayPanel() {
    return (this.searchResultsWithSearchHistoryAndFavouritesOverlay);
  }
  
  
  // *** Callback for ActionListener interface ***
  
  public void actionPerformed(ActionEvent e)
  {
    // FIXME -- remove this...
//    if (e.getSource().equals(bRefreshLastSearch))
//    {
//      // restore state of the search options panel
//      pluginPerspectiveMainComponent.getSearchTab().restoreSearchOptions(siPreviousSearch);
//      
//      // completely re-run the previous search
//      startNewSearch(siPreviousSearch);
//    }
//    else if (e.getSource().equals(bClearSearchResults))
//    {
//      // manual request to clear results of previous search
//      
//      // if any search thread was running, deactivate it as well
//      if (isSearchThreadRunning()) {
//        vCurrentSearchThreadID.set(0, null);
//      }
//      
//      // changing both - spinner image and the status text simultaneously
//      setSearchStatusText("No searches were made yet", false);
//      
//      // removed the previous search, hence makes no sense to allow to clear "previous" results again
//      bClearSearchResults.setEnabled(false);
//      
//      // only remove data about previous search and disable refresh button 
//      // if no search thread is currently running - otherwise keep the button
//      // enabled in case there is a need to re-start the search if it's frozen
//      if (!isSearchThreadRunning()) {
//        siPreviousSearch = null;
//        bRefreshLastSearch.setEnabled(false);
//      }
//      
//      // also notify tabbed results panel, so that it removes the actual search results 
//      searchResultsPanel.clearPreviousSearchResults();
//    }
//    else if (e.getSource().equals(this.jclPreviewCurrentFilteringCriteria))
//    {
//      // open a preview window showing current filtering settings
//      SwingUtilities.invokeLater(new Runnable()
//      {
//        public void run() {
//          ServiceFilteringSettingsPreview p = new ServiceFilteringSettingsPreview(siPreviousSearch.getFilteringSettings());
//          p.setVisible(true);
//        }
//      });
//      
//    }
  }
  
  
  // *** Callbacks for SearchInstanceTracker interface ***
  
  public synchronized void clearPreviousSearchInstances() {
    this.currentSearchInstances.clear();
  }
  
  public synchronized boolean isCurrentSearchInstance(TYPE searchType, SearchInstance searchInstance) {
    // NB! it is crucial to perform test by reference here (hence the use of "==", not equals()!)
    return (this.currentSearchInstances.get(searchType) == searchInstance);
  }
  
  public synchronized void registerSearchInstance(TYPE searchType, SearchInstance searchInstance) {
    this.currentSearchInstances.put(searchType, searchInstance);
  }
  
  public synchronized SearchInstance getCurrentSearchInstance(TYPE searchType) {
    return this.currentSearchInstances.get(searchType);
  }
  
}
