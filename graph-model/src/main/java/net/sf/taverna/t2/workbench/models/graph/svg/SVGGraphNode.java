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
import java.awt.Dimension;
import java.awt.Point;

import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.GraphElement.LineStyle;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseClickEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseDownEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseMovedEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOutEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOverEventListener;

import org.apache.batik.dom.svg.SVGOMAnimationElement;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMRectElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Node;
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
public class SVGGraphNode extends GraphNode {

	private SVGGraphController graphController;

	private SVGGraphElementDelegate delegate;

	private SVGMouseClickEventListener mouseClickAction;

	private SVGMouseMovedEventListener mouseMovedAction;

	private SVGMouseDownEventListener mouseDownAction;

	@SuppressWarnings("unused")
	private SVGMouseOverEventListener mouseOverAction;

	@SuppressWarnings("unused")
	private SVGMouseOutEventListener mouseOutAction;

	private SVGOMGElement mainGroup, labelGroup, portsGroup;

	private SVGElement expandedElement, contractedElement;

	private SVGOMPolygonElement polygon, completedPolygon;

	private SVGOMEllipseElement ellipse;

	private SVGOMTextElement label, iteration, error;

	private Text labelText, iterationText, errorsText;

	private SVGElement deleteButton;

	private SVGOMAnimationElement animateShape, animatePosition, animateLabel, animateIteration,
			animateErrors;

	public SVGGraphNode(SVGGraphController graphController) {
		super(graphController);
		this.graphController = graphController;
		mouseClickAction = new SVGMouseClickEventListener(this);
		mouseDownAction = new SVGMouseDownEventListener(this);
		mouseMovedAction = new SVGMouseMovedEventListener(this);
		mouseOverAction = new SVGMouseOverEventListener(this);
		mouseOutAction = new SVGMouseOutEventListener(this);

		mainGroup = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		mainGroup.setAttribute("alignment-baseline", SVGConstants.SVG_MIDDLE_VALUE);
		mainGroup.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		mainGroup.setAttribute(SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE,
				CSSConstants.CSS_NONE_VALUE);
		mainGroup.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1");
		mainGroup.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, CSSConstants.CSS_NONE_VALUE);

