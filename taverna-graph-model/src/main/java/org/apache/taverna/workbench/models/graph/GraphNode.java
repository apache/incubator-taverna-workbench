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

import java.util.ArrayList;
import java.util.List;

/**
 * A node of a graph that can optionally contain other graphs.
 * 
 * @author David Withers
 */
public class GraphNode extends GraphShapeElement {
	private List<GraphNode> sourceNodes = new ArrayList<>();
	private List<GraphNode> sinkNodes = new ArrayList<>();
	private Graph graph;
	private boolean expanded;

	/**
	 * Constructs a new instance of Node.
	 * 
	 */
	public GraphNode(GraphController graphController) {
		super(graphController);
	}

	/**
	 * Adds a sink node.
	 * 
	 * @param sinkNode
	 *            the sink node to add
	 */
	public void addSinkNode(GraphNode sinkNode) {
		sinkNode.setParent(this);
		sinkNodes.add(sinkNode);
	}

	/**
	 * Adds a source node.
	 * 
	 * @param sourceNode
	 *            the source node to add
	 */
	public void addSourceNode(GraphNode sourceNode) {
		sourceNode.setParent(this);
		sourceNodes.add(sourceNode);
	}

	/**
	 * Returns the graph that this node contains.
	 * 
	 * @return the graph that this node contains
	 */
	public Graph getGraph() {
		return graph;
	}

	/**
	 * Returns the sinkNodes.
	 * 
	 * @return the sinkNodes
	 */
	public List<GraphNode> getSinkNodes() {
		return sinkNodes;
	}

	/**
	 * Returns the sourceNodes.
	 * 
	 * @return the sourceNodes
	 */
	public List<GraphNode> getSourceNodes() {
		return sourceNodes;
	}

	/**
	 * Returns true if this node is expanded to show the contained graph.
	 * 
	 * @return true if this node is expanded
	 */
	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * Removes a sink node.
	 * 
	 * @param sinkNode
	 *            the node to remove
	 * @return true if the node was removed, false otherwise
	 */
	public boolean removeSinkNode(GraphNode sinkNode) {
		return sinkNodes.remove(sinkNode);
	}

	/**
	 * Removes a source node.
	 * 
	 * @param sourceNode
	 *            the node to remove
	 * @return true if the node was removed, false otherwise
	 */
	public boolean removeSourceNode(GraphNode sourceNode) {
		return sourceNodes.remove(sourceNode);
	}

	/**
	 * Sets whether this node is expanded to show the contained graph.
	 * 
	 * @param expanded
	 *            true if this node is expanded
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	/**
	 * Sets the graph that this node contains.
	 * 
	 * @param graph
	 *            the new graph
	 */
	public void setGraph(Graph graph) {
		if (graph != null)
			graph.setParent(this);
		this.graph = graph;
	}

	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);
		if (isExpanded())
			getGraph().setSelected(selected);
	}
}
