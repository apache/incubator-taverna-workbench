package org.apache.taverna.ui.menu.items.activityport;
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

import java.net.URI;

import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.DefaultContextualMenu;
import org.apache.taverna.scufl2.api.port.InputProcessorPort;

public class ActivityInputPortSection extends AbstractMenuSection implements
		ContextualMenuComponent {

	private static final String ACTIVITY_INPUT_PORT = "Service input port: ";
	public static final URI activityInputPortSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/activityInputPort");
	private ContextualSelection contextualSelection;

	public ActivityInputPortSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 10,
				activityInputPortSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return getContextualSelection().getSelection() instanceof InputProcessorPort;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.action = null;
	}

	@Override
	protected Action createAction() {
		InputProcessorPort port = (InputProcessorPort) getContextualSelection().getSelection();
		String name = ACTIVITY_INPUT_PORT + port.getName();
		return new DummyAction(name);
	}

}
