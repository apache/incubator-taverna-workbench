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
package net.sf.taverna.t2.workbench.selection.impl;

import static net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage.Type.ADDED;
import static net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage.Type.REMOVED;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.taverna.lang.observer.MultiCaster;
import org.apache.taverna.lang.observer.Observer;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage;

/**
 * Default implementation of a <code>DataflowSelectionModel</code>.
 *
 * @author David Withers
 */
public class DataflowSelectionModelImpl implements DataflowSelectionModel {
	private MultiCaster<DataflowSelectionMessage> multiCaster;
	private Set<Object> selection = new TreeSet<>(new Comparator<Object>() {
		@Override
		public int compare(Object o1, Object o2) {
			if (o1 == o2)
				return 0;
			return o1.hashCode() - o2.hashCode();
		}
	});

	/**
	 * Constructs a new instance of DataflowSelectionModelImpl.
	 */
	public DataflowSelectionModelImpl() {
		multiCaster = new MultiCaster<>(this);
	}

	@Override
	public void addSelection(Object element) {
		if (element != null) {
			if (!selection.contains(element)) {
				clearSelection();
				selection.add(element);
				multiCaster.notify(new DataflowSelectionMessage(ADDED, element));
			}
		}
	}

	@Override
	public void clearSelection() {
		for (Object element : new HashSet<>(selection))
			removeSelection(element);
	}

	@Override
	public Set<Object> getSelection() {
		return new HashSet<>(selection);
	}

	@Override
	public void removeSelection(Object element) {
		if (element != null && selection.remove(element))
			multiCaster.notify(new DataflowSelectionMessage(REMOVED, element));
	}

	@Override
	public void setSelection(Set<Object> elements) {
		if (elements == null) {
			clearSelection();
			return;
		}
		Set<Object> newSelection = new HashSet<>(elements);
		for (Object element : new HashSet<>(selection))
			if (!newSelection.remove(element))
				removeSelection(element);
		for (Object element : newSelection)
			addSelection(element);
	}

	@Override
	public void addObserver(Observer<DataflowSelectionMessage> observer) {
		multiCaster.addObserver(observer);
	}

	@Override
	public List<Observer<DataflowSelectionMessage>> getObservers() {
		return multiCaster.getObservers();
	}

	@Override
	public void removeObserver(Observer<DataflowSelectionMessage> observer) {
		multiCaster.removeObserver(observer);
	}
}
