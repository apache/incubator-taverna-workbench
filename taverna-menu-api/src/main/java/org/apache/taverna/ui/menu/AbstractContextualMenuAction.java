/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.ui.menu;

import java.net.URI;

/**
 * An {@link AbstractMenuAction} that is {@link ContextualMenuComponent} aware.
 * The contextual selection can be retrieved from
 * {@link #getContextualSelection()}.
 * <p>
 * The cached action will be flushed everytime the contextual selection changes,
 * forcing a new call to {@link #createAction()} - given that
 * {@link #isEnabled()} returns true.
 * 
 * @author Stian Soiland-Reyes
 */
public abstract class AbstractContextualMenuAction extends AbstractMenuAction
		implements ContextualMenuComponent {

	private ContextualSelection contextualSelection;

	public AbstractContextualMenuAction(URI parentId, int positionHint) {
		super(parentId, positionHint);
	}

	public AbstractContextualMenuAction(URI parentId, int positionHint, URI id) {
		super(parentId, positionHint, id);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return contextualSelection != null;
	}

	@Override
	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		// Force new createAction() call
		action = null;
	}
}
