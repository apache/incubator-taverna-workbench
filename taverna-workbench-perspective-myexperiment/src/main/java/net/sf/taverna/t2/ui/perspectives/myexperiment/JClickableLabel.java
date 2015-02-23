/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester
 * 
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.ui.perspectives.myexperiment;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import net.sf.taverna.t2.ui.perspectives.myexperiment.model.Util;

/**
 * @author Sergejs Aleksejevs, Jiten Bhagat
 */

public class JClickableLabel extends JLabel implements MouseListener
{
  // This will hold the data which is relevant to processing the 'click' event on this label
  private String strData;
  
  // This will hold a reference to ResourcePreviewBrowser instance that is supposed to process the clicks
  // on JClickableLabels
  private ActionListener clickHandler;
  
  
  public JClickableLabel(String strLabel, String strDataForAction, EventListener eventHandler)
  {
    this(strLabel, strDataForAction, eventHandler, null);
  }
  
  public JClickableLabel(String strLabel, String strDataForAction, EventListener eventHandler, Icon icon)
  {
    this(strLabel, strDataForAction, eventHandler, icon, SwingUtilities.LEFT);
  }
  
  public JClickableLabel(String strLabel, String strDataForAction, EventListener eventHandler, Icon icon, int horizontalAlignment)
  {
    this(strLabel, strDataForAction, eventHandler, icon, horizontalAlignment, null);
  }
  
  /**
   * 
   * @param strLabel Textual label that will be visible in the UI.
   * @param strDataForAction Data that will be passed to eventHandler when click on the label is made.
   * @param eventHandler ActionListener that will process clicks on this label.
   * @param icon Icon to display in the label.
   * @param horizontalAlignment This is one of SwingConstants: LEFT, CENTER, RIGHT, LEADING or TRAILING
   * @param strTooltip Tooltip to show over the label - if none is provided (e.g. null value), the strLabel will be used as a tooltip.
   */
  public JClickableLabel(String strLabel, String strDataForAction, EventListener eventHandler, Icon icon, int horizontalAlignment, String strTooltip)
  {
    super(strLabel, icon, horizontalAlignment);
    
    this.strData = strDataForAction;
    this.clickHandler = (ActionListener)eventHandler;
    
    // empty border at the top and bottom will simulate "line-spacing"
    // (this is only needed when an icon is displayed)
    if (icon != null) {
      this.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
    }
    
    // the tooltip for now only shows the full label text
    this.setToolTipText(strTooltip == null ? strLabel : strTooltip);
    this.setForeground(Color.BLUE);
    this.addMouseListener(this);
  }
  
  
  /* This class extends JLabel, so it can't extend MouseAdapter;
   * therefore, empty methods will be added for not useful callbacks
   * from the MouseListener interface.
   */
  public void mouseClicked(MouseEvent e) 
  {
    // call 'actionPerfermed' method on the clickHandler instance that was supplied
    // on creation of the JClickableLabel instance
    this.clickHandler.actionPerformed(new ActionEvent(this, e.getID(), this.strData));
  }
  
  public void mouseEntered(MouseEvent e) 
  {
    this.setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) ) ;
  }
  
  public void mouseExited(MouseEvent e) 
  {
    this.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) ) ;
  }
  
  public void mousePressed(MouseEvent e) 
  {
    // do nothing
  }
  
  public void mouseReleased(MouseEvent e) 
  {
    // do nothing
  }
  
}
