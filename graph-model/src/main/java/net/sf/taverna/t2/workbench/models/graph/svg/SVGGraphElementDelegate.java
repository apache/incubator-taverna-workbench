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
package net.sf.taverna.t2.workbench.models.graph.svg;

import java.awt.Color;

import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphElement.LineStyle;

import org.apache.batik.dom.svg.SVGOMAnimationElement;
import org.apache.batik.dom.svg.SVGOMElement;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGConstants;

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

	public SVGGraphElementDelegate(SVGGraphController graphController, GraphElement graphElement,
			SVGOMElement mainGroup) {
		this.graphController = graphController;
		this.graphElement = graphElement;
		this.mainGroup = mainGroup;

		animateOpacity = SVGUtil.createAnimationElement(graphController,
				SVGConstants.SVG_ANIMATE_TAG, CSSConstants.CSS_OPACITY_PROPERTY, null);
	}

	public void setSelected(final boolean selected) {
		boolean currentSelected = graphElement.isSelected();
		if (currentSelected != selected) {
			if (!LineStyle.NONE.equals(graphElement.getLineStyle())) {
				graphController.updateSVGDocument(new Runnable() {
					public void run() {
						mainGroup.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE,
								selected ? SVGGraphSettings.SELECTED_COLOUR : SVGUtil
										.getHexValue(graphElement.getColor()));
						mainGroup.setAttribute(SVGConstants.SVG_STROKE_WIDTH_ATTRIBUTE, selected ? "2"
								: "1");
					}
				});
			}
		}
	}

	public void setLineStyle(final LineStyle lineStyle) {
		LineStyle currentLineStyle = graphElement.getLineStyle();
		if (!currentLineStyle.equals(lineStyle)) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					if (LineStyle.NONE.equals(lineStyle)) {
						mainGroup.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE,
								SVGConstants.SVG_NONE_VALUE);
						mainGroup.setAttribute(SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE,
								SVGConstants.SVG_NONE_VALUE);
					} else if (LineStyle.DOTTED.equals(lineStyle)) {
						mainGroup.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil
								.getHexValue(graphElement.getColor()));
						mainGroup.setAttribute(SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE, "1,5");
					} else if (LineStyle.SOLID.equals(lineStyle)) {
						mainGroup.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil
								.getHexValue(graphElement.getColor()));
						mainGroup.setAttribute(SVGConstants.SVG_STROKE_DASHARRAY_ATTRIBUTE,
								SVGConstants.SVG_NONE_VALUE);
					}
				}
			});
		}
	}

	public void setColor(final Color color) {
		Color currentColor = graphElement.getColor();
		if (currentColor != color && !LineStyle.NONE.equals(graphElement.getLineStyle())) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					mainGroup.setAttribute(SVGConstants.SVG_STROKE_ATTRIBUTE, SVGUtil
							.getHexValue(color));
				}
			});
		}
	}

	public void setFillColor(final Color fillColor) {
		Color currentFillColor = graphElement.getFillColor();
		if (currentFillColor != fillColor) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					mainGroup.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, SVGUtil
							.getHexValue(fillColor));
				}
			});
		}
	}

	public void setVisible(final boolean visible) {
		boolean currentVisible = graphElement.isVisible();
		if (currentVisible != visible) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					mainGroup.setAttribute(CSSConstants.CSS_DISPLAY_PROPERTY,
							visible ? CSSConstants.CSS_INLINE_VALUE : CSSConstants.CSS_NONE_VALUE);
				}
			});
		}
	}

	public void setOpacity(final float opacity) {
		final float currentOpacity = graphElement.getOpacity();
		if (currentOpacity != opacity) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					if (graphController.isAnimatable()) {
						SVGUtil.animate(animateOpacity, mainGroup, graphController
								.getAnimationSpeed(), String.valueOf(currentOpacity), String
								.valueOf(opacity));
					} else {
						mainGroup.setAttribute(CSSConstants.CSS_OPACITY_PROPERTY, String
								.valueOf(opacity));
					}
				}
			});
		}
	}

	public void setFiltered(final boolean filtered) {
		boolean currentFiltered = graphElement.isFiltered();
		if (currentFiltered != filtered) {
			graphController.updateSVGDocument(new Runnable() {
				public void run() {
					mainGroup.setAttribute(CSSConstants.CSS_POINTER_EVENTS_PROPERTY,
							filtered ? CSSConstants.CSS_NONE_VALUE
									: CSSConstants.CSS_VISIBLEPAINTED_VALUE);
					setOpacity(filtered ? 0.2f : 1f);
				}
			});
		}
	}

}
