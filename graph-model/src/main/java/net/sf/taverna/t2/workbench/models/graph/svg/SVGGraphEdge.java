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
import org.apache.batik.dom.svg.SVGOMAnimationElement;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SMILConstants;
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

	private SVGGraphElementDelegate delegate;

	private SVGMouseClickEventListener mouseClickAction;

	private SVGMouseDownEventListener mouseDownAction;

	@SuppressWarnings("unused")
	private SVGMouseOverEventListener mouseOverAction;

	@SuppressWarnings("unused")
	private SVGMouseOutEventListener mouseOutAction;

	private SVGOMGElement mainGroup;

	private SVGOMPathElement path, deleteButton;

	private SVGOMPolygonElement polygon;

	private SVGOMEllipseElement ellipse;

	private SVGGraphicsElement arrowHead;

	private SVGOMAnimationElement animatePath, animatePosition, animateRotation;

	public SVGGraphEdge(SVGGraphController graphController) {
		super(graphController);
		this.graphController = graphController;

		mouseClickAction = new SVGMouseClickEventListener(this);
		mouseDownAction = new SVGMouseDownEventListener(this);
		mouseOverAction = new SVGMouseOverEventListener(this);
		mouseOutAction = new SVGMouseOutEventListener(this);

		mainGroup = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		mainGroup.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		mainGroup.setAttribute(SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE,
				CSSConstants.CSS_NONE_VALUE);
		mainGroup.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1");

		path = (SVGOMPathElement) graphController.createElement(SVGConstants.SVG_PATH_TAG);
		path.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		EventTarget t = (EventTarget) path;
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, mouseClickAction, false);
		// t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE,
		// mouseOverAction, false);
		// t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE,
		// mouseOutAction, false);
		mainGroup.appendChild(path);

		polygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		polygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, ARROW_LENGTH + ", 0" + " 0, -"
				+ ARROW_WIDTH + " 0," + ARROW_WIDTH);
		t = (EventTarget) polygon;
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, mouseClickAction, false);
		t.addEventListener(SVGConstants.SVG_MOUSEDOWN_EVENT_TYPE, mouseDownAction, false);
		// t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE,
		// mouseOverAction, false);
		// t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE,
		// mouseOutAction, false);

		ellipse = (SVGOMEllipseElement) graphController.createElement(SVGConstants.SVG_ELLIPSE_TAG);
		ellipse.setAttribute(SVGConstants.SVG_CX_ATTRIBUTE, ELLIPSE_RADIUS);
		ellipse.setAttribute(SVGConstants.SVG_CY_ATTRIBUTE, SVGConstants.SVG_ZERO_VALUE);
		ellipse.setAttribute(SVGConstants.SVG_RX_ATTRIBUTE, ELLIPSE_RADIUS);
		ellipse.setAttribute(SVGConstants.SVG_RY_ATTRIBUTE, ELLIPSE_RADIUS);

		arrowHead = polygon;
		mainGroup.appendChild(arrowHead);

		deleteButton = (SVGOMPathElement) graphController.createElement(SVGConstants.SVG_PATH_TAG);
		deleteButton.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, "red");
		deleteButton.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
		deleteButton.setAttribute(SVGConstants.SVG_D_ATTRIBUTE, "M-3.5,-7L3.5,0M-3.5,0L3.5,-7");
		deleteButton.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_NONE_VALUE);
		mainGroup.appendChild(deleteButton);

		animatePath = SVGUtil.createAnimationElement(graphController, SVGConstants.SVG_ANIMATE_TAG,
				SVGConstants.SVG_D_ATTRIBUTE, null);

		animatePosition = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TRANSFORM_TAG, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
				SVGConstants.TRANSFORM_TRANSLATE);

		animateRotation = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TRANSFORM_TAG, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
				SVGConstants.TRANSFORM_ROTATE);
		animateRotation.setAttribute(SMILConstants.SMIL_ADDITIVE_ATTRIBUTE,
				SMILConstants.SMIL_SUM_VALUE);

		delegate = new SVGGraphElementDelegate(graphController, this, mainGroup);
	}

	public SVGElement getSVGElement() {
		return mainGroup;
	}

	/**
	 * Returns the path.
	 * 
	 * @return the path
	 */
	public SVGOMPathElement getPathElement() {
		return path;
	}

	@Override
	public void setSelected(final boolean selected) {
		super.setSelected(selected);
		graphController.updateSVGDocument(new Runnable() {
			public void run() {
				mainGroup.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE,
						selected ? SVGGraphSettings.SELECTED_COLOUR : SVGUtil
								.getHexValue(getColor()));
				mainGroup.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE,
						selected ? SVGGraphSettings.SELECTED_COLOUR : SVGUtil
								.getHexValue(getColor()));
			}
		});
	}

	@Override
	public void setActive(final boolean active) {
		super.setActive(active);
		graphController.updateSVGDocument(new Runnable() {
			public void run() {
				if (active) {
					path.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
					deleteButton.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
							CSSConstants.CSS_INLINE_VALUE);
				} else {
					path.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1");
					deleteButton.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
							CSSConstants.CSS_NONE_VALUE);
				}
			}
		});
	}

	@Override
	public void setArrowHeadStyle(final ArrowStyle arrowHeadStyle) {
		super.setArrowHeadStyle(arrowHeadStyle);
		graphController.updateSVGDocument(new Runnable() {
			public void run() {
				if (ArrowStyle.NONE.equals(arrowHeadStyle)) {
					mainGroup.removeChild(arrowHead);
				} else if (ArrowStyle.NORMAL.equals(arrowHeadStyle)) {
					mainGroup.removeChild(arrowHead);
					arrowHead = polygon;
					mainGroup.appendChild(arrowHead);
				} else if (ArrowStyle.DOT.equals(arrowHeadStyle)) {
					mainGroup.removeChild(arrowHead);
					arrowHead = ellipse;
					mainGroup.appendChild(arrowHead);
				}
			}
		});
	}

	@Override
	public void setPath(final List<Point> pointList) {
		if (pointList == null) {
			return;
		}
		final List<Point> oldPointList = getPath();
		super.setPath(pointList);
		graphController.updateSVGDocument(new Runnable() {
			public void run() {
				Point lastPoint = pointList.get(pointList.size() - 1);
				double angle = SVGUtil.calculateAngle(pointList);
				if (graphController.isAnimatable() && oldPointList != null) {
					SVGUtil.adjustPathLength(oldPointList, pointList.size());
					Point oldLastPoint = oldPointList.get(oldPointList.size() - 1);
					double oldAngle = SVGUtil.calculateAngle(oldPointList);
					SVGUtil.animate(animatePath, path, graphController.getAnimationSpeed(),
							SVGUtil.getPath(oldPointList), SVGUtil.getPath(pointList));

					SVGUtil.animate(animatePosition, polygon, graphController
							.getAnimationSpeed(), oldLastPoint.x + ", " + oldLastPoint.y,
							lastPoint.x + ", " + lastPoint.y);

					SVGUtil.animate(animateRotation, polygon, graphController
							.getAnimationSpeed(), oldAngle + " 0 0", angle + " 0 0");

					ellipse.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
							+ lastPoint.x + " " + lastPoint.y + ") rotate(" + angle + " 0 0) ");
					deleteButton.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
							+ lastPoint.x + " " + lastPoint.y + ")");
				} else {
					path.setAttribute(SVGConstants.SVG_D_ATTRIBUTE, SVGUtil.getPath(pointList));
					polygon.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
							+ lastPoint.x + " " + lastPoint.y + ") rotate(" + angle + " 0 0) ");
					ellipse.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
							+ lastPoint.x + " " + lastPoint.y + ") rotate(" + angle + " 0 0) ");
					deleteButton.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
							+ lastPoint.x + " " + lastPoint.y + ")");
				}
			}
		});
	}

	@Override
	public void setColor(final Color color) {
		delegate.setColor(color);
		super.setColor(color);
	}

	@Override
	public void setFillColor(final Color fillColor) {
		delegate.setFillColor(fillColor);
		super.setFillColor(fillColor);
	}

	@Override
	public void setVisible(final boolean visible) {
		delegate.setVisible(visible);
		super.setVisible(visible);
	}

	@Override
	public void setFiltered(final boolean filtered) {
		delegate.setFiltered(filtered);
		super.setFiltered(filtered);
	}

	@Override
	public void setOpacity(final float opacity) {
		delegate.setOpacity(opacity);
		super.setOpacity(opacity);
	}

}
