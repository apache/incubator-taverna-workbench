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
package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Manages the colour of elements in a graph.
 * 
 * @author David Withers
 * @author Start Owen
 */
public class GraphColorManager {

	private static Color[] subGraphFillColors = new Color[] {
			Color.decode("#ffffff"), Color.decode("#f0f8ff"),
			Color.decode("#faebd7"), Color.decode("#f5f5dc") };

	/**
	 * Returns the colour associated with the Activity.
	 * 
	 * For unknown activities Color.WHITE is returned.
	 * 
	 * For {@link LocalworkerActivity} which have been user configured use the
	 * BeanshellActivity colour
	 * 
	 * @return the colour associated with the Activity
	 */
	public static Color getFillColor(Activity<?> activity) {

		if (activity.getClass().getName().equals(
				"net.sf.taverna.t2.activities.localworker.LocalworkerActivity")) {
			try {
				// To avoid compile time dependency - read isAltered property as bean
				if (Boolean.TRUE.equals(PropertyUtils.getProperty(activity, "altered"))) {
					Color colour = ColourManager
							.getInstance()
							.getPreferredColour(
									"net.sf.taverna.t2.activities.beanshell.BeanshellActivity");
					return colour;
				}
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
			} catch (NoSuchMethodException e) {
			}

		}
		Color colour = ColourManager.getInstance().getPreferredColour(
				activity.getClass().getName());
		return colour;
	}

	public static Color getSubGraphFillColor(int depth) {
		return subGraphFillColors[depth % subGraphFillColors.length];
	}

}
