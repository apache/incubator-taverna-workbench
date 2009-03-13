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

import net.sf.taverna.t2.annotation.AnnotationAssertion;
import net.sf.taverna.t2.annotation.AnnotationChain;
import net.sf.taverna.t2.annotation.annotationbeans.HostInstitution;
import net.sf.taverna.t2.workbench.ui.impl.configuration.colour.ColourManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

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
			if (checkAnnotations(activity)) {
				Color colour = ColourManager
						.getInstance()
						.getPreferredColour(
								"net.sf.taverna.t2.activities.beanshell.BeanshellActivity");
				return colour;

			}

		}
		Color colour = ColourManager.getInstance().getPreferredColour(
				activity.getClass().getName());
		return colour;
	}

	private static boolean checkAnnotations(Activity<?> activity) {
		for (AnnotationChain chain : activity.getAnnotations()) {
			for (AnnotationAssertion<?> assertion : chain.getAssertions()) {
				Object detail = assertion.getDetail();
				System.out.println(detail.getClass().getName());
				if (detail instanceof HostInstitution) {
					// this is a user defined localworker so use the beanshell
					// colour!
					return true;
				}
			}
		}
		return false;
	}

	public static Color getSubGraphFillColor(int depth) {
		return subGraphFillColors[depth % subGraphFillColors.length];
	}

}
