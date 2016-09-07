/*******************************************************************************
 ******************************************************************************/
package org.apache.taverna.workbench.models.graph.svg;

import static java.awt.Color.BLACK;
import static java.awt.Color.GREEN;
import static java.lang.Float.parseFloat;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.animate;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.calculateAngle;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.createAnimationElement;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.createSVGDocument;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.getDot;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.getHexValue;
import static org.apache.taverna.workbench.models.graph.svg.SVGUtil.svgNS;
import static org.apache.batik.util.SVGConstants.SVG_ANIMATE_TAG;
import static org.apache.batik.util.SVGConstants.SVG_ELLIPSE_TAG;
import static org.apache.batik.util.SVGConstants.SVG_FILL_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_FONT_FAMILY_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_FONT_SIZE_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_G_TAG;
import static org.apache.batik.util.SVGConstants.SVG_LINE_TAG;
import static org.apache.batik.util.SVGConstants.SVG_PATH_TAG;
import static org.apache.batik.util.SVGConstants.SVG_POINTS_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_POLYGON_TAG;
import static org.apache.batik.util.SVGConstants.SVG_RECT_TAG;
import static org.apache.batik.util.SVGConstants.SVG_STYLE_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_TEXT_TAG;
import static org.apache.batik.util.SVGConstants.SVG_TRANSFORM_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_VIEW_BOX_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_X1_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_X2_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_Y1_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_Y2_ATTRIBUTE;
import static org.apache.batik.util.SVGConstants.SVG_Y_ATTRIBUTE;


import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.taverna.ui.menu.MenuManager;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.configuration.workbench.WorkbenchConfiguration;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.models.graph.DotWriter;
import org.apache.taverna.workbench.models.graph.Graph;
import org.apache.taverna.workbench.models.graph.Graph.Alignment;
import org.apache.taverna.workbench.models.graph.GraphController;
import org.apache.taverna.workbench.models.graph.GraphEdge;
import org.apache.taverna.workbench.models.graph.GraphElement;
import org.apache.taverna.workbench.models.graph.GraphNode;
import org.apache.taverna.workbench.models.graph.dot.GraphLayout;
import org.apache.taverna.workbench.models.graph.dot.ParseException;

import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.dom.svg.SVGOMAnimationElement;
import org.apache.batik.dom.svg.SVGOMEllipseElement;
import org.apache.batik.dom.svg.SVGOMGElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMPolygonElement;
import org.apache.batik.dom.svg.SVGOMRectElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGSVGElement;

import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.profiles.Profile;

public class SVGGraphController extends GraphController {
	private static final Logger logger = Logger.getLogger(SVGGraphController.class);
	@SuppressWarnings("unused")
	private static final Timer timer = new Timer("SVG Graph controller timer", true);
	private static final String dotErrorMessage = "Cannot draw diagram(s)\n" +
			"\n" +
			"Install dot as described\n" +
			"at http://www.taverna.org.uk\n" +
			"and specify its location\n" +
			"in the workbench preferences";

	private Map<String, List<SVGGraphEdge>> datalinkMap = new HashMap<>();
	private final JSVGCanvas svgCanvas;
	private SVGDocument svgDocument;
	private GraphLayout graphLayout = new GraphLayout();
	private EdgeLine edgeLine;
	private UpdateManager updateManager;
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	private boolean drawingDiagram = false;
	private int animationSpeed;
	private Rectangle bounds, oldBounds;
	private SVGOMAnimationElement animateBounds;
	private boolean dotMissing = false;
	private final WorkbenchConfiguration workbenchConfiguration;

	public SVGGraphController(Workflow dataflow, Profile profile,
			boolean interactive, JSVGCanvas svgCanvas, EditManager editManager,
			MenuManager menuManager, ColourManager colourManager,
			WorkbenchConfiguration workbenchConfiguration) {
		super(dataflow, profile, interactive, svgCanvas, editManager,
				menuManager, colourManager);
		this.svgCanvas = svgCanvas;
		this.workbenchConfiguration = workbenchConfiguration;
		installUpdateManager();
		layoutSVGDocument(svgCanvas.getBounds());
		svgCanvas.setDocument(getSVGDocument());
	}

	public SVGGraphController(Workflow dataflow, Profile profile,
			boolean interactive, JSVGCanvas svgCanvas, Alignment alignment,
			PortStyle portStyle, EditManager editManager,
			MenuManager menuManager, ColourManager colourManager,
			WorkbenchConfiguration workbenchConfiguration) {
		super(dataflow, profile, interactive, svgCanvas, alignment, portStyle,
				editManager, menuManager, colourManager);
		this.svgCanvas = svgCanvas;
		this.workbenchConfiguration = workbenchConfiguration;
		installUpdateManager();
		layoutSVGDocument(svgCanvas.getBounds());
		svgCanvas.setDocument(getSVGDocument());
	}

