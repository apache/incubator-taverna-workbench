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
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import net.sf.taverna.t2.workbench.models.graph.DotWriter;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;
import net.sf.taverna.t2.workbench.models.graph.dot.GraphLayout;
import net.sf.taverna.t2.workbench.models.graph.dot.ParseException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.svg.SVGOMAnimationElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGSVGElement;

public class SVGGraphController extends GraphController {

	private static Logger logger = Logger.getLogger(SVGGraphController.class);

	private Map<String, List<SVGGraphEdge>> datalinkMap = new HashMap<String, List<SVGGraphEdge>>();
	
	private JSVGCanvas svgCanvas;

	private SVGDocument svgDocument;
	
	private GraphLayout graphLayout = new GraphLayout();

	private EdgeLine edgeLine;
	
	private UpdateManager updateManager;
	
	private static Executor executor = Executors.newFixedThreadPool(1);
	
	private boolean drawingDiagram = false;
	
	private int animationSpeed;
	
	private Rectangle bounds, oldBounds;
	
	private SVGOMAnimationElement animateBounds;

	static final Timer timer = new Timer("SVG Graph controller timer", true);
	
	private static final String dotErrorMessage = "Cannot draw diagram(s)\n" +
			"\n" +
			"Install dot as described\n" +
			"at http://www.taverna.org.uk\n" +
			"and specify its location\n" +
			"in the workbench preferences";
	
