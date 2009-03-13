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

import java.awt.Point;

/**
 * A Graph element that has shape, size and position properties.
 *
 * @author David Withers
 */
public class GraphShapeElement extends GraphElement {

	public enum Shape {BOX, RECORD, HOUSE, INVHOUSE, DOT, CIRCLE, TRIANGLE, INVTRIANGLE}
	
	private Shape shape;
	
	private Point position = new Point();
	
	private int width;
	
	private int height;
	
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
		return position;
	}

	/**
	 * Returns the shape of the node.
	 *
	 * @return the shape of the node
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
	 * Sets the height of the node in points. Value must be >= 0.
	 *
	 * @param height the new height of the node in points
	 */
	public void setHeight(int height) throws IllegalArgumentException {
		this.height = height;
	}

	/**
	 * Sets the position.
	 *
	 * @param position the new position
	 */
	public void setPosition(Point position) {
		this.position = position;
	}

	/**
	 * Sets the shape of the node.
	 *
	 * @param shape the new shape of the node
	 */
	public void setShape(Shape shape) {
		this.shape = shape;
	}

	/**
	 * Sets the width of the node in points. Value must be >= 0.
	 *
	 * @param width the new width of the node in points
	 */
	public void setWidth(int width) throws IllegalArgumentException {
		this.width = width;
	}

}
