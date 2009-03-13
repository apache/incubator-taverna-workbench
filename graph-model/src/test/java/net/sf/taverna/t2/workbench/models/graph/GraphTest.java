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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge;
import net.sf.taverna.t2.workbench.models.graph.Graph;
import net.sf.taverna.t2.workbench.models.graph.GraphNode;
import net.sf.taverna.t2.workbench.models.graph.Graph.Alignment;

import org.junit.Before;
import org.junit.Test;

public class GraphTest {

	private Graph graph;
	
	private Alignment alignment;
	
	private GraphController graphController;
	
	@Before
	public void setUp() throws Exception {
		alignment = Alignment.VERTICAL;
		graph = new Graph(graphController);
	}

	@Test
	public void testGraph() {
		assertNotNull(new Graph(graphController));
	}

	@Test
	public void testAddEdge() {
		GraphEdge newEdge = new GraphEdge(graphController);
		graph.addEdge(newEdge);
		assertEquals(1, graph.getEdges().size());
		assertTrue(graph.getEdges().contains(newEdge));
	}

	@Test
	public void testAddNode() {
		GraphNode newNode = new GraphNode(graphController);
		graph.addNode(newNode);
		assertEquals(1, graph.getNodes().size());
		assertTrue(graph.getNodes().contains(newNode));
		assertEquals(graph, newNode.getParent());
	}

	@Test
	public void testAddSubgraph() {
		Graph newGraph = new Graph(graphController);
		graph.addSubgraph(newGraph);
		assertEquals(1, graph.getSubgraphs().size());
		assertTrue(graph.getSubgraphs().contains(newGraph));
		assertEquals(graph, newGraph.getParent());
	}

	@Test
	public void testGetAlignment() {
		assertEquals(alignment, graph.getAlignment());
	}

	@Test
	public void testGetEdges() {
		assertNotNull(graph.getNodes());
		assertEquals(0, graph.getNodes().size());
	}

	@Test
	public void testGetNodes() {
		assertNotNull(graph.getEdges());
		assertEquals(0, graph.getEdges().size());
	}

	@Test
	public void testGetSubgraphs() {
		assertNotNull(graph.getSubgraphs());
		assertEquals(0, graph.getSubgraphs().size());
	}

	@Test
	public void testRemoveEdge() {
		GraphEdge newEdge = new GraphEdge(graphController);
		assertFalse(graph.removeEdge(newEdge));
		graph.addEdge(newEdge);
		assertTrue(graph.removeEdge(newEdge));
		assertFalse(graph.getNodes().contains(newEdge));
	}

	@Test
	public void testRemoveNode() {
		GraphNode newNode = new GraphNode(graphController);
		assertFalse(graph.removeNode(newNode));
		graph.addNode(newNode);
		assertTrue(graph.removeNode(newNode));
		assertFalse(graph.getNodes().contains(newNode));
	}

	@Test
	public void testRemoveSubgraph() {
		Graph newGraph = new Graph(graphController);
		assertFalse(graph.removeSubgraph(newGraph));
		graph.addSubgraph(newGraph);
		assertTrue(graph.removeSubgraph(newGraph));
		assertFalse(graph.getSubgraphs().contains(newGraph));
	}

	@Test
	public void testSetAlignment() {
		graph.setAlignment(Alignment.VERTICAL);
		assertEquals(Alignment.VERTICAL, graph.getAlignment());
		graph.setAlignment(Alignment.HORIZONTAL);
		assertEquals(Alignment.HORIZONTAL, graph.getAlignment());
	}

}
