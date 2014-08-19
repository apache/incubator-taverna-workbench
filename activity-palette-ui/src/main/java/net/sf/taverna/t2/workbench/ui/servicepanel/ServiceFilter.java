/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester   
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
package net.sf.taverna.t2.workbench.ui.servicepanel;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;

import org.apache.log4j.Logger;

public class ServiceFilter {

	private final String filterString;
	private final String[] filterLowerCaseSplit;
	private final Object rootToIgnore;
	private static Logger logger = Logger.getLogger(ServiceFilter.class);

	public ServiceFilter(final String filterString, final Object rootToIgnore) {
		this.filterString = filterString;
		this.rootToIgnore = rootToIgnore;
		this.filterLowerCaseSplit = filterString.toLowerCase().split(" ");
	}

	private boolean basicFilter(final DefaultMutableTreeNode node) {
		if (node == rootToIgnore) {
			return false;
		}
		if (filterString.equals("")) {
			return true;
		}
		if (node.getUserObject() instanceof ServiceDescription) {
			@SuppressWarnings("rawtypes")
			final ServiceDescription serviceDescription = (ServiceDescription) node
					.getUserObject();
			search: for (final String searchTerm : filterLowerCaseSplit) {
				final String[] typeSplit = searchTerm.split(":", 2);
				String type;
				String keyword;
				if (typeSplit.length == 2) {
					type = typeSplit[0];
					keyword = typeSplit[1];
				} else {
					type = null;
					keyword = searchTerm;
				}
				try {
					final BeanInfo beanInfo = Introspector
							.getBeanInfo(serviceDescription.getClass());
					for (final PropertyDescriptor property : beanInfo
							.getPropertyDescriptors()) {
						if (((type == null) && !property.isHidden() && !property
								.isExpert())
								|| property.getName().equalsIgnoreCase(type)) {
							final Method readMethod = property.getReadMethod();
							if (readMethod == null) {
								continue;
							}
							final Object readProperty = readMethod.invoke(
									serviceDescription, new Object[0]);
							if (readProperty == null) {
								continue;
							}
							if (readProperty.toString().toLowerCase()
									.contains(keyword)) {
								continue search;
							} else {
								// Dig deeper?
							}
						}
					}
					return false;
				} catch (final IntrospectionException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					logger.error("Bean inspection failed", e);
				}
				return false;
			}
			return true;
		}
		for (final String searchString : filterLowerCaseSplit) {
			if (!node.getUserObject().toString().toLowerCase()
					.contains(searchString)) {
				return false;
			}
		}
		return true;
	}

	public boolean pass(final DefaultMutableTreeNode node) {
		return basicFilter(node);
	}

	/**
	 * @return the filterString
	 */
	public String getFilterString() {
		return filterString;
	}

}
