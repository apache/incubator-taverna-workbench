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
import static org.junit.Assert.assertNull;
import net.sf.taverna.t2.workbench.models.graph.GraphEdge.ArrowStyle;

import org.junit.Before;
import org.junit.Test;

public class GraphEdgeTest {

	private GraphEdge edge;
	
	private GraphNode source;
	
	private GraphNode sink;
	
	private ArrowStyle arrowHeadStyle;

	private ArrowStyle arrowTailStyle;
	
	private GraphController graphController;
	
	@Before
	public void setUp() throws Exception {
		source = new GraphNode(graphController);
		sink = new GraphNode(graphController);
		arrowHeadStyle = ArrowStyle.DOT;
		arrowTailStyle = ArrowStyle.NORMAL;
		edge = new GraphEdge(graphController);
		edge.setArrowHeadStyle(arrowHeadStyle);
		edge.setArrowTailStyle(arrowTailStyle);
		edge.setSink(sink);
		edge.setSource(source);
	}

	@Test
	public void testEdge() {
		edge = new GraphEdge(graphController);
		assertNull(edge.getSource());
		assertNull(edge.getSink());
		assertNull(edge.getLabel());
	}

	@Test
	public void testEdgeNodeNode() {
		edge = new GraphEdge(graphController);
		edge.setSource(source);
		edge.setSink(sink);
		assertEquals(source, edge.getSource());
		assertEquals(sink, edge.getSink());
		assertNull(edge.getLabel());
	}

	@Test
	public void testGetSource() {
		assertEquals(source, edge.getSource());
	}

	@Test
	public void testSetSource() {
		GraphNode node = new GraphNode(graphController);
		edge.setSource(node);
		assertEquals(node, edge.getSource());
		edge.setSource(null);
		assertNull(edge.getSource());
	}

	@Test
	public void testGetSink() {
		assertEquals(sink, edge.getSink());
	}

	@Test
	public void testSetSink() {
		GraphNode node = new GraphNode(graphController);
		edge.setSink(node);
		assertEquals(node, edge.getSink());
		edge.setSink(null);
		assertNull(edge.getSink());
	}

	@Test
	public void testGetArrowHeadStyle() {
		assertEquals(arrowHeadStyle, edge.getArrowHeadStyle());
	}

	@Test
	public void testSetArrowHeadStyle() {
		edge.setArrowHeadStyle(ArrowStyle.DOT);
		assertEquals(ArrowStyle.DOT, edge.getArrowHeadStyle());
		edge.setArrowHeadStyle(null);
		assertNull(edge.getArrowHeadStyle());
	}

	@Test
	public void testGetArrowTailStyle() {
		assertEquals(arrowTailStyle, edge.getArrowTailStyle());
	}

	@Test
	public void testSetArrowTailStyle() {
		edge.setArrowTailStyle(ArrowStyle.NORMAL);
		assertEquals(ArrowStyle.NORMAL, edge.getArrowTailStyle());
		edge.setArrowTailStyle(null);
		assertNull(edge.getArrowTailStyle());
	}

}
