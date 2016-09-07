/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.models.graph.svg.event;

import org.apache.taverna.workbench.models.graph.GraphElement;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.w3c.dom.events.MouseEvent;

/**
 * SVG event listener for handling mouse movement events.
 * 
 * @author David Withers
 */
public class SVGMouseMovedEventListener extends SVGEventListener {
	public SVGMouseMovedEventListener(GraphElement graphElement) {
		super(graphElement);
	}

	@Override
	protected void event(SVGOMPoint point, MouseEvent mouseEvent) {
		graphElement.getEventManager().mouseMoved(graphElement,
				mouseEvent.getButton(), mouseEvent.getAltKey(),
				mouseEvent.getCtrlKey(), mouseEvent.getMetaKey(),
				(int) point.getX(), (int) point.getY(),
				mouseEvent.getScreenX(), mouseEvent.getScreenY());
	}
}
