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

import static net.sf.taverna.t2.workbench.models.graph.svg.SVGGraphSettings.COMPLETED_COLOUR;
import static net.sf.taverna.t2.workbench.models.graph.svg.SVGUtil.animate;
import static net.sf.taverna.t2.workbench.models.graph.svg.SVGUtil.calculatePoints;
import static net.sf.taverna.t2.workbench.models.graph.svg.SVGUtil.createAnimationElement;
import static org.apache.batik.util.CSSConstants.CSS_BLACK_VALUE;
import static org.apache.batik.util.CSSConstants.CSS_NONE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_ANIMATE_TAG;
import static org.apache.batik.util.SVGConstants.SVG_ANIMATE_TRANSFORM_TAG;
import static org.apache.batik.util.SVGConstants.SVG_CLICK_EVENT_TYPE;
import static org.apache.batik.util.SVGConstants.SVG_END_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_FILL_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_FONT_SIZE_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_MIDDLE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_MOUSEMOVE_EVENT_TYPE;
import static org.apache.batik.util.SVGConstants.SVG_MOUSEUP_EVENT_TYPE;
import static org.apache.batik.util.SVGConstants.SVG_NONE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_POINTS_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_TEXT_ANCHOR_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_TRANSFORM_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.TRANSFORM_TRANSLATE;

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

		mainGroup = graphController.createGElem();
		mainGroup.setAttribute(SVG_FONT_SIZE_ATTRIBUTE, "10");
		mainGroup.setAttribute(SVG_FONT_FAMILY_ATTRIBUTE, "Helvetica");
		mainGroup.setAttribute(SVG_STROKE_ATTRIBUTE, CSS_BLACK_VALUE);
		mainGroup.setAttribute(SVG_STROKE_DASHARRAY_ATTRIBUTE, CSS_NONE_VALUE);
		mainGroup.setAttribute(SVG_STROKE_WIDTH_ATTRIBUTE, "1");
		mainGroup.setAttribute(SVG_FILL_ATTRIBUTE, CSS_NONE_VALUE);

		EventTarget t = (EventTarget) mainGroup;
		t.addEventListener(SVG_CLICK_EVENT_TYPE, mouseClickAction, false);
		t.addEventListener(SVG_MOUSEMOVE_EVENT_TYPE, mouseMovedAction, false);
		t.addEventListener(SVG_MOUSEUP_EVENT_TYPE, mouseUpAction, false);
		// t.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, mouseOverAction, false);
		// t.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, mouseOutAction, false);

		polygon = graphController.createPolygon();
		mainGroup.appendChild(polygon);

		completedPolygon = graphController.createPolygon();
		completedPolygon.setAttribute(SVG_POINTS_ATTRIBUTE,
				calculatePoints(getShape(), 0, 0));
		completedPolygon.setAttribute(SVG_FILL_ATTRIBUTE, COMPLETED_COLOUR);
		// completedPolygon.setAttribute(SVGConstants.SVG_FILL_OPACITY_ATTRIBUTE, "0.8");
		// completedPolygon.setAttribute(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
		mainGroup.appendChild(completedPolygon);

		labelText = graphController.createText("");
		label = graphController.createText(labelText);
		label.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_MIDDLE_VALUE);
		label.setAttribute(SVG_FILL_ATTRIBUTE, CSS_BLACK_VALUE);
		label.setAttribute(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
		labelGroup = graphController.createGElem();
		labelGroup.appendChild(label);
		mainGroup.appendChild(labelGroup);

		iterationText = graphController.createText("");
		iteration = graphController.createText(iterationText);
		iteration.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_END_VALUE);
		iteration.setAttribute(SVG_FONT_SIZE_ATTRIBUTE, "6");
		iteration.setAttribute(SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		iteration.setAttribute(SVG_FILL_ATTRIBUTE, CSS_BLACK_VALUE);
		iteration.setAttribute(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
		polygon.appendChild(iteration);

		errorsText = graphController.createText("");
		error = graphController.createText(errorsText);
		error.setAttribute(SVG_TEXT_ANCHOR_ATTRIBUTE, SVG_END_VALUE);
		error.setAttribute(SVG_FONT_SIZE_ATTRIBUTE, "6");
		error.setAttribute(SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
		error.setAttribute(SVG_FILL_ATTRIBUTE, CSS_BLACK_VALUE);
		error.setAttribute(SVG_STROKE_ATTRIBUTE, SVG_NONE_VALUE);
		polygon.appendChild(error);

		animateShape = createAnimationElement(graphController, SVG_ANIMATE_TAG,
				SVG_POINTS_ATTRIBUTE, null);

		animatePosition = createAnimationElement(graphController,
				SVG_ANIMATE_TRANSFORM_TAG, SVG_TRANSFORM_ATTRIBUTE,
				TRANSFORM_TRANSLATE);

		animateLabel = createAnimationElement(graphController,
				SVG_ANIMATE_TRANSFORM_TAG, SVG_TRANSFORM_ATTRIBUTE,
				TRANSFORM_TRANSLATE);

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
				@Override
				public void run() {
				    float opacity = svgGraphEdge.getOpacity();
					svgGraphEdge.setOpacity(0);
					mainGroup.appendChild(svgGraphEdge.getSVGElement());
					svgGraphEdge.setOpacity(opacity);
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
				@Override
				public void run() {
				    float opacity = svgGraphNode.getOpacity();
					svgGraphNode.setOpacity(0);
					mainGroup.appendChild(svgGraphNode.getSVGElement());
					svgGraphNode.setOpacity(opacity);
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
				@Override
				public void run() {
				    float opacity = svgGraph.getOpacity();
					svgGraph.setOpacity(0);
					mainGroup.appendChild(svgGraph.getSVGElement());
					svgGraph.setOpacity(opacity);
				}
			});
		}
	}

	@Override
	public boolean removeEdge(GraphEdge edge) {
		if (edge instanceof SVGGraphEdge) {
			final SVGGraphEdge svgGraphEdge = (SVGGraphEdge) edge;
			graphController.updateSVGDocument(new Runnable() {
				@Override
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
				@Override
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
				@Override
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
				@Override
				public void run() {
					if (graphController.isAnimatable())
						animate(animatePosition, polygon,
								graphController.getAnimationSpeed(),
								oldPosition.x + ", " + oldPosition.y,
								position.x + ", " + position.y);
					else
						polygon.setAttribute(SVG_TRANSFORM_ATTRIBUTE,
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
			@Override
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
				@Override
				public void run() {
					if (graphController.isAnimatable()
							&& oldLabelPosition != null)
						animate(animateLabel, labelGroup,
								graphController.getAnimationSpeed(),
								oldLabelPosition.x + ", " + oldLabelPosition.y,
								labelPosition.x + ", " + labelPosition.y);
					else
						labelGroup.setAttribute(SVG_TRANSFORM_ATTRIBUTE,
								"translate(" + labelPosition.x + " "
										+ labelPosition.y + ")");
				}
			});
		}
	}

	@Override
	public void setIteration(final int iteration) {
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
	public void setCompleted(final float complete) {
		super.setCompleted(complete);
		graphController.updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				Dimension size = getSize();
				Point position = getPosition();
				completedPolygon.setAttribute(
						SVG_POINTS_ATTRIBUTE,
						calculatePoints(getShape(),
								(int) (size.width * complete), size.height));
				completedPolygon.setAttribute(SVG_TRANSFORM_ATTRIBUTE,
						"translate(" + position.x + " " + position.y + ")");
			}
		});
	}

	private void updateShape(final int oldWidth, final int oldHeight) {
		if (getShape() != null && getWidth() > 0f && getHeight() > 0f) {
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					if (graphController.isAnimatable())
						animate(animateShape,
								polygon,
								graphController.getAnimationSpeed(),
								calculatePoints(getShape(), oldWidth, oldHeight),
								calculatePoints(getShape(), getWidth(),
										getHeight()));
					else {
						polygon.setAttribute(
								SVG_POINTS_ATTRIBUTE,
								calculatePoints(getShape(), getWidth(),
										getHeight()));
						iteration.setAttribute(SVG_TRANSFORM_ATTRIBUTE,
								"translate(" + (getWidth() - 1.5) + " 5.5)");
						error.setAttribute(SVG_TRANSFORM_ATTRIBUTE,
								"translate(" + (getWidth() - 1.5) + " "
										+ (getHeight() - 1) + ")");
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
