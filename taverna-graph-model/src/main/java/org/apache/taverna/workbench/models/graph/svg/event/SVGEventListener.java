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
