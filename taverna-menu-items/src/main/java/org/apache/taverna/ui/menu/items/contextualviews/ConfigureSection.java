package org.apache.taverna.ui.menu.items.contextualviews;
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

import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;

/**
 * Menu section containing the actions to add service templates, i.e. activities
 * than are not readily runnable but need to be configured first. The actual actions that
 * go into this menu can be found in the ui modules for the activities.
 *
 * @author Alex Nenadic
 *
 */
public class ConfigureSection extends AbstractMenuSection
		implements ContextualMenuComponent {

	public static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configure");
	private ContextualSelection contextualSelection;

	public ConfigureSection() {
		super(EditSection.editSection, 100, configureSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return super.isEnabled();
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
	}
}
