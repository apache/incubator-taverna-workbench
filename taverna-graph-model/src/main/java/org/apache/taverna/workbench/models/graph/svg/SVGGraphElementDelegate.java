/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
