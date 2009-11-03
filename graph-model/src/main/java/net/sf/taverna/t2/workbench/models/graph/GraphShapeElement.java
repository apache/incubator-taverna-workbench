/*******************************************************************************
 * Copyright (C) 2008 The University of Manchester   
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

import java.awt.Dimension;
import java.awt.Point;

/**
 * A Graph element that has shape, size and position properties.
 *
 * @author David Withers
 */
public class GraphShapeElement extends GraphElement {

	public enum Shape {BOX, RECORD, HOUSE, INVHOUSE, DOT, CIRCLE, TRIANGLE, INVTRIANGLE}
	
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
	 * @param position the new position
	 */
	public void setPosition(Point position) {
		x = position.x;
		y = position.y;
	}

	/**
	 * Sets the shape of the element.
	 *
	 * @param shape the new shape of the element
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
	 * @param size the new size of the node
	 */
	public void setSize(Dimension size) {
		width = size.width;
		height = size.height;
	}

}
