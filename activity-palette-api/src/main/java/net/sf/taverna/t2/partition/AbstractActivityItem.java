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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.Icon;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper;

import org.apache.log4j.Logger;

public abstract class AbstractActivityItem implements ActivityItem {

	private static Logger logger = Logger.getLogger(AbstractActivityItem.class);

	/**
	 * Compare the values that are shown in the activity palette and re-order
	 * alphabetically by the lower case representation
	 */
	public int compareTo(ActivityItem o) {
		if (toString().toLowerCase().compareTo(o.toString().toLowerCase()) > 0) {

			return 1;
		}
		return 0;
	}

	/**
	 * Returns a {@link Transferable} containing an
	 * {@link ActivityAndBeanWrapper} with an {@link Activity} and its
	 * configuration bean inside. To get the Transferable you call
	 * {@link ActivityItem#getActivityTransferable()}, with this you call this
	 * method which returns an {@link ActivityAndBeanWrapper}. From the callers
	 * side you ask for a DataFlavor of
	 * {@link DataFlavor#javaJVMLocalObjectMimeType} of type
	 * {@link ActivityAndBeanWrapper} <code>
	 * new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=net.sf.taverna.t2.workflowmodel.processor.activity.ActivityAndBeanWrapper"));
	 * </code>
	 */
	public Transferable getActivityTransferable() {
		Transferable transferable = new Transferable() {

			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException, IOException {
				ActivityAndBeanWrapper wrapper = new ActivityAndBeanWrapper();
				wrapper.setActivity(getUnconfiguredActivity());
				wrapper.setBean(getConfigBean());
				wrapper.setName(AbstractActivityItem.this.toString());
				return wrapper;
			}

			public DataFlavor[] getTransferDataFlavors() {
				DataFlavor[] flavors = new DataFlavor[1];
				DataFlavor flavor = null;
				try {
					flavor = new DataFlavor(
							DataFlavor.javaJVMLocalObjectMimeType
									+ ";class="
									+ ActivityAndBeanWrapper.class
											.getCanonicalName(), "Activity",
							getClass().getClassLoader());
				} catch (ClassNotFoundException e) {
					logger.error("Error casting Dataflavor", e);
				}
				flavors[0] = flavor;
				return flavors;
			}

			public boolean isDataFlavorSupported(DataFlavor flavor) {
				DataFlavor thisFlavor = null;
				try {
					thisFlavor = new DataFlavor(
							DataFlavor.javaJVMLocalObjectMimeType
									+ ";class="
									+ ActivityAndBeanWrapper.class
											.getCanonicalName(), "Activity",
							getClass().getClassLoader());
				} catch (ClassNotFoundException e) {
					logger.error("Error casting Dataflavor", e);
				}
				return flavor.equals(thisFlavor);
			}

		};
		return transferable;
	}

	public abstract Object getConfigBean() ;

	public abstract Icon getIcon();

	public abstract Activity<?> getUnconfiguredActivity();

	
}
