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

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.svg.SVGGraphicsElement;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGPointList;

/**
 * SVG representation of a graph.
 * 
 * @author David Withers
 */
public class SVGGraph extends Graph implements SVGMonitorShape {

	private SVGGraphController graphController;

	private SVGMouseClickEventListener mouseClickAction;

	private SVGMouseMovedEventListener mouseMovedAction;

	private SVGMouseUpEventListener mouseUpAction;

	private SVGMouseOverEventListener mouseOverAction;

	private SVGMouseOutEventListener mouseOutAction;

	private SVGOMGElement g;

	private SVGOMPolygonElement polygon;

	private SVGOMEllipseElement ellipse;

	private SVGGraphicsElement shapeElement;

	private SVGOMTextElement label;

	private SVGOMTextElement iteration;

	private SVGOMPolygonElement completedPolygon;

	private Text labelText;

	private Text iterationText;

//	private Text errorsText;
//
//	private SVGPoint errorsPosition;

//	private String originalStyle;

//	private String errorStyle;

//	private String selectedStyle;
	
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
		ellipse = (SVGOMEllipseElement) graphController.createElement(SVGConstants.SVG_ELLIPSE_TAG);
		shapeElement = polygon;

		completedPolygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		completedPolygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil.calculatePoints(getShape(), 0, 0));
		completedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGGraphComponent.COMPLETED_COLOUR);
		completedPolygon.setAttribute(SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE, "0.8");
		completedPolygon.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, "black");
//		completedPolygon.setAttribute(SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE, "0.6");
		g.appendChild(completedPolygon);

		labelText = graphController.createText("");
		label = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		label.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_MIDDLE_VALUE);
		label.appendChild(labelText);
		g.appendChild(label);
		
		iterationText = graphController.createText("");
		iteration = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		iteration.appendChild(iterationText);
		g.appendChild(iteration);
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
						shapeElement.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGGraphComponent.SELECTED_COLOUR);
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


//	/* (non-Javadoc)
//	 * @see net.sf.taverna.t2.workbench.models.graph.svg.SVGBox#setErrors(int)
//	 */
//	public void setErrors(final int errors) {
//		graphController.updateSVGDocument(
//			new Runnable() {
//				public void run() {
//					if (errors > 0) {
//						errorsText.setData(String.valueOf(errors));
//						polygon.setAttribute(
//								SVGConstants.SVG_STYLE_ATTRIBUTE, errorStyle);
//						
//					} else {
//						errorsText.setData("");
//						polygon.setAttribute(
//								SVGConstants.SVG_STYLE_ATTRIBUTE, originalStyle);
//					}
//				}
//			}
//		);
//	}

	
//	private void addErrorsText() {
//		if (this.graphController.updateManager != null) {
//			this.graphController.updateManager.getUpdateRunnableQueue().invokeLater(
//					new Runnable() {
//						public void run() {
//							Element text = SVGGraph.this.graphController.getSvgCanvas().getSVGDocument().createElementNS(
//									SVGUtil.svgNS, SVGConstants.SVG_TEXT_TAG);
//							text
//							.setAttribute(
//									SVGConstants.SVG_X_ATTRIBUTE,
//									String.valueOf(errorsPosition
//											.getX() - 1.5));
//							text
//							.setAttribute(
//									SVGConstants.SVG_Y_ATTRIBUTE,
//									String.valueOf(errorsPosition
//											.getY() - 1.0));
//							text.setAttribute(
//									SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE,
//									"end");
//							text.setAttribute(
//									SVGConstants.SVG_FONT_SIZE_ATTRIBUTE,
//									"5.5");
//							text.setAttribute(
//									SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE,
//									"sans-serif");
//							text.setAttribute(
//									SVGConstants.SVG_FILL_ATTRIBUTE, "red");
//							synchronized (g) {
//								if (errorsText == null) {
//									errorsText = SVGGraph.this.graphController.getSvgCanvas().getSVGDocument()
//									.createTextNode("");
//									text.appendChild(errorsText);
//									g.appendChild(text);
//								}
//							}
//						}
//					});
//		}
//	}
//
//	private void addCompletedBox() {
//		if (this.graphController.updateManager != null) {
//			this.graphController.updateManager.getUpdateRunnableQueue().invokeLater(
//					new Runnable() {
//						public void run() {
//							synchronized (g) {
//								if (completedBox == null) {
//									completedBox = (SVGOMPolygonElement) SVGGraph.this.graphController.getSvgCanvas().getSVGDocument()
//									.createElementNS(
//											SVGUtil.svgNS,
//											SVGConstants.SVG_POLYGON_TAG);
//									completedBox
//									.setAttribute(
//											SVGConstants.SVG_POINTS_ATTRIBUTE,
//											calculatePoints(0f));
//									completedBox
//									.setAttribute(
//											SVGConstants.SVG_FILL_ATTRIBUTE,
//											SVGGraphComponent.COMPLETED_COLOUR);
//									completedBox
//									.setAttribute(
//											SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE,
//											"0.8");
////									completedBox
////.setAttribute(
////									SVGConstants.SVG_STROKE_ATTRIBUTE,
////									"black");
////									completedBox
////									.setAttribute(
////									SVGConstants.SVG_STROKE_OPACITY_ATTRIBUTE,
////									"0.6");
//									g.insertBefore(completedBox, text);
//								}
//							}
//						}
//					});
//		}
//	}

	public void setCompleted(final float complete) {
		super.setCompleted(complete);
		UpdateManager updateManager = this.graphController.updateManager;
		if (updateManager != null) {
			updateManager.getUpdateRunnableQueue().invokeLater(
					new Runnable() {
						public void run() {
							Point position = getPosition();
							completedPolygon.setAttribute(
									SVGConstants.SVG_POINTS_ATTRIBUTE,
									SVGUtil.calculatePoints(getShape(), (int) (getWidth() * complete), getHeight()));
							completedPolygon.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
									+ position.x + " " + position.y + ")");
							if (complete == 0f) {
								completedPolygon
								.setAttribute(
										SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
								"0");
							} else {
								completedPolygon
								.setAttribute(
										SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE,
								"1");
							}
						}
					});
		}
	}

//	/**
//	 * Calculates the points that specify the proportion completed polygon.
//	 * 
//	 * @param complete
//	 *            the proportion completed
//	 * @return the points that specify the proportion completed polygon
//	 */
//	private String calculatePoints(float complete) {
//		StringBuffer sb = new StringBuffer();
//		SVGPointList points = polygon.getPoints();
//		float x1, x2, y1, y2;
//		x1 = points.getItem(2).getX() - 0.4f;
//		x2 = points.getItem(0).getX() + 0.4f;
//		y1 = points.getItem(2).getY() + 0.4f;
//		y2 = points.getItem(0).getY() - 0.4f;
//		x1 = x2 + ((x1 - x2) * complete);
//		sb.append(x1 + "," + y1 + " ");
//		sb.append(x2 + "," + y1 + " ");
//		sb.append(x2 + "," + y2 + " ");
//		sb.append(x1 + "," + y2 + " ");
//		sb.append(x1 + "," + y1);
//
//		return sb.toString();
//	}

	private void updateShape() {
		if (getShape() != null && getWidth() > 0f && getHeight() > 0f) {
			graphController.updateSVGDocument(
					new Runnable() {
						public void run() {
							shapeElement.setAttribute(
									SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil.calculatePoints(getShape(), getWidth(), getHeight()));
						}
					}
			);
		}
	}

	public SVGOMPolygonElement getCompletedPolygon() {
		return completedPolygon;
	}

	public void setCompletedPolygon(SVGOMPolygonElement polygon) {
	}

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
