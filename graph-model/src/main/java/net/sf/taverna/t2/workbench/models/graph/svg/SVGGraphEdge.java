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
package net.sf.taverna.t2.workbench.models.graph.svg;

import java.awt.Color;
import java.awt.Point;
import java.util.List;

import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseClickEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseDownEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOutEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOverEventListener;

import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGElement;

/**
 * SVG representation of a graph edge.
 * 
 * @author David Withers
 */
public class SVGGraphEdge extends GraphEdge {

	private static final String ARROW_LENGTH = "8.5";

	private static final String ARROW_WIDTH = "3";

	private static final String ELLIPSE_RADIUS = "3.5";

	private SVGGraphController graphController;
	
	private SVGMouseClickEventListener mouseClickAction;

	private SVGMouseDownEventListener mouseDownAction;

	@SuppressWarnings("unused")
	private SVGMouseOverEventListener mouseOverAction;

	@SuppressWarnings("unused")
	private SVGMouseOutEventListener mouseOutAction;

	private SVGOMGElement g;

	private SVGOMPathElement path;

	private SVGOMPolygonElement polygon;

	private SVGOMEllipseElement ellipse;

	private SVGOMPathElement deleteButton;

	private SVGGraphicsElement arrowHead;

	public SVGGraphEdge(SVGGraphController graphController) {
		super(graphController);
		this.graphController = graphController;
		
		mouseClickAction = new SVGMouseClickEventListener(eventManager, this);
		mouseDownAction = new SVGMouseDownEventListener(eventManager, this);
		mouseOverAction = new SVGMouseOverEventListener(eventManager, this);
		mouseOutAction = new SVGMouseOutEventListener(eventManager, this);

		g = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		
		path = (SVGOMPathElement) graphController.createElement(SVGConstants.SVG_PATH_TAG);
		path.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		EventTarget t = (EventTarget) path;		
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, mouseClickAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, mouseOverAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, mouseOutAction, false);
		g.appendChild(path);
		
		polygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		polygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE,
				ARROW_LENGTH + ", 0" +
				" 0, -"+ ARROW_WIDTH +
				" 0," + ARROW_WIDTH);
		t = (EventTarget) polygon;
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, mouseClickAction, false);
		t.addEventListener(SVGConstants.SVG_MOUSEDOWN_EVENT_TYPE, mouseDownAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, mouseOverAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, mouseOutAction, false);
		
		ellipse = (SVGOMEllipseElement) graphController.createElement(SVGConstants.SVG_ELLIPSE_TAG);
		ellipse.setAttribute(SVGConstants.SVG_CX_ATTRIBUTE, ELLIPSE_RADIUS);
		ellipse.setAttribute(SVGConstants.SVG_CY_ATTRIBUTE, SVGConstants.SVG_ZERO_VALUE);
		ellipse.setAttribute(SVGConstants.SVG_RX_ATTRIBUTE, ELLIPSE_RADIUS);
		ellipse.setAttribute(SVGConstants.SVG_RY_ATTRIBUTE, ELLIPSE_RADIUS);
		

		arrowHead = polygon;
		g.appendChild(arrowHead);

		deleteButton = (SVGOMPathElement) graphController.createElement(SVGConstants.SVG_PATH_TAG);
		deleteButton.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, "red");
		deleteButton.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
		deleteButton.setAttribute(SVGConstants.SVG_D_ATTRIBUTE, "M-3.5,-7L3.5,0M-3.5,0L3.5,-7");
		deleteButton.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_NONE_VALUE);
		g.appendChild(deleteButton);

	}
		
	public SVGElement getSVGElement() {
		return g;
	}
	
	/**
	 * Returns the path.
	 *
	 * @return the path
	 */
	public SVGOMPathElement getPathElement() {
		return path;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.models.graph.GraphElement#setSelected(boolean)
	 */
	public void setSelected(final boolean selected) {
		super.setSelected(selected);
		graphController.updateSVGDocument(
				new Runnable() {
					public void run() {
						if (selected) {
							path.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGGraphSettings.SELECTED_COLOUR);
							arrowHead.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGGraphSettings.SELECTED_COLOUR);
							if (getFillColor() != null) {
								arrowHead.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGGraphSettings.SELECTED_COLOUR);
							}

//								System.out.println("Path = " +path.getAttribute("d"));
//								SVGOMAnimateMotionElement animateMotion = (SVGOMAnimateMotionElement) graphController.svgDocument
//								.createElementNS(SVGUtil.svgNS, SVGConstants.SVG_ANIMATE_MOTION_TAG);
//								animateMotion.setAttribute("begin", "0s");
//								animateMotion.setAttribute("dur", "1s");
//								animateMotion.setAttribute("repeatDur", "indefinite");
//								animateMotion.setAttribute("path", path.getAttribute("d"));

//								polygon.appendChild(animateMotion);
//								animateMotion.beginElement();

						} else {
							path.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(getColor()));
							arrowHead.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(getColor()));
							if (getFillColor() != null) {
								arrowHead.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGUtil.getHexValue(getColor()));
							}
						}
					}
				}
		);
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		if (active) {
			path.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
			deleteButton.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_INLINE_VALUE);
		} else {
			path.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1");
			deleteButton.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_NONE_VALUE);
		}
