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
package net.sf.taverna.t2.partition;

import java.awt.datatransfer.Transferable;

import javax.swing.Icon;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * An interface that defines an "item" that is the result of performing a query
 * for an Activity type.
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * 
 * @see ActivityQuery
 * @see Query
 * 
 */
public interface ActivityItem extends Comparable<ActivityItem>{
	/**
	 * Used during drag and drop operations. The activity palette (ie the Tree
	 * which has the {@link RootPartition} stuff) is made up of
	 * {@link ActivityItem}s each of which will return a {@link Transferable}.
	 * Inside this will be a wrapper containing the activity and a bean
	 */
	public Transferable getActivityTransferable();
	
	/**
	 * @return the Icon that should be displayed in the activity palette tree.
	 */
	public Icon getIcon();
	
	public Object getConfigBean();

	public Activity<?> getUnconfiguredActivity();


}
