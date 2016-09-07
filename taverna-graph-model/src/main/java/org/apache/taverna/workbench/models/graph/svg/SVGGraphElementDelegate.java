package org.apache.taverna.workbench.models.graph.svg;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.apache.taverna.workbench.models.graph.GraphElement.LineStyle.NONE;
import static org.apache.taverna.workbench.models.graph.svg.SVGGraphSettings.SELECTED_COLOUR;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.animate;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.createAnimationElement;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.getHexValue;
import static org.apache.batik.util.CSSConstants.CSS_DISPLAY_PROPERTY;
import static org.apache.batik.util.CSSConstants.CSS_INLINE_VALUE;
import static org.apache.batik.util.CSSConstants.CSS_NONE_VALUE;
import static org.apache.batik.util.CSSConstants.CSS_OPACITY_PROPERTY;
import static org.apache.batik.util.CSSConstants.CSS_POINTER_EVENTS_PROPERTY;
import static org.apache.batik.util.CSSConstants.CSS_VISIBLEPAINTED_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_ANIMATE_TAG;
import static org.apache.batik.util.SVGConstants.SVG_FILL_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_NONE_VALUE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE;

import java.awt.Color;

import org.apache.taverna.workbench.models.graph.GraphElement;
import org.apache.taverna.workbench.models.graph.GraphElement.LineStyle;

import org.apache.batik.dom.svg.SVGOMAnimationElement;
import org.apache.batik.dom.svg.SVGOMElement;

/**
 * Delegate for GraphElements. Logically a superclass of SVGGraph, SVGGraphNode
 * and SVGGraphEdge (if java had multiple inheritance).
 * 
 * @author David Withers
 */
public class SVGGraphElementDelegate {
	private SVGGraphController graphController;
	private GraphElement graphElement;
	private SVGOMElement mainGroup;
	private SVGOMAnimationElement animateOpacity;

	public SVGGraphElementDelegate(SVGGraphController graphController,
			GraphElement graphElement, SVGOMElement mainGroup) {
		this.graphController = graphController;
		this.graphElement = graphElement;
		this.mainGroup = mainGroup;

		animateOpacity = createAnimationElement(graphController,
				SVG_ANIMATE_TAG, CSS_OPACITY_PROPERTY, null);
	}

	public void setSelected(final boolean selected) {
		boolean currentSelected = graphElement.isSelected();
		if (currentSelected != selected
				&& !LineStyle.NONE.equals(graphElement.getLineStyle()))
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					mainGroup.setAttribute(SVG_STROKE_ATTRIBUTE,
							selected ? SELECTED_COLOUR
									: getHexValue(graphElement.getColor()));
					mainGroup.setAttribute(SVG_STROKE_WIDTH_ATTRIBUTE,
							selected ? "2" : "1");
				}
			});
	}

	public void setLineStyle(final LineStyle lineStyle) {
		LineStyle currentLineStyle = graphElement.getLineStyle();
		if (!currentLineStyle.equals(lineStyle))
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					String stroke = SVG_NONE_VALUE, dash = SVG_NONE_VALUE;
					switch (lineStyle) {
					case DOTTED:
						stroke = getHexValue(graphElement.getColor());
						dash = "1,5";
						break;
					case SOLID:
						stroke = getHexValue(graphElement.getColor());
					default:
						break;
					}
					mainGroup.setAttribute(SVG_STROKE_ATTRIBUTE, stroke);
					mainGroup
							.setAttribute(SVG_STROKE_DASHARRAY_ATTRIBUTE, dash);
				}
			});
	}

	public void setColor(final Color color) {
		Color currentColor = graphElement.getColor();
		if (currentColor != color && NONE != graphElement.getLineStyle())
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					mainGroup.setAttribute(SVG_STROKE_ATTRIBUTE,
							getHexValue(color));
				}
			});
	}

	public void setFillColor(final Color fillColor) {
		Color currentFillColor = graphElement.getFillColor();
		if (currentFillColor != fillColor)
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					mainGroup.setAttribute(SVG_FILL_ATTRIBUTE,
							getHexValue(fillColor));
				}
			});
	}

	public void setVisible(final boolean visible) {
		boolean currentVisible = graphElement.isVisible();
		if (currentVisible != visible)
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					mainGroup.setAttribute(CSS_DISPLAY_PROPERTY,
							visible ? CSS_INLINE_VALUE : CSS_NONE_VALUE);
				}
			});
	}

	public void setOpacity(final float opacity) {
		final float currentOpacity = graphElement.getOpacity();
		if (currentOpacity != opacity)
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					if (graphController.isAnimatable())
						animate(animateOpacity, mainGroup,
								graphController.getAnimationSpeed(),
								String.valueOf(currentOpacity),
								String.valueOf(opacity));
					else
						mainGroup.setAttribute(CSS_OPACITY_PROPERTY,
								String.valueOf(opacity));
				}
			});
	}

	public void setFiltered(final boolean filtered) {
		boolean currentFiltered = graphElement.isFiltered();
		if (currentFiltered != filtered)
			graphController.updateSVGDocument(new Runnable() {
				@Override
				public void run() {
					mainGroup.setAttribute(CSS_POINTER_EVENTS_PROPERTY,
							filtered ? CSS_NONE_VALUE
									: CSS_VISIBLEPAINTED_VALUE);
					setOpacity(filtered ? 0.2f : 1f);
				}
			});
	}
}
