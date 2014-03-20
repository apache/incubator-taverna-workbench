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
package net.sf.taverna.t2.workbench.models.graph;

import java.awt.Point;
import java.util.List;

/**
 * An edge connecting two nodes in a graph.
 * 
 * @author David Withers
 */
public class GraphEdge extends GraphElement {

	public enum ArrowStyle {NONE, NORMAL, DOT}
	
	private GraphNode source;
	
	private GraphNode sink;
	
	private ArrowStyle arrowHeadStyle = ArrowStyle.NORMAL;

	private ArrowStyle arrowTailStyle = ArrowStyle.NONE;
	
	private List<Point> path;
	
	private boolean active;

	/**
	 * Constructs a new instance of Edge.
	 *
	 */
	public GraphEdge(GraphController graphController) {
		super(graphController);
	}
	
	/**
	 * Returns the source.
	 *
	 * @return the source
	 */
	public GraphNode getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the new source
	 */
	public void setSource(GraphNode source) {
		this.source = source;
	}

	/**
	 * Returns the sink.
	 *
	 * @return the sink
	 */
	public GraphNode getSink() {
		return sink;
	}

	/**
	 * Sets the sink.
	 *
	 * @param sink the new sink
	 */
	public void setSink(GraphNode sink) {
		this.sink = sink;
	}

	/**
	 * Returns the arrowHeadStyle.
	 *
	 * @return the arrowHeadStyle
	 */
	public ArrowStyle getArrowHeadStyle() {
		return arrowHeadStyle;
	}

	/**
	 * Sets the arrowHeadStyle.
	 *
	 * @param arrowHeadStyle the new arrowHeadStyle
	 */
	public void setArrowHeadStyle(ArrowStyle arrowHeadStyle) {
		this.arrowHeadStyle = arrowHeadStyle;
	}

	/**
	 * Returns the arrowTailStyle.
	 *
	 * @return the arrowTailStyle
	 */
	public ArrowStyle getArrowTailStyle() {
		return arrowTailStyle;
	}

	/**
	 * Sets the arrowTailStyle.
	 *
	 * @param arrowTailStyle the new arrowTailStyle
	 */
	public void setArrowTailStyle(ArrowStyle arrowTailStyle) {
		this.arrowTailStyle = arrowTailStyle;
	}

	/**
	 * Returns the path.
	 *
	 * @return the path
	 */
	public List<Point> getPath() {
		return path;
	}

	/**
	 * Sets the path.
	 *
	 * @param path the new path
	 */
	public void setPath(List<Point> path) {
		this.path = path;
	}

	/**
	 * Sets the active state of the Edge.
	 * 
	 * @param active the active state of the Edge
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
}
