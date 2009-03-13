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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A graph model of a dataflow.
 * 
 * @author David Withers
 */
public class Graph extends GraphShapeElement {

	public enum Alignment {
		HORIZONTAL, VERTICAL
	}

	private List<GraphNode> nodes = new ArrayList<GraphNode>();

	private Set<GraphEdge> edges = new HashSet<GraphEdge>();

	private Set<Graph> subgraphs = new HashSet<Graph>();

	private Alignment alignment = Alignment.VERTICAL;

	/**
	 * Constructs a Graph that uses the specified GraphEventManager to handle
	 * any user generated events on GraphElements.
	 * 
	 * @param eventManager
	 */
	public Graph(GraphController graphController) {
		super(graphController);
	}

	/**
	 * Adds an edge to the Graph and sets its parent to be this Graph.
	 * 
	 * @param edge
	 *            the edge to add
	 */
	public void addEdge(GraphEdge edge) {
		edge.setParent(this);
		edges.add(edge);
	}

	/**
	 * Adds a node to the Graph and sets its parent to be this Graph.
	 * 
	 * @param node
	 *            the node to add
	 */
	public void addNode(GraphNode node) {
		node.setParent(this);
		nodes.add(node);
	}

	/**
	 * Adds a subgraph to the Graph and sets its parent to be this Graph.
	 * 
	 * @param subgraph
	 *            the subgraph to add
	 */
	public void addSubgraph(Graph subgraph) {
		subgraph.setParent(this);
		subgraphs.add(subgraph);
	}

	/**
	 * Returns the alignment of the Graph.
	 * 
	 * @return the alignment of the Graph
	 */
	public Alignment getAlignment() {
		return alignment;
	}

	/**
	 * Returns the edges contained in the Graph.
	 * 
	 * @return the edges contained in the Graph
	 */
	public Set<GraphEdge> getEdges() {
		return Collections.unmodifiableSet(edges);
	}

	/**
	 * Returns the nodes contained in the Graph.
	 * 
	 * @return the nodes contained in the Graph
	 */
	public List<GraphNode> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	/**
	 * Returns the subgraphs contained in the Graph.
	 * 
	 * @return the subgraphs contained in the Graph
	 */
	public Set<Graph> getSubgraphs() {
		return Collections.unmodifiableSet(subgraphs);
	}

	/**
	 * Removes an edge from the Graph.
	 * 
	 * @param edge
	 *            the edge to remove
	 * @return true if the edge is removed from the Graph
	 */
	public boolean removeEdge(GraphEdge edge) {
		return edges.remove(edge);
	}

	/**
	 * Removes a node from the Graph.
	 * 
	 * @param node
	 *            the node to remove
	 * @return true if the node is removed from the Graph
	 */
	public boolean removeNode(GraphNode node) {
		return nodes.remove(node);
	}

	/**
	 * Removes a subgraph from the Graph.
	 * 
	 * @param subgraph
	 *            the subgraph to remove
	 * @return true if the subgraph is removed from the Graph
	 */
	public boolean removeSubgraph(Graph subgraph) {
		return subgraphs.remove(subgraph);
	}

	/**
	 * Sets the alignment of the Graph.
	 * 
	 * @param alignment
	 *            the new alignment
	 */
	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

}
