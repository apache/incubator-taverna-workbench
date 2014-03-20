/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.ui.zaria;

import java.io.InputStream;
import javax.swing.ImageIcon;

import org.jdom.Element;

/**
 * SPI representing UI perspectives
 * @author Stuart Owen
 *
 */

public interface PerspectiveSPI {
	
	/**
	 * 
	 * @return the input stream to the layout XML
	 */
	public InputStream getLayoutInputStream();
	
	/**
	 * 
	 * @return the icon image for the toolbar button
	 */
	public ImageIcon getButtonIcon();
	
	/**
	 * 
	 * @return the text for the perspective
	 */
	public String getText();
	
	/**
	 * Store internally any changes to the layout xml
	 */
	public void update(Element layoutElement);
	
	/**
	 * Provides a hint for the position of perspective in the toolbar and menu.
	 * The lower the value the earlier it will appear in the list.
	 * 
	 * Custom plugins are recommended to start with a value > 100 (allowing for a whopping 100 built in plugins!)
	 */
	public int positionHint();
	
	/**
	 * returns true if the perspective is set to be visible
	 * @return boolean
	 */
	public boolean isVisible();
	
	/**
	 * sets whether the perspective should be visible or not.
	 *
	 */
	public void setVisible(boolean visible);
		
	
		
	
}
