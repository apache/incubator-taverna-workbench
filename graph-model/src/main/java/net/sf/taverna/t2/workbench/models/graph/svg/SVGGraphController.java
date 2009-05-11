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

import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.models.graph.DotWriter;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphEventManager;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.dot.GraphLayout;
import net.sf.taverna.t2.workbench.models.graph.dot.ParseException;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.SVGConstants;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGSVGElement;

public abstract class SVGGraphController extends GraphController {

	private static Logger logger = Logger.getLogger(SVGGraphController.class);

	private Map<String, List<SVGGraphEdge>> datalinkMap = new HashMap<String, List<SVGGraphEdge>>();

	private SVGDocument svgDocument;
	
	private GraphLayout graphLayout = new GraphLayout();

	private EdgeLine edgeLine;
	
	private UpdateManager updateManager;
	
	private Executor executor = Executors.newFixedThreadPool(1);
	
	private boolean drawingDiagram = false;

//	public static final String OUTPUT_COLOUR = "blue";
//
//	public static final int OUTPUT_FLASH_PERIOD = 200;

	static final Timer timer = new Timer("SVG Graph controller timer", true);
	
	private static final String dotErrorMessage = "Cannot draw diagram(s)\n" +
			"\n" +
			"Install dot as described\n" +
			"at http://www.taverna.org.uk\n" +
			"and specify its location\n" +
			"in the workbench preferences";

	public SVGGraphController(Dataflow dataflow, JComponent component) {
		super(dataflow, component);
	}

	public SVGGraphController(Dataflow dataflow, GraphEventManager graphEventManager, JComponent component) {
		super(dataflow, graphEventManager, component);
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

	public SVGDocument generateSVGDocument(Rectangle bounds) {
		svgDocument = SVGUtil.createSVGDocument();
		updateManager = null;
		datalinkMap.clear();

		double aspectRatio = ((float) bounds.width) / ((float) bounds.height);
		
		Graph graph = generateGraph();
		if (graph instanceof SVGGraph) {
			SVGGraph svgGraph = (SVGGraph) graph;
			SVGSVGElement svgElement = svgDocument.getRootElement();
			SVGElement graphElement = svgGraph.getSVGElement();
			svgElement.appendChild(graphElement);
			try {
				StringWriter stringWriter = new StringWriter();
				DotWriter dotWriter = new DotWriter(stringWriter);
				dotWriter.writeGraph(graph);

				String layout = SVGUtil.getDot(stringWriter.toString());

				Rectangle actualBounds = graphLayout.layoutGraph(this, graph, layout, aspectRatio);
				svgElement.setAttribute(SVGConstants.SVG_WIDTH_ATTRIBUTE, String.valueOf(actualBounds.width));
				svgElement.setAttribute(SVGConstants.SVG_HEIGHT_ATTRIBUTE, String.valueOf(actualBounds.height));
				svgElement.setAttribute(SVGConstants.SVG_VIEW_BOX_ATTRIBUTE, "0 0 " +
						String.valueOf(actualBounds.width) + " " +
						String.valueOf(actualBounds.height));

				
				edgeLine = EdgeLine.createAndAdd(svgDocument, this);
//SVGUtil.writeSVG(svgDocument);
			} catch (IOException e) {
				outputMessage(dotErrorMessage, svgElement);
		logger.error("Couldn't generate svg", e);
			} catch (ParseException e) {
				logger.error("Couldn't layout svg", e);
			}
		}
		drawingDiagram = true;
		return svgDocument;
	}
	
	private void outputMessage(final String message, SVGSVGElement svgElement) {
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
	}
	
	public void setUpdateManager(UpdateManager updateManager) {
		this.updateManager = updateManager;
		drawingDiagram = false;
		resetSelection();
	}

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

	public boolean stopEdgeCreation(GraphElement graphElement, Point point) {
		GraphEdge movedEdge = edgeMoveElement;
		boolean edgeCreated = super.stopEdgeCreation(graphElement, point);
		if (!edgeCreated && movedEdge instanceof SVGGraphEdge) {
			((SVGGraphEdge) movedEdge).setVisible(true);
		}
		edgeLine.setVisible(false);
		return edgeCreated;
	}

	public void setEdgeActive(String edgeId, boolean active) {
		if (datalinkMap.containsKey(edgeId)) {
			for (GraphEdge datalink : datalinkMap.get(edgeId)) {
				datalink.setActive(active);
			}
		}
	}

	public Element createElement(String tag) {
		return svgDocument.createElementNS(SVGUtil.svgNS, tag);
	}
	
	public Text createText(String text) {
		return svgDocument.createTextNode(text);
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
					updateManager.getUpdateRunnableQueue().invokeLater(thread);
				}			
			});
		}
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
