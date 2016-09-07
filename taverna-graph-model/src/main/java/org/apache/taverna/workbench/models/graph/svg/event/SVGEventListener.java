/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.models.graph.svg.event;

import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.screenToDocument;
import org.apache.taverna.workbench.models.graph.GraphElement;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.svg.SVGLocatable;

/**
 * Abstract superclass for SVG event listeners.
 * 
 * @author David Withers
 */
public abstract class SVGEventListener implements EventListener {
	protected GraphElement graphElement;

	public SVGEventListener(GraphElement graphElement) {
		this.graphElement = graphElement;
	}

	protected abstract void event(SVGOMPoint point, MouseEvent evt);

	@Override
	public final void handleEvent(Event evt) {
		if (evt instanceof MouseEvent) {
			MouseEvent me = (MouseEvent) evt;
			SVGOMPoint point = screenToDocument((SVGLocatable) me.getTarget(),
					new SVGOMPoint(me.getClientX(), me.getClientY()));
			event(point, me);
			evt.stopPropagation();
		}
	}
}
