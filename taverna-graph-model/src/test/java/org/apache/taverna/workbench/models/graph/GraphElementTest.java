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


import org.apache.taverna.workbench.models.graph.GraphNode;
import org.apache.taverna.workbench.models.graph.GraphController;
import org.apache.taverna.workbench.models.graph.GraphElement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;

import org.apache.taverna.workbench.models.graph.GraphElement.LineStyle;

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
