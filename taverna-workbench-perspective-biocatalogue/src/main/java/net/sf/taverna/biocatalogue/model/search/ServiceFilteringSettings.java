package net.sf.taverna.biocatalogue.model.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreePath;

import net.sf.taverna.biocatalogue.model.Util;
import net.sf.taverna.biocatalogue.ui.filtertree.FilterTreeNode;
import net.sf.taverna.biocatalogue.ui.tristatetree.JTriStateTree;

/**
 * This class provides functionality to deal with service filtering settings.
 * Particularly used to save the current state of the filtering tree as a
 * favourite filter.
 * 
 * Instances of this class hold all necessary information to restore the
 * filtering state at a later point.
 * 
 * @author Sergejs Aleksejevs
 */
public class ServiceFilteringSettings implements Comparable<ServiceFilteringSettings>, Serializable
{
  private static final long serialVersionUID = -5706169924295062628L;
  
  private String filterName;
  private int filteringCriteriaNumber;
  private List<TreePath> filterTreeRootsOfCheckedPaths;
  
  
  
  /**
   * Stores current filtering selection in the provided JTriStateTree
   * instance into the instance of this class.
   * 
   * @param filterTree The JTriStateTree instance to get the current selection from.
   */
  public ServiceFilteringSettings(JTriStateTree filterTree)
  {
    this(null, filterTree);
  }
  
  
  /**
   * Stores current filtering selection in the provided JTriStateTree
   * instance into the instance of this class.
   * 
   * @param filterName The name to associate with this filter.
   * @param filterTree The JTriStateTree instance to get the current selection from.
   */
  @SuppressWarnings("unchecked")
  public ServiceFilteringSettings(String filterName, JTriStateTree filterTree)
  {
    this.filterName = filterName;
    
    this.filteringCriteriaNumber = filterTree.getLeavesOfCheckedPaths().size();
    
    // a deep copy of the data from the filter tree is created, so that the data stored in this instance
    // is fully independent of the filter tree itself; therefore local copy of this data may be modified
    // as needed and will not affect the main filter (and vice versa) 
    this.filterTreeRootsOfCheckedPaths = (List<TreePath>)Util.deepCopy(filterTree.getRootsOfCheckedPaths());
  }
  
  
  /**
   * Analyses the filter tree and produces part of the request URL containing settings regarding filters.
   */
  @SuppressWarnings("unchecked")
  public Map<String,String[]> getFilteringURLParameters()
  {
    // analyse filter tree to get checked elements 
    Map<String,HashSet<String>> selections = new HashMap<String,HashSet<String>>(); 
    
    // cycle through the deepest selected nodes;
    // NB! the CheckboxTree acts in a way that if A contains B,C --
    // 1) if only B is checked, tp.getLastPathComponent() will be B;
    // 2) if both B,C are checked, tp.getLastPathComponent() will be A;
    for (TreePath selectedRootNodePath : getFilterTreeRootsOfCheckedPaths()) {
      FilterTreeNode selectedNode = (FilterTreeNode)selectedRootNodePath.getLastPathComponent();
      
      // identify affected nodes
      HashSet<FilterTreeNode> affectedNodes = new HashSet<FilterTreeNode>();
      if (selectedNode.isFilterCategory()) {
        // case as in example 2) -- need to "extract" nodes that are one level deeper
        for (Enumeration children = selectedNode.children(); children.hasMoreElements(); ) {
          affectedNodes.add((FilterTreeNode)children.nextElement());
        }
      }
      else {
        // case as in example 1)
        affectedNodes.add(selectedNode);
      }
      
      // walk through the identified collection of nodes and build the data structure with URL values
      for (FilterTreeNode node : affectedNodes) {
        if (selections.containsKey(node.getType())) {
          selections.get(node.getType()).add(node.getUrlValue());
        }
        else {
          HashSet<String> newSet = new HashSet<String>();
          newSet.add(node.getUrlValue());
          
          selections.put(node.getType(), newSet);
        }
      }
    }
    
    
    // now use the constructed set of data to build the map of filtering URL parameters
    Map<String,String[]> filterUrlParameters = new HashMap<String,String[]>();
    for(String key : selections.keySet())
    {
      List<String> categoryValues = new ArrayList<String>();
      for (String value : selections.get(key)) {
        categoryValues.add(value);
      }
      
      filterUrlParameters.put(key, categoryValues.toArray(new String[0]));
    }
    
    return (filterUrlParameters);
  }
  
  
  // *** Getters ***
  
  public String getFilterName() {
    return (this.filterName == null || filterName.length() == 0 ? "untitled filter" : this.filterName);
  }
  
  public List<TreePath> getFilterTreeRootsOfCheckedPaths() {
    return filterTreeRootsOfCheckedPaths;
  }
  
  /**
   * @return Number of filtering criteria within the current filter.
   */
  public int getNumberOfFilteringCriteria() {
    return filteringCriteriaNumber;
  }
  
  // *** End of getters ***
  
  
  public boolean equals(Object other)
  {
    if (other instanceof ServiceFilteringSettings)
    {
      ServiceFilteringSettings o = (ServiceFilteringSettings)other;
      return (this.filterName.equals(o.filterName) &&
              this.filterTreeRootsOfCheckedPaths.equals(o.filterTreeRootsOfCheckedPaths));
    }
    else {
      return false;
    }
  }
  
  
  public int compareTo(ServiceFilteringSettings other)
  {
    int iOrdering = this.filterName.compareTo(other.filterName);
    if (iOrdering == 0) {
      iOrdering = this.getNumberOfFilteringCriteria() - other.getNumberOfFilteringCriteria();
    }
    
    // inverse order, as the traversal of lists in the favourite filters panel is
    // done this way round
    return (-1 * iOrdering);
  }
  
  
  public String toString() {
    return ("Filter: '" + getFilterName() + "' [" + detailsAsString() + "]");
  }
  
  public String detailsAsString() {
    return (getNumberOfFilteringCriteria() + " filtering criteria");
  }
}
