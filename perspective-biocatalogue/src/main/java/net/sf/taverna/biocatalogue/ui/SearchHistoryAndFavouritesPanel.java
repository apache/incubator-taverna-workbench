package net.sf.taverna.biocatalogue.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.lowagie.text.Font;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.search.SearchInstance;
import net.sf.taverna.biocatalogue.ui.HistoryOrFavouritesBlock.Entry;
import net.sf.taverna.biocatalogue.ui.search_results.SearchResultsMainPanel;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponent;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;


/**
 * This class represents a panel with search history and favourites (searches and filters) that is
 * intended to be shown as an overlay over the search results.
 * 
 * @author Sergejs Aleksejevs
 */
public class SearchHistoryAndFavouritesPanel extends JPanel implements ActionListener, HistoryOrFavoritesBlockEntryDetailsProvider, HistoryOrFavouritesBlockObjectCollectionChangeListener
{
  protected final static String ACTION_PREVIEW_FAVOURITE_FILTER = BioCataloguePluginConstants.APP_PREFIX + "previewFavouriteFilter";
  protected final static String ACTION_SEARCH_FROM_FAVOURITE_FILTER = BioCataloguePluginConstants.APP_PREFIX + "searchFromFavouriteFilter";
  protected final static String ACTION_SEARCH_FROM_FAVOURITE_SEARCH = BioCataloguePluginConstants.APP_PREFIX + "searchFromFavourites";
  protected final static String ACTION_SEARCH_FROM_HISTORY = BioCataloguePluginConstants.APP_PREFIX + "searchFromHistory";
  protected final static String ACTION_ADD_FAVOURITE_SEARCH_INSTANCE = BioCataloguePluginConstants.APP_PREFIX + "addFavouriteSearchInstance";
  protected final static String ACTION_REMOVE_FAVOURITE_SEARCH_INSTANCE = BioCataloguePluginConstants.APP_PREFIX + "removeFavouriteSearchInstance";
  protected final static String ACTION_REMOVE_FAVOURITE_FILTER_INSTANCE = BioCataloguePluginConstants.APP_PREFIX + "removeFavouriteFilterInstance";
  
  // main UI components
  private final MainComponent pluginPerspectiveMainComponent;
  private final SearchResultsMainPanel searchResultsMainPanel;
  
  private JPanel jpHistoryAndAllFavourites;
  private HistoryOrFavouritesBlock jpFavouriteFilters;
  private HistoryOrFavouritesBlock jpFavouriteSearches;
  private HistoryOrFavouritesBlock jpSearchHistory;
  