//		if (active) {
//			setColour(SVGGraphController.OUTPUT_COLOUR);
//			SVGGraphController.timer.schedule(new TimerTask() {
//				public void run() {
//					resetStyle();
//				}
//			}, SVGGraphController.OUTPUT_FLASH_PERIOD);
//		}
	}

	public void setVisible(final boolean visible) {
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (visible) {
						path.setAttribute("visibility", "visible");
						if (polygon != null) {
							polygon.setAttribute("visibility", "visible");
						} else {
							ellipse.setAttribute("visibility", "visible");
						}
					} else {
						path.setAttribute("visibility", "hidden");
						if (polygon != null) {
							polygon.setAttribute("visibility", "hidden");
						} else {
							ellipse.setAttribute("visibility", "hidden");
						}
					}
				}
			});
	}
	
	public void setColor(final Color color) {
		super.setColor(color);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					path.setAttribute(
							SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(color));
					polygon.setAttribute(
							SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(color));
					ellipse.setAttribute(
							SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(color));
				}
			}
		);
	}
	
	public void setFillColor(final Color fillColor) {
		super.setFillColor(fillColor);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					polygon.setAttribute(
							SVGConstants.SVG_FILL_ATTRIBUTE, SVGUtil.getHexValue(fillColor));
					ellipse.setAttribute(
							SVGConstants.SVG_FILL_ATTRIBUTE, SVGUtil.getHexValue(fillColor));
				}
			}
		);
	}
	
	@Override
	public void setArrowHeadStyle(final ArrowStyle arrowHeadStyle) {
		super.setArrowHeadStyle(arrowHeadStyle);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (ArrowStyle.NONE.equals(arrowHeadStyle)) {
						g.removeChild(arrowHead);
					} else if (ArrowStyle.NORMAL.equals(arrowHeadStyle)) {
						g.removeChild(arrowHead);
						arrowHead = polygon;
						g.appendChild(arrowHead);
					} else if (ArrowStyle.DOT.equals(arrowHeadStyle)) {
						g.removeChild(arrowHead);
						arrowHead = ellipse;
						g.appendChild(arrowHead);
					}
				}
			}
		);
	}

	public void setPath(final List<Point> pointList) {
		super.setPath(pointList);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					path.setAttribute(
							SVGConstants.SVG_D_ATTRIBUTE, SVGUtil.getPath(pointList));
					if (pointList.size() > 1) {
						Point a = pointList.get(pointList.size() - 2);
						Point b = pointList.get(pointList.size() - 1);
						double angle = SVGUtil.calculateAngle(a.x, a.y, b.x, b.y);
						polygon.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
								+ b.x + " " + b.y + ") rotate(" + angle + " 0 0) ");
						ellipse.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
								+ b.x + " " + b.y + ") rotate(" + angle + " 0 0) ");
						deleteButton.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("+b.x+" "+b.y+")");
					}
				}
			}
		);
	}

}
