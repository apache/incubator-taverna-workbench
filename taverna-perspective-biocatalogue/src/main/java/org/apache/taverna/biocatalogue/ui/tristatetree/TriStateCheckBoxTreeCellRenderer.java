package org.apache.taverna.biocatalogue.ui.tristatetree;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.apache.taverna.biocatalogue.model.ResourceManager;


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
