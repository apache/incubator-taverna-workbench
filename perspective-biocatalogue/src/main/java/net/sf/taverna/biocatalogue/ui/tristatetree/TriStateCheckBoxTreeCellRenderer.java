package net.sf.taverna.biocatalogue.ui.tristatetree;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.biocatalogue.model.ResourceManager;


/**
 * Provides a mechanism for rendering tri-state tree nodes.
 * 
 * @author Sergejs Aleksejevs
 */
public class TriStateCheckBoxTreeCellRenderer extends DefaultTreeCellRenderer
{
  public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean selected, boolean expanded, boolean leaf, int row,
      boolean hasFocus)
  {
    Border treeNodePanelBorder = null; // will be obtained from default rendering and applied to the new one
    Color backgroundColor = null;      // likewise: will be applied to all constituents of the new rendering
    
    // obtain the default rendering, we'll then customize it
    Component defaultRendering = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    
    // it is most likely that the default rendering will be a JLabel, check just to be safe
    if (defaultRendering instanceof JLabel)
    {
      JLabel defaultRenderedLabel = ((JLabel)defaultRendering);
      
      // if this is not the case, it kind of undermines the whole purpose
      // of using this tree cell renderer, but check just to be sure
      if (value instanceof TriStateTreeNode) {
        // a state value from within the TriStateTreeNode will be used to
        // set the correct state in its rendering
        switch (((TriStateTreeNode)value).getState()) {
          case CHECKED: 
            if (((TriStateTreeNode)value).getPath().length > 2) {
              // only allow CHECKED state icon for nodes that are deeper than second
              // level in the tree - that is for any nodes that do not represent categories
              // in the tree (root is not shown, so nodes that represent categories are
              // effectively multiple category "roots" that have actual contents inside them)
              defaultRenderedLabel.setIcon(ResourceManager.getImageIcon(ResourceManager.TRISTATE_CHECKBOX_CHECKED_ICON));
              break;
            }
            // else -- 'fall through' to PARTIAL icon: this was a CHECKED state for the category node
          case PARTIAL: defaultRenderedLabel.setIcon(ResourceManager.getImageIcon(ResourceManager.TRISTATE_CHECKBOX_PARTIAL_ICON)); break;
          case UNCHECKED: defaultRenderedLabel.setIcon(ResourceManager.getImageIcon(ResourceManager.TRISTATE_CHECKBOX_UNCHECKED_ICON)); break;
          default: defaultRenderedLabel.setIcon(null); break;
        }
      }
    }
    
    return (defaultRendering);
  }

}
