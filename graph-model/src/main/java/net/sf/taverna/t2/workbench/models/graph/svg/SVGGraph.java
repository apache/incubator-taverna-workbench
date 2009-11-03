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
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseClickEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseMovedEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOutEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseOverEventListener;
import net.sf.taverna.t2.workbench.models.graph.svg.event.SVGMouseUpEventListener;

import org.apache.batik.dom.svg.SVGOMAnimationElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGElement;

/**
 * SVG representation of a graph.
 * 
 * @author David Withers
 */
public class SVGGraph extends Graph {

	private SVGGraphController graphController;

	private SVGGraphElementDelegate delegate;

	private SVGMouseClickEventListener mouseClickAction;

	private SVGMouseMovedEventListener mouseMovedAction;

	private SVGMouseUpEventListener mouseUpAction;

	@SuppressWarnings("unused")
	private SVGMouseOverEventListener mouseOverAction;

	@SuppressWarnings("unused")
	private SVGMouseOutEventListener mouseOutAction;

	private SVGOMGElement mainGroup, labelGroup;

	private SVGOMPolygonElement polygon, completedPolygon;

	private SVGOMTextElement label, iteration, error;

	private Text labelText, iterationText, errorsText;

	private SVGOMAnimationElement animateShape, animatePosition, animateLabel;

