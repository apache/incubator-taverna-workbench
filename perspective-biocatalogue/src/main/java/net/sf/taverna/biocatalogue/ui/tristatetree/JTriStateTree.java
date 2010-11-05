package net.sf.taverna.biocatalogue.ui.tristatetree;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sf.taverna.biocatalogue.model.ResourceManager;


/**
 * @author Sergejs Aleksejevs
 */
public class JTriStateTree extends JTree
{
  // This is used to manage location and padding of tooltips on long items
  // that don't fit into the visible part of this tree.
  private static final int JCHECKBOX_WIDTH = 16;
  
  private JTriStateTree instanceOfSelf;
  private JPopupMenu contextualMenu;
  
  private TriStateTreeNode root;
  
  // will enable/disable checkboxes - when disabled the selection
  // will remain, but will appear as read-only
  private boolean bCheckingEnabled;
  
  private List<Action> contextualMenuActions; 
  private Action expandAllAction;
  private Action collapseAllAction;
  private Action selectAllAction;
  private Action deselectAllAction;
  
  private Set<TriStateTreeCheckingListener> checkingListeners;
  
  
  public JTriStateTree(TriStateTreeNode root)
  {
    super(root);
    this.root = root;
    this.instanceOfSelf = this;
    
    // by default checking is allowed
    this.bCheckingEnabled = true;
    
    // initially, no checking listeners
    checkingListeners = new HashSet<TriStateTreeCheckingListener>();
    
    // initially set to show the [+]/[-] icons for expanding collapsing top-level nodes
    this.setShowsRootHandles(true);
    
    // use the cell rendered which understands the three states of the
    // nodes of this tree
    this.setCellRenderer(new TriStateCheckBoxTreeCellRenderer());
    
    
    // create all necessary actions for the popup menu: selecting/deselecting and expanding/collapsing all nodes
    this.selectAllAction = new AbstractAction("Select all", ResourceManager.getImageIcon(ResourceManager.SELECT_ALL_ICON))
    {
      // Tooltip
      { this.putValue(SHORT_DESCRIPTION, "Select all nodes in the tree"); }
      
      public void actionPerformed(ActionEvent e) {
        selectAllNodes(true);
      }
    };
    
    this.deselectAllAction = new AbstractAction("Deselect all", ResourceManager.getImageIcon(ResourceManager.DESELECT_ALL_ICON))
    {
      // Tooltip
      { this.putValue(SHORT_DESCRIPTION, "Deselect all nodes in the tree"); }
      
      public void actionPerformed(ActionEvent e) {
        selectAllNodes(false);
      }
    };
    
    
    this.expandAllAction = new AbstractAction("Expand all", ResourceManager.getImageIcon(ResourceManager.EXPAND_ALL_ICON))
    {
      // Tooltip
      { this.putValue(SHORT_DESCRIPTION, "Expand all nodes in the tree"); }
      
      public void actionPerformed(ActionEvent e) {
        expandAll();
      }
    };
    
    this.collapseAllAction = new AbstractAction("Collapse all", ResourceManager.getImageIcon(ResourceManager.COLLAPSE_ALL_ICON))
    {
      // Tooltip
      { this.putValue(SHORT_DESCRIPTION, "Collapse all expanded nodes in the tree"); }
      
      public void actionPerformed(ActionEvent e) {
        collapseAll();
      }
    };
    
    
    // populate the popup menu with created menu items
    contextualMenuActions = Arrays.asList(new Action[] {expandAllAction, collapseAllAction,
                                                        selectAllAction, deselectAllAction});
    
    contextualMenu = new JPopupMenu();
    contextualMenu.add(expandAllAction);
    contextualMenu.add(collapseAllAction);
    contextualMenu.add(new JPopupMenu.Separator());
    contextualMenu.add(selectAllAction);
    contextualMenu.add(deselectAllAction);
    
    
    this.addMouseListener(new MouseAdapter() {
      // use mousePressed, not mouseClicked to make sure that
      // quick successive clicks get processed correctly, otherwise
      // some clicks are disregarded
      public void mousePressed(MouseEvent e)
      {
        // only listen to checkbox checking requests if this is
        // a correct type of mouse event for this
        if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1)
        {
          int clickedRow = instanceOfSelf.getRowForLocation(e.getX(), e.getY());
          
          // only make changes to node selections if checking is enabled in the tree and
          // it was a node which was clicked, not [+]/[-] or blank space
          if (bCheckingEnabled && clickedRow != -1)
          {
            Object clickedObject = instanceOfSelf.getPathForRow(clickedRow).getLastPathComponent();
            if (clickedObject instanceof TriStateTreeNode) {
              TriStateTreeNode node = ((TriStateTreeNode)clickedObject);
              
              // toggle state of the clicked node + propagate the changes to
              // the checking state of all nodes
              node.toggleState(true);
              
              // repaint the whole tree
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  instanceOfSelf.repaint();
                }
              });
              
              // notify all listeners
              notifyCheckingListeners();
            }
          }
        }
        else {
          // not a checking action - instead, bring up a popup menu
          contextualMenu.show(instanceOfSelf, e.getX(), e.getY());
        }
      }
      
      public void mouseReleased(MouseEvent e)
      {
        if (e.isPopupTrigger()) {
          // another way a popup menu may be called on different systems
          contextualMenu.show(instanceOfSelf, e.getX(), e.getY());
        }
      }
      
      
      /**
       * This method enables tooltips on this instance of JTriStateTree
       * when mouse enters its bounds. Custom tooltips will be used, but
       * this notifies ToolTipManager that tooltips must be shown on this
       * tree. 
       */
      public void mouseEntered(MouseEvent e) {
        instanceOfSelf.setToolTipText("Filter tree");
      }
      
      /**
       * Removes tooltips from this JTriStateTree when mouse leaves its bounds.
       */
      public void mouseExited(MouseEvent e) {
        instanceOfSelf.setToolTipText(null);
      }
      
    });
    
  }
  
  
  /**
   * This method is used to determine tooltip location.
   * 
   * Helps to ensure that the tooltip appears directly over the
   * text in the row over which the mouse currently hovers.
   */
  public Point getToolTipLocation(MouseEvent e)
  {
    int iRowIndex = this.getRowForLocation(e.getX(), e.getY());
    if (iRowIndex != -1) {
      // mouse hovers over one of the rows - make top-left corner of
      // the tooltip to appear over the top-left corner of that row
      Rectangle bounds = this.getRowBounds(iRowIndex);
      return (new Point(bounds.x + JCHECKBOX_WIDTH, bounds.y));
    }
    else {
      // let ToolTipManager determine where to show the tooltip (if it will be shown)
      return null;
    }
  }
  
  
  /**
   * Supports dynamic tooltips for the contents of this JTriStateTree -
   * the tooltips will only be shown for those tree nodes that don't
   * fully fit within the visible bounds of the tree.
   * 
   * For other nodes no tooltip will be shown.
   */
  public String getToolTipText(MouseEvent e)
  {
    String strTooltip = null;
    
    Object correspondingObject = getTreeNodeObject(e);
    if (correspondingObject != null) {
      // mouse is hovering over some row in the tree, not a blank space --
      // obtain a component that is identical to the one which is currently displayed at the identified row in the tree
      Component rendering = this.getCellRenderer().getTreeCellRendererComponent(this, correspondingObject, false, false,
                                                                true, this.getRowForLocation(e.getX(), e.getY()), false);
      
      if (rendering.getPreferredSize().width + getToolTipLocation(e).x - JCHECKBOX_WIDTH > this.getVisibleRect().width) {
        // if the component is not fully visible, the tooltip will be displayed -
        // tooltip text matches the one for this row in the tree, will just be shown in full
        strTooltip = correspondingObject.toString();
      }
    }
    
    // return either tooltip text or 'null' if no tooltip is currently required
    return (strTooltip);
  }
  
  
  /**
   * Check whether a {@link MouseEvent} happened in such a location
   * in the {@link JTriStateTree} that corresponds to some node or a
   * blank space.
   * 
   * @param e
   * @return Object contained in the tree node that corresponds to the
   *         location of specified {@link MouseEvent} <code>e</code>;
   *         or <code>null</code> if the event happened over a blank space.
   */
  public Object getTreeNodeObject(MouseEvent e)
  {
    int iRowIndex = this.getRowForLocation(e.getX(), e.getY());
    if (iRowIndex != -1) {
      // mouse is hovering over some row in the tree, not a blank space
      return (this.getPathForRow(iRowIndex).getLastPathComponent());
    }
    
    return (null);
  }
  
  
  /**
   * @return List of the highest-level nodes of the tree that have full (not partial) selection and,
   *         therefore, act as roots of checked paths.
   */
  public List<TreePath> getRootsOfCheckedPaths()
  {
    return getRootsOfCheckedPaths(this.root);
  }
  
  /**
   * A recursive version of the getCheckedRootsOfCheckedPaths().
   * Performs all the work for a given node and returns result to
   * the caller.
   * 
   * @param startNode Node to start with.
   * @return
   */
  private List<TreePath> getRootsOfCheckedPaths(TriStateTreeNode startNode)
  {
    ArrayList<TreePath> pathsToRootsOfCheckings = new ArrayList<TreePath>();
    
    Object currentNode = null;
    for (Enumeration e = startNode.children(); e.hasMoreElements(); )
    {
      currentNode = e.nextElement();
      if (currentNode instanceof TriStateTreeNode) {
        TriStateTreeNode curTriStateNode = (TriStateTreeNode)currentNode;
        
        if (curTriStateNode.getState().equals(TriStateCheckBox.State.CHECKED)) {
          pathsToRootsOfCheckings.add(new TreePath(curTriStateNode.getPath()));
        }
        else if (curTriStateNode.getState().equals(TriStateCheckBox.State.PARTIAL)) {
          pathsToRootsOfCheckings.addAll(getRootsOfCheckedPaths(curTriStateNode));
        }
      }
    }
    
    return (pathsToRootsOfCheckings);
  }
  
  
  /**
   * @return List of TreePath objects, where the last component in each
   *         path is the root of an unchecked path in this tree. In other
   *         words each of those last components is either an unchecked
   *         leaf node or a node, none of whose children (and itself as
   *         well) are checked.
   */
  public List<TreePath> getRootsOfUncheckedPaths()
  {
    return (getRootsOfUncheckedPaths(this.root));
  }
  
  
  /**
   * Recursive worker method for <code>getRootsOfUncheckedPaths()</code>.
   */
  private List<TreePath> getRootsOfUncheckedPaths(TreeNode startNode)
  {
    List<TreePath> rootNodesOfUncheckedPaths = new ArrayList<TreePath>();
    
    Object currentNode = null;
    for (Enumeration e = startNode.children(); e.hasMoreElements(); )
    {
      currentNode = e.nextElement();
      if (!(currentNode instanceof TriStateTreeNode) ||
          ((TriStateTreeNode)currentNode).getState().equals(TriStateCheckBox.State.UNCHECKED))
      {
        rootNodesOfUncheckedPaths.add(new TreePath(((DefaultMutableTreeNode)currentNode).getPath()));
      }
      else {
        rootNodesOfUncheckedPaths.addAll(getRootsOfUncheckedPaths((TreeNode)currentNode));
      }
    }
    
    return (rootNodesOfUncheckedPaths);
  }
  
  
  /**
   * @return List of TreePath objects, that point to all "leaf"
   *         nodes in the tree that are checked - in other words
   *         this method returns a collection of paths to all "deepest"
   *         nodes in this tree that are checked and do not have any
   *         (checked) children.
   */
  public List<TreePath> getLeavesOfCheckedPaths() {
    return (getLeavesOfCheckedPaths(this.root));
  }
  
  
  /**
   * Recursive worker method for {@link JTriStateTree#getLeavesOfCheckedPaths()}
   */
  private List<TreePath> getLeavesOfCheckedPaths(TriStateTreeNode startNode)
  {
    List<TreePath> leavesOfCheckedPaths = new ArrayList<TreePath>();
    
    // this node is only relevant if it is checked itself - if not,
    // it must be the first-level child of another node that is checked
    // and is only considered here on the recursive pass (but will be discarded)
    if (startNode.getState().equals(TriStateCheckBox.State.CHECKED) ||
        startNode.getState().equals(TriStateCheckBox.State.PARTIAL))
    {
      // "ask" all children to do the same...
      Object currentNode = null;
      for (Enumeration e = startNode.children(); e.hasMoreElements(); ) {
        currentNode = e.nextElement();
        if (currentNode instanceof TriStateTreeNode) {
          leavesOfCheckedPaths.addAll(getLeavesOfCheckedPaths((TriStateTreeNode)currentNode));
        }
      }
      
      // ...if we have a list of leaf nodes, then this node can't be a leaf;
      // -> but alternatively, if the list is empty, it means that this node is
      //    itself a leaf node and must be added to the result
      if (leavesOfCheckedPaths.isEmpty()) {
        leavesOfCheckedPaths.add(new TreePath(startNode.getPath()));
      }
    }
    
    return (leavesOfCheckedPaths);
  }
  
  
  /**
   * @return List of all contextual menu actions that are available for this tree.
   */
  public List<Action> getContextualMenuActions() {
    return this.contextualMenuActions;
  }
  
  
  /**
   * Enables or disables all actions in the contextual menu
   * @param actionsAreEnabled
   */
  public void enableAllContextualMenuAction(boolean actionsAreEnabled) {
    for (Action a : getContextualMenuActions()) {
      a.setEnabled(actionsAreEnabled);
    }
  }
  
  
  /**
   * Selects or deselects all nodes.
   * @param selectAll True - to select all; false - to reset all selections.
   */
  public void selectAllNodes(boolean selectAll) {
    root.setState(selectAll ? TriStateCheckBox.State.CHECKED : TriStateCheckBox.State.UNCHECKED);
    root.updateStateOfRelatedNodes();
    this.repaint();
    
    // even though this isn't a click in the tree, the selection has changed -
    // notify all listeners
    notifyCheckingListeners();
  }
  
  
  /**
   * TODO - this method doesn't take into account a possibility that the
   *        filter tree might have changed
   * 
   * @param rootsOfCheckedPaths A list of TreePath objects which represent a checking state of
   * the nodes in this tree (as returned by <code>getRootsOfCheckedPaths()</code>).
   * 
   * The last node of each path is the one that should have <code>TriStateCheckBox.State.CHECKED</code>
   * state (so that last node is a root of checked path that start at that node). Related partial
   * checkings for the UI can be computed from that by the tree checking model.
   * 
   * Therefore, a single "real" checking per provided TreePath from <code>rootsOfCheckedPaths</code> is
   * made.
   */
  public void restoreFilterCheckingSettings(List<TreePath> rootsOfCheckedPaths)
  {
    // start with removing all selections
    this.selectAllNodes(false);
    
    for (TreePath p : rootsOfCheckedPaths) {
      restoreTreePathCheckingSettings(this.root, p);
    }
  }
  
  /**
   * A worker method for <code>restoreFilterCheckingSettings(List<TreePath> rootsOfCheckedPaths)</code>.
   * See that method for further details.
   * 
   * @param startNode A node of this tree.
   * @param pathFromStartNode A TreePath object from the stored filter, where the first node must be 
   *                          equals to <code>startNode</code> (based on the <code>userObject</code>,
   *                          but not the checking state), should the traversal of the tree result in
   *                          checking the last node of this TreePath eventually - which is the goal
   *                          of this method.
   * @return True if traversal of <code>pathFromStartNode</code> succeeded and a node in this tree was checked;
   *         false if traversal couldn't find a matching node in this tree, and so no checking was made.
   */
  private boolean restoreTreePathCheckingSettings(TriStateTreeNode startNode, TreePath pathFromStartNode)
  {
    if (startNode == null || pathFromStartNode == null || pathFromStartNode.getPathCount() == 0) {
      // no match - no data to work with
      return (false);
    }
    
    // compare the "roots" - provided start node and the root of the provided path
    // (based on the 'user object', but not the selection state)
    if (startNode.equals(pathFromStartNode.getPathComponent(0)))
    {
      if (pathFromStartNode.getPathCount() == 1) {
        // provided startNode is equals to the only node in the provided tree path -
        // so it is the node to mark as checked; also - make sure that this selection
        // propagates through tree
        startNode.setState(TriStateCheckBox.State.CHECKED, true);
        
        // we've found the required node in this path - no further search needed,
        // so terminate this method
        return (true);
      }
      else {
        // provided startNode is equals to the first node of the provided tree path -
        // meaning that at this stage we need to traverse all children of the startNode
        // and look for the child that would match the next element in the provided tree path
        //
        // to do this, produce a new tree path from the provided one that doesn't contain
        // the first node - then proceed recursively
        Object[] currentPathComponents = pathFromStartNode.getPath();
        Object[] reducedPathComponents = new Object[currentPathComponents.length - 1];
        System.arraycopy(currentPathComponents, 1, reducedPathComponents, 0, currentPathComponents.length - 1);
        
        Enumeration children = startNode.children();
        while (children.hasMoreElements()) {
          TriStateTreeNode currentChild = (TriStateTreeNode)children.nextElement();
          
          // if recursive call succeeds, no need to iterate any further
          if (restoreTreePathCheckingSettings(currentChild, new TreePath(reducedPathComponents))) return (true);
        }
      }
    }
    
    // the startNode doesn't match the the first element in the provided tree path
    // or no match could be found during recursive search for the node to "check"
    return (false);
  }
  
  
  /**
   * Expands all paths in this tree.
   */
  public void expandAll()
  {
    // this simply expands all tree nodes
    // TODO - this actually "freezes" the UI if there are many nodes in the tree
    //        some better solution to be found (e.g. expand the nodes in the model, then update UI, or similar)
    for (int i = 0; i < getRowCount(); i++) {
      instanceOfSelf.expandRow(i);
    }
  }
  
  
  /**
   * Collapses all paths in this tree.
   */
  public void collapseAll()
  {
    // this simply collapses all expanded nodes - this is very quick, execute just as it is
    for (int i = getRowCount() - 1; i >= 0; i--) {
      instanceOfSelf.collapseRow(i);
    }
  }
  
  
  /**
   * Removes all nodes in this tree that are unchecked.
   * 
   * It doesn't iterate through *all* nodes - if some node is
   * indeed unchecked, it removes that node and any children that
   * it has (because unchecked node is the root of an unchecked path).
   */
  public void removeAllUncheckedNodes()
  {
    // get the tree model first - will be used to remove the nodes
    DefaultTreeModel theTreeModel = (DefaultTreeModel)this.treeModel;
    
    // remove unchecked nodes
    List<TreePath> allNodesToRemove = this.getRootsOfUncheckedPaths();
    for (TreePath p : allNodesToRemove) {
      theTreeModel.removeNodeFromParent((MutableTreeNode)p.getLastPathComponent());
    }
  }
  
  
  /**
   * Provides access to the contextual menu of this JTriStateTree.
   * 
   * @return Reference to the contextual menu.
   */
  public JPopupMenu getContextualMenu() {
    return contextualMenu;
  }
  
  
  public void setCheckingEnabled(boolean bCheckingEnabled) {
    this.bCheckingEnabled = bCheckingEnabled;
  }
  
  /**
   * @return True if the current state of this JTriStateTree
   *         allows making changes to checking of checkboxes
   *         in its nodes.
   */
  public boolean isCheckingEnabled() {
    return bCheckingEnabled;
  }
  
  
  /**
   * @param listener New tree checking listener to register for updates
   *                 to tree node selections.
   */
  public void addCheckingListener(TriStateTreeCheckingListener listener) {
    if (listener != null) {
      this.checkingListeners.add(listener);
    }
  }
  
  
  /**
   * @param listener Tree checking listener to remove.
   */
  public void removeCheckingListener(TriStateTreeCheckingListener listener) {
    if (listener != null) {
      this.checkingListeners.remove(listener);
    }
  }
  
  
  /**
   * Sends a signal to all listeners to check the state of the tree,
   * as it has changed. 
   */
  private void notifyCheckingListeners() {
    for (TriStateTreeCheckingListener listener : this.checkingListeners) {
      listener.triStateTreeCheckingChanged(instanceOfSelf);
    }
  }
  
}
