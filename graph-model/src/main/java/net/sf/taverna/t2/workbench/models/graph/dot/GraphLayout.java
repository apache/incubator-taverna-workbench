/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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
package net.sf.taverna.t2.workbench.models.graph.dot;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphController;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.GraphElement;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;

/**
 * Lays out a graph from a DOT layout.
 *
 * @author David Withers
 */
public class GraphLayout implements DOTParserVisitor {
	
	private static final int BORDER = 10;
	
	private Rectangle bounds;
	
//	private Rectangle adjustedBounds;
	
	private double aspectRatio;
	
	private GraphController graphController;
	
	private int xOffset;
	
	private int yOffset;
	
	public Rectangle layoutGraph(GraphController graphController, Graph graph, String laidOutDot, double aspectRatio) throws ParseException {
		this.graphController = graphController;
		this.aspectRatio = aspectRatio;
		bounds = null;
		xOffset = 0;
		yOffset = 0;
		
		DOTParser parser = new DOTParser(new StringReader(laidOutDot));
		parser.parse().jjtAccept(this, graph);
		
//		int xOffset = (bounds.width - bounds.width) / 2;
//		int yOffset = (bounds.height - bounds.height) / 2;
		
		return new Rectangle(xOffset, yOffset, bounds.width, bounds.height);
	}

	public Object visit(SimpleNode node, Object data) {
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTParse node, Object data) {
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGraph node, Object data) {
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStatementList node, Object data) {
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStatement node, Object data) {
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTAttributeStatement node, Object data) {
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNodeStatement node, Object data) {
		GraphElement element = graphController.getElement(removeQuotes(node.name));
		if (element != null) {
			return node.childrenAccept(this, element);
		}
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNodeId node, Object data) {
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPort node, Object data) {
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTEdgeStatement node, Object data) {
		StringBuilder id = new StringBuilder();
		id.append(removeQuotes(node.name));
		if (node.port != null) {
			id.append(":");
			id.append(removeQuotes(node.port));
		}
	    if (node.children != null) {
	        for (Node child : node.children) {
	        	if (child instanceof ASTEdgeRHS) {
	        		NamedNode rhsNode = (NamedNode) child.jjtAccept(this, data);
	        		id.append("->");
	        		id.append(removeQuotes(rhsNode.name));
	        		if (rhsNode.port != null) {
	        			id.append(":");
	        			id.append(removeQuotes(rhsNode.port));
	        		}
	        	}
	        }
	    }
		GraphElement element = graphController.getElement(id.toString());
		if (element != null) {
			return node.childrenAccept(this, element);
		}
	    return node.childrenAccept(this, data);
	}

	public Object visit(ASTSubgraph node, Object data) {
		GraphElement element = graphController.getElement(removeQuotes(node.name).substring("cluster_".length()));
		if (element != null) {
			return node.childrenAccept(this, element);
		}
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTEdgeRHS node, Object data) {
		return node;
	}

	public Object visit(ASTAttributeList node, Object data) {
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTAList node, Object data) {
		if (data instanceof Graph) {
			Graph graph = (Graph) data;
			if ("bb".equalsIgnoreCase(node.name)) {
				Rectangle rect = getRectangle(node.value);
				if (rect.width == 0 && rect.height == 0) {
					rect.width = 500;
					rect.height = 500;
				}
				if (bounds == null) {
					bounds = calculateBounds(rect);
					rect = bounds;
				}
				graph.setWidth(rect.width);
				graph.setHeight(rect.height);
				graph.setPosition(new Point(rect.x, rect.y));
			} else if ("lp".equalsIgnoreCase(node.name)) {
				graph.setLabelPosition(getPoint(node.value));
			}
		} else if (data instanceof GraphNode) {
			GraphNode graphNode = (GraphNode) data;
			if ("width".equalsIgnoreCase(node.name)) {
				graphNode.setWidth(getSize(node.value));
			} else if ("height".equalsIgnoreCase(node.name)) {
				graphNode.setHeight(getSize(node.value));
			} else if ("pos".equalsIgnoreCase(node.name)) {
				Point position = getPoint(node.value);
				position.x = position.x - (graphNode.getWidth() / 2);
				position.y = position.y - (graphNode.getHeight() / 2);
				graphNode.setPosition(position);
			} else if ("rects".equalsIgnoreCase(node.name)) {
				List<Rectangle> rectangles = getRectangles(node.value);
				List<GraphNode> sinkNodes = graphNode.getSinkNodes();
				if (graphController.getAlignment().equals(Alignment.HORIZONTAL)) {
					Rectangle rect = rectangles.remove(0);
					graphNode.setWidth(rect.width);
					graphNode.setHeight(rect.height);
					graphNode.setPosition(new Point(rect.x, rect.y));
				} else {
					Rectangle rect = rectangles.remove(sinkNodes.size());
					graphNode.setWidth(rect.width);
					graphNode.setHeight(rect.height);
					graphNode.setPosition(new Point(rect.x, rect.y));
				}
				Point origin = graphNode.getPosition();
				for (GraphNode sinkNode : sinkNodes) {
					Rectangle rect = rectangles.remove(0);
					sinkNode.setWidth(rect.width);
					sinkNode.setHeight(rect.height);
					sinkNode.setPosition(new Point(rect.x - origin.x, rect.y - origin.y));
				}
				List<GraphNode> sourceNodes = graphNode.getSourceNodes();
				for (GraphNode sourceNode : sourceNodes) {
					Rectangle rect = rectangles.remove(0);
					sourceNode.setWidth(rect.width);
					sourceNode.setHeight(rect.height);
					sourceNode.setPosition(new Point(rect.x - origin.x, rect.y - origin.y));
				}
			}
		} else if (data instanceof GraphEdge) {
			GraphEdge graphEdge = (GraphEdge) data;
			if ("pos".equalsIgnoreCase(node.name)) {
				graphEdge.setPath(getPath(node.value));
			}
		}
		return node.childrenAccept(this, data);
	}

