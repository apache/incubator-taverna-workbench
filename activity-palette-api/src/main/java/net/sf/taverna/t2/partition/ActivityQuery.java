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
package net.sf.taverna.t2.partition;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * An abstract Query type that is performed for an Activity.
 * <p>
 * In particular the query takes a property in its constructor that is used by the query. So for example, an WSDLActivity would take a WSDL url as a property.
 * </p>
 * <p>
 * The class that extends this abstract class needs to implement doQuery to perform the query and produce ActivityItems.<br>
 * Each ActivityItem needs to be added to the internal HashSetModel with a call to add(ActivityItem).
 * </p>
 * @author Stuart Owen
 * 
 * @see ActivityItem
 * @see Query
 *
 */
public abstract class ActivityQuery implements Query<ActivityItem> {

	HashSetModel<ActivityItem> model = new HashSetModel<ActivityItem>();
	Date lastQueryTime = new Date(0); //defaults to 1970
	String property;
	

	/**
	 * Constructs the Activity item with the property relevant to that query type.
	 *  
	 * @param property
	 */
	public ActivityQuery(String property) {
		super();
		this.property = property;
	}

	/* (non-Javadoc)
	 * @see java.util.Set#add(java.lang.Object)
	 */
	public boolean add(ActivityItem item) {
		return model.add(item);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends ActivityItem> arg0) {
		return model.addAll(arg0);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.partition.SetModel#addSetModelChangeListener(net.sf.taverna.t2.partition.SetModelChangeListener)
	 */
	public void addSetModelChangeListener(
			SetModelChangeListener<ActivityItem> listener) {
		model.addSetModelChangeListener(listener);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#clear()
	 */
	public void clear() {
		model.clear();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		return model.clone();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	public boolean contains(Object arg0) {
		return model.contains(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> arg0) {
		return model.containsAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		return model.equals(arg0);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return model.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#isEmpty()
	 */
	public boolean isEmpty() {
		return model.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#iterator()
	 */
	public Iterator<ActivityItem> iterator() {
		return model.iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	public boolean remove(Object item) {
		return model.remove(item);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> arg0) {
		return model.removeAll(arg0);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.partition.SetModel#removeSetModelChangeListener(net.sf.taverna.t2.partition.SetModelChangeListener)
	 */
	public void removeSetModelChangeListener(
			SetModelChangeListener<ActivityItem> listener) {
		model.removeSetModelChangeListener(listener);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> arg0) {
		return model.retainAll(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.Set#size()
	 */
	public int size() {
		return model.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#toArray()
	 */
	public Object[] toArray() {
		return model.toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.Set#toArray(T[])
	 */
	public <T> T[] toArray(T[] arg0) {
		return model.toArray(arg0);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return model.toString();
	}

	/**
	 * This method needs implementing, and performs the actual query for the relevant activity type.
	 * <p>
	 * The results of the query should produce ActivityItems with are then added to the internal model with a call to add.
	 * <br>
	 * This will cause the SetModelChangeListener to fire an event to indicate a new item has been produced.
	 * </p>
	 * @see net.sf.taverna.t2.partition.Query#doQuery()
	 * @see ActivityItem
	 */
	public abstract void doQuery();

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.partition.Query#getLastQueryTime()
	 */
	public Date getLastQueryTime() {
		return lastQueryTime;
	}

	/**
	 * @return the property this ActivityQuery was constructed with
	 */
	protected String getProperty() {
		return property;
	}

}
