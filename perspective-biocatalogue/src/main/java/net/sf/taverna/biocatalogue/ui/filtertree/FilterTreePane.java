package net.sf.taverna.biocatalogue.ui.filtertree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.sf.taverna.biocatalogue.model.BioCataloguePluginConstants;
import net.sf.taverna.biocatalogue.model.Resource.TYPE;
import net.sf.taverna.biocatalogue.model.ResourceManager;
import net.sf.taverna.biocatalogue.model.connectivity.BioCatalogueClient;
import net.sf.taverna.biocatalogue.model.search.SearchInstance;
import net.sf.taverna.biocatalogue.model.search.ServiceFilteringSettings;
import net.sf.taverna.biocatalogue.ui.tristatetree.JTriStateTree;
import net.sf.taverna.biocatalogue.ui.tristatetree.TriStateTreeCheckingListener;
import net.sf.taverna.t2.ui.perspectives.biocatalogue.MainComponentFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import org.biocatalogue.x2009.xml.rest.Filter;
import org.biocatalogue.x2009.xml.rest.FilterGroup;
import org.biocatalogue.x2009.xml.rest.FilterType;
import org.biocatalogue.x2009.xml.rest.Filters;

/**
 * 
 * @author Sergejs Aleksejevs
 */
public class FilterTreePane extends JPanel implements TriStateTreeCheckingListener
{
  // FIXME - should be true and need to implement the whole mechanism of saving filters
  private static final boolean ENABLE_SAVING_FILTERS = false;
  
  
  private TYPE resourceType;
  private String filtersURL;
  private BioCatalogueClient client;
  private Logger logger;
  
  private FilterTreePane thisPanel;
  
  private JToolBar tbFilterTreeToolbar;
  private Action saveFilterAction;
  private Action refreshFiltersAction;
  
  
  private JPanel jpFilters = null;
  private JFilterTree filterTree;  // tree component to display filter selections
  private Filters filtersRoot;     // last filters element which was received from the API

  
  
  public FilterTreePane(TYPE resourceType)
  {
    this.thisPanel = this;
    
    this.resourceType = resourceType;
    this.filtersURL = resourceType.getAPIResourceCollectionFiltersURL();
    this.client = MainComponentFactory.getSharedInstance().getBioCatalogueClient();
    this.logger = Logger.getLogger(this.getClass());
    
    initialiseUI();
    loadFiltersAndBuildTheTree();
  }
  
  
  private void initialiseUI()
  {
    jpFilters = new JPanel();
    jpFilters.setBackground(Color.WHITE);
    
    JScrollPane spFilters = new JScrollPane(jpFilters);
    spFilters.setMinimumSize(new Dimension(235,0));
    spFilters.setPreferredSize(new Dimension(300,0));
    spFilters.getVerticalScrollBar().setUnitIncrement(BioCataloguePluginConstants.DEFAULT_SCROLL);
    
    
    tbFilterTreeToolbar = createTreeActionToolbar();
    resetTreeActionToolbar();
    
    this.setLayout(new BorderLayout());
    this.add(tbFilterTreeToolbar, BorderLayout.NORTH);
    this.add(spFilters, BorderLayout.CENTER);
  }
  
  
  /**
   * @return A toolbar that replicates all actions available in the contextual menu of
   *         the filtering tree - mainly: saving current filter, reloading filter tree,
   *         expanding/collapsing and selecting/deselecting everything in the tree.
   */
  private JToolBar createTreeActionToolbar()
  {
    // --- actions that this pane additionally enables for the filter tree ---
    this.saveFilterAction = new AbstractAction("Save filter", ResourceManager.getImageIcon(ResourceManager.SAVE_ICON))
    {
      // Tooltip
      { this.putValue(SHORT_DESCRIPTION, "Save current filter"); }
      
      public void actionPerformed(ActionEvent e) {
        saveCurrentFilter();
      }
    };
    
    this.refreshFiltersAction = new AbstractAction("Refresh filter tree", ResourceManager.getImageIcon(ResourceManager.REFRESH_ICON))
    {
      // Tooltip
      { this.putValue(SHORT_DESCRIPTION, "Refresh the filter tree"); }
      
      public void actionPerformed(ActionEvent e) {
        loadFiltersAndBuildTheTree();
      }
    };
    
    // the actual toolbar - no actions are added to it yet: done in a separate method
    JToolBar tbTreeActions = new JToolBar(JToolBar.HORIZONTAL);
    tbTreeActions.setAlignmentX(RIGHT_ALIGNMENT);
    tbTreeActions.setBorderPainted(true);
    tbTreeActions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    tbTreeActions.setFloatable(false);
    return (tbTreeActions);
  }
  
  
  /**
   * Resets the action toolbar to the original state.
   */
  private void resetTreeActionToolbar()
  {
    saveFilterAction.setEnabled(false);
    refreshFiltersAction.setEnabled(false);
    
    tbFilterTreeToolbar.removeAll();
    if (ENABLE_SAVING_FILTERS) { tbFilterTreeToolbar.add(saveFilterAction); }
    tbFilterTreeToolbar.add(refreshFiltersAction);
    tbFilterTreeToolbar.repaint();
  }
  
  
  /**
   * This method loads filter data from API and populates the view.
   */
  private void loadFiltersAndBuildTheTree()
  {
    SwingUtilities.invokeLater(new Runnable() {
      public void run()
      {
        resetTreeActionToolbar();
        
        jpFilters.removeAll();
        jpFilters.setLayout(new BorderLayout());
        jpFilters.add(new JLabel(" Loading filters..."), BorderLayout.NORTH);
        jpFilters.add(new JLabel(ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE)), BorderLayout.CENTER);
        thisPanel.validate();
        thisPanel.repaint();      // validate and repaint this component to make sure that
                                  // scroll bar around the filter tree placeholder panel disappears
      }
    });
    