	private Rectangle calculateBounds(Rectangle bounds) {
		bounds = new Rectangle(bounds);
		bounds.width += BORDER;
		bounds.height += BORDER;
		Rectangle newBounds = new Rectangle(bounds);
		double ratio = ((float) bounds.width) / ((float) bounds.height);
		double requiredRatio = aspectRatio;
		if (ratio > requiredRatio) {
			newBounds.height = (int) ((ratio / requiredRatio) * bounds.height);
		} else if (ratio < requiredRatio) {
			newBounds.width = (int) ((requiredRatio / ratio) * bounds.width);
		}
		xOffset = (newBounds.width - bounds.width) / 2;
		yOffset = (newBounds.height - bounds.height) / 2;
		xOffset += BORDER/2;
		yOffset += BORDER/2;
		return newBounds;
	}

	private List<Point> getPath(String value) {
		String[] points = removeQuotes(value).split(" ");
		List<Point> path = new ArrayList<Point>();
		for (String point : points) {
			String[] coords = point.split(",");
			if (coords.length == 2) {
				path.add(new Point(Integer.parseInt(coords[0]) + xOffset, flipY(Integer.parseInt(coords[1]) + yOffset)));
			}
		}
		return path;
	}

	private int flipY(int y) {
		return bounds.height - y;
	}

	private List<Rectangle> getRectangles(String value) {
		List<Rectangle> rectangles = new ArrayList<Rectangle>();
		String[] rects = value.split(" ");
		for (String rectangle : rects) {
			rectangles.add(getRectangle(rectangle));
		}
		return rectangles;
	}

	private Rectangle getRectangle(String value) {
		String[] coords = removeQuotes(value).split(",");
		Rectangle rectangle = new Rectangle();
		rectangle.x = (int) Float.parseFloat(coords[0]);
		rectangle.y = (int) Float.parseFloat(coords[3]);
		rectangle.width = (int) Float.parseFloat(coords[2]) - rectangle.x;
		rectangle.height = rectangle.y - (int) Float.parseFloat(coords[1]);
		rectangle.x += xOffset;
		rectangle.y += yOffset;
		if (bounds != null) {
			rectangle.y = flipY(rectangle.y);
		} else {
			rectangle.y = rectangle.height - rectangle.y;
		}
		return rectangle;
	}

	private Point getPoint(String value) {
		String[] coords = removeQuotes(value).split(",");
		return new Point((int) Float.parseFloat(coords[0]) + xOffset, flipY((int) Float.parseFloat(coords[1]) + yOffset));
	}

	private int getSize(String value) {
		double size = Double.parseDouble(removeQuotes(value));
		return (int) (size * 72);
	}

	private String removeQuotes(String value) {
		String result = value.trim();
		if (result.startsWith("\"")) {
			result = result.substring(1);
		}
		if (result.endsWith("\"")) {
			result = result.substring(0, result.length() - 1);
		}
		result = result.replaceAll("\\\\", "");
		return result;
	}

}
