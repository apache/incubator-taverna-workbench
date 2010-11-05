package net.sf.taverna.biocatalogue.ui.filtertree;

import java.awt.event.MouseEvent;

import net.sf.taverna.biocatalogue.ui.tristatetree.JTriStateTree;
import net.sf.taverna.biocatalogue.ui.tristatetree.TriStateTreeNode;

/**
 * This subclass of {@link JTriStateTree} provides custom behaviour
 * for tooltips: ontological terms will now always get a tooltip that
 * displays the namespace for the tag, but plain text tags will still
 * behave as before - the way it is defined in the superclass (so that
 * the tooltip will only be shown if the tag does not fully fit into
 * the visible part of the {@link FilterTreePane}.
 * 
 * @author Sergejs Aleksejevs
 */
public class JFilterTree extends JTriStateTree
{
  
  public JFilterTree(TriStateTreeNode root) {
    super(root);
  }
  
  
  public String getToolTipText(MouseEvent e)
  {
    Object correspondingObject = super.getTreeNodeObject(e);
    if (correspondingObject != null && correspondingObject instanceof FilterTreeNode) {
      FilterTreeNode filterNode = (FilterTreeNode) correspondingObject;
      
      if (filterNode.isTagWithNamespaceNode())
      {
        String nameAndNamespace = filterNode.getUrlValue().substring(1, filterNode.getUrlValue().length() - 1);
        String[] namePlusNamespace = nameAndNamespace.split("#");
        
        return ("<html>" + namePlusNamespace[1] + " (<b>Namespace: </b>" + namePlusNamespace[0] + ")</html>");
      }
    }
    
    return super.getToolTipText(e);
  }
  
}
