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
package net.sf.taverna.zaria;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * Simple ImageIcon cache for Zaria
 * 
 * @author Tom Oinn
 */
public class ZIcons {

	private static Map<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();

	public static ImageIcon iconFor(String name) {
		if (iconMap.containsKey(name)) {
			return iconMap.get(name);
		} else {
			URL iconLocation = ZIcons.class.getResource("icons/" + name
					+ ".png");
			// Prefer .png icons but use .gif if available
			if (iconLocation == null) {
				iconLocation = ZIcons.class.getResource("icons/" + name
						+ ".gif");
			}
			if (iconLocation != null) {
				ImageIcon icon = new ImageIcon(iconLocation);
				iconMap.put(name, icon);
				return icon;
			} else {
				return null;
			}
		}
	}

}
