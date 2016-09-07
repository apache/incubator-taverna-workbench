/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.models.graph;

import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

import org.apache.taverna.workbench.configuration.colour.ColourManager;

import org.apache.commons.beanutils.PropertyUtils;

import org.apache.taverna.scufl2.api.activity.Activity;

/**
 * Manages the colour of elements in a graph.
 *
 * @author David Withers
 * @author Start Owen
 */
public class GraphColorManager {
	private static final String BEANSHELL = "http://ns.taverna.org.uk/2010/activity/beanshell";
	private static final String LOCALWORKER = "http://ns.taverna.org.uk/2010/activity/localworker";

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
	public static Color getFillColor(Activity activity, ColourManager colourManager) {
		try {
			if (activity.getType().equals(LOCALWORKER)) {
				// To avoid compile time dependency - read isAltered property as bean
				if (Boolean.TRUE.equals(PropertyUtils.getProperty(activity, "altered"))) {
					Color colour = colourManager.getPreferredColour(BEANSHELL);
					return colour;
				}
			}
		} catch (IllegalAccessException | InvocationTargetException
				| NoSuchMethodException e) {
		}
		Color colour = colourManager.getPreferredColour(activity.getType().toASCIIString());
		return colour;
	}

	public static Color getSubGraphFillColor(int depth) {
		return subGraphFillColors[depth % subGraphFillColors.length];
	}
}