  // These data collections will override those in the three HistoryOrFavouritesBlock's;
  // this is required, because this panel (and the blocks) will be displayed in 2 tabs -
  // and must be kept in sync.
  private static LinkedList<SearchInstance> llFavouriteFilters;
  private static LinkedList<SearchInstance> llFavouriteSearches;
  private static LinkedList<SearchInstance> llSearchHistory;
  
  
  public SearchHistoryAndFavouritesPanel(SearchResultsMainPanel searchResultsMainPanel)
  {
    this.pluginPerspectiveMainComponent = MainComponentFactory.getSharedInstance();
    this.searchResultsMainPanel = searchResultsMainPanel;
    
    llFavouriteFilters = new LinkedList<SearchInstance>();
    llFavouriteSearches = new LinkedList<SearchInstance>();
    llSearchHistory = new LinkedList<SearchInstance>();
    
    initialiseUI();
    updateUIFromDataCollections();
  }
  
  
  private void initialiseUI()
  {
    // create separate block panels for favourite filters, favourite searches and search history
    jpFavouriteFilters = new HistoryOrFavouritesBlock("Favourite Filters", "No favourite filters", 
                                      BioCataloguePluginConstants.FAVOURITE_FILTERS_LENGTH, this);
    jpFavouriteFilters.addObjectCollectionChangeListener(this);
    
    jpFavouriteSearches = new HistoryOrFavouritesBlock("Favourite Searches", "No favourite searches", 
                                      BioCataloguePluginConstants.FAVOURITE_SEARCHES_LENGTH, this);
    jpFavouriteSearches.addObjectCollectionChangeListener(this);
    
    jpSearchHistory = new HistoryOrFavouritesBlock("Search History", "No favourite filters", 
                                      BioCataloguePluginConstants.SEARCH_HISTORY_LENGTH, this);
    jpSearchHistory.addObjectCollectionChangeListener(this);
    
    
    // put everything together into a single panel
    jpHistoryAndAllFavourites = new JPanel();
    jpHistoryAndAllFavourites.setBorder(BorderFactory.createEmptyBorder());
    jpHistoryAndAllFavourites.setLayout(new GridBagLayout());
    jpHistoryAndAllFavourites.setPreferredSize(new Dimension(0, 200));    // HACK: this is to make sure that the scroll pane which wraps
                                                                          // this panel only acts as a vertical scroll bar; this makes the
                                                                          // scroll pane think that the element is not requiring more space
                                                                          // and hence no horizontal scrolling behaviour will be shown
    
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.NORTH;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 1.0;
    c.weighty = 0;
    jpHistoryAndAllFavourites.add(jpFavouriteFilters, c);
    
    c.gridy = 1;
    jpHistoryAndAllFavourites.add(jpFavouriteSearches, c);
    
    c.gridy = 2;
    c.weighty = 1.0;
    jpHistoryAndAllFavourites.add(jpSearchHistory, c);
    
    
    // wrap the panel into a scroll pane
    JScrollPane spHistoryAndFavourites = new JScrollPane(jpHistoryAndAllFavourites);
    spHistoryAndFavourites.getVerticalScrollBar().setUnitIncrement(BioCataloguePluginConstants.DEFAULT_SCROLL);
    spHistoryAndFavourites.setBorder(BorderFactory.createEmptyBorder());
    
    
    // put components into the current panel
    this.setLayout(new BorderLayout());
    this.add(spHistoryAndFavourites, BorderLayout.CENTER);
    this.setPreferredSize(new Dimension(250, 0));       // 250 is the absolute minimum width to make sure that notification text fits into search history/favourites boxes
    this.setMinimumSize(new Dimension(150, 0));
    this.setBorder(BorderFactory.createEmptyBorder());
  }
  
  
  /**
   * Updates the view in the Search History, Favourite Searches and
   * Search History boxes from the underlying data collections for
   * each of those boxes.
   * 
   * This method is useful when the underlying data collections were
   * updates manually or when switching between main tabs in the
   * perspective, as the actual UI components are different (limitation
   * of Java Swing - can't display the same component more than once),
   * but the underlying data collections are static, and so are shared
   * among all instances of this class. So if an update was made in one
   * instance, this method "propagates" the changes in data collections
   * into the UI.
   */
  @SuppressWarnings("unchecked")
  protected void updateUIFromDataCollections()
  {
    this.jpFavouriteFilters.getObjectCollection().clear();
    this.jpFavouriteFilters.getObjectCollection().addAll(llFavouriteFilters);
    this.jpFavouriteFilters.updateUIFromObjectCollection();
    
    this.jpFavouriteSearches.getObjectCollection().clear();
    this.jpFavouriteSearches.getObjectCollection().addAll(llFavouriteSearches);
    this.jpFavouriteSearches.updateUIFromObjectCollection();
    
    this.jpSearchHistory.getObjectCollection().clear();
    this.jpSearchHistory.getObjectCollection().addAll(llSearchHistory);
    this.jpSearchHistory.updateUIFromObjectCollection();
    
    updateSearchHistoryAndFavouritesPanelPreferredSize();
  }
  
  
  protected void addToFavouriteFilters(SearchInstance searchInstance)
  {
    // want the favourite filters to be sorted by filter name & search term
    jpFavouriteFilters.addObjectToCollection(searchInstance, true);
    jpFavouriteFilters.updateUIFromObjectCollection();
    updateSearchHistoryAndFavouritesPanelPreferredSize();
  }
  
  
  protected void addToFavouriteSearches(SearchInstance searchInstance)
  {
    // want the favourite searches to be sorted alphabetically by search term
    jpFavouriteSearches.addObjectToCollection(searchInstance, true);
    jpFavouriteSearches.updateUIFromObjectCollection();
    updateSearchHistoryAndFavouritesPanelPreferredSize();
  }
  
  
  protected void addToSearchHistory(SearchInstance searchInstance)
  {
    // for search history use the natural "queue" order - no sorting required
    jpSearchHistory.addObjectToCollection(searchInstance, false);
    jpSearchHistory.updateUIFromObjectCollection();
    updateSearchHistoryAndFavouritesPanelPreferredSize();
  }
  
  
  /**
   * This helper provides a solution for the difficulty with the scroll pane
   * around both search history and favourite searches. For the scroll pane
   * to work correctly it should "know" the preferred size of the panel it
   * wraps.
   * 
   * Number of items in the two panes change, hence we have to dynamically
   * adjust the preferred size of the surrounding panel.
   */
  private void updateSearchHistoryAndFavouritesPanelPreferredSize()
  {
    // width of zero indicates that the whole of the width of the panel
    // "fits" anyway - and hence no horizontal scroll will appear either
    // way, because the scroll pane would "think" that the wrapped panel
    // doesn't require "more" width than the preferred size which is zero
    int iWidth = 0;
    
    // --- Height is something which we actually need to calculate; it depends on
    // the current contents of the panels inside ---
    
    // just sum together preferred heights of all 3 panels
    int iHeight = 0; 
    iHeight += jpFavouriteFilters.getPreferredSize().height;
    iHeight += jpFavouriteSearches.getPreferredSize().height;
    iHeight += jpSearchHistory.getPreferredSize().height;
    
    // apply the calculated size
    this.jpHistoryAndAllFavourites.setPreferredSize(new Dimension(iWidth, iHeight));
    this.jpHistoryAndAllFavourites.validate();
  }
  
  
  
