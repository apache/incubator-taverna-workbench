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

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.taverna.scufl2.api.core.Processor;

import org.apache.taverna.ui.menu.AbstractContextualMenuAction;
import org.apache.taverna.workbench.edits.EditManager;
import org.apache.taverna.workbench.file.FileManager;
import org.apache.taverna.workflowmodel.processor.dispatch.DispatchLayer;

public class LoopConfigureMenuAction extends AbstractContextualMenuAction {

	public static final URI configureRunningSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/configureRunning");

	private static final URI LOOP_CONFIGURE_URI = URI
	.create("http://taverna.sf.net/2008/t2workbench/loopConfigure");

	private static final String LOOP_CONFIGURE = "Loop configure";

	private EditManager editManager;

	private FileManager fileManager;

	public LoopConfigureMenuAction() {
		super(configureRunningSection, 20, LOOP_CONFIGURE_URI);
	}

	@SuppressWarnings("serial")
	@Override
	protected Action createAction() {
		return new AbstractAction("Looping...") {
			public void actionPerformed(ActionEvent e) {
				Processor p = (Processor) getContextualSelection().getSelection();
				configureLoopLayer(p, e);
			}
		};
	}

	public void configureLoopLayer(Processor p, ActionEvent e) {
	    ObjectNode loopLayer = getLoopLayer(p);
		if (loopLayer != null) {
			LoopConfigureAction loopConfigureAction = new LoopConfigureAction(null, null, loopLayer, editManager, fileManager);
			loopConfigureAction.actionPerformed(e);
		}
	}

	public static ObjectNode getLoopLayer(Processor p) {
		for (DispatchLayer dl : p.getDispatchStack().getLayers()) {
			if (dl instanceof Loop) {
				result = (Loop) dl;
				break;
			}
		}
		return result;
	}

	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		return (super.isEnabled() && (selection instanceof Processor) && (getLoopLayer((Processor)selection) != null));
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

}
