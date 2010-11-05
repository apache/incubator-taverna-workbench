package net.sf.taverna.biocatalogue.ui.tristatetree;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * This class models tri-state nodes in the tree. Effectively
 * it associates a tri-state checkbox with each tree node.
 * 
 * Useful for partial selections of hierarchical data -
 * partial selection of a node indicates that some of the
 * children of that node are selected.
 * 
 * @author Sergejs Aleksejevs
 */
public class TriStateTreeNode extends DefaultMutableTreeNode
{
  private TriStateCheckBox.State state;
  
  /**
   * Creates a regular tree node; associated tri-state checkbox state is set to UNCHECKED.
   *  
   * @param userObject The object this tree node will represent.
   */
  public TriStateTreeNode(Object userObject) {
    this(userObject, TriStateCheckBox.State.UNCHECKED);
  }
  
  /**
   * Creates a regular tree node; associated tri-state checkbox state is set to the provided <code>initialState</code> value.
   *  
   * @param userObject The object this tree node will represent.
   * @param initialState One of the enum values of <code>TriStateCheckBox.State</code>.
   */
  public TriStateTreeNode(Object userObject, TriStateCheckBox.State initialState) {
    super(userObject);
    this.state = initialState;
  }
  
  
  /**
   * Compares based on the user object, not the state of this node.
   */
  public boolean equals(Object other) {
    if (other instanceof TriStateTreeNode) {
      return (this.userObject.equals(((TriStateTreeNode)other).userObject));
    }
    else {
      return (false);
    }
  }
  
  
  /**
   * Sets the state of the current node and (optionally) propagates
   * those changes through the tree.
   * 
   * @param state The new state to set - value from <code>TriStateCheckBox.State</code> enum.
   * @param propagateChangesToRelatedNodes True - to use the tree checking model to
   *                 propagate changes of the state of the current tree node to the
   *                 other related tree nodes (e.g. all descendants and ancestors) -
   *                 up and down the tree hierarchy. False - to only update the current
   *                 node and make no changes to the rest of the tree.
   */
  public void setState(TriStateCheckBox.State state, boolean propagateChangesToRelatedNodes)
  {
    this.state = state;
    
    // check if the tree checking model should be activated
    if (propagateChangesToRelatedNodes) {
      updateStateOfRelatedNodes();
    }
  }
  
  
  /**
   * Sets the state of the current node.
   * 
   * @param state The new state to set - value from <code>TriStateCheckBox.State</code> enum.
   */
  public void setState(TriStateCheckBox.State state) {
    setState(state, false);
  }
  
  
  public TriStateCheckBox.State getState() {
    return state;
  }
  
  
  /**
   * Toggles the state of the associated tri-state checkbox.
   * State transitions are as follows:</br>
   * <code>
   * TriStateCheckBox.State.CHECKED -> TriStateCheckBox.State.UNCHECKED
   * TriStateCheckBox.State.PARTIAL -> TriStateCheckBox.State.UNCHECKED
   * TriStateCheckBox.State.UNCHECKED -> TriStateCheckBox.State.CHECKED
   * </code>
   *
   * @param propagateChangesToRelatedNodes True - to use the tree checking model to
   *                 propagate changes of the state of the current tree node to the
   *                 other related tree nodes (e.g. all descendants and ancestors) -
   *                 up and down the tree hierarchy. False - to only update the current
   *                 node and make no changes to the rest of the tree. 
   * @return The value of the new state.
   */
  public TriStateCheckBox.State toggleState(boolean propagateChangesToRelatedNodes)
  {
    if (state.equals(TriStateCheckBox.State.CHECKED) || state.equals(TriStateCheckBox.State.PARTIAL)) {
      state = TriStateCheckBox.State.UNCHECKED;
    }
    else if (state.equals(TriStateCheckBox.State.UNCHECKED)) {
      state = TriStateCheckBox.State.CHECKED;
    }
    
    // check if the tree checking model should be activated
    if (propagateChangesToRelatedNodes) {
      updateStateOfRelatedNodes();
    }
    
    return (state);
  }
  
  
  /* 
   * === The tree CHECKING MODEL ===
   * 
   * Effectively, this defines the way the tree reacts to it's nodes
   * being checked / unchecked. Only one model is implemented at the
   * moment, therefore it's not extracted into a separate class, but
   * remains to be a part of the TriStateTreeNode.
   * 
   * Could possibly be better placed within the JTriStateTree, rather
   * than TriStateTreeNode.
   */
  
