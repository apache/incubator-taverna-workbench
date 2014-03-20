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

import java.awt.Color;

import net.sf.taverna.t2.workbench.models.graph.GraphElement.LineStyle;

import org.junit.Before;
import org.junit.Test;

public class GraphElementTest {
	
	private GraphElement element;

	private String id;
	
	private String label;
	
	private LineStyle lineStyle;
	
	private Color color;
	
	private Color fillColor;
	
	private GraphElement parent;

	private GraphController graphController;
	
	@Before
	public void setUp() throws Exception {
		element = new GraphElement(graphController) {};
		id = "element-id";
		label = "element-label";
		lineStyle = LineStyle.NONE;
		color = Color.BLUE;
		fillColor = Color.GREEN;
		parent = new GraphNode(graphController);
		element.setId(id);
		element.setLabel(label);
		element.setLineStyle(lineStyle);
		element.setColor(color);
		element.setFillColor(fillColor);
		element.setParent(parent);
	}

	@Test
	public void testGetParent() {
		assertEquals(parent, element.getParent());
	}

	@Test
	public void testSetParent() {
		GraphNode newParent = new GraphNode(graphController);
		element.setParent(newParent);
		assertEquals(newParent, element.getParent());
		element.setParent(null);
		assertNull(element.getParent());
	}

	@Test
	public void testGetLabel() {
		assertEquals(label, element.getLabel());
	}

	@Test
	public void testSetLabel() {
		element.setLabel("new-label");
		assertEquals("new-label", element.getLabel());
		element.setLabel(null);
		assertNull(element.getLabel());
	}

	@Test
	public void testGetId() {
		assertEquals(id, element.getId());
	}

	@Test
	public void testSetId() {
		element.setId("new-id");
		assertEquals("new-id", element.getId());
		element.setId(null);
		assertNull(element.getId());
	}

	@Test
	public void testGetColor() {
		assertEquals(color, element.getColor());
	}

	@Test
	public void testSetColor() {
		element.setColor(Color.RED);
		assertEquals(Color.RED, element.getColor());
		element.setColor(null);
		assertNull(element.getColor());
	}

	@Test
	public void testGetFillColor() {
		assertEquals(fillColor, element.getFillColor());
	}

	@Test
	public void testSetFillColor() {
		element.setFillColor(Color.RED);
		assertEquals(Color.RED, element.getFillColor());
		element.setFillColor(null);
		assertNull(element.getFillColor());
	}

	@Test
	public void testGetLineStyle() {
		assertEquals(lineStyle, element.getLineStyle());
	}

	@Test
	public void testSetLineStyle() {
		element.setLineStyle(LineStyle.DOTTED);
		assertEquals(LineStyle.DOTTED, element.getLineStyle());
		element.setLineStyle(null);
		assertNull(element.getLineStyle());
	}

}