		EventTarget t = (EventTarget) mainGroup;
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, mouseClickAction, false);
		t.addEventListener(SVGConstants.SVG_MOUSEMOVE_EVENT_TYPE, mouseMovedAction, false);
		t.addEventListener(SVGConstants.SVG_MOUSEDOWN_EVENT_TYPE, mouseDownAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, mouseOverAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, mouseOutAction, false);

		expandedElement = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		contractedElement = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);

		portsGroup = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		portsGroup.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_NONE_VALUE);
		contractedElement.appendChild(portsGroup);

		mainGroup.appendChild(contractedElement);

		polygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		contractedElement.appendChild(polygon);

		ellipse = (SVGOMEllipseElement) graphController.createElement(SVGConstants.SVG_ELLIPSE_TAG);
		ellipse.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY, CSSConstants.CSS_NONE_VALUE);
		ellipse.setAttribute(SVGConstants.SVG_RX_ATTRIBUTE, String.valueOf(2));
		ellipse.setAttribute(SVGConstants.SVG_CX_ATTRIBUTE, String.valueOf(0));
		ellipse.setAttribute(SVGConstants.SVG_RY_ATTRIBUTE, String.valueOf(2));
		ellipse.setAttribute(SVGConstants.SVG_CY_ATTRIBUTE, String.valueOf(0));
		contractedElement.appendChild(ellipse);

		completedPolygon = (SVGOMPolygonElement) graphController
				.createElement(SVGConstants.SVG_POLYGON_TAG);
		completedPolygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil.calculatePoints(
				getShape(), 0, 0));
		completedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE,
				SVGGraphSettings.COMPLETED_COLOUR);
		completedPolygon.setAttribute(SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE, "0.8");
		contractedElement.appendChild(completedPolygon);

		labelText = graphController.createText("");
		label = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		label.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_MIDDLE_VALUE);
		label.setAttribute("baseline-shift", "-35%");
		label.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		label.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		label.appendChild(labelText);
		labelGroup = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		labelGroup.appendChild(label);
		contractedElement.appendChild(labelGroup);

		iterationText = graphController.createText("");
		iteration = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		iteration.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_END_VALUE);
		iteration.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "6");
		iteration.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		iteration.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		iteration.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		iteration.appendChild(iterationText);
		contractedElement.appendChild(iteration);

		errorsText = graphController.createText("");
		error = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		error.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_END_VALUE);
		error.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "6");
		error.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		error.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		error.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		error.appendChild(errorsText);
		contractedElement.appendChild(error);

		// deleteButton = createDeleteButton();
		// g.appendChild(deleteButton);

		animateShape = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TAG, SVGConstants.SVG_POINTS_ATTRIBUTE, null);

		animatePosition = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TRANSFORM_TAG, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
				SVGConstants.TRANSFORM_TRANSLATE);

		animateLabel = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TRANSFORM_TAG, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
				SVGConstants.TRANSFORM_TRANSLATE);

		animateIteration = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TRANSFORM_TAG, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
				SVGConstants.TRANSFORM_TRANSLATE);

		animateErrors = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TRANSFORM_TAG, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
				SVGConstants.TRANSFORM_TRANSLATE);

		delegate = new SVGGraphElementDelegate(graphController, this, mainGroup);
	}

	@SuppressWarnings("unused")
	private SVGElement createDeleteButton() {
		final SVGOMGElement button = (SVGOMGElement) graphController
				.createElement(SVGConstants.SVG_G_TAG);
		button.setAttribute(CSSConstants.CSS_VISIBILITY_PROPERTY, CSSConstants.CSS_HIDDEN_VALUE);
		button.setAttribute(CSSConstants.CSS_POINTER_EVENTS_PROPERTY, CSSConstants.CSS_ALL_VALUE);

		SVGOMRectElement rect = (SVGOMRectElement) graphController
				.createElement(SVGConstants.SVG_RECT_TAG);
		rect.setAttribute(SVGConstants.SVG_X_ATTRIBUTE, "4");
		rect.setAttribute(SVGConstants.SVG_Y_ATTRIBUTE, "4");
		rect.setAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE, "13");
		rect.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, "13");
		rect.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, "none");
		button.appendChild(rect);

		final SVGOMPathElement path = (SVGOMPathElement) graphController
				.createElement(SVGConstants.SVG_PATH_TAG);
		path.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, "white");
		path.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "2");
		path.setAttribute(SVGConstants.SVG_D_ATTRIBUTE, "M5,5L12,12M5,12L12,5");
		button.appendChild(path);

		EventTarget t = (EventTarget) button;
		t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, new EventListener() {
			public void handleEvent(Event evt) {
				if (isInteractive()) {
					deleteButton.setAttribute(CSSConstants.CSS_VISIBILITY_PROPERTY,
							CSSConstants.CSS_VISIBLE_VALUE);
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
		return mainGroup;
	}

	@Override
	public void setActive(final boolean active) {
		super.setActive(active);
		if (isInteractive()) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					if (active) {
						deleteButton.setAttribute(CSSConstants.CSS_VISIBILITY_PROPERTY,
								CSSConstants.CSS_VISIBLE_VALUE);
						// deleteButton.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
						// CSSConstants.CSS_INLINE_VALUE);
					} else {
						deleteButton.setAttribute(CSSConstants.CSS_VISIBILITY_PROPERTY,
								CSSConstants.CSS_HIDDEN_VALUE);
						// button.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
						// CSSConstants.CSS_NONE_VALUE);
					}
				}
			});
		}
	}

	@Override
	public void setGraph(Graph graph) {
		super.setGraph(graph);
		if (graph instanceof SVGGraph) {
			SVGGraph svgGraph = (SVGGraph) graph;
			final SVGElement graphElement = svgGraph.getSVGElement();
			if (isExpanded()) {
				graphController.updateSVGDocument(new Runnable() {
					public void run() {
						mainGroup.replaceChild(expandedElement, graphElement);
					}
				});
			}
			expandedElement = graphElement;
		}
	}

	@Override
	public void setExpanded(final boolean expanded) {
		if (isExpanded() != expanded) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					if (expanded) {
						mainGroup.replaceChild(expandedElement, contractedElement);
					} else {
						mainGroup.replaceChild(contractedElement, expandedElement);
					}
				}
			});
		}
		super.setExpanded(expanded);
	}

	@Override
	public void addSourceNode(final GraphNode sourceNode) {
		super.addSourceNode(sourceNode);
		if (sourceNode instanceof SVGGraphNode) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					SVGGraphNode svgGraphNode = (SVGGraphNode) sourceNode;
					portsGroup.appendChild(svgGraphNode.getSVGElement());
				}
			});
		}
	}

	@Override
	public boolean removeSourceNode(final GraphNode sourceNode) {
		if (sourceNode instanceof SVGGraphNode) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					SVGGraphNode svgGraphNode = (SVGGraphNode) sourceNode;
					portsGroup.removeChild(svgGraphNode.getSVGElement());
				}
			});
		}
		return super.removeSourceNode(sourceNode);
	}

	@Override
	public void addSinkNode(final GraphNode sinkNode) {
		super.addSinkNode(sinkNode);
		if (sinkNode instanceof SVGGraphNode) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					SVGGraphNode svgGraphNode = (SVGGraphNode) sinkNode;
					portsGroup.appendChild(svgGraphNode.getSVGElement());
				}
			});
		}
	}

	@Override
	public boolean removeSinkNode(final GraphNode sinkNode) {
		if (sinkNode instanceof SVGGraphNode) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					SVGGraphNode svgGraphNode = (SVGGraphNode) sinkNode;
					portsGroup.removeChild(svgGraphNode.getSVGElement());
				}
			});
		}
		return super.removeSinkNode(sinkNode);
	}

	@Override
	public void setPosition(final Point position) {
		final Point oldPosition = getPosition();
		if (position != null && !position.equals(oldPosition)) {
			super.setPosition(position);
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					if (graphController.isAnimatable()) {
						SVGUtil.animate(animatePosition, mainGroup, graphController
								.getAnimationSpeed(), oldPosition.x + ", " + oldPosition.y,
								position.x + ", " + position.y);
					} else {
						mainGroup.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
								+ position.x + " " + position.y + ")");
					}
				}
			});
		}
	}

	@Override
	public void setSize(final Dimension size) {
		final Dimension oldSize = getSize();
		if (size != null && !size.equals(oldSize)) {
			super.setSize(size);
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					int oldWidth = oldSize.width;
					int oldHeight = oldSize.height;
					if (graphController.isAnimatable()) {
						if (Shape.CIRCLE.equals(getShape())) {
							ellipse.setAttribute(SVGConstants.SVG_RX_ATTRIBUTE, String
									.valueOf(size.width / 2f));
							ellipse.setAttribute(SVGConstants.SVG_CX_ATTRIBUTE, String
									.valueOf(size.width / 2f));
							ellipse.setAttribute(SVGConstants.SVG_RY_ATTRIBUTE, String
									.valueOf(size.height / 2f));
							ellipse.setAttribute(SVGConstants.SVG_CY_ATTRIBUTE, String
									.valueOf(size.height / 2f));
						} else {
							SVGUtil.animate(animateShape, polygon, graphController
									.getAnimationSpeed(), SVGUtil.calculatePoints(getShape(),
									oldWidth, oldHeight), SVGUtil.calculatePoints(getShape(),
									getWidth(), getHeight()));
						}

						if (getLabel() != null && !getLabel().equals("")) {
							SVGUtil.animate(animateLabel, labelGroup, graphController
									.getAnimationSpeed(), (oldWidth / 2f) + ", "
									+ (oldHeight / 2f), (getWidth() / 2f) + ", "
									+ (getHeight() / 2f));
						} else {
							labelGroup.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
									"translate(" + getWidth() / 2f + " " + getHeight() / 2f + ")");
						}

						if (getIteration() > 0) {
							SVGUtil.animate(animateIteration, iteration, graphController
									.getAnimationSpeed(), (oldWidth - 1.5) + ", 5.5",
									(getWidth() - 1.5) + ", 5.5");
						} else {
							iteration.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
									"translate(" + (getWidth() - 1.5) + " 5.5)");
						}

						if (getErrors() > 0) {
							SVGUtil.animate(animateErrors, error, graphController
									.getAnimationSpeed(), (oldWidth - 1.5) + ", "
									+ (oldHeight - 1), (getWidth() - 1.5) + ", "
									+ (getHeight() - 1));
						} else {
							error.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
									+ (getWidth() - 1.5) + " " + (getHeight() - 1) + ")");
						}
					} else {
						if (Shape.CIRCLE.equals(getShape())) {
							ellipse.setAttribute(SVGConstants.SVG_RX_ATTRIBUTE, String
									.valueOf(size.width / 2f));
							ellipse.setAttribute(SVGConstants.SVG_CX_ATTRIBUTE, String
									.valueOf(size.width / 2f));
							ellipse.setAttribute(SVGConstants.SVG_RY_ATTRIBUTE, String
									.valueOf(size.height / 2f));
							ellipse.setAttribute(SVGConstants.SVG_CY_ATTRIBUTE, String
									.valueOf(size.height / 2f));
						} else {
							polygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil
									.calculatePoints(getShape(), getWidth(), getHeight()));
						}
						labelGroup.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
								+ getWidth() / 2f + " " + getHeight() / 2f + ")");
						iteration.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
								+ (getWidth() - 1.5) + " 5.5)");
						error.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
								+ (getWidth() - 1.5) + " " + (getHeight() - 1) + ")");
					}
				}
			});
		}
	}

	@Override
	public void setShape(final Shape shape) {
		final Shape currentShape = getShape();
		if (shape != null && !shape.equals(currentShape)) {
			super.setShape(shape);
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					if (Shape.CIRCLE.equals(shape)) {
						ellipse.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
								CSSConstants.CSS_INLINE_VALUE);
						polygon.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
								CSSConstants.CSS_NONE_VALUE);
					} else if (Shape.CIRCLE.equals(currentShape)) {
						ellipse.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
								CSSConstants.CSS_NONE_VALUE);
						polygon.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
								CSSConstants.CSS_INLINE_VALUE);
					}
					if (Shape.RECORD.equals(shape)) {
						portsGroup.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
								CSSConstants.CSS_INLINE_VALUE);
					} else if (Shape.RECORD.equals(currentShape)) {
						portsGroup.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
								CSSConstants.CSS_NONE_VALUE);
					}
				}
			});
		}
	}

	@Override
	public void setLabel(final String label) {
		super.setLabel(label);
		graphController.updateSVGDocument(new Runnable() {
			public void run() {
				labelText.setData(label);
			}
		});
	}

	@Override
	public void setIteration(final int iteration) {
		super.setIteration(iteration);
		graphController.updateSVGDocument(new Runnable() {
			public void run() {
				if (iteration > 0) {
					iterationText.setData(String.valueOf(iteration));
				} else {
					iterationText.setData("");
				}
			}
		});
	}

	@Override
	public void setErrors(final int errors) {
		super.setErrors(errors);
		graphController.updateSVGDocument(new Runnable() {
			public void run() {
				if (errors > 0) {
					errorsText.setData(String.valueOf(errors));
					completedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE,
							SVGGraphSettings.ERROR_COLOUR);
				} else {
					errorsText.setData("");
					completedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE,
							SVGGraphSettings.COMPLETED_COLOUR);
				}
			}
		});
	}

	@Override
	public void setCompleted(final float complete) {
		super.setCompleted(complete);
		graphController.updateSVGDocument(new Runnable() {
			public void run() {
				completedPolygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil
						.calculatePoints(getShape(), (int) (getWidth() * complete), getHeight()));
			}
		});
	}

	@Override
	public void setSelected(final boolean selected) {
		delegate.setSelected(selected);
		super.setSelected(selected);
	}

	@Override
	public void setLineStyle(final LineStyle lineStyle) {
		delegate.setLineStyle(lineStyle);
		super.setLineStyle(lineStyle);
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