  // *** Callback for ActionListener interface ***
  
  public void actionPerformed(final ActionEvent e)
  {
    if (e.getSource() instanceof JClickableLabel)
    {
      // click on one of JClickableLabels - determine which action is required
      if (e.getActionCommand().startsWith(ACTION_SEARCH_FROM_HISTORY) ||
          e.getActionCommand().startsWith(ACTION_SEARCH_FROM_FAVOURITE_SEARCH) ||
          e.getActionCommand().startsWith(ACTION_SEARCH_FROM_FAVOURITE_FILTER))
      {
        // the part of the action command that is following the prefix is the ID in the search history / favourites storage;
        // this search instance is removed from history and will be re-added at the top of it when search is launched 
        int iEntryID = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().lastIndexOf(":") + 1));
        
        HistoryOrFavouritesBlock displayPanel = null;
        if (e.getActionCommand().startsWith(ACTION_SEARCH_FROM_FAVOURITE_FILTER)) 
          displayPanel = jpFavouriteFilters;
        else if (e.getActionCommand().startsWith(ACTION_SEARCH_FROM_FAVOURITE_SEARCH))
          displayPanel = jpFavouriteSearches;
        else if (e.getActionCommand().startsWith(ACTION_SEARCH_FROM_HISTORY))
          displayPanel = jpSearchHistory;
        
        final SearchInstance si = (SearchInstance)
                                  (e.getActionCommand().startsWith(ACTION_SEARCH_FROM_HISTORY) ? 
                                   displayPanel.removeObjectFromCollectionAt(iEntryID) : 
                                   displayPanel.getObjectCollection().get(iEntryID)); // in case of favourites (filter/search), no need to remove the entry
        
        // re-set search options in the settings box and re-run the search
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            // - activate the relevant tab;
            // - restore state of the search options panel / filter tree as the search starts;
            // - run the search (and update the search history)
            if (e.getActionCommand().startsWith(ACTION_SEARCH_FROM_FAVOURITE_FILTER)) {
              // FIXME - this must be changed in such a way that in re-loads filter tree on each
              //         active resource type tab in SearchResultsMainPanel  
//              pluginPerspectiveMainComponent.getServiceFilteringTab().restoreFilteringSettings(si);
//              pluginPerspectiveMainComponent.getServiceFilteringTab().getSearchResultsMainPanel().startNewSearch(new SearchOptions(si)); // FIXME!!!
              // FIXME - end of problem
            }
            else {
              // FIXME - need to decide what will actually happen: e.g. will all tabs start loading (if saved the overall state of
              //         all search results panel) or just an individual resource type tab will start loading
//              pluginPerspectiveMainComponent.getSearchTab().restoreSearchOptions(si);
//              pluginPerspectiveMainComponent.getSearchTab().getSearchResultsMainPanel().startNewSearch(new SearchOptions(si)); // FIXME !!!
              // FIXME - end of problem
            }
            
            // now hide the overlay with search history / favourite searches - but do this in the tab
            // which was active, which is not always the Search Tab, could also be Filtering Tab 
            searchResultsMainPanel.getHistoryAndFavouritesOverlayPanel().setOverlayVisible(false);
          }
        });
      }
      
      else if (e.getActionCommand().startsWith(ACTION_ADD_FAVOURITE_SEARCH_INSTANCE))
      {
        // get the ID of the entry in the history listing first; then fetch the instance itself
        int iHistID = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().lastIndexOf(":") + 1));
        final SearchInstance si = (SearchInstance)this.jpSearchHistory.getObjectCollection().get(iHistID);
        
        // add item to favourites and re-draw the panel
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            addToFavouriteSearches(si);
            updateSearchHistoryAndFavouritesPanelPreferredSize();
          }
        });
      }
      else if (e.getActionCommand().startsWith(ACTION_REMOVE_FAVOURITE_SEARCH_INSTANCE))
      {
        // get the ID of the entry in the favourite searches listing first; then remove the instance with that ID from the list
        int iFavouriteID = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().lastIndexOf(":") + 1));
        this.jpFavouriteSearches.removeObjectFromCollectionAt(iFavouriteID);
        
        // item removed from favourites - re-draw the panel now
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            jpFavouriteSearches.updateUIFromObjectCollection();
            updateSearchHistoryAndFavouritesPanelPreferredSize();
          }
        });
      }
      else if (e.getActionCommand().startsWith(ACTION_PREVIEW_FAVOURITE_FILTER))
      {
        // open a preview window showing current filtering settings
        int iFavouriteID = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().lastIndexOf(":") + 1));
        SearchInstance si = (SearchInstance)this.jpFavouriteFilters.getObjectCollection().get(iFavouriteID);
        
        ServiceFilteringSettingsPreview p = new ServiceFilteringSettingsPreview(si.getFilteringSettings());
        p.setVisible(true);
      }
      else if (e.getActionCommand().startsWith(ACTION_REMOVE_FAVOURITE_FILTER_INSTANCE))
      {
        // get the ID of the entry in the favourite filters listing first; then remove the instance with that ID from the list
        int iFavouriteID = Integer.parseInt(e.getActionCommand().substring(e.getActionCommand().lastIndexOf(":") + 1));
        this.jpFavouriteFilters.removeObjectFromCollectionAt(iFavouriteID);
        
        // item removed from favourite filters - re-draw the panel now
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            jpFavouriteFilters.updateUIFromObjectCollection();
            updateSearchHistoryAndFavouritesPanelPreferredSize();
          }
        });
      }
    }
    
  }
  
  
  // *** Callback for HistoryOrFavoritesBlockEntryDetailsProvider interface ***
  
  /**
   * This method is required to produce three components that are
   * necessary to place an object into the history or favourites block.
   */
  public Entry provideEntryDetails(HistoryOrFavouritesBlock displayPanel, Object objectToProvideDetailsFor,
                                   int indexOfObjectInDisplayPanelDataCollection)
  {
    // prepare objects to hold return values
    JClickableLabel jclEntryLabel = null;
    JComponent entryDetailsComponent = null;
    JClickableLabel jclActionIcon = null;
    
    // either way, cast the object to SearchInstance - all 3 blocks in this panel
    // work with SearchInstance objects: favourite filters, favourite searches and
    // search history
    SearchInstance si = (SearchInstance)objectToProvideDetailsFor;
    
    // now based on where did the request come from, provide different content
    // in the required components
    if (displayPanel.equals(jpFavouriteFilters))
    {
      jclEntryLabel = new JClickableLabel(si.getFilteringSettings().getFilterName(),
          ACTION_SEARCH_FROM_FAVOURITE_FILTER + ":" + indexOfObjectInDisplayPanelDataCollection, this, ResourceManager.getImageIcon(ResourceManager.FILTER_ICON),
          SwingConstants.LEFT, si.toString());
      
       JClickableLabel jclDetails = new JClickableLabel("[" + si.getFilteringSettings().getNumberOfFilteringCriteria() + " filtering criteria]",
          ACTION_PREVIEW_FAVOURITE_FILTER + ":" + indexOfObjectInDisplayPanelDataCollection, this, null, SwingConstants.LEFT, "Preview this filter");
       jclDetails.setFont(jclDetails.getFont().deriveFont(Font.ITALIC));
       jclDetails.setRegularForegroundColor(jclDetails.getRegularForegroundColor().darker().darker());
       entryDetailsComponent = jclDetails;
      
      jclActionIcon = new JClickableLabel("",
          ACTION_REMOVE_FAVOURITE_FILTER_INSTANCE + ":" + indexOfObjectInDisplayPanelDataCollection, this,
          ResourceManager.getImageIcon(ResourceManager.DELETE_ITEM_ICON), SwingConstants.LEFT,
          "<html>Click to remove from your local favourite service filters.<br>" +
          "(This will not affect your BioCatalogue profile settings.)</html>");
    }
    else if (displayPanel.equals(jpFavouriteSearches))
    {
      jclEntryLabel = new JClickableLabel(si.getSearchTerm(),
          ACTION_SEARCH_FROM_FAVOURITE_SEARCH + ":" + indexOfObjectInDisplayPanelDataCollection, this, si.getSearchType().getIcon(),
          SwingConstants.LEFT, si.toString());
      
      entryDetailsComponent = new JLabel("[" + si.detailsAsString() + "]");
      
      jclActionIcon = new JClickableLabel("",
          ACTION_REMOVE_FAVOURITE_SEARCH_INSTANCE + ":" + indexOfObjectInDisplayPanelDataCollection, this,
          ResourceManager.getImageIcon(ResourceManager.DELETE_ITEM_ICON), SwingConstants.LEFT,
          "<html>Click to remove from your local favourite searches.<br>" +
          "(This will not affect your BioCatalogue profile settings.)</html>");
    }
    else if (displayPanel.equals(jpSearchHistory))
    {
      jclEntryLabel = new JClickableLabel(si.getSearchTerm(),
          ACTION_SEARCH_FROM_HISTORY + ":" + indexOfObjectInDisplayPanelDataCollection, this, si.getSearchType().getIcon(),
          SwingConstants.LEFT, si.toString());
      
      entryDetailsComponent = new JLabel("[" + si.detailsAsString() + "]");
      
      jclActionIcon = new JClickableLabel("",
          ACTION_ADD_FAVOURITE_SEARCH_INSTANCE + ":" + indexOfObjectInDisplayPanelDataCollection, this,
          ResourceManager.getImageIcon(ResourceManager.FAVOURITE_ICON), SwingConstants.LEFT,
          "<html>Click to add to your local favourite" +
          " searches - these will be available every time you use Taverna.<br>(This will not affect your" +
          " BioCatalogue profile settings.)</html>");
    }
    
    return (new Entry(jclEntryLabel, entryDetailsComponent, jclActionIcon));
  }
  
  
  // *** Callback for HistoryOrFavouritesBlockObjectCollectionChangeListener interface ***
  
  /**
   * Replaces local copy of the corresponding data collection, as it was changed.
   */
  @SuppressWarnings("unchecked")
  public void objectCollectionChanged(HistoryOrFavouritesBlock displayBlock)
  {
    if (displayBlock.equals(jpFavouriteFilters)) {
      llFavouriteFilters.clear();
      llFavouriteFilters.addAll(jpFavouriteFilters.getObjectCollection());
    }
    else if (displayBlock.equals(jpFavouriteSearches)) {
      llFavouriteSearches.clear();
      llFavouriteSearches.addAll(jpFavouriteSearches.getObjectCollection());
    }
    else if (displayBlock.equals(jpSearchHistory)) {
      llSearchHistory.clear();
      llSearchHistory.addAll(jpSearchHistory.getObjectCollection());
    }
    
  }
  
}