	public SVGGraphController(Dataflow dataflow, boolean interactive, JSVGCanvas svgCanvas) {
		super(dataflow, interactive, svgCanvas);
		this.svgCanvas = svgCanvas;
		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				setUpdateManager(SVGGraphController.this.svgCanvas.getUpdateManager());
			}
		});
		layoutSVGDocument(svgCanvas.getBounds());
		svgCanvas.setDocument(getSVGDocument());
	}

	public SVGGraphController(Dataflow dataflow, boolean interactive, JSVGCanvas svgCanvas, Alignment alignment, PortStyle portStyle) {
		super(dataflow, interactive, svgCanvas, alignment, portStyle);
		this.svgCanvas = svgCanvas;
		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingCompleted(GVTTreeRendererEvent arg0) {
				setUpdateManager(SVGGraphController.this.svgCanvas.getUpdateManager());
			}
		});
		layoutSVGDocument(svgCanvas.getBounds());
		svgCanvas.setDocument(getSVGDocument());
	}

	public GraphEdge createGraphEdge() {
		return new SVGGraphEdge(this);
	}

	public Graph createGraph() {
		return new SVGGraph(this);
	}

	public GraphNode createGraphNode() {
		return new SVGGraphNode(this);
	}

	public synchronized SVGDocument getSVGDocument() {
		if (svgDocument == null) {
			svgDocument = SVGUtil.createSVGDocument();
		}
		return svgDocument;
	}
	
	public void redraw() {
		Graph graph = generateGraph();
		Rectangle actualBounds = layoutGraph(graph, svgCanvas.getBounds());
		setBounds(actualBounds);
		transformGraph(getGraph(), graph);
	}
	
	private void layoutSVGDocument(Rectangle bounds) {
		animateBounds = SVGUtil.createAnimationElement(this, SVGConstants.SVG_ANIMATE_TAG,
				SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, null);
		updateManager = null;
		datalinkMap.clear();

		Graph graph = getGraph();
		if (graph instanceof SVGGraph) {
			SVGGraph svgGraph = (SVGGraph) graph;
			SVGSVGElement svgElement = getSVGDocument().getRootElement();
			SVGElement graphElement = svgGraph.getSVGElement();
			svgElement.appendChild(graphElement);

			setBounds(layoutGraph(graph, bounds));
				
			edgeLine = EdgeLine.createAndAdd(getSVGDocument(), this);
		}
		drawingDiagram = true;
	}
	
	public Rectangle layoutGraph(Graph graph, Rectangle bounds) {
		Rectangle actualBounds = null;
		bounds = new Rectangle(bounds);
		StringWriter stringWriter = new StringWriter();
		DotWriter dotWriter = new DotWriter(stringWriter);
		try {
			dotWriter.writeGraph(graph);
			String layout = SVGUtil.getDot(stringWriter.toString());
			actualBounds = graphLayout.layoutGraph(this, graph, layout, bounds);
		} catch (IOException e) {
			outputMessage(dotErrorMessage);
			logger.warn("Couldn't generate dot");
		} catch (ParseException e) {
			logger.warn("Couldn't layout graph");
		}
		return actualBounds;
	}
	
	public void setBounds(final Rectangle bounds) {
		oldBounds = this.bounds;
		this.bounds = bounds;
		updateSVGDocument(new Runnable() {
			public void run() {
				SVGSVGElement svgElement = getSVGDocument().getRootElement();
				if (isAnimatable() && oldBounds != null) {
					String from =  "0 0 " + oldBounds.width + " " + oldBounds.height;
					String to =  "0 0 " + bounds.width + " " + bounds.height;
					SVGUtil.animate(animateBounds, svgElement, getAnimationSpeed(), from, to);
				} else {
					if ((svgElement != null) && (bounds != null)) {
					svgElement.setAttribute(SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, "0 0 " +
							String.valueOf(bounds.width) + " " +
							String.valueOf(bounds.height));
					}
				}
			}
		});
	}
		
	private void outputMessage(final String message) {
		SVGSVGElement svgElement = getSVGDocument().getRootElement();
		String[] parts = message.split("\n");
		int initialPosition = 200;
		for (int i = 0; i < parts.length; i++) {
			Text errorsText = createText(parts[i]);
			SVGOMTextElement error = (SVGOMTextElement) createElement(SVGConstants.SVG_TEXT_TAG);
			error.setAttribute(SVGConstants.SVG_Y_ATTRIBUTE, Integer.toString(initialPosition + i * 60));
			error.setAttribute(SVGConstants.SVG_FONT_SIZE_ATTRIBUTE, "20");
			error.setAttribute(SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
			error.setAttribute(SVGConstants.SVG_FILL_ATTRIBUTE, "red");
			error.appendChild(errorsText);
			svgElement.appendChild(error);		
		}
		bounds = new Rectangle(300, parts.length * 60 + 200);
		svgCanvas.setDocument(getSVGDocument());
	}
	
	public void setUpdateManager(UpdateManager updateManager) {
		this.updateManager = updateManager;
		drawingDiagram = false;
		resetSelection();
	}

	@Override
	public boolean startEdgeCreation(GraphElement graphElement, Point point) {
		boolean alreadyStarted = edgeCreationFromSource || edgeCreationFromSink;
		boolean started = super.startEdgeCreation(graphElement, point);
		if (!alreadyStarted && started) {
			if (edgeMoveElement instanceof SVGGraphEdge) {
				SVGGraphEdge svgGraphEdge = (SVGGraphEdge) edgeMoveElement;
				SVGPoint sourcePoint = svgGraphEdge.getPathElement().getPointAtLength(0f);
				edgeLine.setSourcePoint(new Point((int) sourcePoint.getX(), (int) sourcePoint.getY()));
			} else {
				edgeLine.setSourcePoint(point);
			}
			edgeLine.setTargetPoint(point);
			edgeLine.setColour(Color.BLACK);
			// edgeLine.setVisible(true);
		}
		return started;
	}

	@Override
	public boolean moveEdgeCreationTarget(GraphElement graphElement, Point point) {
		boolean linkValid = super.moveEdgeCreationTarget(graphElement, point);
		if (edgeMoveElement instanceof SVGGraphEdge) {
			((SVGGraphEdge) edgeMoveElement).setVisible(false);
		}
		if (edgeCreationFromSink) {
			edgeLine.setSourcePoint(point);
			if (linkValid) {
				edgeLine.setColour(Color.GREEN);
			} else {
				edgeLine.setColour(Color.BLACK);
			}
			edgeLine.setVisible(true);
		} else if (edgeCreationFromSource) {
			edgeLine.setTargetPoint(point);
			if (linkValid) {
				edgeLine.setColour(Color.GREEN);
			} else {
				edgeLine.setColour(Color.BLACK);
			}
			edgeLine.setVisible(true);
		}
		return linkValid;
	}

	@Override
	public boolean stopEdgeCreation(GraphElement graphElement, Point point) {
		GraphEdge movedEdge = edgeMoveElement;
		boolean edgeCreated = super.stopEdgeCreation(graphElement, point);
		if (!edgeCreated && movedEdge instanceof SVGGraphEdge) {
			((SVGGraphEdge) movedEdge).setVisible(true);
		}
		edgeLine.setVisible(false);
		return edgeCreated;
	}

	@Override
	public void setEdgeActive(String edgeId, boolean active) {
		if (datalinkMap.containsKey(edgeId)) {
			for (GraphEdge datalink : datalinkMap.get(edgeId)) {
				datalink.setActive(active);
			}
		}
	}

	public Element createElement(String tag) {
		return getSVGDocument().createElementNS(SVGUtil.svgNS, tag);
	}
	
	public Text createText(String text) {
		return getSVGDocument().createTextNode(text);
	}
	
	public void updateSVGDocument(final Runnable thread) {
		if (updateManager == null && !drawingDiagram) {
			thread.run();
		} else {
			executor.execute(new Runnable() {
				public void run() {
					while (updateManager == null) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					}
					try {
						updateManager.getUpdateRunnableQueue().invokeLater(thread);
					}
					catch (IllegalStateException e) {
						logger.error("Update of SVG failed", e);
					}
				}			
			});
		}
//		if (updateManager == null) {
//			thread.run();
//		} else {
//			updateManager.getUpdateRunnableQueue().invokeLater(thread);
//		}
	}

	public boolean isAnimatable() {
		return animationSpeed > 0 && updateManager != null && !drawingDiagram;
	}

	/**
	 * Returns the animation speed in milliseconds.
	 * 
	 * @return the animation speed in milliseconds
	 */
	public int getAnimationSpeed() {
		return animationSpeed;
	}

	/**
	 * Sets the animation speed in milliseconds. A value of 0 turns off animation.
	 * 
	 * @param animationSpeed the animation speed in milliseconds
	 */
	public void setAnimationSpeed(int animationSpeed) {
		this.animationSpeed = animationSpeed;
	}
	
}

