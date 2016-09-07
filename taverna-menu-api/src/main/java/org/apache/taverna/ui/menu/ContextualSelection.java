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
/*

package org.apache.taverna.ui.menu;

import java.awt.Component;

import javax.swing.JPopupMenu;

import org.apache.taverna.scufl2.api.core.Workflow;

/**
 * A contextual selection as passed to a {@link ContextualMenuComponent}.
 * 
 * @author Stian Soiland-Reyes
 */
public class ContextualSelection {
	private final Object parent;
	private final Object selection;
	private final Component relativeToComponent;

	public ContextualSelection(Object parent, Object selection,
			Component relativeToComponent) {
		this.parent = parent;
		this.selection = selection;
		this.relativeToComponent = relativeToComponent;
	}

	/**
	 * The parent object of the selected object, for instance a {@link Workflow}.
	 */
	public Object getParent() {
		return parent;
	}

	/**
	 * The selected object which actions in the contextual menu relate to, for
	 * instance a Processor.
	 */
	public Object getSelection() {
		return selection;
	}

	/**
	 * A UI component which the returned {@link JPopupMenu} (and it's actions)
	 * is to be relative to, for instance as a parent of pop-up dialogues.
	 */
	public Component getRelativeToComponent() {
		return relativeToComponent;
	}
}
