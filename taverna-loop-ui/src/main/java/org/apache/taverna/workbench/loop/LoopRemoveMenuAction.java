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

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;

import org.apache.log4j.Logger;

import org.apache.taverna.scufl2.api.core.Processor;
import org.apache.taverna.workflowmodel.processor.dispatch.layers.Loop;

public class LoopRemoveMenuAction extends AbstractContextualMenuAction {

	private static Logger logger = Logger
	.getLogger(LoopRemoveMenuAction.class);

	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");

	private static final URI LOOP_REMOVE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/loopRemove");

	private static final String LOOP_REMOVE = "Loop remove";

	public LoopRemoveMenuAction() {
		super(configureRunningSection, 25, LOOP_REMOVE_URI);
	}

	private EditManager editManager;
	private FileManager fileManager;


	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Disable looping") {
			public void actionPerformed(ActionEvent e) {
				Processor p = (Processor) getContextualSelection().getSelection();
					Loop loopLayer = LoopConfigureMenuAction.getLoopLayer(p);
					Edit<DispatchStack> deleteEdit = editManager.getEdits().getDeleteDispatchLayerEdit(
							p.getDispatchStack(), loopLayer);
					// TODO: Should warn before removing "essential" layers
					try {
						editManager.doDataflowEdit(fileManager.getCurrentDataflow(),
								deleteEdit);
					} catch (EditException ex) {
						logger.warn("Could not remove layer " + loopLayer, ex);
					}

			}
		};
	}

	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return (super.isEnabled() && (selection instanceof Processor) && (LoopConfigureMenuAction.getLoopLayer((Processor)selection) != null));
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

}
