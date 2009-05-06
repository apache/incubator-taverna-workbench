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

import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseClickEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseDownEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseMovedEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOutEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOverEventListener;

import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.apache.batik.dom.svg.SVGOMAnimateTransformElement;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMRectElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Text;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGElement;

/**
 * SVG representation of a graph node.
 * 
 * @author David Withers
 */

public class SVGGraphNode extends GraphNode /*implements SVGMonitorShape*/ {

	private SVGGraphController graphController;

	private SVGMouseClickEventListener mouseClickAction;

	private SVGMouseMovedEventListener mouseMovedAction;

	private SVGMouseDownEventListener mouseDownAction;

	@SuppressWarnings("unused")
	private SVGMouseOverEventListener mouseOverAction;

	@SuppressWarnings("unused")
	private SVGMouseOutEventListener mouseOutAction;

	private SVGOMGElement g;

	private SVGElement expandedElement;

	private SVGElement contractedElement;

	private SVGOMGElement portsGroup;

	private SVGOMPolygonElement polygon;

	private SVGOMPolygonElement selectedPolygon;

	private SVGOMEllipseElement ellipse;
	
	private SVGOMEllipseElement selectedEllipse;
	
	private SVGGraphicsElement shapeElement;

	private SVGGraphicsElement selectedShapeElement;

	private SVGOMTextElement label;

	private SVGOMTextElement iteration;

	private SVGOMTextElement error;

	private SVGOMPolygonElement completedPolygon;
	
	private SVGElement deleteButton;

	private Text labelText;

	private Text iterationText;

	private Text errorsText;

	public SVGGraphNode(SVGGraphController graphController) {
		super(graphController);
		this.graphController = graphController;
		mouseClickAction = new SVGMouseClickEventListener(eventManager, this);
		mouseDownAction = new SVGMouseDownEventListener(eventManager, this);
		mouseMovedAction = new SVGMouseMovedEventListener(eventManager, this);
		mouseOverAction = new SVGMouseOverEventListener(eventManager, this);
		mouseOutAction = new SVGMouseOutEventListener(eventManager, this);
		
		g = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		g.setAttribute("alignment-baseline", SVGConstants.SVG_MIDDLE_VALUE);
		EventTarget t = (EventTarget) g;
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, mouseClickAction, false);			
		t.addEventListener(SVGConstants.SVG_MOUSEMOVE_EVENT_TYPE, mouseMovedAction, false);
		t.addEventListener(SVGConstants.SVG_MOUSEDOWN_EVENT_TYPE, mouseDownAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, mouseOverAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, mouseOutAction, false);

		portsGroup = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		expandedElement = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		contractedElement = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		g.appendChild(contractedElement);
		
		polygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		selectedPolygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		selectedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		selectedPolygon.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGGraphSettings.SELECTED_COLOUR);
		selectedPolygon.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
		selectedPolygon.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_NONE_VALUE);
		
		ellipse = (SVGOMEllipseElement) graphController.createElement(SVGConstants.SVG_ELLIPSE_TAG);
		selectedEllipse = (SVGOMEllipseElement) graphController.createElement(SVGConstants.SVG_ELLIPSE_TAG);
		selectedEllipse.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		selectedEllipse.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGGraphSettings.SELECTED_COLOUR);
		selectedEllipse.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
		selectedEllipse.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_NONE_VALUE);
		
		shapeElement = polygon;
		selectedShapeElement = selectedPolygon;
//		contractedElement.appendChild(shapeElement);
//		contractedElement.appendChild(selectedShapeElement);		

		completedPolygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		completedPolygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil.calculatePoints(getShape(), 0, 0));
		completedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGGraphSettings.COMPLETED_COLOUR);
		completedPolygon.setAttribute(SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE, "0.8");
		contractedElement.appendChild(completedPolygon);

		labelText = graphController.createText("");
		label = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		label.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_MIDDLE_VALUE);
		label.setAttribute("baseline-shift", "-35%");
		label.appendChild(labelText);
		contractedElement.appendChild(label);
		
		iterationText = graphController.createText("");
		iteration = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		iteration.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_END_VALUE);
		iteration.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "6");
		iteration.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		iteration.appendChild(iterationText);
		contractedElement.appendChild(iteration);
		
		errorsText = graphController.createText("");
		error = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		error.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_END_VALUE);
		error.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "6");
		error.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