  /**
   * The entry point - must be invoked to traverse the tree and make
   * changes to checking states of related tree nodes. 
   */
  public void updateStateOfRelatedNodes()
  {
    updateStateOfAncestors(this.getParent());
    updateStateOfDescendants(this);
  }
  
  
  /**
   * Recursively visits all ancestors of the <code>parentNode</code>
   * and decides on their checking states based on the states of their
   * children nodes.
   * 
   * @param parentNode Initially - parent node of the current node (i.e. the one,
   *                   for which a state update has been made); then updated for
   *                   recursive calls.
   */
  private void updateStateOfAncestors(TreeNode parentNode)
  {
    // reached root of the tree, do nothing - return
    if (parentNode == null) {
      return;
    }
    
    if (parentNode instanceof TriStateTreeNode) {
      TriStateTreeNode parentTriStateNode = (TriStateTreeNode)parentNode;
      
      // explicitly fetch children into a new enumeration - this is
      // to make sure that we work with the same enumeration, rather
      // than obtaining a fresh one with every reference to 'parentTriStateNode.children()'
      Enumeration childNodes = parentTriStateNode.children();
      
      // go through all the children and count the number of selected ones
      int iChildrenCount = 0;
      int iPartiallySelectedChildren = 0;
      int iSelectedChildren = 0;
      
      while(childNodes.hasMoreElements()) {
        Object node = childNodes.nextElement();
        if (node instanceof TriStateTreeNode) {
          TriStateTreeNode currentNode = (TriStateTreeNode)node;
          iChildrenCount++;
          if (currentNode.getState().equals(TriStateCheckBox.State.CHECKED)) {
            iSelectedChildren++;
          }
          else if (currentNode.getState().equals(TriStateCheckBox.State.PARTIAL)) {
            iPartiallySelectedChildren++;
          }
        }
      }
      
      
      // decide on the state of the 'parentNode' based on the checking state of its children
      if (iSelectedChildren == 0 && iPartiallySelectedChildren == 0) {
        // no children are selected
        parentTriStateNode.setState(TriStateCheckBox.State.UNCHECKED);
      }
      else if ((iSelectedChildren + iPartiallySelectedChildren) > 0 && iSelectedChildren < iChildrenCount) {
        // some children are selected (either partially or fully)
        parentTriStateNode.setState(TriStateCheckBox.State.PARTIAL);
      }
      else if (iSelectedChildren > 0 && iSelectedChildren == iChildrenCount) {
        // all children are selected
        parentTriStateNode.setState(TriStateCheckBox.State.CHECKED);
      }
      
      
      // repeat the same recursively up the hierarchy
      updateStateOfAncestors(parentTriStateNode.getParent());
    }
  }

  /**
   * Recursively traverses all descendants of the <code>parentNode</code>
   * to set their checking state to the value of the state of the <code>parentNode</code>. 
   * 
   * @param parentNode Initially - the tree node for which the state
   *                   change was made; then updated for recursive calls.
   */
  private void updateStateOfDescendants(TriStateTreeNode parentNode)
  {
    // explicitly fetch children into a new enumeration - this is
    // to make sure that we work with the same enumeration, rather
    // than obtaining a fresh one with every reference to 'parentNode.children()'
    Enumeration childNodes = parentNode.children();
    
    // for all child nodes do 2 things:
    // - set their state as that of the parent;
    // - repeat the same recursively with their children
    while(childNodes.hasMoreElements()) {
      Object node = childNodes.nextElement();
      if (node instanceof TriStateTreeNode) {
        TriStateTreeNode currentNode = (TriStateTreeNode) node; 
        currentNode.setState(parentNode.getState());
        currentNode.updateStateOfDescendants(currentNode);
      }
    }
  }
  
  /*
   * === End of CHECKING MODEL implementation.
   */
  
}