class EdgeLine {

	private static final float arrowLength = 10f;

	private static final float arrowWidth = 3f;

	private Element line;

	private Element pointer;
	
	private SVGGraphController graphController;

	private EdgeLine(SVGGraphController graphController) {
		this.graphController = graphController;
	}

	public static EdgeLine createAndAdd(SVGDocument svgDocument, SVGGraphController graphController) {
		EdgeLine edgeLine = new EdgeLine(graphController);
		edgeLine.line = svgDocument.createElementNS(SVGUtil.svgNS,
				SVGConstants.SVG_LINE_TAG);
		edgeLine.line.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE,
				"fill:none;stroke:black");
		edgeLine.line.setAttribute("pointer-events", "none");
		edgeLine.line.setAttribute("visibility", "hidden");
		edgeLine.line.setAttribute(SVGConstants.SVG_X1_ATTRIBUTE, "0");
		edgeLine.line.setAttribute(SVGConstants.SVG_Y1_ATTRIBUTE, "0");
		edgeLine.line.setAttribute(SVGConstants.SVG_X2_ATTRIBUTE, "0");
		edgeLine.line.setAttribute(SVGConstants.SVG_Y2_ATTRIBUTE, "0");

		edgeLine.pointer = svgDocument.createElementNS(SVGUtil.svgNS,
				SVGConstants.SVG_POLYGON_TAG);
		edgeLine.pointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE,
				"fill:black;stroke:black");
		edgeLine.pointer.setAttribute(SVGConstants.SVG_POINTS_ATTRIBUTE, "0,0 "
				+ -arrowLength + "," + arrowWidth + " " + -arrowLength + ","
				+ -arrowWidth + " 0,0");
		edgeLine.pointer.setAttribute("pointer-events", "none");
		edgeLine.pointer.setAttribute("visibility", "hidden");

		Element svgRoot = svgDocument.getDocumentElement();
		svgRoot.insertBefore(edgeLine.line, null);
		svgRoot.insertBefore(edgeLine.pointer, null);

		return edgeLine;
	}

	public void setSourcePoint(final Point point) {
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					line.setAttribute(SVGConstants.SVG_X1_ATTRIBUTE, String.valueOf(point
							.getX()));
					line.setAttribute(SVGConstants.SVG_Y1_ATTRIBUTE, String.valueOf(point
							.getY()));

					float x = Float.parseFloat(line
							.getAttribute(SVGConstants.SVG_X2_ATTRIBUTE));
					float y = Float.parseFloat(line
							.getAttribute(SVGConstants.SVG_Y2_ATTRIBUTE));
					double angle = SVGUtil.calculateAngle(line);

					pointer.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
							+ x + " " + y + ") rotate(" + angle + " 0 0) ");
				}
			}
		);
	}

	public void setTargetPoint(final Point point) {
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					line.setAttribute(SVGConstants.SVG_X2_ATTRIBUTE, String.valueOf(point
							.getX()));
					line.setAttribute(SVGConstants.SVG_Y2_ATTRIBUTE, String.valueOf(point
							.getY()));

					double angle = SVGUtil.calculateAngle(line);
					pointer.setAttribute(SVGConstants.SVG_TRANSFORM_ATTRIBUTE, "translate("
							+ point.x + " " + point.y + ") rotate(" + angle + " 0 0) ");
				}
			}
		);
	}

	public void setColour(final Color colour) {
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					String hexColour = SVGUtil.getHexValue(colour);
					line.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:none;stroke:"
							+ hexColour + ";");
					pointer.setAttribute(SVGConstants.SVG_STYLE_ATTRIBUTE, "fill:"
							+ hexColour + ";stroke:" + hexColour + ";");
				}
			}
		);
	}

	public void setVisible(final boolean visible) {
		graphController.updateSVGDocument(
			new Runnable() {
				public void run() {
					if (visible) {
						line.setAttribute("visibility", "visible");
						pointer.setAttribute("visibility", "visible");
					} else {
						line.setAttribute("visibility", "hidden");
						pointer.setAttribute("visibility", "hidden");
					}
				}
			}
		);
	}
	
}
