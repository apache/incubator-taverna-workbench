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
package org.apache.taverna.workbench.selection.events;

import org.apache.taverna.workbench.ui.zaria.PerspectiveSPI;

/**
 * {@link SelectionManagerEvent} for changes to the selected
 * {@linkplain PerspectiveSPI perspective}.
 * 
 * @author David Withers
 */
public class PerspectiveSelectionEvent implements SelectionManagerEvent {
	private PerspectiveSPI previouslySelectedPerspective;
	private PerspectiveSPI selectedPerspective;

	public PerspectiveSelectionEvent(
			PerspectiveSPI previouslySelectedPerspective,
			PerspectiveSPI selectedPerspective) {
		this.previouslySelectedPerspective = previouslySelectedPerspective;
		this.selectedPerspective = selectedPerspective;
	}

	/**
	 * Returns the previously selected Perspective.
	 * 
	 * @return the previously selected Perspective
	 */
	public PerspectiveSPI getPreviouslySelectedPerspective() {
		return previouslySelectedPerspective;
	}

	/**
	 * Returns the currently selected Perspective.
	 * 
	 * @return the currently selected Perspective
	 */
	public PerspectiveSPI getSelectedPerspective() {
		return selectedPerspective;
	}
}
