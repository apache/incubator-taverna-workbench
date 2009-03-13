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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.workbench.models.graph.GraphShapeElement.Shape;

import org.junit.Before;
import org.junit.Test;

public class GraphNodeTest {

	private GraphNode node;
	
	private Shape shape;
		
	private int width;
	
	private int height;
	
	private Graph graph;
	
	private boolean expanded;
	
	private GraphController graphController;
	
	@Before
	public void setUp() throws Exception {
		shape = Shape.HOUSE;
		width = 1;
		height = 2;
		graph = new Graph(graphController);
		expanded = false;
		node = new GraphNode(graphController);
		node.setShape(shape);
		node.setWidth(width);
		node.setHeight(height);
		node.setGraph(graph);
		node.setExpanded(expanded);
	}

	@Test
	public void testNode() {
		assertNotNull(new GraphNode(graphController));
	}

	@Test
	public void testAddSinkNode() {
		GraphNode newNode = new GraphNode(graphController);
		node.addSinkNode(newNode);
		assertEquals(1, node.getSinkNodes().size());
		assertTrue(node.getSinkNodes().contains(newNode));
		assertEquals(node, newNode.getParent());
	}

	@Test
	public void testAddSourceNode() {
		GraphNode newNode = new GraphNode(graphController);
		node.addSourceNode(newNode);
		assertEquals(1, node.getSourceNodes().size());
		assertTrue(node.getSourceNodes().contains(newNode));
		assertEquals(node, newNode.getParent());
	}

	@Test
	public void testGetGraph() {
		assertEquals(graph, node.getGraph());
	}

	@Test
	public void testGetHeight() {
		assertEquals(height, node.getHeight(), 0);
	}

	@Test
	public void testGetShape() {
		assertEquals(shape, node.getShape());
	}

	@Test
	public void testGetSinkNodes() {
		assertNotNull(node.getSinkNodes());
		assertEquals(0, node.getSinkNodes().size());
	}

	@Test
	public void testGetSourceNodes() {
		assertNotNull(node.getSourceNodes());
		assertEquals(0, node.getSourceNodes().size());
	}

	@Test
	public void testGetWidth() {
		assertEquals(width, node.getWidth(), 0);
	}

	@Test
	public void testIsExpanded() {
		assertEquals(expanded, node.isExpanded());
	}

	@Test
	public void testRemoveSinkNode() {
		GraphNode newNode = new GraphNode(graphController);
		assertFalse(node.removeSinkNode(newNode));
		node.addSinkNode(newNode);
		assertTrue(node.removeSinkNode(newNode));
		assertFalse(node.getSinkNodes().contains(newNode));
	}

	@Test
	public void testRemoveSourceNode() {
		GraphNode newNode = new GraphNode(graphController);
		assertFalse(node.removeSourceNode(newNode));
		node.addSourceNode(newNode);
		assertTrue(node.removeSourceNode(newNode));
		assertFalse(node.getSourceNodes().contains(newNode));
	}

	@Test
	public void testSetExpanded() {
		node.setExpanded(true);
		assertEquals(true, node.isExpanded());
		node.setExpanded(false);
		assertEquals(false, node.isExpanded());
	}

	@Test
	public void testSetGraph() {
		Graph newGraph = new Graph(graphController);
		node.setGraph(newGraph);
		assertEquals(newGraph, node.getGraph());
		node.setGraph(null);
		assertNull(node.getGraph());
	}

	@Test
	public void testSetHeight() {
		node.setHeight(6);
		assertEquals(6, node.getHeight(), 0);
		node.setHeight(4);
		assertEquals(4, node.getHeight(), 0);
	}

	@Test
	public void testSetShape() {
		node.setShape(Shape.INVTRIANGLE);
		assertEquals(Shape.INVTRIANGLE, node.getShape());
		node.setShape(Shape.TRIANGLE);
		assertEquals(Shape.TRIANGLE, node.getShape());
	}

	@Test
	public void testSetWidth() {
		node.setWidth(23);
		assertEquals(23, node.getWidth(), 0);
		node.setWidth(14);
		assertEquals(14, node.getWidth(), 0);
	}

}
