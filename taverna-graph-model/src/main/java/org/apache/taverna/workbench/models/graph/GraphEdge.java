package org.apache.taverna.workbench.models.graph;
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
}