    new Thread("Load filters") {
      public void run() {
        try {
          // load filter data
          filtersRoot = client.getBioCatalogueFilters(filtersURL);
          
          // Create root of the filter tree component
          FilterTreeNode root = new FilterTreeNode("root");
          
          // populate the tree via its root element
          for (FilterGroup fgroup : filtersRoot.getGroupList())
          {
            // attach filter group directly to the root node
            FilterTreeNode fgroupNode = new FilterTreeNode("<html><span style=\"color: black; font-weight: bold;\">" + StringEscapeUtils.escapeHtml(fgroup.getName().toString()) + "</span></html>");
            root.add(fgroupNode);
            
            
            // go through all filter types in this group and add them to the tree
            for (FilterType ftype : fgroup.getTypeList())
            {
              // if there's more than one filter type in the group, add the type node as another level of nesting
              // (otherwise, attach filters inside the single type directly to the group node)
              FilterTreeNode filterTypeNode = fgroupNode;
              if (fgroup.getTypeList().size() > 1) {
                filterTypeNode = new FilterTreeNode("<html><span style=\"color: black; font-weight: bold;\">" + StringEscapeUtils.escapeHtml(ftype.getName().toString()) + "</span></html>");
                fgroupNode.add(filterTypeNode);
              }
              
              addFilterChildren(filterTypeNode, ftype.getUrlKey().toString(), ftype.getFilterList());
            }
          }
          
          // Create the tree view with the populated root
          filterTree = new JFilterTree(root);
          filterTree.setRootVisible(false);      // don't want the root to be visible; not a standard thing, so not implemented within JTriStateTree
          filterTree.setLargeModel(true);        // potentially can have many filters!
          filterTree.addCheckingListener(thisPanel);
          
          
          // Add custom functionality to the tree - ability to reload the filters and save the current filter
          filterTree.getContextualMenu().insert(new JPopupMenu.Separator(), 0);
          filterTree.getContextualMenu().insert(refreshFiltersAction, 0);
          if (ENABLE_SAVING_FILTERS) { filterTree.getContextualMenu().insert(saveFilterAction, 0); }
          
          
          // insert the created tree view into the filters panel
          jpFilters.removeAll();
          jpFilters.setLayout(new GridLayout(0,1));
          jpFilters.add(filterTree);
          jpFilters.validate();
          
          
          // add actions from the contextual menu of the filter tree into the toolbar
          // that replicates those plus adds additional ones in this panel
          tbFilterTreeToolbar.add(new JToolBar.Separator());
          for (Action a : filterTree.getContextualMenuActions()) {
            tbFilterTreeToolbar.add(a);
          }
          
          
          // enable all actions
          saveFilterAction.setEnabled(true);
          refreshFiltersAction.setEnabled(true);
          filterTree.enableAllContextualMenuAction(true);
        }
        catch (Exception e) {
          logger.error("Failed to load filter tree from the following URL: " + filtersURL, e);
        }
      }
      
      
      /**
       * Recursive method to populate a node of the filter tree with all
       * sub-filters.
       * 
       * Ontological terms will be underlined.
       * 
       * @param root Tree node to add children to.
       * @param filterList A list of Filters to add to "root" as childred.
       */
      private void addFilterChildren(FilterTreeNode root, String filterCategory, List<Filter> filterList) {
        for (Filter f : filterList) {
          FilterTreeNode fNode =
            new FilterTreeNode("<html><span color=\"black\"" + (FilterTreeNode.isTagWithNamespaceNode(filterCategory, f.getUrlValue()) ? " style=\"text-decoration: underline;\"" : "") + ">" +
                               StringEscapeUtils.escapeHtml(f.getName()) + "</span><span color=\"gray\">&nbsp;&nbsp;(" + f.getCount().intValue() + ")</span></html>",
                               filterCategory, f.getUrlValue());
          
          addFilterChildren(fNode, filterCategory, f.getFilterList());
          root.add(fNode);
        }
      }
      
    }.start();
  }
  
  
  private void saveCurrentFilter()
  {
    // check if there is any selection in the filter tree
    if (filterTree.getRootsOfCheckedPaths().isEmpty()) {
      JOptionPane.showMessageDialog(null, "No filtering criteria are currently specified.\n" +
          "Please choose required criteria from the filtering tree and try again.",
          BioCataloguePluginConstants.APP_VISIBLE_NAME, JOptionPane.WARNING_MESSAGE);
      return;
    }
    
    String filterName = null;
    do {
      // keep asking for the filter name until users clicks
      // "cancel" button or actually inputs a valid name
      filterName = JOptionPane.showInputDialog("Please choose a name for the current filter");
    } while (filterName != null && filterName.length() == 0);
    
    // if filter name was entered, store it
    if (filterName != null) {
      JOptionPane.showMessageDialog(null, "ERROR: not implemented!!!");  // TODO - fix saving the filters
//      System.err.println("\nOnly saves filter itself, but not other search criteria - e.g. term / tag.\n");
//      ServiceFilteringSettings currentFilter = new ServiceFilteringSettings(filterName, filterTree);
//      SearchInstance filteringSearchInstance = new SearchInstance(new SearchInstance("", true, false, false, false), currentFilter);
//      searchResultsMainPanel.getHistoryAndFavouritesPanel().addToFavouriteFilters(filteringSearchInstance);
    }
  }
  
  
  /**
   * @param si Uses this SearchInstance to restore the checking
   *           state of filtering criteria in the filter tree. 
   */
  public void restoreFilteringSettings(SearchInstance si) {
    this.filterTree.restoreFilterCheckingSettings(si.getFilteringSettings().getFilterTreeRootsOfCheckedPaths());
  }
  
  
  /**
   * Clears any selections made in the filter tree -
   * i.e. both clears checked nodes and removes all tree path selections.
   */
  public void clearSelection() {
    // filter tree may not have been initialised yet, so perform a check
    if (this.filterTree != null)
    {
      // remove, then restore self as a listener - this is to avoid
      // receiving checking state change event
      this.filterTree.removeCheckingListener(thisPanel);
      this.filterTree.selectAllNodes(false);
      this.filterTree.clearSelection();
      this.filterTree.addCheckingListener(thisPanel);
    }
  }
  
  
  /**
   * Collapses all expanded nodes in the filter tree.
   */
  public void collapseAll() {
    // filter tree may not have been initialised yet, so perform a check
    if (this.filterTree != null) {
      this.filterTree.collapseAll();
    }
  }
  
  
  /**
   * Used for making preferred height of the search status label
   * the same as the height of this toolbar.
   * 
   * @return
   */
  public Dimension getTreeToolbarPreferredSize() {
    return this.tbFilterTreeToolbar.getPreferredSize();
  }
  
  
  // *** Callback for TriStateTreeCheckingListener ***
  
  /**
   * We start a new search as soon as checking state of the filter tree changes.
   */
  public void triStateTreeCheckingChanged(JTriStateTree source)
  {
    MainComponentFactory.getSharedInstance().getBioCatalogueExplorationTab().getTabbedSearchResultsPanel().
        startNewFiltering(resourceType, new ServiceFilteringSettings(filterTree));
  }
  
}
