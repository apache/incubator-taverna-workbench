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

import static java.beans.Introspector.getBeanInfo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workbench.ui.servicepanel.tree.Filter;

import org.apache.log4j.Logger;

public class ServiceFilter implements Filter {
	private static Logger logger = Logger.getLogger(ServiceFilter.class);

	private String filterString;
	private boolean superseded;
	private String[] filterLowerCaseSplit;
	private final Object rootToIgnore;

	public ServiceFilter(String filterString, Object rootToIgnore) {
		this.filterString = filterString;
		this.rootToIgnore = rootToIgnore;
		this.filterLowerCaseSplit = filterString.toLowerCase().split(" ");
		this.superseded = false;
	}

	private boolean basicFilter(DefaultMutableTreeNode node) {
		if (node == rootToIgnore)
			return false;
		if (filterString.isEmpty())
			return true;

		if (node.getUserObject() instanceof ServiceDescription) {
			ServiceDescription serviceDescription = (ServiceDescription) node
					.getUserObject();
			for (String searchTerm : filterLowerCaseSplit) {
				if (superseded)
					return false;
				String[] typeSplit = searchTerm.split(":", 2);
				String type;
				String keyword;
				if (typeSplit.length == 2) {
					type = typeSplit[0];
					keyword = typeSplit[1].toLowerCase();
				} else {
					type = null;
					keyword = searchTerm.toLowerCase();
				}
				try {
					if (!doesPropertySatisfy(serviceDescription, type, keyword))
						return false;
				} catch (IntrospectionException | IllegalArgumentException
						| IllegalAccessException | InvocationTargetException e) {
					logger.error(
							"failed to get properties of service description",
							e);
					return false;
				}
			}
			return true;
		}
		for (String searchString : filterLowerCaseSplit)
			if (!node.getUserObject().toString().toLowerCase().contains(
					searchString))
				return false;
		return true;
	}

	/**
	 * Determine whether a service description satisfies a search term.
	 * 
	 * @param serviceDescription
	 *            The service description bean to look in.
	 * @param type
	 *            The name of the property to look in, or <tt>null</tt> to
	 *            search in all public non-expert properties.
	 * @param searchTerm
	 *            The string to search for.
	 * @return <tt>true</tt> if-and-only-if the description matches.
	 */
	private boolean doesPropertySatisfy(ServiceDescription serviceDescription,
			String type, String searchTerm) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException,
			IntrospectionException {
		BeanInfo beanInfo = getBeanInfo(serviceDescription.getClass());
		for (PropertyDescriptor property : beanInfo.getPropertyDescriptors()) {
			if (superseded)
				return false;
			if ((type == null && !property.isHidden() && !property.isExpert())
					|| property.getName().equalsIgnoreCase(type)) {
				Method readMethod = property.getReadMethod();
				if (readMethod == null)
					continue;
				Object readProperty = readMethod.invoke(serviceDescription,
						new Object[0]);
				if (readProperty == null)
					continue;
				if (readProperty.toString().toLowerCase().contains(searchTerm))
					return true;
				// Dig deeper?
			}
		}
		return false;
	}

	@Override
	public boolean pass(DefaultMutableTreeNode node) {
		return basicFilter(node);
	}

	@Override
	public String filterRepresentation(String original) {
		return original;
	}

	/**
	 * @return the superseded
	 */
	@Override
	public boolean isSuperseded() {
		return superseded;
	}

	/**
	 * @param superseded
	 *            the superseded to set
	 */
	@Override
	public void setSuperseded(boolean superseded) {
		this.superseded = superseded;
	}
}
