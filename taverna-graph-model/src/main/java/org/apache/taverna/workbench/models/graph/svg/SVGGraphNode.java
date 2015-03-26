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
package org.apache.taverna.workbench.models.graph.svg;

import static org.apache.taverna.workbench.models.graph.svg.SVGGraphSettings.COMPLETED_COLOUR;
import static org.apache.taverna.workbench.models.graph.svg.SVGGraphSettings.ERROR_COLOUR;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.animate;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.calculatePoints;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.createAnimationElement;
import static org.apache.batik.util.CSSConstants.CSS_ALL_VALUE;
import static org.apache.batik.util.CSSConstants.CSS_BLACK_VALUE;
import static org.apache.batik.util.CSSConstants.CSS_DISPLAY_PROPERTY;
import static org.apache.batik.util.CSSConstants.CSS_HIDDEN_VALUE;
import static org.apache.batik.util.CSSConstants.CSS_INLINE_VALUE;
import static org.apache.batik.util.CSSConstants.CSS_NONE_VALUE;
import static org.apache.batik.util.CSSConstants.CSS_POINTER_EVENTS_PROPERTY;
import static org.apache.batik.util.CSSConstants.CSS_VISIBILITY_PROPERTY;
import static org.apache.batik.util.CSSConstants.CSS_VISIBLE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_ANIMATE_TAG;
import static org.apache.batik.util.SVGConstants.SVG_ANIMATE_TRANSFORM_TAG;
import static org.apache.batik.util.SVGConstants.SVG_CLICK_EVENT_TYPE;
import static org.apache.batik.util.SVGConstants.SVG_CX_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_CY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_D_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_END_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_FILL_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_FONT_SIZE_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_HEIGHT_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_MIDDLE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_MOUSEDOWN_EVENT_TYPE;
import static org.apache.batik.util.SVGConstants.SVG_MOUSEMOVE_EVENT_TYPE;
import static org.apache.batik.util.SVGConstants.SVG_MOUSEOUT_EVENT_TYPE;
import static org.apache.batik.util.SVGConstants.SVG_MOUSEOVER_EVENT_TYPE;
import static org.apache.batik.util.SVGConstants.SVG_NONE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_POINTS_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_RX_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_RY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_TRANSFORM_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_WIDTH_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_X_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_Y_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.TRANSFORM_TRANSLATE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import org.apache.taverna.workbench.models.graph.Graph;
import org.apache.taverna.workbench.models.graph.GraphNode;
import org.apache.taverna.workbench.models.graph.svg.event.SVGMouseClickEventListener;
import org.apache.taverna.workbench.models.graph.svg.event.SVGMouseDownEventListener;
import org.apache.taverna.workbench.models.graph.svg.event.SVGMouseMovedEventListener;
import org.apache.taverna.workbench.models.graph.svg.event.SVGMouseOutEventListener;
import org.apache.taverna.workbench.models.graph.svg.event.SVGMouseOverEventListener;

import org.apache.batik.dom.svg.SVGOMAnimationElement;
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

		mainGroup = graphController.createGElem();
		mainGroup.setAttribute("alignment-baseline", SVG_MIDDLE_VALUE);
		mainGroup.setAttribute(SVG_STROKE_ATTRIBUTE, CSS_BLACK_VALUE);
		mainGroup.setAttribute(SVG_STROKE_DASHARRAY_ATTRIBUTE, CSS_NONE_VALUE);
		mainGroup.setAttribute(SVG_STROKE_WIDTH_ATTRIBUTE, "1");
		mainGroup.setAttribute(SVG_FILL_ATTRIBUTE, CSS_NONE_VALUE);

		EventTarget t = (EventTarget) mainGroup;
		t.addEventListener(SVG_CLICK_EVENT_TYPE, mouseClickAction, false);
		t.addEventListener(SVG_MOUSEMOVE_EVENT_TYPE, mouseMovedAction, false);
		t.addEventListener(SVG_MOUSEDOWN_EVENT_TYPE, mouseDownAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, mouseOverAction, false);
