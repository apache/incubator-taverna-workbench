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
package org.apache.taverna.workbench.selection.impl;

import static org.apache.taverna.workbench.selection.events.DataflowSelectionMessage.Type.ADDED;
import static org.apache.taverna.workbench.selection.events.DataflowSelectionMessage.Type.REMOVED;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.taverna.lang.observer.MultiCaster;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.selection.DataflowSelectionModel;
import org.apache.taverna.workbench.selection.events.DataflowSelectionMessage;

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
