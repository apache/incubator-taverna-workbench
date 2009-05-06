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
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseClickEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseMovedEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOutEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOverEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseUpEventListener;

import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGElement;

/**
 * SVG representation of a graph.
 * 
 * @author David Withers
 */
public class SVGGraph extends Graph /*implements SVGMonitorShape*/ {

	private SVGGraphController graphController;

	private SVGMouseClickEventListener mouseClickAction;

	private SVGMouseMovedEventListener mouseMovedAction;

	private SVGMouseUpEventListener mouseUpAction;

	@SuppressWarnings("unused")
	private SVGMouseOverEventListener mouseOverAction;

	@SuppressWarnings("unused")
	private SVGMouseOutEventListener mouseOutAction;

	private SVGOMGElement g;

	private SVGOMPolygonElement polygon;

	private SVGGraphicsElement shapeElement;

	private SVGOMTextElement label;

	private SVGOMTextElement iteration;

	private SVGOMTextElement error;

	private SVGOMPolygonElement completedPolygon;

	private Text labelText;

	private Text iterationText;

	private Text errorsText;
	
	public SVGGraph(SVGGraphController graphController) {
		super(graphController);
		this.graphController = graphController;
		
		mouseClickAction = new SVGMouseClickEventListener(eventManager, this);
		mouseMovedAction = new SVGMouseMovedEventListener(eventManager, this);
		mouseUpAction = new SVGMouseUpEventListener(eventManager, this);
		mouseOverAction = new SVGMouseOverEventListener(eventManager, this);
		mouseOutAction = new SVGMouseOutEventListener(eventManager, this);

		g = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		g.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "10");
		g.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "Helvetica");
		EventTarget t = (EventTarget) g;
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, mouseClickAction, false);			
		t.addEventListener(SVGConstants.SVG_MOUSEMOVE_EVENT_TYPE, mouseMovedAction, false);
		t.addEventListener(SVGConstants.SVG_MOUSEUP_EVENT_TYPE, mouseUpAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, mouseOverAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, mouseOutAction, false);
		
		polygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		g.appendChild(polygon);
//		ellipse = (SVGOMEllipseElement) graphController.createElement(SVGConstants.SVG_ELLIPSE_TAG);
		shapeElement = polygon;

		completedPolygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		completedPolygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil.calculatePoints(getShape(), 0, 0));
		completedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGGraphSettings.COMPLETED_COLOUR);
//		completedPolygon.setAttribute(SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE, "0.8");
//		completedPolygon.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		g.appendChild(completedPolygon);

		labelText = graphController.createText("");
		label = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		label.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_MIDDLE_VALUE);
		label.appendChild(labelText);
		g.appendChild(label);
		
		iterationText = graphController.createText("");
		iteration = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		iteration.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_END_VALUE);
		iteration.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "6");
		iteration.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		iteration.appendChild(iterationText);
		shapeElement.appendChild(iteration);
		
		errorsText = graphController.createText("");
		error = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		error.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_END_VALUE);
		error.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "6");
		error.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
//		error.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGGraphComponent.ERROR_COLOUR);
		error.appendChild(errorsText);
		shapeElement.appendChild(error);

	}
	
	public SVGElement getSVGElement() {
		return g;
	}	

	@Override
	public void addEdge(GraphEdge edge) {
		if (edge instanceof SVGGraphEdge) {
			SVGGraphEdge svgGraphEdge = (SVGGraphEdge) edge;
			g.appendChild(svgGraphEdge.getSVGElement());
		}
		super.addEdge(edge);
	}

	@Override
	public void addNode(GraphNode node) {
		super.addNode(node);
		if (node instanceof SVGGraphNode) {
			SVGGraphNode svgGraphNode = (SVGGraphNode) node;
			g.appendChild(svgGraphNode.getSVGElement());
		}
	}

	@Override
	public void addSubgraph(Graph subgraph) {
		super.addSubgraph(subgraph);
		if (subgraph instanceof SVGGraph) {
			SVGGraph svgGraph = (SVGGraph) subgraph;
			g.appendChild(svgGraph.getSVGElement());
		}
	}

	public void setPosition(final Point position) {
		super.setPosition(position);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					shapeElement.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
							+ position.x + " " + position.y + ")");
				}
			}
		);
	}
	
	public void setWidth(final int width) {
		super.setWidth(width);
		updateShape();
	}
	
	public void setHeight(final int height) {
		super.setHeight(height);
		updateShape();
	}
	
	public void setShape(Shape shape) {
		super.setShape(shape);
		updateShape();
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
	
	public void setLabelPosition(final Point labelPosition) {
		super.setLabelPosition(labelPosition);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					label.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
							+ labelPosition.x + " " + labelPosition.y + ")");
				}
			}
		);
	}

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
	
	public void setSelected(final boolean selected) {
		super.setSelected(selected);
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (selected) {
						shapeElement.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGGraphSettings.SELECTED_COLOUR);
						shapeElement.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
					} else {
						shapeElement.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil.getHexValue(getColor()));
						shapeElement.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1");
					}
				}
			}
		);
	}

	public void setIteration(final int iteration) {
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (iteration > 0) {
						iterationText.setData(String
								.valueOf(iteration));
					} else {
						iterationText.setData("");
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
						Point position = getPosition();
						completedPolygon.setAttribute(
								SVGConstants.SVG_POINTS_ATTRIBUTE,
								SVGUtil.calculatePoints(getShape(), (int) (getWidth() * complete), getHeight()));
						completedPolygon.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
								+ position.x + " " + position.y + ")");
//						if (complete == 0f) {
//							completedPolygon
//							.setAttribute(
//									SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
//									"0");
//						} else {
//							completedPolygon
//							.setAttribute(
//									SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
//									"1");
//						}
					}
				}
			);
	}

	private void updateShape() {
		if (getShape() != null && getWidth() > 0f && getHeight() > 0f) {
			graphController.updateSVGDocument(
					new Runnable() {
						public void run() {
							shapeElement.setAttribute(
									SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil.calculatePoints(getShape(), getWidth(), getHeight()));
							iteration.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
									+ (getWidth()-1.5) + " 5.5)");
							error.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
									+ (getWidth()-1.5) + " " + (getHeight()-1) + ")");
						}
					}
			);
		}
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
