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
package net.sf.taverna.t2.workbench.models.graph.svg.event;

import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.svg.SVGUtil;

import org.apache.batik.dom.svg.SVGOMPoint;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.svg.SVGLocatable;

/**
 * SVG event listener for handling mouse button up events.
 * 
 * @author David Withers
 */
public class SVGMouseOutEventListener extends SVGEventListener {

	public SVGMouseOutEventListener(GraphEventManager graphEventManager, GraphElement graphElement) {
		super(graphEventManager, graphElement);
	}

	public void handleEvent(Event evt) {
		if (evt instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) evt;
			SVGOMPoint point = SVGUtil.screenToDocument((SVGLocatable)evt.getTarget(),
					new SVGOMPoint(mouseEvent.getClientX(), mouseEvent.getClientX()));
			graphEventManager.mouseOut(graphElement, mouseEvent.getButton(),
					mouseEvent.getAltKey(), mouseEvent.getCtrlKey(), mouseEvent.getMetaKey(),
					(int) point.getX(), (int) point.getY(),
					mouseEvent.getScreenX(), mouseEvent.getScreenY());
			evt.stopPropagation();
		}
	}
	
}
