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

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.ui.Workbench;

import org.apache.log4j.Logger;

public class ShowDetailsContextualMenuAction extends AbstractContextualMenuAction {
	private static final String SHOW_DETAILS = "Show details";
	private String namedComponent = "contextualView";

	private static Logger logger = Logger.getLogger(ShowDetailsContextualMenuAction.class);
	private Workbench workbench;

	public ShowDetailsContextualMenuAction() {
		super(ConfigureSection.configureSection, 40);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
		// FIXME: Should we list all the applicable types here?
		// && getContextualSelection().getSelection() instanceof Processor;
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction(SHOW_DETAILS) {
			public void actionPerformed(ActionEvent e) {
				workbench.makeNamedComponentVisible(namedComponent);
			}
		};
	}

	public void setWorkbench(Workbench workbench) {
		this.workbench = workbench;
	}

}