//		t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, mouseOutAction, false);

		expandedElement = graphController.createGElem();
		contractedElement = graphController.createGElem();

		portsGroup = graphController.createGElem();
		portsGroup.setAttribute(CSS_DISPLAY_PROPERTY, CSS_NONE_VALUE);
		contractedElement.appendChild(portsGroup);

		mainGroup.appendChild(contractedElement);

		polygon = graphController.createPolygon();
		contractedElement.appendChild(polygon);

		ellipse = graphController.createEllipse();
		ellipse.setAttribute(CSS_DISPLAY_PROPERTY, CSS_NONE_VALUE);
		ellipse.setAttribute(SVG_RX_ATTRIBUTE, String.valueOf(2));
		ellipse.setAttribute(SVG_CX_ATTRIBUTE, String.valueOf(0));
		ellipse.setAttribute(SVG_RY_ATTRIBUTE, String.valueOf(2));
		ellipse.setAttribute(SVG_CY_ATTRIBUTE, String.valueOf(0));
		contractedElement.appendChild(ellipse);

		completedPolygon = graphController.createPolygon();
		completedPolygon.setAttribute(SVG_POINTS_ATTRIBUTE,
				calculatePoints(getShape(), 0, 0));
		completedPolygon.setAttribute(SVG_FILL_ATTRIBUTE, COMPLETED_COLOUR);
		completedPolygon.setAttribute(SVG_FILL_OPACITY_ATTRIBUTE, "0.8");
		contractedElement.appendChild(completedPolygon);

		labelText = graphController.createText("");
		label = graphController.createText(labelText);
		label.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_MIDDLE_VALUE);
		label.setAttribute("baseline-shift", "-35%");
		label.setAttribute(SVG_FILL_ATTRIBUTE, CSS_BLACK_VALUE);
		label.setAttribute(SVG_STROKE_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		labelGroup = graphController.createGElem();
		labelGroup.appendChild(label);
		contractedElement.appendChild(labelGroup);

		iterationText = graphController.createText("");
		iteration = graphController.createText(iterationText);
		iteration.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_END_VALUE);
		iteration.setAttribute(SVG_FONT_SIZE_ATTRIBUTE, "6");
		iteration.setAttribute(SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		iteration.setAttribute(SVG_FILL_ATTRIBUTE, CSS_BLACK_VALUE);
		iteration.setAttribute(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
		contractedElement.appendChild(iteration);

		errorsText = graphController.createText("");
		error = graphController.createText(errorsText);
		error.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_END_VALUE);
		error.setAttribute(SVG_FONT_SIZE_ATTRIBUTE, "6");
		error.setAttribute(SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		error.setAttribute(SVG_FILL_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		error.setAttribute(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
		contractedElement.appendChild(error);

		// deleteButton = createDeleteButton();
		// g.appendChild(deleteButton);

		animateShape = createAnimationElement(graphController, SVG_ANIMATE_TAG,
				SVG_POINTS_ATTRIBUTE, null);

		animatePosition = createAnimationElement(graphController,
				SVG_ANIMATE_TRANSFORM_TAG, SVG_TRANSFORM_ATTRIBUTE,
				TRANSFORM_TRANSLATE);

		animateLabel = SVGUtil.createAnimationElement(graphController,
				SVG_ANIMATE_TRANSFORM_TAG, SVG_TRANSFORM_ATTRIBUTE,
				TRANSFORM_TRANSLATE);

		animateIteration = createAnimationElement(graphController,
				SVG_ANIMATE_TRANSFORM_TAG, SVG_TRANSFORM_ATTRIBUTE,
				TRANSFORM_TRANSLATE);

		animateErrors = createAnimationElement(graphController,
				SVG_ANIMATE_TRANSFORM_TAG, SVG_TRANSFORM_ATTRIBUTE,
				TRANSFORM_TRANSLATE);

		delegate = new SVGGraphElementDelegate(graphController, this, mainGroup);
	}

	@SuppressWarnings("unused")
	private SVGElement createDeleteButton() {
		final SVGOMGElement button = graphController.createGElem();
		button.setAttribute(CSS_VISIBILITY_PROPERTY, CSS_HIDDEN_VALUE);
		button.setAttribute(CSS_POINTER_EVENTS_PROPERTY, CSS_ALL_VALUE);

		SVGOMRectElement rect = graphController.createRect();
		rect.setAttribute(SVG_X_ATTRIBUTE, "4");
		rect.setAttribute(SVG_Y_ATTRIBUTE, "4");
		rect.setAttribute(SVG_WIDTH_ATTRIBUTE, "13");
		rect.setAttribute(SVG_HEIGHT_ATTRIBUTE, "13");
		rect.setAttribute(SVG_FILL_ATTRIBUTE, "none");
		button.appendChild(rect);

		final SVGOMPathElement path = graphController.createPath();
		path.setAttribute(SVG_STROKE_ATTRIBUTE, "white");
		path.setAttribute(SVG_STROKE_WIDTH_ATTRIBUTE, "2");
		path.setAttribute(SVG_D_ATTRIBUTE, "M5,5L12,12M5,12L12,5");
		button.appendChild(path);

		EventTarget t = (EventTarget) button;
		t.addEventListener(SVG_MOUSEOVER_EVENT_TYPE, new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				if (isInteractive()) {
					deleteButton.setAttribute(CSS_VISIBILITY_PROPERTY,
							CSS_VISIBLE_VALUE);
					path.setAttribute(SVG_STROKE_ATTRIBUTE, "red");
					evt.stopPropagation();
				}
			}
		}, false);
		t.addEventListener(SVG_MOUSEOUT_EVENT_TYPE, new EventListener() {
			@Override
			public void handleEvent(Event evt) {
				if (isInteractive()) {
					path.setAttribute(SVG_STROKE_ATTRIBUTE, "white");
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
		if (isInteractive())
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					if (active) {
						deleteButton.setAttribute(CSS_VISIBILITY_PROPERTY,
								CSS_VISIBLE_VALUE);
						// deleteButton.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
						// CSSConstants.CSS_INLINE_VALUE);
					} else {
						deleteButton.setAttribute(CSS_VISIBILITY_PROPERTY,
								CSS_HIDDEN_VALUE);
						// button.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
						// CSSConstants.CSS_NONE_VALUE);
					}
				}
			});
	}

	@Override
	public void setGraph(Graph graph) {
		super.setGraph(graph);
		if (graph instanceof SVGGraph) {
			SVGGraph svgGraph = (SVGGraph) graph;
			final SVGElement graphElement = svgGraph.getSVGElement();
			if (isExpanded())
				graphController.updateSVGDocument(new Runnable() {
					@Override
					public void run() {
						mainGroup.replaceChild(expandedElement, graphElement);
					}
				});
			expandedElement = graphElement;
		}
	}

	@Override
	public void setExpanded(final boolean expanded) {
		if (isExpanded() != expanded)
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					if (expanded)
						mainGroup.replaceChild(expandedElement, contractedElement);
					else
						mainGroup.replaceChild(contractedElement, expandedElement);
				}
			});
		super.setExpanded(expanded);
	}

	@Override
	public void addSourceNode(final GraphNode sourceNode) {
		super.addSourceNode(sourceNode);
		if (sourceNode instanceof SVGGraphNode)
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					SVGGraphNode svgGraphNode = (SVGGraphNode) sourceNode;
					portsGroup.appendChild(svgGraphNode.getSVGElement());
				}
			});
	}

	@Override
	public boolean removeSourceNode(final GraphNode sourceNode) {
		if (sourceNode instanceof SVGGraphNode)
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					SVGGraphNode svgGraphNode = (SVGGraphNode) sourceNode;
					portsGroup.removeChild(svgGraphNode.getSVGElement());
				}
			});
		return super.removeSourceNode(sourceNode);
	}

	@Override
	public void addSinkNode(final GraphNode sinkNode) {
		super.addSinkNode(sinkNode);
		if (sinkNode instanceof SVGGraphNode)
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					SVGGraphNode svgGraphNode = (SVGGraphNode) sinkNode;
					portsGroup.appendChild(svgGraphNode.getSVGElement());
				}
			});
	}

	@Override
	public boolean removeSinkNode(final GraphNode sinkNode) {
		if (sinkNode instanceof SVGGraphNode)
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					SVGGraphNode svgGraphNode = (SVGGraphNode) sinkNode;
					portsGroup.removeChild(svgGraphNode.getSVGElement());
				}
			});
		return super.removeSinkNode(sinkNode);
	}

	@Override
	public void setPosition(final Point position) {
		final Point oldPosition = getPosition();
		if (position != null && !position.equals(oldPosition)) {
			super.setPosition(position);
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					if (graphController.isAnimatable())
						animate(animatePosition, mainGroup,
								graphController.getAnimationSpeed(),
								oldPosition.x + ", " + oldPosition.y,
								position.x + ", " + position.y);
					else
						mainGroup.setAttribute(SVG_TRANSFORM_ATTRIBUTE,
								"translate(" + position.x + " " + position.y
										+ ")");
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
				@Override
				public void run() {
					adjustSize(size, oldSize);
				}
			});
		}
	}

	/** core of implementation of {@link #setSize(Dimension)} */
	private void adjustSize(Dimension size, Dimension oldSize) {
		int oldWidth = oldSize.width;
		int oldHeight = oldSize.height;
		if (graphController.isAnimatable()) {
			if (Shape.CIRCLE.equals(getShape())) {
				ellipse.setAttribute(SVG_RX_ATTRIBUTE,
						String.valueOf(size.width / 2f));
				ellipse.setAttribute(SVG_CX_ATTRIBUTE,
						String.valueOf(size.width / 2f));
				ellipse.setAttribute(SVG_RY_ATTRIBUTE,
						String.valueOf(size.height / 2f));
				ellipse.setAttribute(SVG_CY_ATTRIBUTE,
						String.valueOf(size.height / 2f));
			} else
				animate(animateShape, polygon,
						graphController.getAnimationSpeed(),
						calculatePoints(getShape(), oldWidth, oldHeight),
						calculatePoints(getShape(), getWidth(), getHeight()));

			if (getLabel() != null && !getLabel().isEmpty())
				animate(animateLabel, labelGroup,
						graphController.getAnimationSpeed(), (oldWidth / 2f)
								+ ", " + (oldHeight / 2f), (getWidth() / 2f)
								+ ", " + (getHeight() / 2f));
			else
				labelGroup.setAttribute(SVG_TRANSFORM_ATTRIBUTE,
						"translate(" + getWidth() / 2f + " " + getHeight() / 2f + ")");

			if (getIteration() > 0)
				animate(animateIteration, iteration,
						graphController.getAnimationSpeed(), (oldWidth - 1.5)
								+ ", 5.5", (getWidth() - 1.5) + ", 5.5");
			else
				iteration.setAttribute(SVG_TRANSFORM_ATTRIBUTE, "translate("
						+ (getWidth() - 1.5) + " 5.5)");

			if (getErrors() > 0)
				animate(animateErrors, error,
						graphController.getAnimationSpeed(), (oldWidth - 1.5)
								+ ", " + (oldHeight - 1), (getWidth() - 1.5)
								+ ", " + (getHeight() - 1));
			else
				error.setAttribute(SVG_TRANSFORM_ATTRIBUTE, "translate("
						+ (getWidth() - 1.5) + " " + (getHeight() - 1) + ")");
		} else {
			if (Shape.CIRCLE.equals(getShape())) {
				ellipse.setAttribute(SVG_RX_ATTRIBUTE,
						String.valueOf(size.width / 2f));
				ellipse.setAttribute(SVG_CX_ATTRIBUTE,
						String.valueOf(size.width / 2f));
				ellipse.setAttribute(SVG_RY_ATTRIBUTE,
						String.valueOf(size.height / 2f));
				ellipse.setAttribute(SVG_CY_ATTRIBUTE,
						String.valueOf(size.height / 2f));
			} else
				polygon.setAttribute(SVG_POINTS_ATTRIBUTE,
						calculatePoints(getShape(), getWidth(), getHeight()));

			labelGroup.setAttribute(SVG_TRANSFORM_ATTRIBUTE, "translate("
					+ getWidth() / 2f + " " + getHeight() / 2f + ")");
			iteration.setAttribute(SVG_TRANSFORM_ATTRIBUTE, "translate("
					+ (getWidth() - 1.5) + " 5.5)");
			error.setAttribute(SVG_TRANSFORM_ATTRIBUTE, "translate("
					+ (getWidth() - 1.5) + " " + (getHeight() - 1) + ")");
		}
	}

	@Override
	public void setShape(final Shape shape) {
		final Shape currentShape = getShape();
		if (shape != null && !shape.equals(currentShape)) {
			super.setShape(shape);
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					if (Shape.CIRCLE.equals(shape)) {
						ellipse.setAttribute(CSS_DISPLAY_PROPERTY,
								CSS_INLINE_VALUE);
						polygon.setAttribute(CSS_DISPLAY_PROPERTY,
								CSS_NONE_VALUE);
					} else if (Shape.CIRCLE.equals(currentShape)) {
						ellipse.setAttribute(CSS_DISPLAY_PROPERTY,
								CSS_NONE_VALUE);
						polygon.setAttribute(CSS_DISPLAY_PROPERTY,
								CSS_INLINE_VALUE);
					}
					if (Shape.RECORD.equals(shape))
						portsGroup.setAttribute(CSS_DISPLAY_PROPERTY,
								CSS_INLINE_VALUE);
					else if (Shape.RECORD.equals(currentShape))
						portsGroup.setAttribute(CSS_DISPLAY_PROPERTY,
								CSS_NONE_VALUE);
				}
			});
		}
	}

	@Override
	public void setLabel(final String label) {
		super.setLabel(label);
		graphController.updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				labelText.setData(label);
			}
		});
	}

	@Override
	public void setIteration(final int iteration) {
		super.setIteration(iteration);
		graphController.updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				if (iteration > 0)
					iterationText.setData(String.valueOf(iteration));
				else
					iterationText.setData("");
			}
		});
	}

	@Override
	public void setErrors(final int errors) {
		super.setErrors(errors);
		graphController.updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				if (errors > 0) {
					errorsText.setData(String.valueOf(errors));
					completedPolygon.setAttribute(SVG_FILL_ATTRIBUTE,
							ERROR_COLOUR);
				} else {
					errorsText.setData("");
					completedPolygon.setAttribute(SVG_FILL_ATTRIBUTE,
							COMPLETED_COLOUR);
				}
			}
		});
	}

	@Override
	public void setCompleted(final float complete) {
		super.setCompleted(complete);
		graphController.updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				completedPolygon.setAttribute(
						SVG_POINTS_ATTRIBUTE,
						calculatePoints(getShape(),
								(int) (getWidth() * complete), getHeight()));
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
