package org.apache.taverna.workbench.models.graph;

import java.awt.Color;
import java.awt.Point;

import org.apache.taverna.scufl2.api.common.WorkflowBean;

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
	private WorkflowBean workflowBean;
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
	 * Returns the workflowBean.
	 * 
	 * @return the workflowBean
	 */
	public WorkflowBean getWorkflowBean() {
		return workflowBean;
	}

	/**
	 * Sets the workflowBean.
	 * 
	 * @param workflowBean
	 *            the new workflowBean
	 */
	public void setWorkflowBean(WorkflowBean workflowBean) {
		this.workflowBean = workflowBean;
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
	 * @param parent
	 *            the new parent
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
	 * @param label
	 *            the new label
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
	 * @param labelPosition
	 *            the new labelPosition
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
	 * @param id
	 *            the new id
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
	 * @param color
	 *            the new colour
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
	 * @param fillColor
	 *            the new fillColor
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
	 * @param lineStyle
	 *            the new lineStyle
	 */
	public void setLineStyle(LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	@Override
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
	 * @param selected
	 *            the new selected
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
	 * @param iteration
	 *            the new value for iteration
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
	 * @param errors
	 *            the new value for errors
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
	 * Returns <code>true</code> if the element is active. The default value is
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> if the element is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets the value of active.
	 * 
	 * @param active
	 *            the new active
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Returns <code>true</code> if the element is interactive. The default
	 * value is <code>false</code>.
	 * 
	 * @return <code>true</code> if the element is interactive
	 */
	public boolean isInteractive() {
		return interactive;
	}

	/**
	 * Sets the value of interactive.
	 * 
	 * @param interactive
	 *            the new interactive
	 */
	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	/**
	 * Returns <code>true</code> if the element is visible. The default value is
	 * <code>true</code>.
	 * 
	 * @return <code>true</code> if the element is visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets whether the element is visible.
	 * 
	 * @param visible
	 *            the new value for visible
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
	 * @param opacity
	 *            the new opacity value
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
	 * @param filtered
	 *            the new value for filtered
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		// Equality by id
		GraphElement other = (GraphElement) obj;
		if (id == null)
			return (other.id == null);
		return id.equals(other.id);
	}

}
