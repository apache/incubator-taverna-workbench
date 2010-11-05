package net.sf.taverna.biocatalogue.ui.filtertree;

import net.sf.taverna.biocatalogue.ui.tristatetree.TriStateTreeNode;

/**
 * This class allows storing two pieces of data relevant to content filtering
 * within the node of a tree. These values are kept hidden from the user and
 * are only used when the filtering is about to happen.
 * 
 * @author Sergejs Aleksejevs
 */
public class FilterTreeNode extends TriStateTreeNode
{
  private String type; 
  private String urlValue;
  final private boolean isFilterCategory;
  
  
  /**
   * This constructor is useful for root nodes, which need not have filter type / value.
   */
  public FilterTreeNode(Object userObject) {
    super(userObject);
    
    this.isFilterCategory = true;
  }
  
  
  /**
   * @param userObject As in the superclass (DefaultMutableTreeNode) - the object which represents the node in the UI
   * @param filterType Type of the filter - e.g. 'Service Categories' --> "cat"; 'Service Types' --> "t"
   * @param filterUrlValue Value that should be added to the URL to perform the filtering operation
   */
  public FilterTreeNode(Object userObject, String filterType, String filterUrlValue) {
    super(userObject);
    
    this.setType(filterType);
    this.setUrlValue(filterUrlValue);
    this.isFilterCategory = false;
  }
  
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }
  
  public void setUrlValue(String urlValue) {
    this.urlValue = urlValue;
  }
  
  
  public String getUrlValue() {
    return urlValue;
  }
  
  /**
   * @return True if and only if this node is one of the "root" filter categories (not to be mixed with root of the filter tree).
   */
  public boolean isFilterCategory() {
    return isFilterCategory;
  }
  
  
  /**
   * @return <code>true</code> if the current {@link FilterTreeNode} represents a tag with a namespace
   *         (i.e. an ontological term), whose full tag name looks like:
   *         <code>< http://example.namespace.com#tag_display_name ></code>
   */
  public boolean isTagWithNamespaceNode() {
    return (this.getType() != null && this.getType().contains("tag") && this.getUrlValue().contains("#") &&
            this.getUrlValue().startsWith("<") && this.getUrlValue().endsWith(">"));
  }
  
  
  /**
   * Static wrapper for {@link FilterTreeNode#isTagWithNamespaceNode()}
   *  
   * @param filterType
   * @param filterUrlValue
   * @return
   */
  public static boolean isTagWithNamespaceNode(String filterType, String filterUrlValue) {
    return (new FilterTreeNode("test_user_object", filterType, filterUrlValue).isTagWithNamespaceNode());
  }
  
}
