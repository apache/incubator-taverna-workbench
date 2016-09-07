package org.apache.taverna.ui.menu.items.datalink;
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

import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.scufl2.api.core.DataLink;

import org.apache.taverna.ui.menu.AbstractMenuSection;
import org.apache.taverna.ui.menu.ContextualMenuComponent;
import org.apache.taverna.ui.menu.ContextualSelection;
import org.apache.taverna.ui.menu.DefaultContextualMenu;

public class LinkSection extends AbstractMenuSection implements
		ContextualMenuComponent {

	public static final URI linkSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/link");
	private ContextualSelection contextualSelection;

	public LinkSection() {
		super(DefaultContextualMenu.DEFAULT_CONTEXT_MENU, 10, linkSection);
	}

	public ContextualSelection getContextualSelection() {
		return contextualSelection;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof DataLink;
	}

	public void setContextualSelection(ContextualSelection contextualSelection) {
		this.contextualSelection = contextualSelection;
		this.action = null;
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		DataLink link = (DataLink) getContextualSelection().getSelection();
		String name = "Data link: " + link.getReceivesFrom().getName() + " -> " + link.getSendsTo().getName();
		return new AbstractAction(name) {
			public void actionPerformed(ActionEvent e) {
			}
		};
	}

}