	private void installUpdateManager() {
		svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			@Override
			public void gvtRenderingCompleted(GVTTreeRendererEvent ev) {
				setUpdateManager(svgCanvas.getUpdateManager());
			}
		});
	}
	
	@Override
	public GraphEdge createGraphEdge() {
		return new SVGGraphEdge(this);
	}

	@Override
	public Graph createGraph() {
		return new SVGGraph(this);
	}

	@Override
	public GraphNode createGraphNode() {
		return new SVGGraphNode(this);
	}

	public JSVGCanvas getSVGCanvas() {
		return svgCanvas;
	}

	public synchronized SVGDocument getSVGDocument() {
		if (svgDocument == null)
			svgDocument = createSVGDocument();
		return svgDocument;
	}

	@Override
	public void redraw() {
		Graph graph = generateGraph();
		Rectangle actualBounds = layoutGraph(graph, svgCanvas.getBounds());
		setBounds(actualBounds);
		transformGraph(getGraph(), graph);
	}

	private void layoutSVGDocument(Rectangle bounds) {
		animateBounds = createAnimationElement(this, SVG_ANIMATE_TAG,
				SVG_VIEW_BOX_ATTRIBUTE, null);
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
			String layout = getDot(stringWriter.toString(), workbenchConfiguration);
			if (layout.isEmpty())
				logger.warn("Invalid dot returned");
			else
				actualBounds = graphLayout.layoutGraph(this, graph, layout, bounds);
    } catch (ParseException e) {
      throw new RuntimeException("Can't parse dot", e);
		} catch (IOException e) {
			outputMessage(dotErrorMessage);
			setDotMissing(true);
			logger.warn("Couldn't generate dot");
		}
		return actualBounds;
	}

	private void setDotMissing(boolean b) {
		this.dotMissing = b;
	}

	public boolean isDotMissing() {
		return dotMissing;
	}

	public void setBounds(final Rectangle bounds) {
		oldBounds = this.bounds;
		this.bounds = bounds;
		updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				SVGSVGElement svgElement = getSVGDocument().getRootElement();
				if (isAnimatable() && oldBounds != null) {
					String from = "0 0 " + oldBounds.width + " "
							+ oldBounds.height;
					String to = "0 0 " + bounds.width + " " + bounds.height;
					animate(animateBounds, svgElement, getAnimationSpeed(),
							from, to);
				} else if ((svgElement != null) && (bounds != null))
					svgElement.setAttribute(SVG_VIEW_BOX_ATTRIBUTE,
							"0 0 " + String.valueOf(bounds.width) + " "
									+ String.valueOf(bounds.height));
			}
		});
	}

	private void outputMessage(final String message) {
		SVGSVGElement svgElement = getSVGDocument().getRootElement();
		String[] parts = message.split("\n");
		int initialPosition = 200;
		for (int i = 0; i < parts.length; i++) {
			Text errorsText = createText(parts[i]);
			SVGOMTextElement error = (SVGOMTextElement) createElement(SVG_TEXT_TAG);
			error.setAttribute(SVG_Y_ATTRIBUTE,
					Integer.toString(initialPosition + i * 60));
			error.setAttribute(SVG_FONT_SIZE_ATTRIBUTE, "20");
			error.setAttribute(SVG_FONT_FAMILY_ATTRIBUTE, "sans-serif");
			error.setAttribute(SVG_FILL_ATTRIBUTE, "red");
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
				SVGPoint sourcePoint = svgGraphEdge.getPathElement()
						.getPointAtLength(0f);
				edgeLine.setSourcePoint(new Point((int) sourcePoint.getX(),
						(int) sourcePoint.getY()));
			} else
				edgeLine.setSourcePoint(point);
			edgeLine.setTargetPoint(point);
			edgeLine.setColour(Color.BLACK);
			// edgeLine.setVisible(true);
		}
		return started;
	}

	@Override
	public boolean moveEdgeCreationTarget(GraphElement graphElement, Point point) {
		boolean linkValid = super.moveEdgeCreationTarget(graphElement, point);
		if (edgeMoveElement instanceof SVGGraphEdge)
			((SVGGraphEdge) edgeMoveElement).setVisible(false);
		if (edgeCreationFromSink) {
			edgeLine.setSourcePoint(point);
			if (linkValid)
				edgeLine.setColour(GREEN);
			else
				edgeLine.setColour(BLACK);
			edgeLine.setVisible(true);
		} else if (edgeCreationFromSource) {
			edgeLine.setTargetPoint(point);
			if (linkValid)
				edgeLine.setColour(GREEN);
			else
				edgeLine.setColour(BLACK);
			edgeLine.setVisible(true);
		}
		return linkValid;
	}

	@Override
	public boolean stopEdgeCreation(GraphElement graphElement, Point point) {
		GraphEdge movedEdge = edgeMoveElement;
		boolean edgeCreated = super.stopEdgeCreation(graphElement, point);
		if (!edgeCreated && movedEdge instanceof SVGGraphEdge)
			((SVGGraphEdge) movedEdge).setVisible(true);
		edgeLine.setVisible(false);
		return edgeCreated;
	}

	@Override
	public void setEdgeActive(String edgeId, boolean active) {
		if (datalinkMap.containsKey(edgeId))
			for (GraphEdge datalink : datalinkMap.get(edgeId))
				datalink.setActive(active);
	}

	public Element createElement(String tag) {
		return getSVGDocument().createElementNS(svgNS, tag);
	}

	SVGOMGElement createGElem() {
		return (SVGOMGElement) createElement(SVG_G_TAG);
	}

	SVGOMPolygonElement createPolygon() {
		return (SVGOMPolygonElement) createElement(SVG_POLYGON_TAG);
	}

	SVGOMEllipseElement createEllipse() {
		return (SVGOMEllipseElement) createElement(SVG_ELLIPSE_TAG);
	}

	SVGOMPathElement createPath() {
		return (SVGOMPathElement) createElement(SVG_PATH_TAG);
	}

	SVGOMRectElement createRect() {
		return (SVGOMRectElement) createElement(SVG_RECT_TAG);
	}

	public Text createText(String text) {
		return getSVGDocument().createTextNode(text);
	}

	SVGOMTextElement createText(Text text) {
		SVGOMTextElement elem = (SVGOMTextElement) createElement(SVG_TEXT_TAG);
		elem.appendChild(text);
		return elem;
	}

	public void updateSVGDocument(final Runnable thread) {
		if (updateManager == null && !drawingDiagram)
			thread.run();
		else if (!executor.isShutdown())
			executor.execute(new Runnable() {
				@Override
				public void run() {
					waitForUpdateManager();
					try {
						updateManager.getUpdateRunnableQueue().invokeLater(
								thread);
					} catch (IllegalStateException e) {
						logger.error("Update of SVG failed", e);
					}
				}

				private void waitForUpdateManager() {
					try {
						while (updateManager == null)
							Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
			});
//		if (updateManager == null)
//			thread.run();
//		else
//			updateManager.getUpdateRunnableQueue().invokeLater(thread);
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

	@Override
	public void shutdown() {
		super.shutdown();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				getSVGCanvas().stopProcessing();
				executor.shutdown();
			}
		});
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
		edgeLine.line = svgDocument.createElementNS(svgNS, SVG_LINE_TAG);
		edgeLine.line.setAttribute(SVG_STYLE_ATTRIBUTE,
				"fill:none;stroke:black");
		edgeLine.line.setAttribute("pointer-events", "none");
		edgeLine.line.setAttribute("visibility", "hidden");
		edgeLine.line.setAttribute(SVG_X1_ATTRIBUTE, "0");
		edgeLine.line.setAttribute(SVG_Y1_ATTRIBUTE, "0");
		edgeLine.line.setAttribute(SVG_X2_ATTRIBUTE, "0");
		edgeLine.line.setAttribute(SVG_Y2_ATTRIBUTE, "0");

		edgeLine.pointer = svgDocument.createElementNS(svgNS, SVG_POLYGON_TAG);
		edgeLine.pointer.setAttribute(SVG_STYLE_ATTRIBUTE,
				"fill:black;stroke:black");
		edgeLine.pointer.setAttribute(SVG_POINTS_ATTRIBUTE, "0,0 "
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
		graphController.updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				line.setAttribute(SVG_X1_ATTRIBUTE,
						String.valueOf(point.getX()));
				line.setAttribute(SVG_Y1_ATTRIBUTE,
						String.valueOf(point.getY()));

				float x = parseFloat(line.getAttribute(SVG_X2_ATTRIBUTE));
				float y = parseFloat(line.getAttribute(SVG_Y2_ATTRIBUTE));
				double angle = calculateAngle(line);

				pointer.setAttribute(SVG_TRANSFORM_ATTRIBUTE, "translate(" + x
						+ " " + y + ") rotate(" + angle + " 0 0) ");
			}
		});
	}

	public void setTargetPoint(final Point point) {
		graphController.updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				line.setAttribute(SVG_X2_ATTRIBUTE,
						String.valueOf(point.getX()));
				line.setAttribute(SVG_Y2_ATTRIBUTE,
						String.valueOf(point.getY()));

				double angle = calculateAngle(line);
				pointer.setAttribute(SVG_TRANSFORM_ATTRIBUTE, "translate("
						+ point.x + " " + point.y + ") rotate(" + angle
						+ " 0 0) ");
			}
		});
	}

	public void setColour(final Color colour) {
		graphController.updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				String hexColour = getHexValue(colour);
				line.setAttribute(SVG_STYLE_ATTRIBUTE, "fill:none;stroke:"
						+ hexColour + ";");
				pointer.setAttribute(SVG_STYLE_ATTRIBUTE, "fill:" + hexColour
						+ ";stroke:" + hexColour + ";");
			}
		});
	}

	public void setVisible(final boolean visible) {
		graphController.updateSVGDocument(new Runnable() {
			@Override
			public void run() {
				if (visible) {
					line.setAttribute("visibility", "visible");
					pointer.setAttribute("visibility", "visible");
				} else {
					line.setAttribute("visibility", "hidden");
					pointer.setAttribute("visibility", "hidden");
				}
			}
		});
	}
}
