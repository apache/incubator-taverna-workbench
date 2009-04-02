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
package net.sf.taverna.t2.workbench.activityicons;

import javax.swing.Icon;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * Defines an interface for getting an icon for an Activity.
 * 
 * @author Alex Nenadic
 * 
 */
public interface ActivityIconSPI {

	/** 
	 * A return value for {@link canProvideIconScore()} indicating an SPI cannot 
	 * provide an icon for a given activity. 
	 */
	public static int NO_ICON = 0;
	
	/** 
	 * {@link DefaultActivityIcon} returns this value that will be used when an activity
	 * that has no other SPI providing an icon for. Any SPI shour return value of at least 
	 * DEFAULT_ICON + 1 if they want to 'override' the default icon.
	 */
	public static int DEFAULT_ICON = 10;

	/**
	 * Returns a positive number if the class can provide an icon for the given 
	 * activity or {@link NO_ICON} otherwise. Out of two SPIs capable of providing 
	 * an icon for the same activity, the one returning a higher score will be used.
	 */
	public int canProvideIconScore(Activity<?> activity);

	/** Returns an icon for the Activity. */
	public Icon getIcon(Activity<?> activity);
}
