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
package org.apache.taverna.workbench.ui.views.contextualviews.activity;

import java.util.List;

import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;

/**
 * Defines a factory class that when associated with a selected object creates a
 * {@link ContextualView} for that selection.
 * <p>
 * This factory acts as an SPI to find {@link ContextualView}s for a given
 * Activity and other workflow components.
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * @author Stian Soiland-Reyes
 * 
 * 
 * @param <SelectionType>
 *            - the selection type this factory is associated with
 * 
 * @see ContextualView
 * @see ContextualViewFactoryRegistry
 */
public interface ContextualViewFactory<SelectionType> {
	/**
	 * @param selection
	 *            - the object for which ContextualViews needs to be generated
	 * @return instance of {@link ContextualView}
	 */
	public List<ContextualView> getViews(SelectionType selection);

	/**
	 * Used by the SPI system to find the correct factory that can handle the
	 * given object type. 
	 * 
	 * @param selection
	 * @return true if this factory relates to the given selection type
	 * @see ContextualViewFactoryRegistry
	 */
	public boolean canHandle(Object selection);
}