	public SVGGraph(SVGGraphController graphController) {
		super(graphController);
		this.graphController = graphController;

		mouseClickAction = new SVGMouseClickEventListener(this);
		mouseMovedAction = new SVGMouseMovedEventListener(this);
		mouseUpAction = new SVGMouseUpEventListener(this);
		mouseOverAction = new SVGMouseOverEventListener(this);
		mouseOutAction = new SVGMouseOutEventListener(this);

		mainGroup = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		mainGroup.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "10");
		mainGroup.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "Helvetica");
		mainGroup.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		mainGroup.setAttribute(SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE,
				CSSConstants.CSS_NONE_VALUE);
		mainGroup.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, "1");
		mainGroup.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, CSSConstants.CSS_NONE_VALUE);

		EventTarget t = (EventTarget) mainGroup;
		t.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, mouseClickAction, false);
		t.addEventListener(SVGConstants.SVG_MOUSEMOVE_EVENT_TYPE, mouseMovedAction, false);
		t.addEventListener(SVGConstants.SVG_MOUSEUP_EVENT_TYPE, mouseUpAction, false);
		// t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE,
		// mouseOverAction, false);
		// t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE,
		// mouseOutAction, false);

		polygon = (SVGOMPolygonElement) graphController.createElement(SVGConstants.SVG_POLYGON_TAG);
		mainGroup.appendChild(polygon);

		completedPolygon = (SVGOMPolygonElement) graphController
				.createElement(SVGConstants.SVG_POLYGON_TAG);
		completedPolygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil.calculatePoints(
				getShape(), 0, 0));
		completedPolygon.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE,
				SVGGraphSettings.COMPLETED_COLOUR);
		// completedPolygon.setAttribute(SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE,
		// "0.8");
		// completedPolygon.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE,
		// SVGConstants.SVG_NONE_VALUE);
		mainGroup.appendChild(completedPolygon);

		labelText = graphController.createText("");
		label = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		label.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_MIDDLE_VALUE);
		label.appendChild(labelText);
		label.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		label.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		labelGroup = (SVGOMGElement) graphController.createElement(SVGConstants.SVG_G_TAG);
		labelGroup.appendChild(label);
		mainGroup.appendChild(labelGroup);

		iterationText = graphController.createText("");
		iteration = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		iteration.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_END_VALUE);
		iteration.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "6");
		iteration.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		iteration.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		iteration.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		iteration.appendChild(iterationText);
		polygon.appendChild(iteration);

		errorsText = graphController.createText("");
		error = (SVGOMTextElement) graphController.createElement(SVGConstants.SVG_TEXT_TAG);
		error.setAttribute(SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE, SVGConstants.SVG_END_VALUE);
		error.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "6");
		error.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		error.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, CSSConstants.CSS_BLACK_VALUE);
		error.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGConstants.SVG_NONE_VALUE);
		error.appendChild(errorsText);
		polygon.appendChild(error);

		animateShape = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TAG, SVGConstants.SVG_POINTS_ATTRIBUTE, null);

		animatePosition = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TRANSFORM_TAG, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
				SVGConstants.TRANSFORM_TRANSLATE);

		animateLabel = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TRANSFORM_TAG, SVGConstants.SVG_TRANSFORM_ATTRIBUTE,
				SVGConstants.TRANSFORM_TRANSLATE);

		delegate = new SVGGraphElementDelegate(graphController, this, mainGroup);
	}

	public SVGElement getSVGElement() {
		return mainGroup;
	}

	@Override
	public void addEdge(GraphEdge edge) {
		if (edge instanceof SVGGraphEdge) {
			final SVGGraphEdge svgGraphEdge = (SVGGraphEdge) edge;
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					svgGraphEdge.setOpacity(0);
					mainGroup.appendChild(svgGraphEdge.getSVGElement());
					svgGraphEdge.setOpacity(1);
				}
			});
		}
		super.addEdge(edge);
	}

	@Override
	public void addNode(GraphNode node) {
		super.addNode(node);
		if (node instanceof SVGGraphNode) {
			final SVGGraphNode svgGraphNode = (SVGGraphNode) node;
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					svgGraphNode.setOpacity(0);
					mainGroup.appendChild(svgGraphNode.getSVGElement());
					svgGraphNode.setOpacity(1);
				}
			});
		}
	}

	@Override
	public void addSubgraph(Graph subgraph) {
		super.addSubgraph(subgraph);
		if (subgraph instanceof SVGGraph) {
			final SVGGraph svgGraph = (SVGGraph) subgraph;
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					svgGraph.setOpacity(0);
					mainGroup.appendChild(svgGraph.getSVGElement());
					svgGraph.setOpacity(1);
				}
			});
		}
	}

	@Override
	public boolean removeEdge(GraphEdge edge) {
		if (edge instanceof SVGGraphEdge) {
			final SVGGraphEdge svgGraphEdge = (SVGGraphEdge) edge;
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					mainGroup.removeChild(svgGraphEdge.getSVGElement());
				}
			});
		}
		return super.removeEdge(edge);
	}

	@Override
	public boolean removeNode(GraphNode node) {
		if (node instanceof SVGGraphNode) {
			final SVGGraphNode svgGraphNode = (SVGGraphNode) node;
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					mainGroup.removeChild(svgGraphNode.getSVGElement());
				}
			});
		}
		return super.removeNode(node);
	}

	@Override
	public boolean removeSubgraph(Graph subgraph) {
		if (subgraph instanceof SVGGraph) {
			final SVGGraph svgGraph = (SVGGraph) subgraph;
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					mainGroup.removeChild(svgGraph.getSVGElement());
				}
			});
		}
		return super.removeSubgraph(subgraph);
	}

	@Override
	public void setPosition(final Point position) {
		final Point oldPosition = getPosition();
		if (position != null && !position.equals(oldPosition)) {
			super.setPosition(position);
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					if (graphController.isAnimatable()) {
						SVGUtil.animate(animatePosition, polygon, graphController
								.getAnimationSpeed(), oldPosition.x + ", " + oldPosition.y,
								position.x + ", " + position.y);
					} else {
						polygon.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
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
			updateShape(oldSize.width, oldSize.height);
		}
	}

	@Override
	public void setShape(Shape shape) {
		final Dimension oldSize = getSize();
		final Shape currentShape = getShape();
		if (shape != null && !shape.equals(currentShape)) {
			super.setShape(shape);
			updateShape(oldSize.width, oldSize.height);
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
	public void setLabelPosition(final Point labelPosition) {
		final Point oldLabelPosition = getLabelPosition();
		if (labelPosition != null && !labelPosition.equals(oldLabelPosition)) {
			super.setLabelPosition(labelPosition);
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					if (graphController.isAnimatable() && oldLabelPosition != null) {
						SVGUtil.animate(animateLabel, labelGroup, graphController
								.getAnimationSpeed(), oldLabelPosition.x + ", "
								+ oldLabelPosition.y, labelPosition.x + ", " + labelPosition.y);
					} else {
						labelGroup.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
								+ labelPosition.x + " " + labelPosition.y + ")");
					}
				}
			});
		}
	}

	@Override
	public void setIteration(final int iteration) {
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
	public void setCompleted(final float complete) {
		super.setCompleted(complete);
		graphController.updateSVGDocument(new Runnable() {
			public void run() {
				Dimension size = getSize();
				Point position = getPosition();
				completedPolygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil
						.calculatePoints(getShape(), (int) (size.width * complete), size.height));
				completedPolygon.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
						+ position.x + " " + position.y + ")");
			}
		});
	}

	private void updateShape(final int oldWidth, final int oldHeight) {
		if (getShape() != null && getWidth() > 0f && getHeight() > 0f) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					if (graphController.isAnimatable()) {
						SVGUtil.animate(animateShape, polygon, graphController
								.getAnimationSpeed(), SVGUtil.calculatePoints(getShape(),
								oldWidth, oldHeight), SVGUtil.calculatePoints(getShape(),
								getWidth(), getHeight()));
					} else {
						polygon.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, SVGUtil
								.calculatePoints(getShape(), getWidth(), getHeight()));
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
