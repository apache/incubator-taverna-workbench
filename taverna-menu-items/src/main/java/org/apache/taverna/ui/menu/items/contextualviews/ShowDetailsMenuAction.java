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

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import org.apache.taverna.ui.menu.AbstractMenuAction;
import org.apache.taverna.ui.menu.DesignOnlyAction;
import org.apache.taverna.workbench.ui.Workbench;

public class ShowDetailsMenuAction extends AbstractMenuAction {
	private static final URI SHOW_DETAILS_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuShowDetailsComponent");

	private static final String SHOW_DETAILS = "Details";
	private String namedComponent = "contextualView";

	private Workbench workbench;

 	public ShowDetailsMenuAction() {
		super(ShowConfigureMenuAction.GRAPH_DETAILS_MENU_SECTION, 20, SHOW_DETAILS_URI);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
		// FIXME: Should we list all the applicable types here?
		// && getContextualSelection().getSelection() instanceof Processor;
	}

	@Override
	protected Action createAction() {
		return new ShowDetailsAction();
	}

	protected class ShowDetailsAction extends AbstractAction implements DesignOnlyAction {

		ShowDetailsAction() {
			super();
			putValue(NAME, "Show details");
			putValue(SHORT_DESCRIPTION, "Show details of selected component");
			putValue(Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			workbench.makeNamedComponentVisible(namedComponent);
		}

	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

}
