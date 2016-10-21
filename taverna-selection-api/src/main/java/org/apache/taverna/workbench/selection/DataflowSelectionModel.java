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
package org.apache.taverna.workbench.selection;

import java.util.Set;

import org.apache.taverna.lang.observer.Observable;
import org.apache.taverna.lang.observer.Observer;
import org.apache.taverna.workbench.selection.events.DataflowSelectionMessage;

/**
 * The current state of the selection of dataflow objects.
 * 
 * @author David Withers
 */
public interface DataflowSelectionModel extends
		Observable<DataflowSelectionMessage> {
	/**
	 * Adds an element to the current selection.
	 * 
	 * If the element is not in the selection the {@link Observer}s are
	 * notified. If <code>element</code> is null, this method has no effect.
	 * 
	 * @param element
	 *            the element to add
	 */
	void addSelection(Object element);

	/**
	 * Removes an element from the current selection.
	 * 
	 * If the element is in the selection the {@link Observer}s are notified. If
	 * <code>element</code> is null, this method has no effect.
	 * 
	 * @param element
	 *            the element to remove
	 */
	void removeSelection(Object element);

	/**
	 * Sets the current selection.
	 * 
	 * If this changes the selection the {@link Observer}s are notified. If
	 * <code>elements</code> is null, this has the same effect as invoking
	 * <code>clearSelection</code>.
	 * 
	 * @param elements
	 *            the current selection
	 */
	void setSelection(Set<Object> elements);

	/**
	 * Returns the current selection.
	 * 
	 * Returns an empty set if nothing is currently selected.
	 * 
	 * @return the current selection
	 */
	Set<Object> getSelection();

	/**
	 * Clears the current selection.
	 * 
	 * If this changes the selection the {@link Observer}s are notified.
	 */
	void clearSelection();
}
