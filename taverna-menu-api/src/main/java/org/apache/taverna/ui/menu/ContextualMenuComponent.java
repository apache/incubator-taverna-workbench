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
package org.apache.taverna.ui.menu;

import java.awt.Component;

/**
 * A contextual menu component.
 * <p>
 * A {@link MenuComponent} that also implements ContextualMenuComponent, when
 * included in a menu tree rooted in the {@link DefaultContextualMenu} and
 * retrieved using
 * {@link MenuManager#createContextMenu(Object, Object, Component)}, will be
 * {@linkplain #setContextualSelection(ContextualSelection) informed} before
 * calls to {@link #isEnabled()} or {@link #getAction()}.
 * <p>
 * In this way the contextual menu item can be visible for only certain
 * selections, and its action can be bound to the current selection.
 * <p>
 * Contextual menu components can be grouped by {@linkplain AbstractMenuSection
 * sections} and {@linkplain AbstractMenu sub-menus}, or directly have the
 * {@link DefaultContextualMenu} as the parent.
 * 
 * @see ContextualSelection
 * @see DefaultContextualMenu
 * @author Stian Soiland-Reyes
 */
public interface ContextualMenuComponent extends MenuComponent {
	/**
	 * Set the contextual selection, or <code>null</code> if there is no current
	 * selection (if the menu item was not included in a contextual menu).
	 * 
	 * @param contextualSelection
	 *            The contextual selection
	 */
	void setContextualSelection(ContextualSelection contextualSelection);
}