//		error.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGGraphComponent.ERROR_COLOUR);
		error.appendChild(errorsText);
		contractedElement.appendChild(error);
				
//		deleteButton = createDeleteButton();
//		g.appendChild(deleteButton);
	}

	@SuppressWarnings("unused")
	private SVGElement createDeleteButton() {
		final SVGOMGElement button = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		button.setAttribute(CSSConstants.CSS_VISIBILITY_PROPERTY, CSSConstants.CSS_HIDDEN_VALUE);
		button.setAttribute(CSSConstants.CSS_POINTER_EVENTS_PROPERTY, CSSConstants.CSS_ALL_VALUE);
		
		SVGOMRectElement rect = (SVGOMRectElement) graphController.createElement(SVGConstants.SVG_RECT_TAG);
		rect.setAttribute(SVGConstants.SVG_X_ATTRIBUTE, "4");
		rect.setAttribute(SVGConstants.SVG_Y_ATTRIBUTE, "4");
		rect.setAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE, "13");
		rect.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, "13");
		rect.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, "none");
		button.appendChild(rect);
		
		final SVGOMPathElement path = (SVGOMPathElement) graphController.createElement(SVGConstants.SVG_PATH_TAG);
		path.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, "white");
		path.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
		path.setAttribute(SVGConstants.SVG_D_ATTRIBUTE, "M5,5L12,12M5,12L12,5");
		button.appendChild(path);
		
		EventTarget t = (EventTarget) button;
		t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, new EventListener() {
			public void handleEvent(Event evt) {
				if (isInteractive()) {
					deleteButton.setAttribute(CSSConstants.CSS_VISIBILITY_PROPERTY, CSSConstants.CSS_VISIBLE_VALUE);
					path.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, "red");
					evt.stopPropagation();
				}
			}
		}, false);
		t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, new EventListener() {
			public void handleEvent(Event evt) {
				if (isInteractive()) {
					path.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, "white");
					evt.stopPropagation();
				}
			}
		}, false);

		return button;
	}

	public SVGElement getSVGElement() {
		return g;
	}
	
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		if (isInteractive()) {
			if (active) {
				deleteButton.setAttribute(CSSConstants.CSS_VISIBILITY_PROPERTY, CSSConstants.CSS_VISIBLE_VALUE);
//				deleteButton.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_INLINE_VALUE);
			} else {
				deleteButton.setAttribute(CSSConstants.CSS_VISIBILITY_PROPERTY, CSSConstants.CSS_HIDDEN_VALUE);
//				button.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_NONE_VALUE);
			}
		}
	}

	public void setGraph(Graph graph) {
		super.setGraph(graph);
		if (graph instanceof SVGGraph) {
			SVGGraph svgGraph = (SVGGraph) graph;
			SVGElement graphElement = svgGraph.getSVGElement();
			if (isExpanded()) {
				g.replaceChild(expandedElement, graphElement);
			}
			expandedElement = graphElement;
		}
	}
	
	public void setExpanded(final boolean expanded) {
		if (isExpanded() != expanded) {
			graphController.updateSVGDocument(
					new Runnable() {
						public void run() {
							if (expanded) {
								g.replaceChild(expandedElement, contractedElement);
							} else {
								g.replaceChild(contractedElement, expandedElement);
							}
						}
					}
			);
		}
		super.setExpanded(expanded);
	}
	
	public void addSourceNode(GraphNode sourceNode) {
		super.addSourceNode(sourceNode);
		if (sourceNode instanceof SVGGraphNode) {
			SVGGraphNode svgGraphNode = (SVGGraphNode) sourceNode;
			portsGroup.appendChild(svgGraphNode.getSVGElement());
		}
	}

	public void addSinkNode(GraphNode sinkNode) {
		super.addSinkNode(sinkNode);
		if (sinkNode instanceof SVGGraphNode) {
			SVGGraphNode svgGraphNode = (SVGGraphNode) sinkNode;
			portsGroup.appendChild(svgGraphNode.getSVGElement());
		}
	}

	public void setPosition(final Point position) {
		super.setPosition(position);
		graphController.updateSVGDocument(
			new Runnable() {
				boolean animate = false;
				public void run() {
					if (animate) {						
						SVGOMAnimateTransformElement animateTransform = (SVGOMAnimateTransformElement) graphController.createElement(SVGConstants.SVG_ANIMATE_TRANSFORM_TAG);
						animateTransform.setAttribute("attributeName", "transform");
						animateTransform.setAttribute("attributeType", "XML");
						animateTransform.setAttribute("type", "translate");
						animateTransform.setAttribute("attributeName", "transform");
						animateTransform.setAttribute("additive", "replace");
//						animateTransform.setAttribute("begin", "0s");
						animateTransform.setAttribute("dur", "1s");
						animateTransform.setAttribute("from", "0, 0");
						animateTransform.setAttribute("to", position.x + ", " + position.y);
					
						shapeElement.appendChild(animateTransform);
						animateTransform.beginElement();
						
					} else {
						g.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
								+ position.x + " " + position.y + ")");
//						shapeElement.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
//								+ position.x + " " + position.y + ")");
//						label.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
//								+ position.x + " " + position.y + ")");
					}
				}
			}
		);
	}
	
	public void setWidth(final int width) {
		super.setWidth(width);
		graphController.updateSVGDocument(
				new Runnable() {
					public void run() {
						if (Shape.CIRCLE.equals(getShape())) {
							shapeElement.setAttribute(SVGConstants.SVG_RX_ATTRIBUTE, String.valueOf(width/2f));
							shapeElement.setAttribute(SVGConstants.SVG_CX_ATTRIBUTE, String.valueOf(width/2f));
							selectedShapeElement.setAttribute(SVGConstants.SVG_RX_ATTRIBUTE, String.valueOf(width/2f));
							selectedShapeElement.setAttribute(SVGConstants.SVG_CX_ATTRIBUTE, String.valueOf(width/2f));
						} else {
							updateShape();
						}
					}
				}
		);
	}
	
	public void setHeight(final int height) {
		super.setHeight(height);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (Shape.CIRCLE.equals(getShape())) {
						shapeElement.setAttribute(SVGConstants.SVG_RY_ATTRIBUTE, String.valueOf(height/2f));
						shapeElement.setAttribute(SVGConstants.SVG_CY_ATTRIBUTE, String.valueOf(height/2f));
						selectedShapeElement.setAttribute(SVGConstants.SVG_RY_ATTRIBUTE, String.valueOf(height/2f));
						selectedShapeElement.setAttribute(SVGConstants.SVG_CY_ATTRIBUTE, String.valueOf(height/2f));
					} else {
						updateShape();
					}
				}
			}
		);
	}
	
	private void updateShape() {
		shapeElement.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil.calculatePoints(getShape(), getWidth(), getHeight()));
		selectedShapeElement.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil.calculatePoints(getShape(), getWidth(), getHeight()));
		label.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
				+ getWidth()/2f + " " + getHeight()/2f + ")");
		iteration.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
				+ (getWidth()-1.5) + " 5.5)");
		error.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
				+ (getWidth()-1.5) + " " + (getHeight()-1) + ")");
	}

	public void setShape(final Shape shape) {
		final Shape currentShape = getShape();
		super.setShape(shape);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (currentShape == null) {
						if (shape != null) {
							if (Shape.CIRCLE.equals(shape)) {
								contractedElement.insertBefore(ellipse, completedPolygon);
								g.appendChild(selectedEllipse);
								shapeElement = ellipse;
								selectedShapeElement = selectedEllipse;
							} else {
								contractedElement.insertBefore(polygon, completedPolygon);
								g.appendChild(selectedPolygon);
								shapeElement = polygon;
								selectedShapeElement = selectedPolygon;
							}
							if (Shape.RECORD.equals(shape)) {
								contractedElement.appendChild(portsGroup);
							}
						}
					} else if (Shape.CIRCLE.equals(currentShape)) {
						if (!Shape.CIRCLE.equals(shape)) {
							contractedElement.replaceChild(ellipse, polygon);
							contractedElement.replaceChild(selectedEllipse, selectedPolygon);
							shapeElement = polygon;
							selectedShapeElement = polygon;
						}
						if (Shape.RECORD.equals(shape)) {
							contractedElement.appendChild(portsGroup);
						}
					} else {
						if (Shape.CIRCLE.equals(shape)) {
							contractedElement.replaceChild(polygon, ellipse);
							contractedElement.replaceChild(selectedPolygon, selectedEllipse);
							contractedElement.removeChild(portsGroup);
							shapeElement = ellipse;
							selectedShapeElement = selectedEllipse;
						}
					}
				}
			}
		);
	}
	
	public void setLineStyle(final LineStyle lineStyle) {
		super.setLineStyle(lineStyle);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (LineStyle.NONE.equals(lineStyle)) {
						shapeElement.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
					} else if (LineStyle.DOTTED.equals(lineStyle)) {
						shapeElement.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(getColor()));
						shapeElement.setAttribute(SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE, "1,5");
					} else if (LineStyle.SOLID.equals(lineStyle)) {
						shapeElement.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(getColor()));
						shapeElement.removeAttribute(SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE);							
					}
				}
			}
		);
	}
	
	public void setLabel(final String label) {
		super.setLabel(label);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					labelText.setData(label);
				}
			}
		);
	}
	
