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

import java.awt.Dimension;
import java.awt.Point;

/**
 * A Graph element that has shape, size and position properties.
 * 
 * @author David Withers
 */
public class GraphShapeElement extends GraphElement {
	public enum Shape {
		BOX, RECORD, HOUSE, INVHOUSE, DOT, CIRCLE, TRIANGLE, INVTRIANGLE
	}

	private Shape shape;
	private int x, y, width, height;

	public GraphShapeElement(GraphController graphController) {
		super(graphController);
	}

	/**
	 * Returns the height.
	 * 
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Returns the position.
	 * 
	 * @return the position
	 */
	public Point getPosition() {
		return new Point(x, y);
	}

	/**
	 * Returns the shape of the element.
	 * 
	 * @return the shape of the element
	 */
	public Shape getShape() {
		return shape;
	}

	/**
	 * Returns the width.
	 * 
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the position.
	 * 
	 * @param position
	 *            the new position
	 */
	public void setPosition(Point position) {
		x = position.x;
		y = position.y;
	}

	/**
	 * Sets the shape of the element.
	 * 
	 * @param shape
	 *            the new shape of the element
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}

	/**
	 * Returns the size of the element.
	 * 
	 * @return the size of the element
	 */
	public Dimension getSize() {
		return new Dimension(width, height);
	}

	/**
	 * Sets the size of the element.
	 * 
	 * @param size
	 *            the new size of the node
	 */
	public void setSize(Dimension size) {
		width = size.width;
		height = size.height;
	}
}
