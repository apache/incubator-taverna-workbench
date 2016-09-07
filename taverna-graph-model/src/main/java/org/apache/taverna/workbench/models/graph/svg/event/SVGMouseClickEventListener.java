package org.apache.taverna.workbench.models.graph.svg.event;

import org.apache.taverna.workbench.models.graph.GraphElement;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.w3c.dom.events.MouseEvent;

/**
 * SVG event listener for handling mouse click events.
 * 
 * @author David Withers
 */
public class SVGMouseClickEventListener extends SVGEventListener {
	public SVGMouseClickEventListener(GraphElement graphElement) {
		super(graphElement);
	}

	@Override
	protected void event(SVGOMPoint point, MouseEvent evt) {
		graphElement.getEventManager().mouseClicked(graphElement,
				evt.getButton(), evt.getAltKey(), evt.getCtrlKey(),
				evt.getMetaKey(), (int) point.getX(), (int) point.getY(),
				evt.getScreenX(), evt.getScreenY());
	}
}
