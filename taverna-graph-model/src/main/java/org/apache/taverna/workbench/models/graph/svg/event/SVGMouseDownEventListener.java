package org.apache.taverna.workbench.models.graph.svg.event;

import org.apache.taverna.workbench.models.graph.GraphElement;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.w3c.dom.events.MouseEvent;

/**
 * SVG event listener for handling mouse button down events.
 * 
 * @author David Withers
 */
public class SVGMouseDownEventListener extends SVGEventListener {
	public SVGMouseDownEventListener(GraphElement graphElement) {
		super(graphElement);
	}

	@Override
	protected void event(SVGOMPoint point, MouseEvent evt) {
		graphElement.getEventManager().mouseDown(graphElement, evt.getButton(),
				evt.getAltKey(), evt.getCtrlKey(), evt.getMetaKey(),
				(int) point.getX(), (int) point.getY(), evt.getScreenX(),
				evt.getScreenY());
	}
}
