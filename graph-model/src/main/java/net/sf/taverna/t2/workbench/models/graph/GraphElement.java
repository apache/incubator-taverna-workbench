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

import java.awt.Color;
import java.awt.Point;

/**
 * An element of a graph.
 * 
 * @author David Withers
 */
public abstract class GraphElement {

	public enum LineStyle {
		NONE, SOLID, DOTTED
	}

	private String id;
	
	private String label;
	
	private Point labelPosition;

	private LineStyle lineStyle = LineStyle.SOLID;
	
	private Color color = Color.BLACK;
	
	private Color fillColor;
	
	private float opacity = 1f;
	
	private GraphElement parent;
	
	private boolean selected;
	
	private boolean active;
	
	private boolean interactive;
	
	private boolean visible = true;
	
	private boolean filtered;
	
	private Object dataflowObject;
	
	protected GraphController graphController;
	
	protected float completed;

	protected int iteration;

	protected int errors;

	protected GraphElement(GraphController graphController) {
		this.graphController = graphController;
	}
	
	/**
	 * Returns the eventManager.
	 *
	 * @return the eventManager
	 */
	public GraphEventManager getEventManager() {
		if (graphController != null) {
			return graphController.getGraphEventManager();
		}
		return null;
	}

	/**
	 * Returns the dataflowObject.
	 *
	 * @return the dataflowObject
	 */
	public Object getDataflowObject() {
		return dataflowObject;
	}

	/**
	 * Sets the dataflowObject.
	 *
	 * @param dataflowObject the new dataflowObject
	 */
	public void setDataflowObject(Object dataflowObject) {
		this.dataflowObject = dataflowObject;
	}

	/**
	 * Returns the parent.
	 *
	 * @return the parent
	 */
	public GraphElement getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parent the new parent
	 */
	protected void setParent(GraphElement parent) {
		this.parent = parent;
	}

	/**
	 * Returns the label.
	 *
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the labelPosition.
	 *
	 * @return the labelPosition
	 */
	public Point getLabelPosition() {
		return labelPosition;
	}

	/**
	 * Sets the labelPosition.
	 *
	 * @param labelPosition the new labelPosition
	 */
	public void setLabelPosition(Point labelPosition) {
		this.labelPosition = labelPosition;
	}

	/**
	 * Returns the id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(String id) {
		if (graphController != null) {
			graphController.mapElement(id, this);
		}
		this.id = id;
	}

	/**
	 * Returns the colour.
	 *
	 * @return the colour
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Sets the colour.
	 *
	 * @param color the new colour
	 */
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Returns the fillColor.
	 *
	 * @return the fillColor
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * Sets the fillColor.
	 *
	 * @param fillColor the new fillColor
	 */
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}

	/**
	 * Returns the lineStyle.
	 *
	 * @return the lineStyle
	 */
	public LineStyle getLineStyle() {
		return lineStyle;
	}

	/**
	 * Sets the lineStyle.
	 *
	 * @param lineStyle the new lineStyle
	 */
	public void setLineStyle(LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	public String toString() {
		return id + "[" + label + "]";
	}

	/**
	 * Returns the selected.
	 *
	 * @return the selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Sets the selected.
	 *
	 * @param selected the new selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	/**
	 * Returns the iteration.
	 *
	 * @return the value of iteration
	 */
	public int getIteration() {
		return iteration;
	}

	/**
	 * Sets the iteration.
	 *
	 * @param iteration the new value for iteration
	 */
	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	/**
	 * Returns the errors.
	 *
	 * @return the value of errors
	 */
	public int getErrors() {
		return errors;
	}

	/**
	 * Sets the errors.
	 *
	 * @param errors the new value for errors
	 */
	public void setErrors(int errors) {
		this.errors = errors;
	}

	/**
	 * Returns the completed.
	 *
	 * @return the value of completed
	 */
	public float getCompleted() {
		return completed;
	}

	/**
	 * Sets the completed value.
	 * 
	 * @param completed
	 */
	public void setCompleted(float completed) {
		this.completed = completed;
	}

	/**
	 * Returns <code>true</code> if the element is active. The default value is <code>false</code>.
	 *
	 * @return <code>true</code> if the element is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets the value of active.
	 *
	 * @param active the new active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Returns <code>true</code> if the element is interactive. The default value is <code>false</code>.
	 *
	 * @return <code>true</code> if the element is interactive
	 */
	public boolean isInteractive() {
		return interactive;
	}

	/**
	 * Sets the value of interactive.
	 *
	 * @param interactive the new interactive
	 */
	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	/**
	 * Returns <code>true</code> if the element is visible. The default value is <code>true</code>.
	 *
	 * @return <code>true</code> if the element is visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets whether the element is visible.
	 *
	 * @param visible the new value for visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * Returns the opacity value. The default value is 1.0
	 *
	 * @return the opacity value
	 */
	public float getOpacity() {
		return opacity;
	}

	/**
	 * Sets the opacity of the element. Must be a value between 0.0 and 1.0.
	 *
	 * @param opacity the new opacity value
	 */
	public void setOpacity(float opacity) {
		this.opacity = opacity;
	}

	/**
	 * Returns <code>true</code> if the element is filtered.
	 *
	 * @return <code>true</code> if the element is filtered
	 */
	public boolean isFiltered() {
		return filtered;
	}

	/**
	 * Sets the value of filtered.
	 *
	 * @param filtered the new value for filtered
	 */
	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GraphElement other = (GraphElement) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}
	
}
