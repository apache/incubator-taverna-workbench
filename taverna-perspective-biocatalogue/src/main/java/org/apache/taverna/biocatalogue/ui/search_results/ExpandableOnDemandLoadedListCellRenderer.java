package org.apache.taverna.biocatalogue.ui.search_results;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;

import org.apache.taverna.biocatalogue.model.LoadingResource;
import org.apache.taverna.biocatalogue.model.Resource;
import org.apache.taverna.biocatalogue.model.ResourceManager;
import org.apache.taverna.biocatalogue.model.Resource.TYPE;

import org.biocatalogue.x2009.xml.rest.ResourceLink;


/**
 * 
 * @author Sergejs Aleksejevs
 */
@SuppressWarnings("serial")
public abstract class ExpandableOnDemandLoadedListCellRenderer extends JPanel implements ListCellRenderer
{
  protected static final int DESCRIPTION_MAX_LENGTH_COLLAPSED = 90;
  protected static final int DESCRIPTION_MAX_LENGTH_EXPANDED = 500;
  
  protected static final int LINE_LENGTH = 90;
  
  
  protected static final int TOOLTIP_DESCRIPTION_LENGTH = 150;
  protected static final int TOOLTIP_LINE_LENGTH = 60;
  
  // list cells are not repainted by Swing by default - hence to use animated GIFs inside cells,
  // need to have a special class that takes care of changing the frames as necessary
  protected JLabel loaderBarAnimationOrange = new JLabel(ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_ORANGE), JLabel.CENTER);
  protected JLabel loaderBarAnimationGrey = new JLabel(ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_GREY), JLabel.CENTER);
  protected JLabel loaderBarAnimationGreyStill = new JLabel (ResourceManager.getImageIcon(ResourceManager.BAR_LOADER_GREY_STILL), JLabel.CENTER);
  
  
  protected JPanel thisPanel;
  private List<Class<? extends ResourceLink>> resourceClasses;
  
  
  protected JLabel jlExpand;
  protected static Rectangle expandRect;
    
  public ExpandableOnDemandLoadedListCellRenderer()
  {
    this.thisPanel = this;
    
    resourceClasses = new ArrayList<Class<? extends ResourceLink>>();
    try {
      for (Resource.TYPE resourceType : Resource.TYPE.values()) {
        resourceClasses.add(resourceType.getXmlBeansGeneratedClass());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
      
  }
  
  
  public static Rectangle getExpandRect() {
    return (expandRect == null ? new Rectangle() : expandRect);
  }
  
  
  public Component getListCellRendererComponent(JList list, Object itemToRender, int itemIndex, boolean isSelected, boolean cellHasFocus)
  {
    // the same instance of the cell renderer is used for all cells, so
    // need to remove everything from the current panel to ensure clean
    // painting of the current cell
    this.removeAll();
    
    // GET THE DATA
    
    // LoadingResource is a placeholder for the detailed data on the resource --
    // it is being quickly fetched from the API and contanins just the name and the URL
    // of the actual resource;
    // 
    // these entries will be placed into the list when the initial part of the search
    // is complete, further details will be loaded asynchronously and inserted into
    // the same area
    if (itemToRender instanceof LoadingResource) {
      prepareInitiallyLoadingEntry(itemToRender);
    }
    
    // real data about some resource: details, but in the collapsed form
    else if (isInstanceOfResourceType(itemToRender)) {
      prepareLoadedEntry(itemToRender, isSelected);
    }
       
    // error case - unknown resource...
    else {
      prepareUnknownResourceTypeEntry();
    }
    
    
    // MAKE SURE CELL SELECTION WORKS AS DESIRED
    if (shouldBeHidden(itemToRender)) {
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 4, 3, 4, list.getBackground()),
                BorderFactory.createLineBorder(Color.DARK_GRAY)));
        setBackground(list.getBackground());
        setForeground(list.getBackground());
    }
    else if (isSelected) {
      this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 4, 3, 4, list.getBackground()),
                                                        BorderFactory.createLineBorder(Color.DARK_GRAY)));
        setBackground(Color.decode("#BAE8FF"));         // very light blue colour
        setForeground(list.getSelectionForeground());
    } else {
        this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(3, 4, 3, 4, list.getBackground()),
                                                          BorderFactory.createLineBorder(Color.DARK_GRAY)));
        setBackground(Color.WHITE);
        setForeground(list.getForeground());
    }
    
    this.revalidate();
    
    if (expandRect == null && jlExpand != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          expandRect = jlExpand.getBounds();
          expandRect.x -= Math.abs(thisPanel.getBounds().x);
        }
      });
    }
    
    return (this);
  }
  
  
  /**
   * This entry can be in one of two states:
   * -- containing only the name of the resource and NOT loading further details;
   * -- containing only the name of the resource and LOADING further details.
   * 
   * @param itemToRender
   * @return
   */
  protected abstract GridBagConstraints prepareInitiallyLoadingEntry(Object itemToRender);
  
  
  /**
   * 
   * @param itemToRender
 * @param isSelected 
   * @param expandedView <code>true</code> to indicate that this method generates the top
   *                     fragment of the expanded list entry for this SOAP operation / REST method.
   * @return
   */
  protected abstract GridBagConstraints prepareLoadedEntry(Object itemToRender, boolean isSelected);
  
  
  private void prepareUnknownResourceTypeEntry()
  {
    this.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    c.insets = new Insets(8, 6, 6, 3);
    this.add(new JLabel(ResourceManager.getImageIcon(ResourceManager.UNKNOWN_RESOURCE_TYPE_ICON)), c);
    
    c.gridx++;
    c.weightx = 1.0;
    c.insets = new Insets(8, 3, 6, 3);
    this.add(new JLabel("<html><font color=\"#FF0000\">ERROR: This item shoulnd't have been here...</font></html>"), c);
    
    c.gridx = 1;
    c.gridy++;
    c.gridheight = 1;
    c.weightx = 1.0;
    c.weighty = 0;
    c.insets = new Insets(3, 3, 3, 3);
    this.add(new JLabel(" "), c);
    
    c.gridy++;
    c.insets = new Insets(3, 3, 8, 3);
    this.add(new JLabel(" "), c);
  }
  
  
  private boolean isInstanceOfResourceType(Object itemToRender)
  {
    for (Class<? extends ResourceLink> resourceClass : resourceClasses) {
      if (resourceClass.isInstance(itemToRender)) {
        return (true);
      }
    }
    
    return (false);
  }
  
  protected TYPE determineResourceType(Object itemToRender) {
    if (itemToRender instanceof ResourceLink) {
      return (Resource.getResourceTypeFromResourceURL(((ResourceLink)itemToRender).getHref()));
    }
    else {
      return (null);
    }
  }
  
  abstract boolean shouldBeHidden(Object itemToRender);
  
}
