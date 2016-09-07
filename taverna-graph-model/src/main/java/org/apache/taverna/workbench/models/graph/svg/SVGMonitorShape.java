package org.apache.taverna.workbench.models.graph.svg;

import org.apache.batik.dom.svg.SVGOMPolygonElement;

public interface SVGMonitorShape extends SVGShape {
	/**
	 * Returns the polygon used to display the completed value.
	 * 
	 * @return the polygon used to display the completed value
	 */
	SVGOMPolygonElement getCompletedPolygon();

	/**
	 * Sets the polygon used to display the completed value.
	 * 
	 * @param polygon
	 *            the new polygon used to display the completed value
	 */
	void setCompletedPolygon(SVGOMPolygonElement polygon);
}
