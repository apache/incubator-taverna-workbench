package org.apache.taverna.workbench.loop;
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

import org.apache.taverna.scufl2.api.core.Processor;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;

public class LoopAddMenuAction extends AbstractContextualMenuAction {

	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");

	private static final URI LOOP_ADD_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/loopAdd");

	private static final String LOOP_ADD = "Loop add";

	public LoopAddMenuAction() {
		super(configureRunningSection, 20, LOOP_ADD_URI);
	}

	private AddLoopFactory addLoopFactory;

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Looping...") {
			public void actionPerformed(ActionEvent e) {
				//Loop loopLayer = null;
				Processor p = (Processor) getContextualSelection().getSelection();
				addLoopFactory.getAddLayerActionFor(p).actionPerformed(e);
				//LoopConfigureMenuAction.configureLoopLayer(p, e); // Configuration dialog pop up is now done from getAddLayerActionFor()
			}
		};
	}

	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return (super.isEnabled() && (selection instanceof Processor) && (LoopConfigureMenuAction.getLoopLayer((Processor)selection) == null));
	}

	public void setAddLoopFactory(AddLoopFactory addLoopFactory) {
		this.addLoopFactory = addLoopFactory;
	}



}