//	public void setLabelPosition(final Point labelPosition) {
//		super.setLabelPosition(labelPosition);
//		graphController.updateSVGDocument(
//			new Runnable() {
//				public void run() {
//					label.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
//							+ labelPosition.x + " " + labelPosition.y + ")");
//				}
//			}
//		);
//	}
	
	public void setColor(final Color color) {
		super.setColor(color);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (!LineStyle.NONE.equals(getLineStyle())) {
						shapeElement.setAttribute(
								SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(color));
						completedPolygon.setAttribute(
								SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(color));
					}
				}
			}
		);
	}
	
	public void setFillColor(final Color fillColor) {
		super.setFillColor(fillColor);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					shapeElement.setAttribute(
							SVGConstants.SVG_FILL_ATTRIBUTE, SVGUtil.getHexValue(fillColor));
				}
			}
		);
	}
	
	public void setStroke(final int stroke) {
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					shapeElement.setAttribute(
							SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, String.valueOf(stroke));
				}
			}
		);
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
						selectedShapeElement.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_INLINE_VALUE);
//						shapeElement.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGGraphComponent.SELECTED_COLOUR);
//						shapeElement.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
					} else {
						selectedShapeElement.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_NONE_VALUE);
//						shapeElement.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(getColor()));
//						shapeElement.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1");
					}
				}
			}
		);
	}

	public void setIteration(final int iteration) {
		super.setIteration(iteration);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (iteration > 0) {
						iterationText.setData(String.valueOf(iteration));
					} else {
						iterationText.setData("");
					}
				}
			}
		);
	}

	public void setErrors(final int errors) {
		super.setErrors(errors);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (errors > 0) {
						errorsText.setData(String.valueOf(errors));
//						shapeElement.setAttribute(
//								SVGConstants.SVG_STROKE_ATTRIBUTE, SVGGraphComponent.ERROR_COLOUR);
						completedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGGraphSettings.ERROR_COLOUR);
					} else {
						errorsText.setData("");
//						shapeElement.setAttribute(
//								SVGConstants.SVG_STROKE_ATTRIBUTE, SVGGraphComponent.NORMAL_COLOUR);
						completedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGGraphSettings.COMPLETED_COLOUR);
					}
				}
			}
		);
	}

	public void setCompleted(final float complete) {
		super.setCompleted(complete);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					completedPolygon.setAttribute(
							SVGConstants.SVG_POINTS_ATTRIBUTE,
							SVGUtil.calculatePoints(getShape(), (int) (getWidth() * complete), getHeight()));
//					if (complete == 0f) {
//						completedPolygon
//						.setAttribute(
//								SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
//								"0");
//					} else {
//						completedPolygon
//						.setAttribute(
//								SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
//								"1");
//					}
				}
			}
		);
	}

//	public SVGOMPolygonElement getCompletedPolygon() {
//		return completedPolygon;
//	}
//
//	public void setCompletedPolygon(SVGOMPolygonElement polygon) {
//	}

	/**
	 * Returns the iterationText.
	 *
	 * @return the iterationText
	 */
	public Text getIterationText() {
		return iterationText;
	}

	/**
	 * Sets the iterationText.
	 *
	 * @param iterationText the new iterationText
	 */
	public void setIterationText(Text iterationText) {
		this.iterationText = iterationText;
	}

}
